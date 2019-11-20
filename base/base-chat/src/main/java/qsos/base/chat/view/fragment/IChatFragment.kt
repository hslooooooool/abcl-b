package qsos.base.chat.view.fragment

import android.view.View
import qsos.base.chat.data.entity.EnumChatMessageType
import qsos.base.chat.data.entity.MChatMessageAudio
import qsos.base.chat.service.IMessageService
import qsos.lib.netservice.file.HttpFileEntity

/**
 * @author : 华清松
 * 聊天消息列表页功能接口
 */
interface IChatFragment {

    /**发送文本消息*/
    fun sendTextMessage()

    /**发送文件消息*/
    fun sendFileMessage(type: EnumChatMessageType, files: ArrayList<HttpFileEntity>)

    /**更新消息发送状态*/
    fun notifySendMessage(result: IMessageService.Message)

    /**语音播放*/
    fun playAudio(view: View, data: MChatMessageAudio)
}