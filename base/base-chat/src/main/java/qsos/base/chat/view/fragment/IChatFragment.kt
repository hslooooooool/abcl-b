package qsos.base.chat.view.fragment

import qsos.base.chat.data.entity.MChatMessage
import qsos.lib.netservice.file.HttpFileEntity

/**
 * @author : 华清松
 * 聊天消息列表页功能接口
 */
interface IChatFragment {

    /**发送文本消息*/
    fun sendTextMessage()

    /**发送图片消息*/
    fun sendImageMessage(images: List<HttpFileEntity>)

    /**更新消息发送状态*/
    fun notifySendMessage(result: MChatMessage)

    /**获取图片*/
    fun takeImage()
}