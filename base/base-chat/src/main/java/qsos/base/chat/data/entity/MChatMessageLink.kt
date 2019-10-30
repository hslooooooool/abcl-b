package qsos.base.chat.data.entity

/**
 * @author : 华清松
 * 消息内容-链接消息
 * @param name 链接名称
 * @param desc 链接描述
 * @param url 链接地址
 */
data class MChatMessageLink(
        val name: String,
        val desc: String,
        val url: String
) : IChatMessageType {
    override val contentDesc: String = MChatMessageType.LINK.contentDesc
    override val contentType: Int = MChatMessageType.LINK.contentType
}