package qsos.base.chat.data.entity

/**
 * @author : 华清松
 * 消息内容-视频消息
 */
class MChatMessageVideo : MBaseChatMessageFile(), IChatMessageType {
    override var contentDesc: String = MChatMessageType.VIDEO.contentDesc
    override var contentType: Int = MChatMessageType.VIDEO.contentType
}