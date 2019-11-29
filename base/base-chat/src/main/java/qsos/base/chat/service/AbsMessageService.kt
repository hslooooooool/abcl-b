package qsos.base.chat.service

import qsos.lib.base.utils.rx.RxBus

/**
 * @author : 华清松
 * 消息服务配置接口
 */
abstract class AbsMessageService : IMessageService {

    override fun notifyMessage(session: IMessageService.Session, message: List<IMessageService.Message>) {
        RxBus.send(IMessageService.MessageSendEvent(session, message, send = false, bottom = true, update = true))
    }

    override fun notifyOldMessage(session: IMessageService.Session, message: List<IMessageService.Message>) {
        RxBus.send(IMessageService.MessageSendEvent(session, message, send = false, bottom = false, update = true))
    }

    override fun notifyNewMessage(session: IMessageService.Session, message: List<IMessageService.Message>) {
        RxBus.send(IMessageService.MessageSendEvent(session, message, send = false, bottom = false, update = false))
    }

}