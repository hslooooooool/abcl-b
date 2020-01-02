package qsos.base.chat.data.entity

/**
 * @author : 华清松
 * 消息内容接口,自定义消息实体需实现此接口
 */
interface IMessageType {
    /**消息摘要*/
    var contentDesc: String
    /**消息内容类型值
     * @see EnumChatMessageType*/
    var contentType: Int
}