package vip.qsos.app_chat.view.activity

import qsos.base.chat.api.MessageViewHelper
import qsos.base.chat.data.entity.ChatContent
import qsos.base.chat.data.entity.EnumChatMessageType
import qsos.lib.netservice.file.HttpFileEntity

/**
 * @author : 华清松
 * 消息会话操作
 */
interface ChatSessionView {
    /**回到主页*/
    fun goToHome()

    /**检测是否有正在发送的消息，友情提示，防止退出后消息未发送*/
    fun checkHaveSending(): Boolean

    /**清除输入框焦点并关闭键盘*/
    fun clearEditFocus()

    /**根据输入字数，修改发送按钮样式*/
    fun changeSendStyle(inputNum: Int = 0)

    /**获取文件
     * @param fileType 0 拍照 1 相册 2 视频 3 语音 4 文件
     * */
    fun takeFile(fileType: Int)

    /**文件上传，采用递归，依次上传
     * @param files 总计需要上传的文件
     * */
    fun uploadFile(files: ArrayList<HttpFileEntity>)

    /**删除（撤销）消息*/
    fun deleteMessage(message: MessageViewHelper.Message)

    /**发送文件消息*/
    fun sendFileMessage(type: EnumChatMessageType, files: ArrayList<HttpFileEntity>)

    /**发送消息*/
    fun sendMessage(content: ChatContent, send: Boolean, bottom: Boolean): MessageViewHelper.Message

    /**轮序拉取新消息*/
    fun pullNewMessage(session: MessageViewHelper.Session)
}