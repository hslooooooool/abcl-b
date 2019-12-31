package qsos.base.user.data.entity

/**
 * @author : 华清松
 * 登录用户信息
 */
data class LoginUser(
        var userId: Long = -1L,
        var name: String,
        var password: String,
        var imAccount: String,
        var avatar: String? = null
)