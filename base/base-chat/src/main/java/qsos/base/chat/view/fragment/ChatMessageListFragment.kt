package qsos.base.chat.view.fragment

import android.annotation.SuppressLint
import android.graphics.Point
import android.os.Bundle
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.noober.menu.FloatMenu
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_chat_message.*
import kotlinx.android.synthetic.main.item_message_audio.view.*
import qsos.base.chat.R
import qsos.base.chat.data.entity.*
import qsos.base.chat.service.DefMessageService
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
        override val layoutId: Int = R.layout.fragment_chat_message,
        override val reload: Boolean = false
) : BaseFragment(), IChatFragment {

    private var mMessageAdapter: ChatMessageAdapter? = null
    private var mLinearLayoutManager: LinearLayoutManager? = null
    private val mPlayList: HashMap<String, AudioPlayerHelper?> = HashMap()
    private val mMessageData: MutableLiveData<ArrayList<IMessageService.Message>> = MutableLiveData()
    private var mActive: Boolean = true

    /**文件消息发送结果缓存，防止文件上传过程中，用户切换到其它页面后，消息状态无法更新的问题*/
    private val mMessageUpdateCancel: MutableLiveData<ArrayList<IMessageService.Message>> = MutableLiveData()

    enum class EnumEvent(val key: String, val type: Int) {
        CANCEL_OK("已撤回消息", -2),
        SEND("发送了消息", -1),
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

    override fun sendTextMessage() {

    }

    override fun sendFileMessage(type: EnumChatMessageType, files: ArrayList<HttpFileEntity>) {

    }

    override fun notifySendMessage(result: IMessageService.Message) {
        mMessageData.value?.find {
            it.timeline == result.timeline
        }?.sendStatus = result.sendStatus
        mMessageAdapter?.notifyDataSetChanged()
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

    /**更新已上传附件的消息*/
    private fun uploadFileMessage(file: HttpFileEntity, state: MBaseChatMessageFile.UpLoadState) {
        val timeline = file.adjoin as Int?
        var position: Int? = null
        for ((index, msg) in mMessageData.value!!.withIndex()) {
            if (msg.timeline == timeline) {
                mMessageData.value!![index].content.fields["uploadState"] = state
                position = index
                break
            }
        }
        position?.let {
            mMessageAdapter?.notifyItemChanged(it, ItemChatMessageBaseViewHolder.UpdateType.UPLOAD_STATE)
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
                    if (obj.realContent is MChatMessageAudio) {
                        playAudio(view, obj.realContent as MChatMessageAudio)
                    }
                }
            }
            R.id.item_message_cancel_reedit -> {
                if (obj != null && obj is IMessageService.Message && obj.content.getContentType() == EnumChatMessageType.TEXT.contentType) {
                    val content = obj.realContent as MChatMessageText
                    obj.sendStatus = EnumChatSendStatus.CANCEL_OK
                    notifySendMessage(obj)

                    RxBus.send(ChatMessageListFragmentEvent(
                            session = DefMessageService.DefSession(
                                    sessionId = mSession.sessionId
                            ),
                            type = EnumEvent.CANCEL,
                            data = content
                    ))
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

    /**删除（撤销）消息*/
    private fun deleteMessage(message: IMessageService.Message) {
        message.sendStatus = EnumChatSendStatus.CANCEL_CAN
        notifySendMessage(message)
    }

    /**检查消息附件上传情况，防止结束此页面时文件上传失败，友情提示*/
    private fun checkMessageFileUploaded(): Boolean {
        var uploaded = true
        for (m in mMessageData.value!!) {
            if (m.realContent is MBaseChatMessageFile) {
                val file = m.realContent as MBaseChatMessageFile
                if (file.uploadState != MBaseChatMessageFile.UpLoadState.SUCCESS) {
                    uploaded = false
                }
                break
            }
        }
        return uploaded
    }

    /**新消息页面更新*/
    private fun notifyNewMessage(message: IMessageService.Message) {
        mMessageData.value!!.add(message)
        mMessageAdapter?.notifyDataSetChanged()
        mLinearLayoutManager?.scrollToPosition(mMessageData.value!!.size - 1)
    }

    /**响应发送往本页面的事件*/
    private fun notifyFragmentEvent(event: ChatMessageListFragmentEvent) {
        when (event.type) {
            EnumEvent.SEND -> {
                if (event.data is IMessageService.Message) {

                    event.data.sendStatus = EnumChatSendStatus.SENDING
                    notifyNewMessage(event.data)

                    mMessageService.sendMessage(
                            message = event.data,
                            failed = { msg, result ->
                                ToastUtils.showToast(mContext, msg)
                                notifySendMessage(result)
                            },
                            success = { result ->
                                notifySendMessage(result)
                            }
                    )

                }
            }
            else -> {
            }
        }
    }
}