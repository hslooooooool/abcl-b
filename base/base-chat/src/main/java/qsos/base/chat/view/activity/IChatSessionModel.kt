package qsos.base.chat.view.activity

import android.content.Context
import android.view.View
import androidx.fragment.app.FragmentManager
import qsos.base.chat.data.entity.EnumChatMessageType
import qsos.base.chat.service.IMessageService
import qsos.lib.netservice.file.HttpFileEntity

/**
 * @author : 华清松
 * 聊天会话功能接口
 */
interface IChatSessionModel {

    /**点击文本消息*/
    fun clickTextMessage(view: View, message: IMessageService.Message, back: (action: Int) -> Unit)

    /**长按文本消息*/
    fun longClickTextMessage(view: View, message: IMessageService.Message, back: (action: Int) -> Unit)

    /**消息重发*/
    fun resendMessage(message: IMessageService.Message, back: (file: HttpFileEntity?) -> Unit)

    /**获取并发送文件消息*/
    fun sendFileMessage(
            context: Context, fm: FragmentManager, fileType: Int,
            back: (type: EnumChatMessageType, files: ArrayList<HttpFileEntity>) -> Unit
    )
}