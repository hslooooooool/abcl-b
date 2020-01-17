package qsos.base.chat.data.entity

/**
 * @author : 华清松
 * 消息内容-图片消息
 */
class MChatMessageImage : AbsChatMessageFile(), IMessageType {
    override var desc: String = EnumChatMessageType.IMAGE.desc
    override var type: Int = EnumChatMessageType.IMAGE.type
}