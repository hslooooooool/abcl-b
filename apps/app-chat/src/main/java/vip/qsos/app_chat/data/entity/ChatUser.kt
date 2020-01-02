package vip.qsos.app_chat.data.entity

/**
 * @author : 华清松
 * 聊天用户
 * @param userId 用户ID
 * @param name 用户姓名
 * @param imAccount 关联的消息账号
 * @param avatar 用户头像
 */
data class ChatUser(
        var userId: Long = -1,
        var name: String,
        var imAccount: String,
        var avatar: String? = null
)