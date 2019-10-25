package qsos.base.chat.data.entity

/**
 * @author : 华清松
 * 消息内容-音频消息
 * @param length 时长，毫秒数
 * @param name 音频名称
 * @param url 音频链接
 */
data class MChatMessageAudio(
        val length: Int,
        val name: String,
        val url: String
) : IChatMessageType {

    override val contentType: Int = MChatMessageType.AUDIO.contentType
}