package qsos.base.user.data.entity

/**
 * @author : 华清松
 * 登录用户信息
 */
data class LoginUser(
        var userId: Int = -1,
        var userName: String,
        var account: String,
        var password: String,
        var avatar: String? = null,
        var birth: String? = null,
        var sexuality: Int = -1
)