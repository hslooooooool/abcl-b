package qsos.base.chat.data.entity

/**
 * @author : 华清松
 * 消息内容-名片消息
 * @param name 人员名称
 * @param avatar 人员头像
 * @param description 人员描述
 */
data class MChatMessageCard(
        val name: String,
        val avatar: String,
        val description: String
) : IMessageType {
    override var desc: String = EnumChatMessageType.CARD.desc
    override var type: Int = EnumChatMessageType.CARD.type
}