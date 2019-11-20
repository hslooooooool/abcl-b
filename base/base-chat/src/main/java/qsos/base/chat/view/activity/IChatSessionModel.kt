package qsos.base.chat.view.activity

import qsos.base.chat.data.entity.ChatContent
import qsos.base.chat.data.entity.EnumChatMessageType
import qsos.lib.netservice.file.HttpFileEntity

/**
 * @author : 华清松
 * 消息会话操作
 */
interface IChatSessionModel {

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
    fun sendMessage(content: ChatContent)
}