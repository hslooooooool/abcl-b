package vip.qsos.app_chat.view.activity

import android.net.NetworkInfo
import android.os.Bundle
import android.widget.Toast
import qsos.lib.base.base.activity.BaseActivity

import vip.qsos.im.lib.IMEventListener
import vip.qsos.im.lib.IMListenerManager
import vip.qsos.im.lib.model.Message
import vip.qsos.im.lib.model.ReplyBody
import vip.qsos.im.lib.model.SendBody

/**
 * @author : 华清松
 * 消息服务活动基类
 */
abstract class AbsIMActivity : BaseActivity(), IMEventListener {

    override val eventDispatchOrder: Int = 0

    public override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        IMListenerManager.registerMessageListener(this)
    }

    override fun finish() {
        IMListenerManager.removeMessageListener(this)
        super.finish()
    }

    override fun onRestart() {
        super.onRestart()
        IMListenerManager.registerMessageListener(this)
    }

    override fun onMessageReceived(message: Message) {
        Toast.makeText(this, "收到消息", Toast.LENGTH_SHORT).show()
    }

    override fun onNetworkChanged(networkInfo: NetworkInfo?) {
        Toast.makeText(this, "网络变化", Toast.LENGTH_SHORT).show()
    }

    override fun onConnectionClosed() {
        Toast.makeText(this, "连接关闭", Toast.LENGTH_SHORT).show()
    }

    override fun onConnectionFailed() {
        Toast.makeText(this, "连接失败", Toast.LENGTH_SHORT).show()
    }

    override fun onConnectionSuccess(hasAutoBind: Boolean) {
        Toast.makeText(this, "连接成功", Toast.LENGTH_SHORT).show()
    }

    override fun onReplyReceived(replyBody: ReplyBody) {
        Toast.makeText(this, "收到回执", Toast.LENGTH_SHORT).show()
    }

    override fun onSentSuccess(sendBody: SendBody) {
        Toast.makeText(this, "发送成功", Toast.LENGTH_SHORT).show()
    }
}
