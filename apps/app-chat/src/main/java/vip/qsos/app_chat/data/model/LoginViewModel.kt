package vip.qsos.app_chat.data.model

import qsos.base.core.base.LoginUser
import kotlin.coroutines.CoroutineContext

/**
 * @author : 华清松
 * 登录用户相关接口
 */
interface LoginViewModel {
    val mJob: CoroutineContext

    fun clear()

    /**账号密码登录*/
    fun login(
            account: String,
            password: String,
            failed: (msg: String) -> Unit,
            success: (user: LoginUser) -> Unit
    )

    /**账号密码注册*/
    fun register(
            account: String,
            password: String,
            failed: (msg: String) -> Unit,
            success: (user: LoginUser) -> Unit
    )

}