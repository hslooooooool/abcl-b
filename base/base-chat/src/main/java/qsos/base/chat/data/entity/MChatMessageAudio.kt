package qsos.base.chat.data.entity

/**
 * @author : 华清松
 * 消息内容-音频消息
 */
class MChatMessageAudio  : MBaseChatMessageFile(), IChatMessageType {
    override var contentDesc: String = EnumChatMessageType.AUDIO.contentDesc
    override var contentType: Int = EnumChatMessageType.AUDIO.contentType
}