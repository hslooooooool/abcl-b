package qsos.base.chat.data.entity

/**
 * @author : 华清松
 * 消息内容-视频消息
 */
class MChatMessageVideo : AbsChatMessageFile(), IMessageType {
    override var desc: String = EnumChatMessageType.VIDEO.desc
    override var type: Int = EnumChatMessageType.VIDEO.type
}