package vip.qsos.app_chat.data.event

import qsos.lib.base.utils.rx.RxBus

/**
 * @author : 华清松
 * 登录成功事件
 */
class LoginSuccessEvent : RxBus.RxBusEvent<LoginSuccessEvent> {
    override fun message(): LoginSuccessEvent {
        return this
    }

    override fun name(): String {
        return "登录成功"
    }

}