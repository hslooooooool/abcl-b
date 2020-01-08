package vip.qsos.app_chat.data.entity

import qsos.base.chat.api.IMessageListService
import qsos.base.chat.data.entity.EnumSessionType

/**
 * @author : 华清松
 * 群聊实体
 * @param lastMessageId 最后一条消息ID
 * @param lastMessageTimeline 最后一条消息时序
 */
data class ChatSession(
        var lastMessageId: Long? = null,
        var lastMessageTimeline: Long? = null
) : IMessageListService.Session {
    override var id: String = ""
    override var type: Int = EnumSessionType.GROUP.key
}