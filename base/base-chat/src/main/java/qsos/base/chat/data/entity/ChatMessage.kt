package qsos.base.chat.data.entity

/**
 * @author : 华清松
 * 聊天消息,一条聊天消息对应唯一一条时间线
 * @param sessionId 会话ID
 * @param messageId 消息ID
 * @param sequence 消息顺序
 * @param content 消息内容
 */
data class ChatMessage(
        /**@see ChatSession.sessionId*/
        val sessionId: Long,
        val messageId: Long,
        val sequence: Long,
        val content: ChatContent
)