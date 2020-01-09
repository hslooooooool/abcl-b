package vip.qsos.app_chat.data.entity

import qsos.base.chat.data.entity.ChatContent

/**
 * @author : 华清松
 * 聊天消息,一条聊天消息对应唯一一条时间线
 * @param sessionId 会话ID
 * @param messageId 消息ID
 * @param timeline 消息顺序
 * @param cancelBack 消息已撤回
 * @param content 消息内容
 */
data class ChatMessage(
        var sessionId: Long = -1L,
        var messageId: Long = -1L,
        var timeline: Long = -1L,
        var cancelBack: Boolean = false,
        var content: ChatContent
)