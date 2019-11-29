package qsos.base.chat.view.fragment

import android.annotation.SuppressLint
import android.os.Bundle
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
import qsos.lib.base.utils.BaseUtils
import qsos.lib.base.utils.LogUtil
import qsos.lib.base.utils.ToastUtils
import qsos.lib.base.utils.rx.RxBus

/**
 * @author : 华清松
 * 聊天页面
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
                LogUtil.d("滚动$dy")
                if (dy < 0) {
                    BaseUtils.closeKeyBord(mContext, recyclerView)
                }
            }
        })

        chat_message_new_message_num.setOnClickListener {
            scrollToBottom()
        }

        mMessageList.observe(this, Observer {
            notifyMessage(it)
        })

        mMessageUpdateCancel.observe(this, Observer {
            it.values.forEach { msg ->
                notifyMessageState(msg.messageId, msg)
                notifyMessageReadNum(msg.messageId, msg)
            }
            mMessageUpdateCancel.value?.clear()
            LogUtil.d("聊天界面", "页面显示，更新缓存数据")
        })

        /**接收消息发送事件*/
        RxBus.toFlow(IMessageService.MessageSendEvent::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (mActive && it.session.sessionId == mSession.sessionId) {
                        when {
                            it.send && it.update -> {
                                it.message.forEach { message ->
                                    notifyMessageState(message.messageId, message)
                                }
                            }
                            it.send && !it.update -> {
                                it.message.forEach { message ->
                                    sendMessage(message)
                                }
                            }
                            it.update -> {
                                notifyMessage(it.message)
                            }
                            else -> {
                                it.message.forEach { message ->
                                    notifyNewMessage(message, it.bottom)
                                }
                            }
                        }
                    }
                }

        /**接收消息已读数更新事件*/
        RxBus.toFlow(IMessageService.MessageUpdateReadNumEvent::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (mActive && it.session.sessionId == mSession.sessionId) {
                        notifyMessageReadNum(it.message.messageId, it.message)
                    }
                }

        /**接收文件消息更新事件*/
        RxBus.toFlow(IMessageService.MessageUpdateFileEvent::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (mActive && it.session.sessionId == mSession.sessionId) {
                        notifyFileMessage(it.message)
                    }
                }

        getData()
    }

    override fun getData() {
        mMessageService.getMessageList(mSession, mMessageList)
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

    override fun notifyMessage(data: ArrayList<IMessageService.Message>) {
        if (data.isNotEmpty()) {
            mMessageAdapter?.data?.clear()
            mMessageAdapter?.data?.addAll(data)
            mMessageAdapter?.notifyDataSetChanged()
            scrollToBottom()
        } else {
            ToastUtils.showToast(mContext, "暂无消息")
        }
    }

    override fun sendMessage(msg: IMessageService.Message, new: Boolean) {
        msg.sendStatus = EnumChatSendStatus.SENDING
        if (new) {
            notifyNewMessage(msg, true)
        }
        mMessageService.sendMessage(
                message = msg,
                failed = { error, result ->
                    ToastUtils.showToast(mContext, error)
                    notifyMessageState(result.messageId, result)
                },
                success = { oldMessageId, message ->
                    notifyMessageState(oldMessageId, message)
                }
        )
    }

    override fun notifyMessageState(oldMessageId: Int, message: IMessageService.Message) {
        val position: Int? = mMessageAdapter?.mStateLiveDataMap!![oldMessageId]?.adapterPosition
        if (mActive) {
            position?.let {
                mMessageAdapter?.data!![it].updateSendState(message.messageId, message.timeline, message.sendStatus!!)
                mMessageAdapter?.notifyItemChanged(it)
            }
        } else {
            LogUtil.d("聊天界面", "页面隐藏，缓存数据")
            val list = mMessageUpdateCancel.value
            list?.put(message.timeline, message)
            mMessageUpdateCancel.postValue(list)
        }
    }

    override fun notifyFileMessage(message: IMessageService.Message) {
        if (message.sendStatus == EnumChatSendStatus.SUCCESS) {
            sendMessage(message, false)
        } else {
            notifyMessageState(message.messageId, message)
        }
    }

    override fun notifyMessageReadNum(oldMessageId: Int, message: IMessageService.Message) {
        val position: Int? = mMessageAdapter?.mStateLiveDataMap!![oldMessageId]?.adapterPosition
        if (mActive) {
            position?.let {
                mMessageAdapter?.data!![it].updateSendState(message.messageId, message.timeline, message.sendStatus!!)
                mMessageAdapter?.notifyItemChanged(it)
            }
        } else {
            LogUtil.d("聊天界面", "页面隐藏，缓存数据")
            val list = mMessageUpdateCancel.value
            list?.put(message.timeline, message)
            mMessageUpdateCancel.postValue(list)
        }
    }

    override fun notifyMessage(messageList: List<IMessageService.Message>) {
        val array = arrayListOf<IMessageService.Message>()
        array.addAll(messageList)
        mMessageList.postValue(array)
    }

    override fun notifyOldMessage(messageList: List<IMessageService.Message>) {
        // TODO 更新历史消息
    }

    override fun notifyNewMessage(message: IMessageService.Message, toBottom: Boolean) {
        mMessageList.value?.add(message)
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
    }

    override fun readMessage(adapterPosition: Int) {
        val data = getMessageList()[adapterPosition]
        if (data.readStatus == false) {
            mMessageService.readMessage(data, failed = { msg, _ ->
                LogUtil.e("聊天详情", msg)
            }, success = { message ->
                notifyMessageReadNum(message.messageId, message)
            })
        }
        LogUtil.d("聊天详情", "查看了消息adapterPosition=$adapterPosition ,desc=${data.content.getContentDesc()}")
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