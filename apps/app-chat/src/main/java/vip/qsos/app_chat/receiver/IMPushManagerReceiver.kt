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
import com.google.gson.Gson
import qsos.base.chat.api.MessageViewHelper
import qsos.base.chat.data.entity.ChatMessage
import qsos.lib.base.utils.rx.RxBus
import vip.qsos.app_chat.R
import vip.qsos.app_chat.config.Constants
import vip.qsos.app_chat.data.entity.ChatMessageBo
import vip.qsos.app_chat.data.entity.ChatSessionBo
import vip.qsos.app_chat.data.entity.MessageExtra
import vip.qsos.app_chat.data.entity.Session
import vip.qsos.app_chat.view.activity.ChatSessionActivity
import vip.qsos.im.lib.AbsIMEventBroadcastReceiver
import vip.qsos.im.lib.IMListenerManager
import vip.qsos.im.lib.model.Message
import vip.qsos.im.lib.model.ReplyBody

/**
 * @author : 华清松
 * 消息接收广播服务
 */
class IMPushManagerReceiver : AbsIMEventBroadcastReceiver() {

    override fun onMessageReceived(message: Message, intent: Intent) {
        IMListenerManager.notifyOnMessageReceived(message)
        /**强行下线消息*/
        if (Constants.MessageAction.ACTION_999 == message.action) {
            ARouter.getInstance().build("/CHAT/LOGIN").navigation()
            return
        }
        val msg = ChatMessageBo.decode(message)
        val extra = MessageExtra.json(message.extra!!)

        showNotify(context, message, msg, extra)

        notifyView(msg, extra)
    }

    /**消息广播*/
    @SuppressLint("TimberArgCount")
    private fun showNotify(context: Context, message: Message, msg: ChatMessage, extra: MessageExtra) {
        val action = message.action ?: "0"
        val title = message.title ?: "新消息"
        val content = ChatMessage.getRealContentDesc(msg.content)
        val time = message.timestamp
        val timeline = msg.timeline
        val avatar = extra.sender?.avatar
        val sessionId = extra.sessionId
        val sessionType = extra.sessionType

        val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(action, action, NotificationManager.IMPORTANCE_DEFAULT)
            channel.enableLights(true)
            notificationManager.createNotificationChannel(channel)
        }
        val sessionIntent = Intent(context, ChatSessionActivity::class.java)
        val mSessionBo = ChatSessionBo(
                id = sessionId,
                type = sessionType,
                title = title,
                timeline = timeline,
                avatar = avatar,
                content = content
        )
        sessionIntent.putExtra("/CHAT/SESSION_JSON", Gson().toJson(mSessionBo))
        val contentIntent = PendingIntent.getActivity(
                context, 1, sessionIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        )
        val builder = NotificationCompat.Builder(context, action)
        builder.setAutoCancel(true)
        builder.setDefaults(Notification.DEFAULT_ALL)
        builder.setWhen(time)
        builder.setSmallIcon(R.drawable.ic_launcher)
        builder.setTicker(title)
        builder.setContentTitle(title)
        builder.setContentText(content)
        builder.setDefaults(Notification.DEFAULT_LIGHTS)
        builder.setContentIntent(contentIntent)
        notificationManager.notify(R.drawable.ic_launcher, builder.build())
    }

    /**更新消息界面*/
    private fun notifyView(msg: ChatMessage, extra: MessageExtra) {
        RxBus.send(MessageViewHelper.MessageEvent(
                session = Session(
                        id = msg.sessionId,
                        type = extra.sessionType.key
                ),
                message = arrayListOf(msg),
                eventType = MessageViewHelper.EventType.SHOW_NEW)
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
