package qsos.base.chat.view.fragment

import android.view.View
import qsos.base.chat.data.entity.MChatMessageAudio
import qsos.base.chat.service.IMessageService
import qsos.lib.netservice.file.HttpFileEntity

/**
 * @author : 华清松
 * 聊天消息列表页功能接口
 */
interface IChatFragment {

    /**发送消息
     * @param msg 消息数据
     * @param new 是否新消息（新消息需要上屏）
     * */
    fun sendMessage(msg: IMessageService.Message, new: Boolean = true)

    /**更新消息发送状态*/
    fun notifySendMessage(msg: IMessageService.Message)

    /**语音播放*/
    fun playAudio(view: View, data: MChatMessageAudio)

    /**更新文件消息状态*/
    fun updateFileMessage(file: HttpFileEntity)

    /**删除（撤销）消息*/
    fun deleteMessage(message: IMessageService.Message)

    /**新消息页面更新*/
    fun notifyNewMessage(message: IMessageService.Message)

    /**响应发送往本页面的事件*/
    fun notifyFragmentEvent(event: ChatMessageListFragment.ChatMessageListFragmentEvent)
}