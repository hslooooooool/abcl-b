package qsos.base.chat.view.widget

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.android.schedulers.AndroidSchedulers
import qsos.base.chat.data.entity.EnumChatSendStatus
import qsos.base.chat.service.IMessageService
import qsos.base.chat.utils.RecycleViewUtils
import qsos.base.chat.view.adapter.ChatMessageAdapter
import qsos.base.chat.view.fragment.IChatFragment
import qsos.lib.base.callback.OnListItemClickListener
import qsos.lib.base.callback.OnTListener
import qsos.lib.base.utils.BaseUtils
import qsos.lib.base.utils.DateUtils
import qsos.lib.base.utils.LogUtil
import qsos.lib.base.utils.ToastUtils
import qsos.lib.base.utils.rx.RxBus

/**
 * @author 华清松
 * 聊天列表
 */
@SuppressLint("CheckResult")
class MessageRecyclerView : RecyclerView, LifecycleObserver, IChatFragment {

    private lateinit var mSession: IMessageService.Session
    private lateinit var mMessageService: IMessageService
    private var mOnListItemClickListener: OnListItemClickListener? = null
    private var mNewMessageNumLimit: Int = 4
    private lateinit var mOwner: LifecycleOwner
    private var mReadNumListener: OnTListener<Int>? = null

    private var mMessageAdapter: ChatMessageAdapter? = null
    private var mLinearLayoutManager: LinearLayoutManager? = null
    private var mActive: Boolean = true
    private var mNewMessageNum = 0
    private var mMessageScrolling = false
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

    /**加载页面*/
    fun initView(
            mSession: IMessageService.Session,
            mMessageService: IMessageService,
            mOnListItemClickListener: OnListItemClickListener? = null,
            mNewMessageNumLimit: Int = 4,
            mOwner: LifecycleOwner,
            mReadNumListener: OnTListener<Int>? = null
    ) {
        this.mSession = mSession
        this.mMessageService = mMessageService
        this.mOnListItemClickListener = mOnListItemClickListener
        this.mNewMessageNumLimit = mNewMessageNumLimit
        this.mOwner = mOwner
        this.mReadNumListener = mReadNumListener

        this.mOwner.lifecycle.addObserver(this)

        mMessageUpdateCache.value = HashMap()
        mMessageAdapter = ChatMessageAdapter(mSession, arrayListOf(), mOnListItemClickListener, object : OnTListener<Int> {
            override fun back(t: Int) {
                readMessage(t)
            }
        })
        mLinearLayoutManager = LinearLayoutManager(context)
        mLinearLayoutManager!!.stackFromEnd = false
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

        mMessageList.observe(mOwner, Observer {
            refreshMessage(it)
        })

        mMessageUpdateCache.observe(mOwner, Observer {
            mActive = true
            it.map { v ->
                notifyMessage(v.key, v.value)
            }
            mMessageUpdateCache.value?.clear()
            LogUtil.d("聊天列表页", "页面显示，更新缓存数据")
        })

        /**接收消息发送事件*/
        RxBus.toFlow(IMessageService.MessageEvent::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (it.session.sessionId == mSession.sessionId) {
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

        mMessageService.getMessageListBySessionId(mSession, mMessageList)
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
            scrollToBottom()
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
        val position: Int? = mMessageAdapter?.mStateLiveDataMap!![oldMessageId]?.adapterPosition
        if (mActive) {
            position?.let {
                mMessageAdapter?.data!![it].updateSendState(message.messageId, message.timeline, message.sendStatus!!)
                mMessageAdapter?.notifyItemChanged(it)
            }
        } else {
            LogUtil.d("聊天列表页", "页面隐藏，缓存数据")
            val list = mMessageUpdateCache.value
            list?.put(oldMessageId, message)
            mMessageUpdateCache.postValue(list)
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

    @SuppressLint("SetTextI18n")
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
        if (data.readStatus == false) {
            mMessageService.readMessage(data, failed = { msg, _ ->
                LogUtil.e("聊天列表页", msg)
            }, success = { message ->
                notifyMessage(message.messageId, message)
            })
        }
        LogUtil.d("聊天列表页", "查看了消息adapterPosition=$adapterPosition ,desc=${data.content.getContentDesc()}")
    }

    /**消息列表滚动到底部*/
    fun scrollToBottom() {
        this.stopScroll()
        if (getMessageList().isNotEmpty()) {
            mLinearLayoutManager?.scrollToPosition(getMessageList().size - 1)
        }
        mNewMessageNum = 0
        this.mReadNumListener?.back(mNewMessageNum)
    }

}
