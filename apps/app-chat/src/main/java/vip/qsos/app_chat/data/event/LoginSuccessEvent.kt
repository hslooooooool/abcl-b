package vip.qsos.app_chat.data.event

import qsos.lib.base.utils.rx.RxBus
import qsos.base.core.base.LoginUser

/**
 * @author : 华清松
 * 登录成功事件
 */
data class LoginSuccessEvent(
        val user: LoginUser
) : RxBus.RxBusEvent<LoginSuccessEvent> {
    override fun message(): LoginSuccessEvent {
        return this
    }

    override fun name(): String {
        return "登录成功"
    }

}