package qsos.base.chat.data.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import qsos.base.chat.data.ApiChatMessage
import qsos.base.chat.data.entity.ChatMessage
import qsos.base.chat.data.entity.MChatMessage
import qsos.base.chat.data.entity.MChatSendStatus
import qsos.lib.netservice.ApiEngine
import qsos.lib.netservice.expand.retrofitByDef
import kotlin.coroutines.CoroutineContext

/**
 * @author : 华清松
 * 消息发送服务，发送消息调用此类下方法，发送应保存发送记录到数据库，根据发送回执更新数据，
 * 消息状态与数据展示页面将绑定对应的数据库进行操作，而非通过发送的消息数据进行监听
 */
object MessageHelper {
    private val mJob: CoroutineContext = Dispatchers.Main + Job()

    /**发送消息*/
    fun sendMessage(
            message: MChatMessage,
            failed: (msg: String, message: MChatMessage) -> Unit,
            success: (message: MChatMessage) -> Unit
    ) {
        CoroutineScope(mJob).retrofitByDef<ChatMessage> {
            api = ApiEngine.createService(ApiChatMessage::class.java).sendMessage(message = message.message)
            onFailed { _, msg, error ->
                message.sendStatus = MChatSendStatus.FAILED
                failed.invoke(msg ?: "发送失败${error?.message}", message)
            }
            onSuccess {
                if (it == null) {
                    message.sendStatus = MChatSendStatus.FAILED
                    failed.invoke("发送失败", message)
                } else {
                    message.sendStatus = MChatSendStatus.SUCCESS
                    message.message.messageId = it.messageId
                    success.invoke(message)
                }
            }
        }
    }
}