package vip.qsos.app_chat.data.entity

import qsos.base.core.base.LoginUser

/**
 * @author : 华清松
 * 聊天用户
 * @param id 用户ID
 * @param name 用户姓名
 * @param imAccount 关联的消息账号
 * @param avatar 用户头像
 */
data class AppUserBo(
        var id: Long = -1,
        var name: String,
        var imAccount: String,
        var avatar: String? = null
) {
    companion object {
        fun create(user: LoginUser): AppUserBo {
            return AppUserBo(
                    user.id, user.name, user.imAccount, user.avatar
            )
        }
    }
}