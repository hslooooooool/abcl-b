package qsos.base.chat.view.activity

import android.annotation.SuppressLint
import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import kotlinx.android.synthetic.main.activity_chat_user.*
import qsos.base.chat.R
import qsos.base.chat.data.model.DefChatSessionModelIml
import qsos.base.chat.data.model.DefChatUserModelIml
import qsos.base.chat.data.model.IChatModel
import qsos.base.core.config.BaseConfig
import qsos.core.lib.utils.image.ImageLoaderUtils
import qsos.lib.base.base.activity.BaseActivity
import qsos.lib.base.utils.ToastUtils

/**
 * @author : 华清松
 * 聊天用户详情页面
 */
@Route(group = "CHAT", path = "/CHAT/USER")
class ChatUserActivity(
        override val layoutId: Int = R.layout.activity_chat_user,
        override val reload: Boolean = false
) : BaseActivity() {

    @Autowired(name = "/CHAT/USER_ID")
    @JvmField
    var mUserId: Int? = -1

    private var mChatSessionModel: IChatModel.ISession? = null
    private var mChatUserModel: IChatModel.IUser? = null

    override fun initData(savedInstanceState: Bundle?) {
        mChatSessionModel = DefChatSessionModelIml()
        mChatUserModel = DefChatUserModelIml()
    }

    @SuppressLint("SetTextI18n")
    override fun initView() {
        if (mUserId == null || mUserId!! < 0) {
            ToastUtils.showToastLong(this, "用户不存在")
            finish()
            return
        }

        chat_user_send.setOnClickListener {
            mChatSessionModel?.createSession(
                    userIdList = arrayListOf(BaseConfig.userId, mUserId!!),
                    failed = {
                        ToastUtils.showToast(this, it)
                    },
                    success = {
                        ARouter.getInstance().build("/CHAT/SESSION")
                                .withInt("/CHAT/SESSION_ID", it.sessionId)
                                .navigation()
                    }
            )
        }

        mChatUserModel?.getUserById(
                userId = mUserId!!,
                failed = {
                    ToastUtils.showToast(this, it)
                },
                success = {
                    ImageLoaderUtils.display(this, chat_user_avatar, it.avatar)
                    chat_user_desc.text = it.userName + "\n" + it.birth
                }
        )
    }

    override fun getData() {

    }

    override fun onDestroy() {
        mChatUserModel?.clear()
        mChatSessionModel?.clear()
        super.onDestroy()
    }
}