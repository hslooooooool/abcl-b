package vip.qsos.app_chat.data.entity

import qsos.base.chat.api.IMessageListService
import qsos.base.chat.data.entity.EnumChatType

/**
 * @author : 华清松
 * 消息会话,一个消息会话关联一个唯一的群,相当于消息订阅主题,关联的群可看做是会话的信息拓展,
 * 在单聊场景下,没有群的概念,当单聊场景下增加第三人后,方才会创建群
 * @param id 群号
 * @param name 群名称
 * @param creator 创建者账号
 * @param member 群成员账号集合
 * @param chatType 群类型
 * @param lastMessageId 最后一条消息ID
 * @param lastMessageTimeline 最后一条消息时序
 *
 * @see ChatGroupInfo
 */
data class ChatGroup(
        override var id: Long,
        override var name: String = "",
        var creator: String,
        var member: List<String>,
        var chatType: EnumChatType,
        var lastMessageId: Long? = null,
        var lastMessageTimeline: Long? = null
) : IMessageListService.Group {
    override var type: Int = this.chatType.key
}