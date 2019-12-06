package qsos.base.chat.view.activity

import android.view.View
import qsos.base.chat.data.entity.ChatContent
import qsos.base.chat.data.entity.EnumChatMessageType
import qsos.base.chat.data.entity.MChatMessageAudio
import qsos.base.chat.service.IMessageService
import qsos.lib.netservice.file.HttpFileEntity

/**
 * @author : 华清松
 * 消息会话操作
 */
interface IChatSessionView {

    /**获取文件
     * @param fileType 0 拍照 1 相册 2 视频 3 语音 4 文件
     * */
    fun takeFile(fileType: Int)

    /**语音播放*/
    fun playAudio(view: View, data: MChatMessageAudio)

    /**停止所有语音播放*/
    fun stopAudioPlay()

    /**文件上传，采用递归，依次上传
     * @param files 总计需要上传的文件
     * */
    fun uploadFile(files: ArrayList<HttpFileEntity>)

    /**删除（撤销）消息*/
    fun deleteMessage(message: IMessageService.Message)

    /**发送文件消息*/
    fun sendFileMessage(type: EnumChatMessageType, files: ArrayList<HttpFileEntity>)

    /**发送消息*/
    fun sendMessage(content: ChatContent, send: Boolean, bottom: Boolean): IMessageService.Message

    /**轮序拉取新消息*/
    fun pullNewMessage(session: IMessageService.Session)
}