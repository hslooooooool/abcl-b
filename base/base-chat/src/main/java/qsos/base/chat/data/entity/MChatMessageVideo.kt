package qsos.base.chat.data.entity

/**
 * @author : 华清松
 * 消息内容-视频消息
 * @param name 视频名称
 * @param url 视频链接
 * @param avatar 封面链接
 */
data class MChatMessageVideo(
        val name: String,
        val url: String,
        val avatar: String
) : IChatMessageType {

    override val contentType: Int = MChatMessageType.VIDEO.contentType
}