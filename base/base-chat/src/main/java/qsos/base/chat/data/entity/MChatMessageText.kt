package qsos.base.chat.data.entity

/**
 * @author : 华清松
 * 消息内容-文本消息
 * @param content 文本内容
 */
data class MChatMessageText(
        val content: String
) : IMessageType {
    override var contentDesc: String = content
    override var contentType: Int = EnumChatMessageType.TEXT.contentType
}