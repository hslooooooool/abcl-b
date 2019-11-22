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
import qsos.base.chat.DefMessageService
import qsos.base.chat.R
import qsos.base.chat.data.entity.*
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
import qsos.lib.base.utils.LogUtil
import qsos.lib.base.utils.ToastUtils
import qsos.lib.base.utils.rx.RxBus
import java.util.*
import kotlin.collections.HashMap

/**
 * @author : 华清松
 * 聊天页面
 */
@SuppressLint("CheckResult")
class ChatMessageListFragment(
        private val mSession: ChatSession,
        private val mMessageService: IMessageService,
        private val mMessageList: MutableLiveData<List<IMessageService.Message>>,
        override val layoutId: Int = R.layout.fragment_chat_message,
        override val reload: Boolean = false
) : BaseFragment(), IChatFragment {

    private var mMessageAdapter: ChatMessageAdapter? = null
    private var mLinearLayoutManager: LinearLayoutManager? = null
    private val mPlayList: HashMap<String, AudioPlayerHelper?> = HashMap()
    private val mMessageData: MutableLiveData<ArrayList<IMessageService.Message>> = MutableLiveData()
    private var mActive: Boolean = true
    private var mNewMessageNum = 0
    /**新消息滚动最小列数，大于此列不自动滚动，小于列表自动滚动到底部
     * @see notifyNewMessage
     * */
    private var mNewMessageNumLimit = 5

    /**文件消息发送结果缓存，防止文件上传过程中，用户切换到其它页面后，消息状态无法更新*/
    private val mMessageUpdateCancel: MutableLiveData<HashMap<Int, IMessageService.Message>> = MutableLiveData()

    enum class EnumEvent(val key: String, val type: Int) {
        SEND("发送消息", -1),
        CANCEL_OK("已撤回消息", -2),
        UPDATE_FILE_MSG("更新文件消息", -3),
        CANCEL("撤回消息", 1)
    }

    override fun initData(savedInstanceState: Bundle?) {
        mMessageData.value = arrayListOf()
        mMessageUpdateCancel.value = HashMap()
    }

    override fun initView(view: View) {

        mMessageAdapter = ChatMessageAdapter(mSession, mMessageData.value!!, object : OnListItemClickListener {
            override fun onItemClick(view: View, position: Int, obj: Any?) {
                preOnItemClick(view, position, obj)
            }

            override fun onItemLongClick(view: View, position: Int, obj: Any?) {
                preOnItemLongClick(view, position, obj)
            }
        })
        mLinearLayoutManager = LinearLayoutManager(mContext)
        mLinearLayoutManager!!.stackFromEnd = false
        mLinearLayoutManager!!.reverseLayout = false
        chat_message_list.layoutManager = mLinearLayoutManager
        chat_message_list.adapter = mMessageAdapter
        chat_message_list.itemAnimator = null

        chat_message_list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
            mMessageData.value?.clear()
            it?.let { messages ->
                mMessageData.value!!.addAll(messages)
                mMessageAdapter?.notifyDataSetChanged()
                mLinearLayoutManager?.scrollToPosition(mMessageData.value!!.size - 1)
            }
        })

        mMessageData.observe(this, Observer {
            mMessageAdapter?.notifyDataSetChanged()
        })

        mMessageUpdateCancel.observe(this, Observer {
            it.values.forEach { msg ->
                notifyMessageSendStatus(msg)
            }
            mMessageUpdateCancel.value?.clear()
            LogUtil.d("聊天界面", "页面显示，更新缓存数据")
        })

        /**接收消息发送事件*/
        RxBus.toFlow(IMessageService.MessageSendEvent::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            if (mActive && it.session.sessionId == mSession.sessionId) {
                                it.message.forEach { message ->
                                    if (it.send) {
                                        sendMessage(message)
                                    } else {
                                        notifyNewMessage(message, it.bottom)
                                    }
                                }
                            }
                        }, {
                    it.printStackTrace()
                })

        /**接收文件消息更新事件*/
        RxBus.toFlow(IMessageService.MessageUpdateFileEvent::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            if (mActive && it.session.sessionId == mSession.sessionId) {
                                notifyFileMessage(it.message)
                            }
                        }, {
                    it.printStackTrace()
                })

        getData()

    }

    override fun getData() {

    }

    override fun onResume() {
        super.onResume()
        mActive = true
    }

    override fun onPause() {
        mActive = false
        stopAudioPlay()
        super.onPause()
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
        var position: Int? = null
        for ((index, msg) in mMessageData.value!!.withIndex()) {
            if (msg.timeline == message.timeline) {
                msg.sendStatus = message.sendStatus
                position = index
                break
            }
        }
        if (mActive) {
            position?.let {
                mMessageAdapter?.notifyItemChanged(it, ItemChatMessageBaseViewHolder.UpdateType.SEND_STATE)
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

    override fun deleteMessage(message: IMessageService.Message) {
        message.sendStatus = EnumChatSendStatus.CANCEL_CAN
        notifyMessageSendStatus(message)
    }

    @SuppressLint("SetTextI18n")
    override fun notifyNewMessage(message: IMessageService.Message, toBottom: Boolean) {
        mMessageData.value!!.add(message)
        val mMessageSize = mMessageData.value!!.size - 1
        mMessageAdapter?.notifyItemInserted(mMessageSize)
        val lastPosition = mLinearLayoutManager?.findLastCompletelyVisibleItemPosition() ?: 0
        val scroll = mMessageSize - mNewMessageNumLimit < lastPosition
        when {
            scroll || toBottom -> {
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
                session = DefMessageService.DefSession(sessionId = mSession.sessionId),
                message = message
        ))
    }

    /**停止所有语音播放*/
    private fun stopAudioPlay() {
        mPlayList.values.forEach {
            it?.stop()
        }
    }

    /**消息列表滚动到底部*/
    private fun scrollToBottom() {
        if (mMessageData.value?.isNotEmpty() == true) {
            mLinearLayoutManager?.scrollToPosition(mMessageData.value!!.size - 1)
        }
        chat_message_new_message_num.visibility = View.GONE
        mNewMessageNum = 0
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