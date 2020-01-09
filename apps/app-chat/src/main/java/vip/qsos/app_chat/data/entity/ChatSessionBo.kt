package vip.qsos.app_chat.data.entity

import qsos.base.chat.data.entity.EnumSessionType

/**
 * @author : 华清松
 * 聊天会话业务实体
 * @param id 会话ID
 * @param type 会话类型
 * @param title 标题
 * @param avatar 封面
 * @param content 消息内容
 */
data class ChatSessionBo(
        var id: Long,
        var type: EnumSessionType,
        var title: String,
        var avatar: String? = null,
        var content: String? = null
) {
    fun getSession(): Session {
        return Session("$id", type.key)
    }
}