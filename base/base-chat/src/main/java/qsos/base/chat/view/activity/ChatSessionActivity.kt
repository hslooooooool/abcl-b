package qsos.base.chat.view.activity

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import kotlinx.coroutines.cancel
import qsos.base.chat.R
import qsos.base.chat.data.entity.ChatSession
import qsos.base.chat.data.model.DefChatModelIml
import qsos.base.chat.data.model.IChatModelConfig
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

    @Autowired(name = "/CHAT/SESSION")
    var mSessionId: Int? = null

    private var mChatMessageModel: IChatModelConfig? = null
    private var mSessionLiveData = MutableLiveData<ChatSession>()

    override fun initData(savedInstanceState: Bundle?) {
        mChatMessageModel = DefChatModelIml()
    }

    override fun initView() {
        if (mSessionId == null) {
            ToastUtils.showToastLong(this, "聊天不存在")
            finish()
            return
        }

        mChatMessageModel?.mDataOfChatSession?.observe(this, Observer {
            it.data?.let { session ->
                supportFragmentManager.beginTransaction().add(
                        R.id.chat_message_frg,
                        ChatFragment(session),
                        "ChatFragment"
                ).commit()
            }
            ToastUtils.showToast(this, it.msg ?: "未知错误")
        })

        getData()
    }

    override fun getData() {
        mChatMessageModel?.getSessionById(mSessionId!!)
    }

    override fun onDestroy() {
        mChatMessageModel?.mJob?.cancel()
        super.onDestroy()
    }
}