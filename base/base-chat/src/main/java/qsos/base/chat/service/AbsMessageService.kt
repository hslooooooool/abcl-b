package qsos.base.chat.service

import qsos.lib.base.utils.rx.RxBus

/**
 * @author : 华清松
 * 消息服务配置接口
 */
abstract class AbsMessageService : IMessageService {

    /**拉取到新消息进行会话内更新
     * @param session 会话实体
     * @param message 消息列表
     * */
    fun notifyMessage(session: IMessageService.Session, message: List<IMessageService.Message>) {
        RxBus.send(IMessageService.MessageSendEvent(session, message))
    }

}