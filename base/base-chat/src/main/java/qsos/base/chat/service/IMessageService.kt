package qsos.base.chat.service

import qsos.base.chat.data.entity.ChatContent
import qsos.base.chat.data.entity.EnumChatSendStatus
import qsos.lib.base.utils.rx.RxBus

/**
 * @author : 华清松
 * 消息服务配置接口
 */
interface IMessageService {

    /**消息发送事件实体
     * @param session 会话实体
     * @param message 消息实体
     * */
    data class MessageSendEvent(
            val session: Session,
            val message: List<Message>
    ) : RxBus.RxBusEvent<MessageSendEvent> {
        override fun message(): MessageSendEvent? {
            return this
        }

        override fun name(): String {
            return "消息发送"
        }
    }

    /**文件消息更新事件实体
     * @param session 会话实体
     * @param message 消息实体
     * */
    data class MessageUpdateFileEvent(
            val session: Session,
            val message: Message
    ) : RxBus.RxBusEvent<MessageUpdateFileEvent> {
        override fun message(): MessageUpdateFileEvent? {
            return this
        }

        override fun name(): String {
            return "文件消息更新"
        }
    }

    /**文本消息撤回事件实体
     * @param session 会话实体
     * @param message 消息实体
     * */
    data class MessageReceiveEvent(
            val session: Session,
            val message: Message
    ) : RxBus.RxBusEvent<MessageReceiveEvent> {
        override fun message(): MessageReceiveEvent? {
            return this
        }

        override fun name(): String {
            return "文件消息撤回"
        }
    }

    /**会话实体属性*/
    interface Session {
        /**会话ID*/
        var sessionId: Int
        /**会话名称*/
        var sessionName: String
    }

    /**消息实体属性*/
    interface Message {
        /**消息ID*/
        var messageId: Int
        /**会话ID*/
        var sessionId: Int
        /**消息时间线，发送时本地以content.hashCode为值，发送后服务器统一设置*/
        var timeline: Int
        /**发送人ID*/
        var sendUserId: Int
        /**发送人名称*/
        var sendUserName: String
        /**发送人头像*/
        var sendUserAvatar: String
        /**创建时间*/
        var createTime: String
        /**消息内容*/
        var content: ChatContent
        /**发送状态*/
        var sendStatus: EnumChatSendStatus?
        /**已读人数*/
        var readNum: Int

        /**消息转换后实体*/
        fun <T> getRealContent(): T?
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