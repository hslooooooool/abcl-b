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
import qsos.base.chat.data.entity.EnumChatSendStatus
import qsos.base.chat.service.IMessageService
import qsos.base.chat.utils.RecycleViewUtils
import qsos.base.chat.view.IMessageListUI
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
class MessageRecyclerView : RecyclerView, LifecycleObserver, IMessageListUI {

    private lateinit var mOwner: LifecycleOwner
    private lateinit var mMessageService: IMessageService
    private lateinit var mSession: IMessageService.Session

    private var mReadNumListener: OnTListener<Int>? = null
    private var mMessageAdapter: ChatMessageAdapter? = null
    private var mLinearLayoutManager: LinearLayoutManager? = null
    private var mOnListItemClickListener: OnListItemClickListener? = null

    private var mNewMessageNum = 0
    private var mActive: Boolean = true
    private var mMessageScrolling = false
    private var mNewMessageNumLimit: Int = 4

    private val mMessageList: MutableLiveData<ArrayList<IMessageService.Message>> = MutableLiveData()

    /**获取现有消息列表*/
    fun getMessageList(): ArrayList<IMessageService.Message> {
        return mMessageAdapter?.data ?: arrayListOf()
    }

    /**文件消息上传/发送结果缓存，防止文件上传过程中，用户切换到其它页面后，消息状态无法更新*/
    private val mMessageUpdateCache: MutableLiveData<HashMap<Int, IMessageService.Message>> = MutableLiveData()

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {}

    /**
     * @author 华清松
     * 聊天列表页，提供:
     * - 消息多类型展示
     * - 新消息追加上屏与新消息数量展示
     * - 历史消息追加上屏
     * - 消息状态发送状态、读取状态更新
     * @param session 消息会话数据
     * @param messageService 消息服务，发送、撤销消息实现
     * @param itemClickListener 消息列表项点击监听
     * @param newMessageNumLimit 新消息滚动最小列数，大于此列不自动滚动，小于列表自动滚动到底部
     */
    fun initView(
            session: IMessageService.Session,
            messageService: IMessageService,
            itemClickListener: OnListItemClickListener? = null,
            newMessageNumLimit: Int = 4,
            lifecycleOwner: LifecycleOwner,
            readNumListener: OnTListener<Int>? = null
    ) {
        this.mSession = session
        this.mMessageService = messageService
        this.mOnListItemClickListener = itemClickListener
        this.mNewMessageNumLimit = newMessageNumLimit
        this.mOwner = lifecycleOwner
        this.mReadNumListener = readNumListener

        this.mOwner.lifecycle.addObserver(this)

        mMessageUpdateCache.value = HashMap()
        mMessageAdapter = ChatMessageAdapter(session, arrayListOf(), itemClickListener, object : OnTListener<Int> {
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

        mMessageUpdateCache.observe(lifecycleOwner, Observer {
            mActive = true
            it.map { v ->
                notifyMessage(v.key, v.value)
            }
            mMessageUpdateCache.value?.clear()
            LogUtil.d("聊天列表页", "页面显示，更新缓存数据")
        })

        mMessageService.mUpdateShowMessageList.observe(lifecycleOwner, Observer {
            it.forEach { msg ->
                notifyMessage(msg.messageId, msg)
            }
        })

        /**接收消息发送事件*/
        RxBus.toFlow(IMessageService.MessageEvent::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (it.session.sessionId == session.sessionId) {
                        when (it.eventType) {
                            IMessageService.EventType.UPDATE_SHOWED -> {
                                it.message.forEach { message ->
                                    notifyMessage(message.messageId, message)
                                }
                            }
                            IMessageService.EventType.SEND -> {
                                it.message.forEach { message ->
                                    sendMessage(message, true)
                                }
                            }
                            IMessageService.EventType.SHOW -> {
                                it.message.forEach { message ->
                                    addNewMessage(message, true)
                                }
                            }
                            IMessageService.EventType.SEND_SHOWED -> {
                                it.message.forEach { message ->
                                    sendMessage(message, false)
                                }
                            }
                            IMessageService.EventType.SHOW_MORE -> {
                                notifyOldMessage(it.message)
                            }
                            IMessageService.EventType.SHOW_NEW -> {
                                it.message.forEach { message ->
                                    addNewMessage(message, false)
                                }
                            }
                        }
                    }
                }

        messageService.getMessageListBySessionId(session, mMessageList)
        messageService.updateShowMessage(this)
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
        mMessageService.clear()
    }

    override fun refreshMessage(data: ArrayList<IMessageService.Message>) {
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

    override fun sendMessage(msg: IMessageService.Message, new: Boolean) {
        msg.sendStatus = EnumChatSendStatus.SENDING
        if (new) {
            addNewMessage(msg, true)
        }
        mMessageService.sendMessage(
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

    override fun notifyMessage(oldMessageId: Int, message: IMessageService.Message) {
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
                val list = mMessageUpdateCache.value
                list?.put(message.messageId, message)
                mMessageUpdateCache.postValue(list)
            }
        }
    }

    override fun notifyFileMessage(message: IMessageService.Message) {
        if (message.sendStatus == EnumChatSendStatus.SUCCESS) {
            sendMessage(message, false)
        } else {
            notifyMessage(message.messageId, message)
        }
    }

    override fun notifyOldMessage(messageList: List<IMessageService.Message>) {
        if (messageList.isNotEmpty()) {
            mMessageAdapter?.data?.addAll(0, messageList)
            mMessageAdapter?.notifyItemRangeInserted(0, messageList.size)
            mMessageList.value?.addAll(0, messageList)
        }
    }

    override fun addNewMessage(message: IMessageService.Message, toBottom: Boolean) {
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
        if (thisTime <= mLastTime || (thisTime - mLastTime) < IMessageService.showTimeLimit) {
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
        mMessageService.readMessage(data, failed = { msg, _ ->
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

    override fun getShowMessageList(): List<IMessageService.Message> {
        val first = mLinearLayoutManager?.findFirstVisibleItemPosition() ?: -1
        val last = mLinearLayoutManager?.findLastVisibleItemPosition() ?: -1
        val messages = getMessageList()
        val size = messages.size

        val list = arrayListOf<IMessageService.Message>()
        if (first != -1 && last != -1 && first < size && last < size) {
            for (i in first..last) {
                list.add(messages[i])
            }
        }
        return list
    }
}
