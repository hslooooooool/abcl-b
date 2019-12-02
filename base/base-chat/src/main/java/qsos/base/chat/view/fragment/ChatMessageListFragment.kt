package qsos.base.chat.view.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_chat_message.*
import qsos.base.chat.R
import qsos.base.chat.data.entity.EnumChatSendStatus
import qsos.base.chat.service.IMessageService
import qsos.base.chat.utils.RecycleViewUtils
import qsos.base.chat.view.adapter.ChatMessageAdapter
import qsos.lib.base.base.fragment.BaseFragment
import qsos.lib.base.callback.OnListItemClickListener
import qsos.lib.base.callback.OnTListener
import qsos.lib.base.utils.DateUtils
import qsos.lib.base.utils.LogUtil
import qsos.lib.base.utils.ToastUtils
import qsos.lib.base.utils.rx.RxBus

/**
 * @author : 华清松
 * 聊天列表页，提供:
 * - 消息多类型展示
 * - 新消息追加上屏与新消息数量展示
 * - 历史消息追加上屏
 * - 消息状态发送状态、读取状态更新
 * @param mSession 消息会话数据
 * @param mMessageService 消息服务，发送、撤销消息实现
 * @param mOnListItemClickListener 消息列表项点击监听
 * @param mNewMessageNumLimit 新消息滚动最小列数，大于此列不自动滚动，小于列表自动滚动到底部
 */
@SuppressLint("CheckResult", "SetTextI18n")
class ChatMessageListFragment(
        private val mSession: IMessageService.Session,
        private val mMessageService: IMessageService,
        private var mOnListItemClickListener: OnListItemClickListener? = null,
        private var mNewMessageNumLimit: Int = 4,
        override val layoutId: Int = R.layout.fragment_chat_message,
        override val reload: Boolean = false
) : BaseFragment(), IChatFragment {

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

    /**文件消息发送结果缓存，防止文件上传过程中，用户切换到其它页面后，消息状态无法更新*/
    private val mMessageUpdateCancel: MutableLiveData<HashMap<Int, IMessageService.Message>> = MutableLiveData()

    override fun initData(savedInstanceState: Bundle?) {
        mMessageUpdateCancel.value = HashMap()
    }

    override fun initView(view: View) {

        mMessageAdapter = ChatMessageAdapter(mSession, arrayListOf(), mOnListItemClickListener, object : OnTListener<Int> {
            override fun back(t: Int) {
                readMessage(t)
            }
        })
        mLinearLayoutManager = LinearLayoutManager(mContext)
        mLinearLayoutManager!!.stackFromEnd = false
        mLinearLayoutManager!!.reverseLayout = false
        chat_message_list.layoutManager = mLinearLayoutManager
        chat_message_list.adapter = mMessageAdapter
        chat_message_list.itemAnimator = null

        chat_message_list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    mMessageScrolling = true
                }
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    mMessageScrolling = false
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (RecycleViewUtils.isSlideToBottom(chat_message_list)) {
                    chat_message_new_message_num.visibility = View.GONE
                    mNewMessageNum = 0
                }
            }
        })

        chat_message_new_message_num.setOnClickListener {
            scrollToBottom()
        }

        mMessageList.observe(this, Observer {
            refreshMessage(it)
        })

        mMessageUpdateCancel.observe(this, Observer {
            mActive = true
            it.values.forEach { msg ->
                notifyMessage(msg.messageId, msg)
            }
            mMessageUpdateCancel.value?.clear()
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

        getData()
    }

    override fun getData() {
        mMessageService.getMessageListBySessionId(mSession, mMessageList)
    }

    override fun onResume() {
        super.onResume()
        mActive = true
    }

    override fun onPause() {
        mActive = false
        super.onPause()
    }

    override fun onDestroy() {
        mMessageService.clear()
        super.onDestroy()
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
            ToastUtils.showToast(mContext, "暂无消息")
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
                    ToastUtils.showToast(mContext, error)
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
            val list = mMessageUpdateCancel.value
            list?.put(message.messageId, message)
            mMessageUpdateCancel.postValue(list)
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
                chat_message_new_message_num.visibility = View.VISIBLE
                chat_message_new_message_num.text = "有 $mNewMessageNum 条新消息"
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
    private fun scrollToBottom() {
        if (getMessageList().isNotEmpty()) {
            mLinearLayoutManager?.scrollToPosition(getMessageList().size - 1)
        }
        chat_message_new_message_num.visibility = View.GONE
        mNewMessageNum = 0
    }

}