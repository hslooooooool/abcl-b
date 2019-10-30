package qsos.base.chat.data.entity

/**
 * @author : 华清松
 * 消息内容-图片消息
 * @param name 图片名称
 * @param url 图片链接
 */
data class MChatMessageImage(
        val name: String,
        val url: String
) : IChatMessageType {
    override val contentDesc: String = MChatMessageType.IMAGE.contentDesc
    override val contentType: Int = MChatMessageType.IMAGE.contentType
}