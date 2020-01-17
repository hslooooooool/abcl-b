package qsos.base.chat.data.entity

/**
 * @author : 华清松
 * 消息内容-音频消息
 */
class MChatMessageAudio : AbsChatMessageFile(), IMessageType {
    override var desc: String = EnumChatMessageType.AUDIO.desc
    override var type: Int = EnumChatMessageType.AUDIO.type
}