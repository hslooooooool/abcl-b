package qsos.base.chat.service

import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import qsos.base.chat.ChatMessageViewConfig
import qsos.base.chat.data.ApiChatMessage
import qsos.base.chat.data.db.DBChatDatabase
import qsos.base.chat.data.entity.*
import qsos.lib.base.utils.LogUtil
import qsos.lib.netservice.ApiEngine
import qsos.lib.netservice.data.BaseResponse
import qsos.lib.netservice.expand.retrofitByDef
import qsos.lib.netservice.expand.retrofitWithSuccess
import java.util.*
import kotlin.coroutines.CoroutineContext

/**
 * @author : 华清松
 * 消息服务配置
 */
class DefMessageService(
        private val mJob: CoroutineContext = Dispatchers.Main + Job()
) : AbsMessageService() {
    private var mPullMessageTimer: Timer? = null

    class DefSession(
            override var sessionId: Int,
            override var sessionName: String = "",
            override var sessionType: Int = ChatType.SINGLE.key
    ) : IMessageService.Session

    class DefMessage(
            override var messageId: Int = -1,
            override var sessionId: Int,
            override var sendUserId: Int = 3,
            override var sendUserName: String = "测试用户",
            override var sendUserAvatar: String = "http://www.qsos.vip/upload/2018/11/ic_launcher20181225044818498.png",
            override var timeline: Int,
            override val content: ChatContent = ChatContent(),
            override var createTime: String,
            override var readNum: Int = 1
    ) : IMessageService.Message {

        override var readStatus: Boolean? = null
            get() = if (field == null) true else field

        override var sendStatus: EnumChatSendStatus? = null
            get() = if (field == null) EnumChatSendStatus.SUCCESS else field

        override fun <T> getRealContent(): T? {
            return if (content.getContentType() == -1) null else {
                val gson = Gson()
                val json = gson.toJson(content.fields)
                val type = ChatMessageViewConfig.getContentType(content.getContentType())
                try {
                    gson.fromJson(json, type) as T?
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
        }
    }

    override fun getMessageList(session: IMessageService.Session, messageList: MutableLiveData<ArrayList<IMessageService.Message>>) {
        DBChatDatabase.DefChatSessionDao.getChatSessionById(session.sessionId) { oldSession ->
            oldSession?.let {
                val lastTimeline: Int = it.lastMessageTimeline ?: -1
                CoroutineScope(mJob).retrofitWithSuccess<BaseResponse<List<ChatMessageBo>>> {
                    /**获取此会话sessionId下最后一条消息lastTimeline以上第1页20条数据*/
                    api = ApiEngine.createService(ApiChatMessage::class.java).getMessageListBySessionIdAndTimeline(
                            sessionId = session.sessionId, timeline = lastTimeline, next = false, page = 1, size = 20
                    )
                    onSuccess { result ->
                        result?.data?.let { list ->
                            list.sortedBy { msg ->
                                msg.timeline
                            }
                            val array = arrayListOf<IMessageService.Message>()
                            array.addAll(list)
                            oldSession.nowLastMessageId = array.last().messageId
                            oldSession.nowLastMessageTimeline = array.last().timeline
                            DBChatDatabase.DefChatSessionDao.update(oldSession) { ok ->
                                messageList.postValue(array)
                                LogUtil.d("会话更新", (if (ok) "已" else "未") + "更新会话最新消息")
                            }
                        }
                    }
                }
            }
        }
    }

    override fun sendMessage(
            message: IMessageService.Message,
            failed: (msg: String, message: IMessageService.Message) -> Unit,
            success: (message: IMessageService.Message) -> Unit
    ) {
        val sendMessage = ChatMessage(
                sessionId = message.sessionId,
                timeline = message.timeline,
                content = message.content
        )
        CoroutineScope(mJob).retrofitByDef<ChatMessage> {
            api = ApiEngine.createService(ApiChatMessage::class.java).sendMessage(message = sendMessage)
            onFailed { _, msg, error ->
                message.sendStatus = EnumChatSendStatus.FAILED
                failed.invoke(msg ?: "发送失败${error?.message}", message)
            }
            onSuccess {
                if (it == null) {
                    message.sendStatus = EnumChatSendStatus.FAILED
                    failed.invoke("发送失败", message)
                } else {
                    message.sendStatus = EnumChatSendStatus.SUCCESS
                    message.messageId = it.messageId
                    success.invoke(message)
                }
            }
        }
    }

    override fun readMessage(message: IMessageService.Message, failed: (msg: String, message: IMessageService.Message) -> Unit, success: (message: IMessageService.Message) -> Unit) {
        CoroutineScope(mJob).retrofitByDef<ChatMessageReadStatusBo> {
            api = ApiEngine.createService(ApiChatMessage::class.java).readMessage(messageId = message.messageId)
            onFailed { _, msg, error ->
                failed.invoke(msg ?: "更新已读失败${error?.message}", message)
            }
            onSuccess {
                if (it?.readStatus == true) {
                    message.readStatus = it.readStatus
                    message.readNum = it.readNum
                    success.invoke(message)
                } else {
                    failed.invoke("更新已读失败", message)
                }
            }
        }
    }

    override fun revokeMessage(
            message: IMessageService.Message,
            failed: (msg: String, message: IMessageService.Message) -> Unit,
            success: (message: IMessageService.Message) -> Unit
    ) {
        CoroutineScope(mJob).retrofitByDef<Boolean> {
            api = ApiEngine.createService(ApiChatMessage::class.java).deleteMessage(messageId = message.messageId)
            onFailed { _, msg, error ->
                failed.invoke(msg ?: "撤回失败${error?.message}", message)
            }
            onSuccess {
                if (it == true) {
                    message.sendStatus = EnumChatSendStatus.CANCEL_OK
                    success.invoke(message)
                } else {
                    failed.invoke("撤回失败", message)
                }
            }
        }
    }

    override fun clear() {
        mPullMessageTimer?.cancel()
        mJob.cancel()
    }
}