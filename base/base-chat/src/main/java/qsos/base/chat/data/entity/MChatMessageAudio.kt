package qsos.base.chat.data.entity

/**
 * @author : 华清松
 * 消息内容-音频消息
 */
class MChatMessageAudio  : MBaseChatMessageFile(), IChatMessageType {
    override var contentDesc: String = MChatMessageType.AUDIO.contentDesc
    override var contentType: Int = MChatMessageType.AUDIO.contentType
}