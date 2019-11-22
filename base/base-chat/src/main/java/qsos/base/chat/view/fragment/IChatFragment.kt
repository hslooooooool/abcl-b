package qsos.base.chat.view.fragment

import android.view.View
import qsos.base.chat.data.entity.MChatMessageAudio
import qsos.base.chat.service.IMessageService

/**
 * @author : 华清松
 * 聊天消息列表页功能接口
 */
interface IChatFragment {

    /**语音播放*/
    fun playAudio(view: View, data: MChatMessageAudio)

    /**发送消息
     * @param msg 消息数据
     * @param new 是否新消息（新消息需要上屏）
     * */
    fun sendMessage(msg: IMessageService.Message, new: Boolean = true)

    /**更新消息发送状态*/
    fun notifyMessageSendStatus(message: IMessageService.Message)

    /**更新新消息*/
    fun notifyNewMessage(message: IMessageService.Message)

    /**更新消息状态*/
    fun notifyFileMessage(message: IMessageService.Message)

    /**删除（撤销）消息*/
    fun deleteMessage(message: IMessageService.Message)

    /**发送撤回消息事件到聊天列表*/
    fun sendMessageRecallEvent(message: IMessageService.Message)

    /**接收聊天消息发送事件*/
    fun receiveSendMessageEvent(message: IMessageService.Message)
}