package vip.qsos.app_chat.data.entity

import qsos.base.chat.api.MessageViewHelper
import qsos.base.chat.data.entity.EnumSessionType

/**
 * @author : 华清松
 * 聊天会话业务实体
 * @param sessionId 会话ID
 * @param creator 创建者IM账号
 * @param sessionType 会话类型
 */
data class ChatSessionBo(
        var sessionId: Long = -1L,
        var creator: String = "",
        var sessionType: EnumSessionType
) : MessageViewHelper.Session {
    override var id: String = ""
        get() = "$sessionId"
        set(v) {
            field = v
            sessionId = v.toLong()
        }
    override var type: Int = 0
        get() = sessionType.key
        set(v) {
            field = v
            sessionType = EnumSessionType.values()[v]
        }
}