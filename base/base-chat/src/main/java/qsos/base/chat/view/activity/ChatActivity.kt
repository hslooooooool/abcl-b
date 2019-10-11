package qsos.base.chat.view.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import qsos.base.chat.R
import qsos.base.chat.view.fragment.ChatFragment
import qsos.lib.base.base.activity.BaseActivity

/**
 * @author : 华清松
 * 聊天页面
 */
@Route(group = "CHAT", path = "/CHAT/MAIN")
class ChatActivity(
        override val layoutId: Int = R.layout.activity_chat_message,
        override val reload: Boolean = false
) : BaseActivity() {

    override fun getData() {}

    override fun initData(savedInstanceState: Bundle?) {}

    override fun initView() {
        supportFragmentManager.beginTransaction().add(R.id.chat_message_frg, ChatFragment(),"1").commit()
    }
}