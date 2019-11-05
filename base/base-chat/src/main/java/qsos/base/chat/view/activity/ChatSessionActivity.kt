package qsos.base.chat.view.activity

import android.Manifest
import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.tbruyelle.rxpermissions2.RxPermissions
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

        RxPermissions(this).request(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO
        ).subscribe({
            if (it) {
                mChatSessionModel?.getSessionById(
                        sessionId = mSessionId!!,
                        failed = {
                            ToastUtils.showToast(this, it)
                        },
                        success = {
                            supportFragmentManager.beginTransaction()
                                    .add(R.id.chat_message_frg, ChatFragment(it), "ChatFragment")
                                    .commit()
                        }
                )
            } else {
                ToastUtils.showToastLong(mContext, "权限开启失败，无法使用此功能")
            }
        }, {
            it.printStackTrace()
        }).takeUnless {
            this.isFinishing
        }

    }

    override fun getData() {}

    override fun onDestroy() {
        mChatSessionModel?.clear()
        super.onDestroy()
    }

    override fun onBackPressed() {
        ARouter.getInstance().build("/CHAT/MAIN")
                .withTransition(R.anim.activity_out_center, R.anim.activity_in_center)
                .navigation()
        super.onBackPressed()
    }
}