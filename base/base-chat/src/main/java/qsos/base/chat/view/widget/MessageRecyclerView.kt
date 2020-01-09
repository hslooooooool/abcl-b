package qsos.base.chat.view.widget

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.android.schedulers.AndroidSchedulers
import qsos.base.chat.api.MessageViewHelper
import qsos.base.chat.data.entity.EnumChatSendStatus
import qsos.base.chat.utils.RecycleViewUtils
import qsos.base.chat.view.IMessageListView
import qsos.base.chat.view.adapter.ChatMessageAdapter
import qsos.lib.base.callback.OnListItemClickListener
import qsos.lib.base.callback.OnTListener
import qsos.lib.base.utils.BaseUtils
import qsos.lib.base.utils.DateUtils
import qsos.lib.base.utils.LogUtil
import qsos.lib.base.utils.ToastUtils
import qsos.lib.base.utils.rx.RxBus
import java.util.*
import kotlin.collections.HashMap

/**
 * @author 华清松
 * 聊天列表页，提供:
 * - 消息多类型展示
 * - 新消息追加上屏与新消息数量展示
 * - 历史消息追加上屏
 * - 消息状态发送状态、读取状态更新
 */
@SuppressLint("CheckResult", "SetTextI18n")
class MessageRecyclerView : RecyclerView, LifecycleObserver, IMessageListView {

    private lateinit var mOwner: LifecycleOwner
    private lateinit var mMessageViewHelper: MessageViewHelper
    private lateinit var mSession: MessageViewHelper.Session

    private var mReadNumListener: OnTListener<Int>? = null
    private var mMessageAdapter: ChatMessageAdapter? = null
    private var mLinearLayoutManager: LinearLayoutManager? = null
    private var mOnListItemClickListener: OnListItemClickListener? = null

    private var mNewMessageNum = 0
    private var mActive: Boolean = true
    private var mMessageScrolling = false
    private var mNewMessageNumLimit: Int = 4

    private val mMessageList: MutableLiveData<ArrayList<MessageViewHelper.Message>> = MutableLiveData()

    /**获取现有消息列表*/
    fun getMessageList(): ArrayList<MessageViewHelper.Message> {
        return mMessageAdapter?.data ?: arrayListOf()
    }

    /**文件消息上传/发送结果缓存，防止文件上传过程中，用户切换到其它页面后，消息状态无法更新*/
    private val mMessageListUpdateCache: MutableLiveData<HashMap<String, MessageViewHelper.Message>> = MutableLiveData()

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    /**
     * @author 华清松
     * 聊天列表页，提供:
     * - 消息多类型展示
     * - 新消息追加上屏与新消息数量展示
     * - 历史消息追加上屏
     * - 消息状态发送状态、读取状态更新
     * @param session 消息会话数据
     * @param messageViewHelper 消息服务，发送、撤销消息实现
     * @param itemClickListener 消息列表项点击监听
     * @param newMessageNumLimit 新消息滚动最小列数，大于此列不自动滚动，小于列表自动滚动到底部
     */
    fun initView(
            session: MessageViewHelper.Session,
            messageViewHelper: MessageViewHelper,
            itemClickListener: OnListItemClickListener? = null,
            newMessageNumLimit: Int = 4,
            lifecycleOwner: LifecycleOwner,
            readNumListener: OnTListener<Int>? = null
    ) {
        this.mSession = session
        this.mMessageViewHelper = messageViewHelper
        this.mOnListItemClickListener = itemClickListener
        this.mNewMessageNumLimit = newMessageNumLimit
        this.mOwner = lifecycleOwner
        this.mReadNumListener = readNumListener
        this.mMessageList.value = arrayListOf()
        this.mOwner.lifecycle.addObserver(this)

        mMessageListUpdateCache.value = HashMap()
        mMessageAdapter = ChatMessageAdapter(mSession, arrayListOf(), itemClickListener, object : OnTListener<Int> {
            override fun back(t: Int) {
                readMessage(t)
            }
        })
        mLinearLayoutManager = LinearLayoutManager(context)
        mLinearLayoutManager!!.stackFromEnd = true
        mLinearLayoutManager!!.reverseLayout = false
        layoutManager = mLinearLayoutManager
        adapter = mMessageAdapter
        itemAnimator = null

        setOnTouchListener { _, _ ->
            BaseUtils.hideKeyboard(activity = context as Activity)
            return@setOnTouchListener false
        }
        addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == SCROLL_STATE_DRAGGING) {
                    mMessageScrolling = true
                }
                if (newState == SCROLL_STATE_IDLE) {
                    mMessageScrolling = false
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (RecycleViewUtils.isSlideToBottom(this@MessageRecyclerView)) {
                    mNewMessageNum = 0
                    this@MessageRecyclerView.mReadNumListener?.back(mNewMessageNum)
                }
            }
        })

        mMessageList.observe(lifecycleOwner, Observer {
            refreshMessage(it)
        })

        mMessageListUpdateCache.observe(lifecycleOwner, Observer {
            mActive = true
            it.map { v ->
                notifyMessage(v.key, v.value)
            }
            mMessageListUpdateCache.value?.clear()
            LogUtil.d("聊天列表页", "页面显示，更新缓存数据")
        })

        mMessageViewHelper.mUpdateShowMessageList.observe(lifecycleOwner, Observer {
            it.forEach { msg ->
                notifyMessage(msg.messageId, msg)
            }
        })

        /**接收消息发送事件*/
        RxBus.toFlow(MessageViewHelper.MessageEvent::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (it.session.id == session.id) {
                        when (it.eventType) {
                            MessageViewHelper.EventType.UPDATE_SHOWED -> {
                                it.message.forEach { message ->
                                    notifyMessage(message.messageId, message)
                                }
                            }
                            MessageViewHelper.EventType.SEND -> {
                                it.message.forEach { message ->
                                    sendMessage(message, true)
                                }
                            }
                            MessageViewHelper.EventType.SHOW -> {
                                it.message.forEach { message ->
                                    addNewMessage(message, true)
                                }
                            }
                            MessageViewHelper.EventType.SEND_SHOWED -> {
                                it.message.forEach { message ->
                                    sendMessage(message, false)
                                }
                            }
                            MessageViewHelper.EventType.SHOW_MORE -> {
                                notifyOldMessage(it.message)
                            }
                            MessageViewHelper.EventType.SHOW_NEW -> {
                                it.message.forEach { message ->
                                    addNewMessage(message, false)
                                }
                            }
                        }
                    }
                }

        messageViewHelper.getMessageListBySessionId(session, mMessageList)
        messageViewHelper.updateShowMessage(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        mActive = true
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        mActive = false
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        mMessageViewHelper.clear()
    }

    override fun refreshMessage(data: ArrayList<MessageViewHelper.Message>) {
        if (data.isNotEmpty()) {
            mMessageAdapter?.data?.clear()
            mMessageAdapter?.data?.addAll(data)
            mMessageAdapter?.notifyDataSetChanged()
            mMessageList.value?.clear()
            mMessageList.value?.addAll(data)
        } else {
            ToastUtils.showToast(context, "暂无消息")
        }
    }

    override fun sendMessage(msg: MessageViewHelper.Message, new: Boolean) {
        msg.sendStatus = EnumChatSendStatus.SENDING
        if (new) {
            addNewMessage(msg, true)
        }
        mMessageViewHelper.sendMessage(
                message = msg,
                failed = { error, result ->
                    ToastUtils.showToast(context, error)
                    notifyMessage(result.messageId, result)
                },
                success = { oldMessageId, message ->
                    notifyMessage(oldMessageId, message)
                }
        )
    }

    override fun notifyMessage(oldMessageId: String, message: MessageViewHelper.Message) {
        mMessageAdapter?.mStateLiveDataMap!![oldMessageId]?.let {
            mMessageAdapter?.mStateLiveDataMap!![message.messageId] = it
            val position: Int = it.adapterPosition
            if (mMessageAdapter?.data == null || position == NO_POSITION || position > mMessageAdapter!!.data.size) {
                return
            }
            if (mActive) {
                mMessageAdapter?.data!![position].updateSendState(
                        message.messageId, message.timeline, message.sendStatus!!,
                        message.readNum, message.readStatus
                )
                mMessageAdapter?.notifyItemChanged(position)
            } else {
                LogUtil.d("聊天列表页", "页面隐藏，缓存数据")
                val list = mMessageListUpdateCache.value
                list?.put(message.messageId, message)
                mMessageListUpdateCache.postValue(list)
            }
        }
    }

    override fun notifyFileMessage(message: MessageViewHelper.Message) {
        if (message.sendStatus == EnumChatSendStatus.SUCCESS) {
            sendMessage(message, false)
        } else {
            notifyMessage(message.messageId, message)
        }
    }

    override fun notifyOldMessage(messageList: List<MessageViewHelper.Message>) {
        if (messageList.isNotEmpty()) {
            mMessageAdapter?.data?.addAll(0, messageList)
            mMessageAdapter?.notifyItemRangeInserted(0, messageList.size)
            mMessageList.value?.addAll(0, messageList)
        }
    }

    override fun addNewMessage(message: MessageViewHelper.Message, toBottom: Boolean) {
        if (mMessageAdapter?.data == null) {
            return
        }
        /**判断是否显示时间*/
        var lastTime = ""
        for (i in mMessageAdapter!!.data.size - 1 downTo 0) {
            val time = mMessageAdapter!!.data[i].createTime
            if (!TextUtils.isEmpty(time)) {
                lastTime = time
                break
            }
        }
        val mLastTime = DateUtils.strToDate(lastTime)?.time
                ?: -1L
        val thisTime = DateUtils.strToDate(message.createTime)?.time
                ?: -1L
        if (thisTime <= mLastTime || (thisTime - mLastTime) < MessageViewHelper.showTimeLimit) {
            message.createTime = ""
        }

        mMessageAdapter?.data?.add(message)
        val mMessageSize = getMessageList().size - 1
        mMessageAdapter?.notifyItemInserted(mMessageSize)
        val lastPosition = mLinearLayoutManager?.findLastCompletelyVisibleItemPosition() ?: 0
        val canScroll = mMessageSize - mNewMessageNumLimit < lastPosition
        when {
            (!mMessageScrolling && canScroll) || toBottom -> {
                mLinearLayoutManager?.scrollToPosition(mMessageSize)
            }
            else -> {
                mNewMessageNum++
                this.mReadNumListener?.back(mNewMessageNum)
            }
        }
        mMessageList.value?.add(message)
    }

    override fun readMessage(adapterPosition: Int) {
        val data = getMessageList()[adapterPosition]
        mMessageViewHelper.readMessage(data, failed = { msg, _ ->
            LogUtil.e("聊天列表页", msg)
        }, success = { message ->
            notifyMessage(message.messageId, message)
        })
        LogUtil.d("聊天列表页", "查看了消息adapterPosition=$adapterPosition ,desc=${data.content.getContentDesc()}")
    }

    override fun scrollToBottom() {
        this.stopScroll()
        if (getMessageList().isNotEmpty()) {
            mLinearLayoutManager?.scrollToPositionWithOffset(getMessageList().size - 1, 0)
        }
        mNewMessageNum = 0
        this.mReadNumListener?.back(mNewMessageNum)
    }

    override fun getShowMessageList(): List<MessageViewHelper.Message> {
        val first = mLinearLayoutManager?.findFirstVisibleItemPosition() ?: -1
        val last = mLinearLayoutManager?.findLastVisibleItemPosition() ?: -1
        val messages = getMessageList()
        val size = messages.size

        val list = arrayListOf<MessageViewHelper.Message>()
        if (first != -1 && last != -1 && first < size && last < size) {
            for (i in first..last) {
                list.add(messages[i])
            }
        }
        return list
    }
}
