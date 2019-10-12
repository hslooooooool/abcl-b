package qsos.base.chat.data.entity

/**
 * @author : 华清松
 * 聊天用户
 * @param userId 用户ID
 * @param userName 用户姓名
 * @param avatar 用户头像
 * @param birth 用户出生日期
 * @param sexuality 用户性别
 */
data class ChatUser(
        var userId: Int,
        var userName: String,
        var avatar: String? = null,
        var birth: String? = null,
        var sexuality: Boolean? = null
)