package vip.qsos.app_chat.view.fragment

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_register.*
import qsos.lib.base.base.fragment.BaseFragment
import qsos.lib.base.utils.ToastUtils
import qsos.lib.base.utils.rx.RxBus
import vip.qsos.app_chat.ChatApplication
import vip.qsos.app_chat.R
import vip.qsos.app_chat.data.event.LoginSuccessEvent
import vip.qsos.app_chat.data.model.LoginUserModelIml

/**
 * @author : 华清松
 * 注册页面
 */
class RegisterFragment(
        override val layoutId: Int = R.layout.fragment_register,
        override val reload: Boolean = false
) : BaseFragment() {

    private val mLoginUserModel: LoginUserModelIml by viewModels()

    override fun getData() {}

    override fun initData(savedInstanceState: Bundle?) {}

    override fun initView(view: View) {
        login.setOnClickListener {
            Navigation.findNavController(it).popBackStack()
        }
        register.setOnClickListener { register ->
            register.isClickable = false
            val account = login_account.text.toString().trim()
            val password = login_password.text.toString().trim()
            login_account.clearFocus()
            login_password.clearFocus()
            when {
                TextUtils.isEmpty(account) || TextUtils.isEmpty(password) -> {
                    ToastUtils.showToast(context, "账号或密码不能为空")
                    register.isClickable = true
                }
                else -> {
                    mLoginUserModel.register(account, password,
                            failed = {
                                ToastUtils.showToast(context, it)
                                register.isClickable = true
                            },
                            success = { user ->
                                ToastUtils.showToast(context, "注册成功")
                                ChatApplication.loginUser.postValue(user)
                                RxBus.send(LoginSuccessEvent())
                            })
                }
            }
        }
    }
}