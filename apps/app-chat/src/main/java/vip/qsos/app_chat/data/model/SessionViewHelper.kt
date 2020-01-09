package vip.qsos.app_chat.data.model

import android.content.Context
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import qsos.base.chat.api.MessageViewHelper
import qsos.base.chat.data.entity.EnumChatMessageType
import qsos.base.chat.data.entity.MChatMessageAudio
import qsos.lib.netservice.file.HttpFileEntity

/**
 * @author : 华清松
 * 会话页操作帮助接口
 */
interface SessionViewHelper : LifecycleObserver {

    /**点击文本消息*/
    fun clickTextMessage(view: View, message: MessageViewHelper.Message, back: (action: Int) -> Unit)

    /**长按文本消息*/
    fun longClickTextMessage(view: View, message: MessageViewHelper.Message, back: (action: Int) -> Unit)

    /**消息重发*/
    fun resendMessage(message: MessageViewHelper.Message, back: (file: HttpFileEntity?) -> Unit)

    /**语音播放*/
    fun playAudio(view: View, stateView: ImageView, data: MChatMessageAudio)

    /**停止所有语音播放*/
    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun stopAudioPlay()

    /**获取并发送文件消息*/
    fun sendFileMessage(
            context: Context, fm: FragmentManager, fileType: Int,
            back: (type: EnumChatMessageType, files: ArrayList<HttpFileEntity>) -> Unit
    )
}