package qsos.base.chat.service

import qsos.lib.base.utils.rx.RxBus

/**
 * @author : 华清松
 * 消息服务配置接口
 */
abstract class AbsMessageService : IMessageService {

    override fun notifyNewMessage(session: IMessageService.Session, message: List<IMessageService.Message>) {
        RxBus.send(IMessageService.MessageSendEvent(session, message, send = false, bottom = false))
    }

}