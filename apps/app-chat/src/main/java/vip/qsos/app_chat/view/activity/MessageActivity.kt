package vip.qsos.app_chat.view.activity

import android.net.NetworkInfo
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_message.*
import vip.qsos.app_chat.R
import vip.qsos.app_chat.data.Constants
import vip.qsos.app_chat.view.adapter.MessageAdapter
import vip.qsos.im.lib.constant.IMConstant
import vip.qsos.im.lib.model.Message
import vip.qsos.im.lib.model.ReplyBody
import java.util.*

/**
 * @author : 华清松
 * 消息界面
 */
class MessageActivity : AbsIMActivity() {
    override val layoutId: Int = R.layout.activity_message
    override val reload: Boolean = false

    private lateinit var mAdapter: MessageAdapter
    private var mList: ArrayList<Message> = arrayListOf()

    override fun getData() {
    }

    override fun initData(savedInstanceState: Bundle?) {
    }

    override fun initView() {
        mList = ArrayList()
        mAdapter = MessageAdapter(this, mList)
        chat_list.adapter = mAdapter
    }

    override fun onReplyReceived(replyBody: ReplyBody) {
        if (replyBody.key == IMConstant.RequestKey.CLIENT_BIND && replyBody.code == IMConstant.ReturnCode.CODE_200) {
            Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMessageReceived(message: Message) {
        if (message.action == Constants.MessageAction.ACTION_999) {
            Toast.makeText(this, "你被系统强制下线!", Toast.LENGTH_LONG).show()
            this.finish()
        } else {
            mList.add(message)
            mAdapter.notifyDataSetChanged()
        }
    }

    override fun onNetworkChanged(networkInfo: NetworkInfo?) {
        if (networkInfo == null) {
            Toast.makeText(this, "网络已断开!", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "网络已恢复，重新连接....", Toast.LENGTH_LONG).show()
        }
    }
}
