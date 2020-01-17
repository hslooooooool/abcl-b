package qsos.base.chat.data.entity

/**
 * @author : 华清松
 * 消息内容-链接消息
 * @param name 链接名称
 * @param url 链接地址
 * @param description 链接描述
 */
data class MChatMessageLink(
        val name: String,
        val url: String,
        val description: String
) : IMessageType {
    override var desc: String = EnumChatMessageType.LINK.desc
    override var type: Int = EnumChatMessageType.LINK.type
}