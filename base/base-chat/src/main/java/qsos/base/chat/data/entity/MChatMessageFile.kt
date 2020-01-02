package qsos.base.chat.data.entity

/**
 * @author : 华清松
 * 消息内容-文件消息
 */
open class MChatMessageFile : AbsChatMessageFile(), IMessageType {
    override var contentDesc: String = EnumChatMessageType.FILE.contentDesc
    override var contentType: Int = EnumChatMessageType.FILE.contentType
}