package qsos.base.chat.data.entity

/**
 * @author : 华清松
 * 消息内容-文本消息
 * @param content 文本内容
 */
data class MChatMessageText(
        val content: String
) : IChatMessageType {

    override val contentType: Int = MChatMessageType.TEXT.contentType
}