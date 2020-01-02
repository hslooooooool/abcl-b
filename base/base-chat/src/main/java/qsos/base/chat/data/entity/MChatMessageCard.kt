package qsos.base.chat.data.entity

/**
 * @author : 华清松
 * 消息内容-名片消息
 * @param name 人员名称
 * @param desc 人员描述
 * @param avatar 人员头像
 */
data class MChatMessageCard(
        val name: String,
        val desc: String,
        val avatar: String
) : IMessageType {
    override var contentDesc: String = EnumChatMessageType.CARD.contentDesc
    override var contentType: Int = EnumChatMessageType.CARD.contentType
}