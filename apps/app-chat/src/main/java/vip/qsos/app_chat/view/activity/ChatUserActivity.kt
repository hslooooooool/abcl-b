package vip.qsos.app_chat.view.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.lifecycle.MutableLiveData
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_chat_user.*
import qsos.core.lib.utils.image.ImageLoaderUtils
import qsos.lib.base.base.activity.BaseActivity
import qsos.lib.base.utils.ToastUtils
import vip.qsos.app_chat.ChatApplication
import vip.qsos.app_chat.R
import vip.qsos.app_chat.data.entity.ChatUser
import vip.qsos.app_chat.data.model.ChatModel
import vip.qsos.app_chat.data.model.ChatSessionModelIml
import vip.qsos.app_chat.data.model.ChatUserModelIml

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
    var mUserId: Long? = -1L

    private val mUser: MutableLiveData<ChatUser> = MutableLiveData()

    private var mChatSessionModel: ChatModel.ISession? = null
    private var mChatUserModel: ChatModel.IUser? = null

    override fun initData(savedInstanceState: Bundle?) {
        mChatSessionModel = ChatSessionModelIml()
        mChatUserModel = ChatUserModelIml()
    }

    @SuppressLint("SetTextI18n")
    override fun initView() {
        if (mUserId == null || mUserId!! < 0) {
            ToastUtils.showToastLong(this, "用户不存在")
            finish()
            return
        }

        base_title_bar.findViewById<View>(R.id.base_title_bar_icon_left)?.setOnClickListener {
            finish()
        }

        chat_user_send.setOnClickListener {
            mChatSessionModel?.createSession(
                    accountList = arrayListOf(
                            ChatApplication.loginUser.value!!.imAccount,
                            mUser.value!!.imAccount
                    ),
                    failed = {
                        ToastUtils.showToast(this, it)
                    },
                    success = {
                        ARouter.getInstance().build("/CHAT/SESSION")
                                .withInt("/CHAT/SESSION_ID", it.id)
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
                    chat_user_desc.text = Gson().toJson(it)
                    this.mUser.value = it
                }
        )
    }

    override fun getData() {}

    override fun onDestroy() {
        mChatUserModel?.clear()
        mChatSessionModel?.clear()
        super.onDestroy()
    }
}