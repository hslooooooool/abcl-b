package qsos.base.chat.data.entity

/**
 * @author : 华清松
 * 消息内容-图片消息
 */
class MChatMessageImage : MBaseChatMessageFile(), IChatMessageType {
    override var contentDesc: String = EnumChatMessageType.IMAGE.contentDesc
    override var contentType: Int = EnumChatMessageType.IMAGE.contentType
}