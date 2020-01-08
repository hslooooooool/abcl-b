package vip.qsos.app_chat.view.fragment

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_login.*
import qsos.base.core.base.db.LoginUserDatabase
import qsos.base.core.config.BaseConfig
import qsos.lib.base.base.fragment.BaseFragment
import qsos.lib.base.utils.ToastUtils
import qsos.lib.base.utils.rx.RxBus
import vip.qsos.app_chat.ChatApplication
import vip.qsos.app_chat.R
import vip.qsos.app_chat.data.event.LoginSuccessEvent
import vip.qsos.app_chat.data.model.LoginUserModelIml

/**
 * @author : 华清松
 * 登录页面
 */
class LoginFragment(
        override val layoutId: Int = R.layout.fragment_login,
        override val reload: Boolean = false
) : BaseFragment() {

    private var mLastLoginUserId: Long = -1L
    private val mLoginUserModel: LoginUserModelIml by viewModels()

    override fun getData() {}

    override fun initData(savedInstanceState: Bundle?) {
        mLastLoginUserId = activity!!.getSharedPreferences("SHARED_PRE", Context.MODE_PRIVATE)
                .getLong("LAST_LOGIN_USER_ID", BaseConfig.userId)
    }

    override fun initView(view: View) {
        register.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_loginFragment_to_registerFragment)
        }
        login.setOnClickListener {
            login.isClickable = false
            val account = login_account.text.toString().trim()
            val password = login_password.text.toString().trim()
            login_account.clearFocus()
            login_password.clearFocus()
            when {
                TextUtils.isEmpty(account) || TextUtils.isEmpty(password) -> {
                    ToastUtils.showToast(context, "账号或密码不能为空")
                    login.isClickable = true
                }
                else -> {
                    login(account, password)
                }
            }
        }

        LoginUserDatabase.DefLoginUserDao.getLoginUserByUserId(
                userId = mLastLoginUserId,
                result = {
                    it?.let {
                        login_account.setText(it.name)
                        login_password.setText(it.password)
                    }
                }
        )
    }

    /**账号密码登录*/
    private fun login(account: String, password: String) {
        mLoginUserModel.login(account, password,
                failed = {
                    ToastUtils.showToast(context, it)
                    login.isClickable = true
                },
                success = { user ->
                    ToastUtils.showToast(context, "登录成功")
                    ChatApplication.loginUser.postValue(user)
                    RxBus.send(LoginSuccessEvent(user))
                })
    }
}