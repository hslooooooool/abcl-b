package qsos.base.chat.view.fragment

import android.annotation.SuppressLint
import android.graphics.Point
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.noober.menu.FloatMenu
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_chat_message.*
import kotlinx.android.synthetic.main.item_message_audio.view.*
import qsos.base.chat.DefMessageService
import qsos.base.chat.R
import qsos.base.chat.data.entity.*
import qsos.base.chat.service.IMessageService
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
import qsos.lib.netservice.file.HttpFileEntity
import java.util.*
import kotlin.collections.HashMap

/**
 * @author : 华清松
 * 聊天页面
 */
class ChatMessageListFragment(
        private val mSession: ChatSession,
        private val mMessageService: IMessageService,
        private val mMessageList: MutableLiveData<List<IMessageService.Message>>,
        private val mActivityHandler: Handler,
        override val layoutId: Int = R.layout.fragment_chat_message,
        override val reload: Boolean = false
) : BaseFragment(), IChatFragment {
    companion object {
        const val SESSION_ID = "SESSION_ID"
        const val DATA = "DATA"
    }

    private var mMessageAdapter: ChatMessageAdapter? = null
    private var mLinearLayoutManager: LinearLayoutManager? = null
    private val mPlayList: HashMap<String, AudioPlayerHelper?> = HashMap()
    private val mMessageData: MutableLiveData<ArrayList<IMessageService.Message>> = MutableLiveData()
    private var mActive: Boolean = true

    /**文件消息发送结果缓存，防止文件上传过程中，用户切换到其它页面后，消息状态无法更新*/
    private val mMessageUpdateCancel: MutableLiveData<ArrayList<IMessageService.Message>> = MutableLiveData()

    private val mFragmentHandler: Handler = Handler {
        when (it.what) {

        }
        return@Handler true
    }

    fun getHandler(): Handler {
        return mFragmentHandler
    }

    enum class EnumEvent(val key: String, val type: Int) {
        SEND("发送消息", -1),
        CANCEL_OK("已撤回消息", -2),
        UPDATE_FILE_MSG("更新文件消息", -3),
        CANCEL("撤回消息", 1)
    }

    /**消息列表通信与交互实体
     * @param session 会话实体
     * @param type 通信与交互类型。<0 为其它页面发送往本页面的动作，本页面响应；>0 为本页面发送往其它页面的动作，其它页面响应
     * */
    data class ChatMessageListFragmentEvent(
            val session: IMessageService.Session,
            val type: EnumEvent,
            val data: Any?
    ) : RxBus.RxBusEvent<ChatMessageListFragmentEvent> {

        override fun message(): ChatMessageListFragmentEvent {
            return this
        }

        override fun name(): String {
            return "消息列表通信与交互实体"
        }
    }

    override fun initData(savedInstanceState: Bundle?) {
        mMessageData.value = arrayListOf()
        mMessageUpdateCancel.value = arrayListOf()
    }

    @SuppressLint("CheckResult")
    override fun initView(view: View) {

        mMessageAdapter = ChatMessageAdapter(mSession, mMessageData.value!!,
                object : OnListItemClickListener {
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
            it.forEach { msg ->
                notifySendMessage(msg)
            }
            mMessageUpdateCancel.value?.clear()
            LogUtil.d("聊天界面", "页面显示，更新数据")
        })

        DefMessageService()

        RxBus.toFlow(IMessageService.MessageData::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            if (mActive && it.session.sessionId == mSession.sessionId) {
                                it.message.forEach { msg ->
                                    notifyNewMessage(msg)
                                }
                            }
                        }, {
                    it.printStackTrace()
                }
                )

        RxBus.toFlow(ChatMessageListFragmentEvent::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            if (
                                    mActive
                                    && it.session.sessionId == mSession.sessionId
                                    && it.type.type < 0
                            ) {
                                notifyFragmentEvent(it)
                            }
                        }, {
                    it.printStackTrace()
                }
                )

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

    override fun sendMessage(msg: IMessageService.Message, new: Boolean) {
        msg.sendStatus = EnumChatSendStatus.SENDING
        if (new) {
            notifyNewMessage(msg)
        }
        mMessageService.sendMessage(
                message = msg,
                failed = { error, result ->
                    ToastUtils.showToast(mContext, error)
                    result.sendStatus = EnumChatSendStatus.FAILED
                    notifySendMessage(result)
                },
                success = { result ->
                    result.sendStatus = EnumChatSendStatus.SUCCESS
                    notifySendMessage(result)
                }
        )
    }

    override fun notifySendMessage(msg: IMessageService.Message) {
        mMessageData.value?.find {
            it.timeline == msg.timeline
        }?.sendStatus = msg.sendStatus
        if (mActive) {
            mMessageAdapter?.notifyDataSetChanged()
        } else {
            LogUtil.d("聊天界面", "页面隐藏，缓存数据")
            val list = mMessageUpdateCancel.value
            list?.add(msg)
            mMessageUpdateCancel.postValue(list)
        }
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

    override fun updateFileMessage(file: HttpFileEntity) {
        val message = file.adjoin as IMessageService.Message
        var position: Int? = null
        val state: EnumChatSendStatus = when {
            file.loadSuccess -> EnumChatSendStatus.SUCCESS
            file.progress >= 0 -> EnumChatSendStatus.SENDING
            else -> EnumChatSendStatus.FAILED
        }
        for ((index, msg) in mMessageData.value!!.withIndex()) {
            if (msg.timeline == message.timeline) {
                position = index
                break
            }
        }
        message.sendStatus = state
        if (state == EnumChatSendStatus.SUCCESS) {
            sendMessage(message, false)
        } else {
            position?.let {
                mMessageAdapter?.notifyItemChanged(it, ItemChatMessageBaseViewHolder.UpdateType.SEND_STATE)
            }
        }
    }

    override fun deleteMessage(message: IMessageService.Message) {
        message.sendStatus = EnumChatSendStatus.CANCEL_CAN
        notifySendMessage(message)
    }

    override fun notifyNewMessage(message: IMessageService.Message) {
        mMessageData.value!!.add(message)
        mMessageAdapter?.notifyDataSetChanged()
        mLinearLayoutManager?.scrollToPosition(mMessageData.value!!.size - 1)
    }

    override fun notifyFragmentEvent(event: ChatMessageListFragmentEvent) {
        when (event.type) {
            EnumEvent.SEND -> {
                if (event.data is IMessageService.Message) {
                    event.data.sendStatus = EnumChatSendStatus.SENDING
                    when (event.data.content.getContentType()) {
                        EnumChatMessageType.FILE.contentType -> notifyNewMessage(event.data)
                        else -> {
                            sendMessage(event.data)
                        }
                    }
                }
            }
            EnumEvent.UPDATE_FILE_MSG -> {
                if (event.data is HttpFileEntity) {
                    updateFileMessage(event.data)
                }
            }
            EnumEvent.CANCEL_OK -> {

            }
            else -> {
            }
        }
    }

    /**停止所有语音播放*/
    private fun stopAudioPlay() {
        mPlayList.values.forEach {
            it?.stop()
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
                        notifySendMessage(obj)

                        RxBus.send(ChatMessageListFragmentEvent(
                                session = DefMessageService.DefSession(
                                        sessionId = mSession.sessionId
                                ),
                                type = EnumEvent.CANCEL,
                                data = it.content
                        ))
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