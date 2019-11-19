package qsos.base.chat.service

import qsos.base.chat.data.entity.ChatContent
import qsos.lib.base.utils.rx.RxBus

/**
 * @author : 华清松
 * 消息服务配置接口
 */
interface IMessageService {

    /**消息推送事件实体
     * @param session 会话实体
     * @param message 消息实体
     * */
    data class MessageData(
            val session: Session,
            val message: List<Message>
    ) : RxBus.RxBusEvent<MessageData> {
        override fun message(): MessageData? {
            return this
        }

        override fun name(): String {
            return "会话"
        }
    }

    interface Session {
        var id: Int
        var name: String
    }

    interface Message {
        var messageId: Int
        var sessionId: Int
        var sendUserId: Int
        var sendUserName: String
        var sendUserAvatar: String
        var timeline: Int
        var content: ChatContent
        var createTime: String
    }

    /**发送消息
     * @param message 消息实体
     * @param failed 失败回执
     * @param success 成功回执
     * */
    fun sendMessage(
            message: Message,
            failed: (msg: String, message: Message) -> Unit,
            success: (message: Message) -> Unit
    )

    /**撤销消息
     * @param message 消息实体
     * @param failed 失败回执
     * @param success 成功回执
     * */
    fun revokeMessage(
            message: Message,
            failed: (msg: String, message: Message) -> Unit,
            success: (message: Message) -> Unit
    )
}