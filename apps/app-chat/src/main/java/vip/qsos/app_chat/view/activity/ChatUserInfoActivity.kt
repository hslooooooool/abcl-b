package vip.qsos.app_chat.view.activity

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_chat_user.*
import qsos.base.core.config.BaseConfig
import qsos.core.lib.utils.image.ImageLoaderUtils
import qsos.lib.base.base.activity.BaseActivity
import qsos.lib.base.utils.ToastUtils
import vip.qsos.app_chat.R
import vip.qsos.app_chat.data.entity.ChatFriendBo
import vip.qsos.app_chat.data.model.UserInfoViewModelImpl

/**
 * @author : 华清松
 * 用户资料页
 */
@Route(group = "CHAT", path = "/CHAT/USER")
class ChatUserInfoActivity(
        override val layoutId: Int = R.layout.activity_chat_user,
        override val reload: Boolean = false
) : BaseActivity() {

    @Autowired(name = "/CHAT/USER_ID")
    @JvmField
    var mUserId: Long? = -1L

    private val mUserInfoViewModel: UserInfoViewModelImpl by viewModels()

    override fun initData(savedInstanceState: Bundle?) {}

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
            if (mUserInfoViewModel.mChatFriend.value?.accept == true) {
                ARouter.getInstance().build("/CHAT/SESSION")
                        .withString("/CHAT/SESSION_JSON", Gson().toJson(mUserInfoViewModel.mChatSession.value))
                        .navigation()
            } else {
                addFriend()
            }
        }

        mUserInfoViewModel.mChatUser.observe(this, Observer {
            getData()
        })

        mUserInfoViewModel.getUserById(
                userId = mUserId!!,
                failed = {
                    ToastUtils.showToast(this, it)
                },
                success = {
                    ImageLoaderUtils.display(this, chat_user_avatar, it.avatar)
                    chat_user_desc.text = Gson().toJson(it)
                    mUserInfoViewModel.mChatUser.value = it
                }
        )
    }

    override fun getData() {
        if (mUserInfoViewModel.mChatUser.value != null) {
            findFriend()
        }
    }

    /**获取会话数据,发起聊天*/
    private fun findSession() {
        mUserInfoViewModel.getSessionOfSingle(
                sender = BaseConfig.getLoginUserAccount(),
                receiver = mUserInfoViewModel.mChatUser.value!!.imAccount,
                failed = {
                    ToastUtils.showToast(this, it)
                },
                success = {
                    mUserInfoViewModel.mChatSession.value = it
                    chat_user_send.isEnabled = true
                }
        )
    }

    /**检查好友关系*/
    private fun findFriend() {
        mUserInfoViewModel.findFriend(
                BaseConfig.getLoginUserId(),
                mUserInfoViewModel.mChatUser.value!!.id,
                failed = {
                    ToastUtils.showToast(this, it)
                },
                success = {
                    changeSendButton(it)
                }
        )
    }

    /**添加好友操作*/
    private fun addFriend() {
        mUserInfoViewModel.addFriend(
                BaseConfig.getLoginUserId(),
                mUserInfoViewModel.mChatUser.value!!.id,
                failed = {
                    ToastUtils.showToast(this, it)
                },
                success = {
                    changeSendButton(it)
                }
        )
    }

    /**修改底部按钮*/
    private fun changeSendButton(friend: ChatFriendBo?) {
        chat_user_send.visibility = View.VISIBLE
        mUserInfoViewModel.mChatFriend.value = friend
        when {
            friend == null -> {
                chat_user_send.text = "加为好友"
                chat_user_send.isEnabled = true
            }
            friend.accept == true -> {
                chat_user_send.text = "发消息"
                findSession()
            }
            friend.accept == false -> {
                chat_user_send.text = "重新申请"
                chat_user_send.isEnabled = true
            }
            else -> {
                chat_user_send.text = "待接受"
                chat_user_send.isEnabled = false
            }
        }
    }

    override fun onDestroy() {
        mUserInfoViewModel.clear()
        super.onDestroy()
    }
}