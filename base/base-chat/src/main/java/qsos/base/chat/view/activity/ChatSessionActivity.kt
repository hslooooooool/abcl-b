package qsos.base.chat.view.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import qsos.base.chat.R
import qsos.base.chat.data.model.DefChatSessionModelIml
import qsos.base.chat.data.model.IChatModel
import qsos.base.chat.view.fragment.ChatFragment
import qsos.lib.base.base.activity.BaseActivity
import qsos.lib.base.utils.ToastUtils

/**
 * @author : 华清松
 * 聊天会话页面
 */
@Route(group = "CHAT", path = "/CHAT/SESSION")
class ChatSessionActivity(
        override val layoutId: Int = R.layout.activity_chat_message,
        override val reload: Boolean = false
) : BaseActivity() {

    @Autowired(name = "/CHAT/SESSION_ID")
    @JvmField
    var mSessionId: Int? = -1

    private var mChatSessionModel: IChatModel.ISession? = null

    override fun initData(savedInstanceState: Bundle?) {
        mChatSessionModel = DefChatSessionModelIml()
    }

    override fun initView() {
        if (mSessionId == null || mSessionId!! < 0) {
            ToastUtils.showToastLong(this, "聊天不存在")
            finish()
            return
        }

        mChatSessionModel?.getSessionById(
                sessionId = mSessionId!!,
                failed = {
                    ToastUtils.showToast(this, it)
                },
                success = {
                    supportFragmentManager.beginTransaction().add(
                            R.id.chat_message_frg,
                            ChatFragment(it),
                            "ChatFragment"
                    ).commit()
                }
        )
    }

    override fun getData() {

    }

    override fun onDestroy() {
        mChatSessionModel?.clear()
        super.onDestroy()
    }
}