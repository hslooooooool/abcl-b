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
import vip.qsos.app_chat.data.entity.ChatFriend
import vip.qsos.app_chat.data.entity.ChatGroup
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
        override val reload: Boolean = true
) : BaseActivity() {

    @Autowired(name = "/CHAT/USER_ID")
    @JvmField
    var mUserId: Long? = -1L

    private val mChatUser: MutableLiveData<ChatUser> = MutableLiveData()
    private val mChatGroup: MutableLiveData<ChatGroup> = MutableLiveData()
    private val mChatFriend: MutableLiveData<ChatFriend> = MutableLiveData()

    private lateinit var mChatSessionModel: ChatModel.ISession
    private lateinit var mChatUserModel: ChatModel.IUser

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
            if (mChatGroup.value == null) {
                addFriend()
            } else if (this.mChatFriend.value?.accept == true) {
                ARouter.getInstance().build("/CHAT/SESSION")
                        .withInt("/CHAT/SESSION_ID", mChatGroup.value!!.id)
                        .navigation()
            }
        }
        mChatUserModel.getUserById(
                userId = mUserId!!,
                failed = {
                    ToastUtils.showToast(this, it)
                },
                success = {
                    ImageLoaderUtils.display(this, chat_user_avatar, it.avatar)
                    chat_user_desc.text = Gson().toJson(it)
                    this.mChatUser.value = it
                }
        )
    }

    override fun getData() {
        if (this.mChatUser.value != null) {
            mChatSessionModel.findSingle(
                    creator = ChatApplication.loginUser.value!!.imAccount,
                    member = mChatUser.value!!.imAccount,
                    failed = {
                        chat_user_send.text = "加为好友"
                    },
                    success = { group ->
                        mChatGroup.value = group
                        findFriend()
                    }
            )
        } else {
            chat_user_send.visibility = View.VISIBLE
        }
    }

    private fun findFriend() {
        mChatUserModel.findFriend(
                ChatApplication.loginUser.value!!.userId,
                this.mChatUser.value!!.userId,
                failed = {
                    ToastUtils.showToast(this, it)
                },
                success = {
                    changeSendButton(it)
                }
        )
    }

    private fun addFriend() {
        mChatUserModel.addFriend(
                ChatApplication.loginUser.value!!.userId,
                this.mChatUser.value!!.userId,
                failed = {
                    ToastUtils.showToast(this, it)
                },
                success = {
                    changeSendButton(it)
                }
        )
    }

    private fun changeSendButton(friend: ChatFriend) {
        chat_user_send.visibility = View.VISIBLE
        this.mChatFriend.value = friend
        when {
            friend.accept == true -> {
                chat_user_send.text = "发消息"
                chat_user_send.isEnabled = true
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
        mChatUserModel.clear()
        mChatSessionModel.clear()
        super.onDestroy()
    }
}