package qsos.base.chat

import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import qsos.base.chat.data.ApiChatMessage
import qsos.base.chat.data.entity.ChatContent
import qsos.base.chat.data.entity.ChatMessage
import qsos.base.chat.data.entity.ChatType
import qsos.base.chat.data.entity.EnumChatSendStatus
import qsos.base.chat.service.AbsMessageService
import qsos.base.chat.service.IMessageService
import qsos.lib.base.utils.DateUtils
import qsos.lib.netservice.ApiEngine
import qsos.lib.netservice.expand.retrofitByDef
import java.util.*
import kotlin.concurrent.timerTask
import kotlin.coroutines.CoroutineContext

/**
 * @author : 华清松
 * 消息服务配置
 */
class DefMessageService(
        private val mJob: CoroutineContext = Dispatchers.Main + Job()
) : AbsMessageService() {

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
            override var content: ChatContent,
            override var createTime: String,
            override var readNum: Int = 1
    ) : IMessageService.Message {

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

    init {
        var mChatContent: ChatContent
        var index: Int
        Timer().schedule(timerTask {
            index = UUID.randomUUID().hashCode()
            mChatContent = ChatContent()
                    .create(0, "自动发送文本$index")
                    .put("content", "自动发送文本$index")

            notifyMessage(DefSession(sessionId = 1), arrayListOf(
                    DefMessage(
                            index,
                            sessionId = 1,
                            timeline = index,
                            content = mChatContent,
                            createTime = DateUtils.getTimeToNow(Date()))
            ))
        }, 5000L, 1000000L)
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

}