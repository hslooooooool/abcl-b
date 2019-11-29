package qsos.base.chat.view.fragment

import android.annotation.SuppressLint
import android.graphics.Point
import android.os.Bundle
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.noober.menu.FloatMenu
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_chat_message.*
import kotlinx.android.synthetic.main.item_message_audio.view.*
import qsos.base.chat.R
import qsos.base.chat.data.entity.EnumChatMessageType
import qsos.base.chat.data.entity.EnumChatSendStatus
import qsos.base.chat.data.entity.MChatMessageAudio
import qsos.base.chat.data.entity.MChatMessageText
import qsos.base.chat.service.IMessageService
import qsos.base.chat.utils.RecycleViewUtils
import qsos.base.chat.view.adapter.ChatMessageAdapter
import qsos.base.chat.view.holder.ItemChatMessageBaseViewHolder
import qsos.core.player.PlayerConfigHelper
import qsos.core.player.audio.AudioPlayerHelper
import qsos.core.player.data.PreAudioEntity
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
 * @param mNewMessageNumLimit 新消息滚动最小列数，大于此列不自动滚动，小于列表自动滚动到底部
 * @param mOnListItemClickListener 消息列表项点击监听
 */
@SuppressLint("CheckResult", "SetTextI18n")
class ChatMessageListFragment(
        private val mSession: IMessageService.Session,
        private val mMessageService: IMessageService,
        private var mNewMessageNumLimit: Int = 4,
        /**消息列表项点击监听*/
        private var mOnListItemClickListener: OnListItemClickListener? = null,

        override val layoutId: Int = R.layout.fragment_chat_message,
        override val reload: Boolean = false
) : BaseFragment(), IChatFragment {

    private var mMessageAdapter: ChatMessageAdapter? = null
    private var mLinearLayoutManager: LinearLayoutManager? = null
    private val mPlayList: HashMap<String, AudioPlayerHelper?> = HashMap()
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

        initOnItemClickListener()

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
                notifyMessageSendStatus(msg)
                notifyMessageReadNum(msg)
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
                            it.send -> {
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
                        notifyMessageReadNum(it.message)
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
        mMessageList.value = getMessageList()
        mActive = false
        stopAudioPlay()
        super.onPause()
    }

    override fun onDestroy() {
        mMessageService.clear()
        super.onDestroy()
    }

    override fun playAudio(view: View, data: MChatMessageAudio) {
        var mAudioPlayerHelper: AudioPlayerHelper? = mPlayList[data.url]
        if (mAudioPlayerHelper == null) {
            /**停止其它播放*/
            stopAudioPlay()
            mAudioPlayerHelper = PlayerConfigHelper.previewAudio(
                    context = mContext, position = 0,
                    list = arrayListOf(
                            PreAudioEntity(
                                    name = data.name,
                                    desc = data.name,
                                    path = data.url
                            )
                    ),
                    onPlayerListener = object : OnTListener<AudioPlayerHelper.State> {
                        override fun back(t: AudioPlayerHelper.State) {
                            view.apply {
                                this.item_message_audio_state.setImageDrawable(AppCompatResources.getDrawable(mContext, when (t) {
                                    AudioPlayerHelper.State.STOP -> {
                                        R.drawable.icon_play
                                    }
                                    AudioPlayerHelper.State.ERROR -> {
                                        ToastUtils.showToast(mContext, "播放错误")
                                        R.drawable.icon_play
                                    }
                                    else -> {
                                        R.drawable.icon_pause
                                    }
                                }))
                            }

                        }
                    }
            )
            mPlayList[data.url] = mAudioPlayerHelper
        } else {
            /**停止当前音频播放*/
            mPlayList.remove(data.url)
            mAudioPlayerHelper.stop()
        }
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
                    result.sendStatus = EnumChatSendStatus.FAILED
                    notifyMessageSendStatus(result)
                },
                success = { result ->
                    result.sendStatus = EnumChatSendStatus.SUCCESS
                    notifyMessageSendStatus(result)
                }
        )
    }

    override fun notifyMessageSendStatus(message: IMessageService.Message) {
        val position: Int? = mMessageAdapter?.mStateLiveDataMap!![message.timeline]?.adapterPosition
        if (mActive) {
            position?.let {
                mMessageAdapter?.notifyItemChanged(it, ItemChatMessageBaseViewHolder.Update(1, message.sendStatus!!))
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
            notifyMessageSendStatus(message)
        }
    }

    override fun notifyMessageReadNum(message: IMessageService.Message) {
        val position: Int? = mMessageAdapter?.mStateLiveDataMap!![message.timeline]?.adapterPosition
        if (mActive) {
            position?.let {
                mMessageAdapter?.notifyItemChanged(it, ItemChatMessageBaseViewHolder.Update(2, message.readNum))
            }
        } else {
            LogUtil.d("聊天界面", "页面隐藏，缓存数据")
            val list = mMessageUpdateCancel.value
            list?.put(message.timeline, message)
            mMessageUpdateCancel.postValue(list)
        }
    }

    override fun deleteMessage(message: IMessageService.Message) {
        mMessageService.revokeMessage(message,
                failed = { msg, _ ->
                    ToastUtils.showToast(mContext, msg)
                },
                success = {
                    it.sendStatus = EnumChatSendStatus.CANCEL_CAN
                    notifyMessageSendStatus(it)
                })
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

    override fun sendMessageRecallEvent(message: IMessageService.Message) {
        RxBus.send(IMessageService.MessageReceiveEvent(
                session = mSession,
                message = message
        ))
    }

    override fun readMessage(adapterPosition: Int) {
        val data = getMessageList()[adapterPosition]
        if (data.readStatus == false) {
            mMessageService.readMessage(data, failed = { msg, _ ->
                LogUtil.e("聊天详情", msg)
            }, success = { message ->
                notifyMessageReadNum(message)
            })
        }
        LogUtil.d("聊天详情", "查看了消息adapterPosition=$adapterPosition ,desc=${data.content.getContentDesc()}")
    }

    /**停止所有语音播放*/
    private fun stopAudioPlay() {
        mPlayList.values.forEach {
            it?.stop()
        }
    }

    /**消息列表滚动到底部*/
    private fun scrollToBottom() {
        if (getMessageList().isNotEmpty()) {
            mLinearLayoutManager?.scrollToPosition(getMessageList().size - 1)
        }
        chat_message_new_message_num.visibility = View.GONE
        mNewMessageNum = 0
    }

    /**初始化消息列表项点击监听*/
    private fun initOnItemClickListener() {
        if (mOnListItemClickListener == null) {
            mOnListItemClickListener = object : OnListItemClickListener {
                override fun onItemClick(view: View, position: Int, obj: Any?) {
                    preOnItemClick(view, position, obj)
                }

                override fun onItemLongClick(view: View, position: Int, obj: Any?) {
                    preOnItemLongClick(view, position, obj)
                }
            }
        }
    }

    /**列表项点击*/
    private fun preOnItemClick(view: View, position: Int, obj: Any?) {
        when (view.id) {
            R.id.item_message_view_audio -> {
                if (obj is IMessageService.Message) {
                    obj.getRealContent<MChatMessageAudio>()?.let {
                        playAudio(view, it)
                    }
                }
            }
            R.id.item_message_cancel_reedit -> {
                if (obj != null && obj is IMessageService.Message && obj.content.getContentType() == EnumChatMessageType.TEXT.contentType) {
                    obj.getRealContent<MChatMessageText>()?.let {
                        obj.sendStatus = EnumChatSendStatus.CANCEL_OK
                        notifyMessageSendStatus(obj)

                        sendMessageRecallEvent(obj)
                    }
                }
            }
        }
    }

    /**列表项长按*/
    private fun preOnItemLongClick(view: View, position: Int, obj: Any?) {
        when (view.id) {
            R.id.item_message_content -> {
                if (obj != null && obj is IMessageService.Message) {
                    val point = IntArray(2)
                    view.getLocationOnScreen(point)
                    val floatMenu = FloatMenu(activity)
                    floatMenu.items("撤销", "其它")
                    floatMenu.setOnItemClickListener { _, index ->
                        when (index) {
                            0 -> {
                                deleteMessage(obj)
                            }
                            else -> {
                            }
                        }
                    }
                    floatMenu.show(Point(point[0], point[1]))
                }
            }
        }
    }

}