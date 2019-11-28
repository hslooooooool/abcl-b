package qsos.base.chat.view.activity

import qsos.base.chat.data.entity.ChatContent
import qsos.base.chat.data.entity.EnumChatMessageType
import qsos.base.chat.service.IMessageService
import qsos.lib.netservice.file.HttpFileEntity

/**
 * @author : 华清松
 * 消息会话操作
 */
interface IChatSessionView {

    /**获取语音*/
    fun takeAudio()

    /**获取拍照*/
    fun takePhoto()

    /**获取相册*/
    fun takeAlbum()

    /**获取视频*/
    fun takeVideo()

    /**获取文件*/
    fun takeFile()

    /**文件上传，采用递归，依次上传
     * @param files 总计需要上传的文件
     * */
    fun uploadFile(files: ArrayList<HttpFileEntity>)

    /**发送文件消息*/
    fun sendFileMessage(type: EnumChatMessageType, files: ArrayList<HttpFileEntity>)

    /**发送消息*/
    fun sendMessage(content: ChatContent, send: Boolean, bottom: Boolean): IMessageService.Message

    /**发送文件消息更新事件到聊天列表*/
    fun sendFileMessageUpdate(message: IMessageService.Message)

    /**轮序拉取新消息*/
    fun pullNewMessage(session: IMessageService.Session)
}