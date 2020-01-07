package vip.qsos.app_chat.view.activity

import android.os.Bundle
import android.widget.Toast
import androidx.navigation.fragment.NavHostFragment
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import qsos.lib.base.utils.ActivityManager
import qsos.lib.base.utils.rx.RxBus
import vip.qsos.app_chat.ChatApplication
import vip.qsos.app_chat.R
import vip.qsos.app_chat.data.Constants
import vip.qsos.app_chat.data.event.LoginSuccessEvent
import vip.qsos.im.lib.IMManagerHelper
import vip.qsos.im.lib.constant.IMConstant
import vip.qsos.im.lib.model.ReplyBody

/**
 * @author : 华清松
 * 登录页面
 */
@Route(group = "APP", path = "/CHAT/LOGIN")
class LoginActivity(
        override val layoutId: Int = R.layout.activity_login,
        override val reload: Boolean = false
) : AbsIMActivity() {

    private lateinit var mLoginDisposable: Disposable

    override fun getData() {}

    override fun initData(savedInstanceState: Bundle?) {
        ChatApplication.loginUser.value = null
    }

    override fun initView() {
        ActivityManager.finishAllButNotMe(this)

        mLoginDisposable = RxBus.toFlow(LoginSuccessEvent::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    bindAccount()
                }
    }

    override fun onSupportNavigateUp(): Boolean {
        val fragment = supportFragmentManager.findFragmentById(R.id.login_fragment)!!
        return NavHostFragment.findNavController(fragment).navigateUp()
    }


    override fun onConnectionSuccess(hasAutoBind: Boolean) {
        if (!hasAutoBind && ChatApplication.loginUser.value != null) {
            IMManagerHelper.bindAccount(this, ChatApplication.loginUser.value!!.imAccount)
        }
    }

    override fun onReplyReceived(replyBody: ReplyBody) {
        super.onReplyReceived(replyBody)
        if (replyBody.key == IMConstant.RequestKey.CLIENT_BIND && replyBody.code == IMConstant.ReturnCode.CODE_200) {
            ARouter.getInstance().build("/CHAT/MAIN").navigation()
            finish()
        } else {
            Toast.makeText(this, replyBody.message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        IMManagerHelper.destroy(this)
        finish()
    }

    override fun onConnectionFailed() {
        Toast.makeText(this, "连接服务器失败，请检查当前设备是否能连接上服务器IP和端口", Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        mLoginDisposable.dispose()
        super.onDestroy()
    }

    /**绑定IM账号*/
    private fun bindAccount() {
        if (IMManagerHelper.isConnected(this)) {
            if (ChatApplication.loginUser.value == null) {
                return
            }
            IMManagerHelper.bindAccount(this, ChatApplication.loginUser.value!!.imAccount)
        } else {
            IMManagerHelper.connect(this, Constants.IM_SERVER_HOST, Constants.IM_SERVER_PORT)
        }
    }
}