package qsos.base.chat.view.activity

import android.view.View
import qsos.base.chat.service.IMessageService

/**
 * @author : 华清松
 * 聊天会话功能接口
 */
interface IChatSessionModel {

    /**点击文本消息*/
    fun clickTextMessage(view: View, message: IMessageService.Message, back: (action: Int) -> Unit)

    /**长按文本消息*/
    fun longClickTextMessage(view: View, message: IMessageService.Message, back: (action: Int) -> Unit)
}