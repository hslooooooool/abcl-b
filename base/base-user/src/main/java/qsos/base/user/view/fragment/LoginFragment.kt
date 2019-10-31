package qsos.base.user.view.fragment

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.navigation.Navigation
import com.alibaba.android.arouter.launcher.ARouter
import kotlinx.android.synthetic.main.fragment_login.*
import qsos.base.core.base.db.DBLoginUser
import qsos.base.core.base.db.LoginUserDatabase
import qsos.base.core.config.BaseConfig
import qsos.base.user.R
import qsos.base.user.data.model.DefLoginUserModelIml
import qsos.base.user.data.model.ILoginUserModel
import qsos.lib.base.base.fragment.BaseFragment
import qsos.lib.base.utils.ToastUtils

/**
 * @author : 华清松
 * 登录页面
 */
class LoginFragment(
        private val mLoginUserModel: ILoginUserModel = DefLoginUserModelIml(),
        override val layoutId: Int = R.layout.fragment_login,
        override val reload: Boolean = false
) : BaseFragment() {

    private var mLastLoginUserId: Int = -1

    override fun getData() {

    }

    override fun initData(savedInstanceState: Bundle?) {
        mLastLoginUserId = activity!!.getSharedPreferences("SHARED_PRE", Context.MODE_PRIVATE)
                .getInt("LAST_LOGIN_USER_ID", BaseConfig.userId)
    }

    override fun initView(view: View) {
        register.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_loginFragment_to_registerFragment)
        }
        login.setOnClickListener { login ->
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
                    mLoginUserModel.login(account, password,
                            failed = {
                                ToastUtils.showToast(context, it)
                                login.isClickable = true
                            },
                            success = { user ->
                                LoginUserDatabase.DefLoginUserDao.insert(
                                        user = DBLoginUser(
                                                userId = user.userId,
                                                userName = user.userName,
                                                account = user.account,
                                                password = user.password,
                                                avatar = user.avatar,
                                                birth = user.birth,
                                                sexuality = user.sexuality
                                        ),
                                        result = {
                                            if (it == null) {
                                                ToastUtils.showToast(context, "登录失败")
                                                login.isClickable = true
                                            } else {
                                                ToastUtils.showToast(context, "登录成功")
                                                BaseConfig.userId = user.userId
                                                mContext.getSharedPreferences("SHARED_PRE", Context.MODE_PRIVATE)
                                                        .edit().putInt("LAST_LOGIN_USER_ID", BaseConfig.userId).apply()
                                                ARouter.getInstance().build("/CHAT/MAIN").navigation()
                                                (context as Activity?)?.finish()
                                            }
                                        }
                                )
                            })
                }
            }
        }

        LoginUserDatabase.DefLoginUserDao.getLoginUserByUserId(
                userId = mLastLoginUserId,
                result = {
                    it?.let {
                        login_account.setText(it.account)
                        login_password.setText(it.password)
                    }
                })
    }
}