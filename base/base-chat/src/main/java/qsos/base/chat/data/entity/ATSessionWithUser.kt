package qsos.base.chat.data.entity

/**
 * @author : 华清松
 * 会话与用户 N:N 关联表, * 用以获取会话下所有用户或用户订阅的所有会话
 * @param userId 用户ID
 * @param sessionId 会话ID
 */
data class ATSessionWithUser(val userId: Long, val sessionId: Long)