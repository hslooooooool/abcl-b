package qsos.base.chat.view.fragment

import android.view.View
import qsos.base.chat.data.entity.MChatMessage
import qsos.base.chat.data.entity.MChatMessageAudio
import qsos.base.chat.data.entity.MChatMessageType
import qsos.lib.netservice.file.HttpFileEntity

/**
 * @author : 华清松
 * 聊天消息列表页功能接口
 */
interface IChatFragment {

    /**发送文本消息*/
    fun sendTextMessage()

    /**发送文件消息*/
    fun sendFileMessage(type: MChatMessageType, files: ArrayList<HttpFileEntity>)

    /**更新消息发送状态*/
    fun notifySendMessage(result: MChatMessage)

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

    /**语音播放*/
    fun playAudio(view: View, data: MChatMessageAudio)
}