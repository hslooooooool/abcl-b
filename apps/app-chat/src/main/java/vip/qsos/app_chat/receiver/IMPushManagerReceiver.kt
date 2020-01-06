package vip.qsos.app_chat.receiver

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.alibaba.android.arouter.launcher.ARouter
import qsos.base.chat.api.IMessageListService
import qsos.lib.base.utils.DateUtils
import qsos.lib.base.utils.rx.RxBus
import vip.qsos.app_chat.R
import vip.qsos.app_chat.data.entity.ChatMessage
import vip.qsos.app_chat.data.entity.ChatMessageBo
import vip.qsos.app_chat.data.entity.MessageBo
import vip.qsos.app_chat.data.model.ChatModel
import vip.qsos.app_chat.view.activity.MessageActivity
import vip.qsos.im.lib.AbsIMEventBroadcastReceiver
import vip.qsos.im.lib.IMListenerManager
import vip.qsos.im.lib.constant.IMConstant
import vip.qsos.im.lib.model.Message
import vip.qsos.im.lib.model.ReplyBody
import java.util.*

/**
 * @author : 华清松
 * 消息接收广播服务
 */
class IMPushManagerReceiver : AbsIMEventBroadcastReceiver() {

    data class Group(override var id: Long, override var name: String, override var type: Int) : IMessageListService.Group

    override fun onMessageReceived(message: Message, intent: Intent) {
        IMListenerManager.notifyOnMessageReceived(message)
        /**以9开头的消息无须广播,如被强行下线消息
         * @sample IMConstant.MessageAction.ACTION_999
         * */
        if (message.action?.startsWith("9") == true) {
            ARouter.getInstance().build("/CHAT/LOGIN").navigation()
            return
        }
        val messageBo = MessageBo.decode(message)
        showNotify(context, messageBo)
        notifyView(messageBo)
    }

    /**消息广播*/
    @SuppressLint("TimberArgCount")
    private fun showNotify(context: Context, msg: MessageBo) {
        val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        var channelId = "normal"
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channelId = "system"
            val channel = NotificationChannel(
                    channelId, "message",
                    NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.enableLights(true)
            notificationManager.createNotificationChannel(channel)
        }
        val title = msg.title ?: "新消息"
        val contentIntent = PendingIntent.getActivity(
                context, 1, Intent(context, MessageActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT
        )
        val builder = NotificationCompat.Builder(context, channelId)
        builder.setAutoCancel(true)
        builder.setDefaults(Notification.DEFAULT_ALL)
        builder.setWhen(msg.timestamp)
        builder.setSmallIcon(R.drawable.ic_launcher)
        builder.setTicker(title)
        builder.setContentTitle(title)
        builder.setContentText(msg.content.getContentDesc())
        builder.setDefaults(Notification.DEFAULT_LIGHTS)
        builder.setContentIntent(contentIntent)
        val notification = builder.build()
        notificationManager.notify(R.drawable.ic_launcher, notification)
    }

    /**TODO 更新消息界面*/
    private fun notifyView(msg: MessageBo) {
        RxBus.send(IMessageListService.MessageEvent(
                group = Group(
                        id = msg.extra.belongId.toLong(),
                        name = "TEST",
                        type = msg.extra.chatType.key
                ),
                message = arrayListOf(formatMessage(msg)),
                eventType = IMessageListService.EventType.SHOW_NEW)
        )
    }

    private fun formatMessage(msg: MessageBo): IMessageListService.Message {
        return ChatMessageBo(
                user = ChatModel.mLoginUser.value!!,
                createTime = DateUtils.format(date = Date()),
                message = ChatMessage(
                        groupId = msg.extra.belongId.toLong(),
                        messageId = msg.id.toInt(),
                        content = msg.content
                )
        )
    }

    override fun onConnectionSuccess(hasAutoBind: Boolean) {
        IMListenerManager.notifyOnConnectionSuccess(hasAutoBind)
    }

    override fun onConnectionClosed() {
        IMListenerManager.notifyOnConnectionClosed()
    }

    override fun onReplyReceived(body: ReplyBody) {
        IMListenerManager.notifyOnReplyReceived(body)
    }

    override fun onConnectionFailed() {
        IMListenerManager.notifyOnConnectionFailed()
    }

}
