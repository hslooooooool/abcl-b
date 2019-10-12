package qsos.base.chat.data.db

/**
 * @author : 华清松
 * 消息与用户 N:1 关联表, * 用以获取消息对应的发送用户信息
 * @param userId 用户ID
 * @param messageId 消息ID
 */
data class RelationMessageWithUser(val userId: Int, val messageId: Int)