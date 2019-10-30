package qsos.base.chat.data.entity

/**
 * @author : 华清松
 * 消息内容-文件消息
 * @param length 文件长度,kb
 * @param name 文件名称
 * @param avatar 文件封面
 * @param url 文件链接
 */
data class MChatMessageFile(
        val length: Int,
        val name: String,
        val avatar: String,
        val url: String
) : IChatMessageType {
    override val contentDesc: String = MChatMessageType.FILE.contentDesc
    override val contentType: Int = MChatMessageType.FILE.contentType
}