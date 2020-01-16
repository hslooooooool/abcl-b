package qsos.base.core.config

import androidx.lifecycle.MutableLiveData
import com.alibaba.android.arouter.launcher.ARouter
import qsos.base.core.base.LoginUser

/**
 * @author : 华清松
 * 配置参数
 */
object BaseConfig {

    private val mLoginUser: MutableLiveData<LoginUser> = MutableLiveData()

    fun getLoginUser(): LoginUser {
        if (mLoginUser.value == null) {
            ARouter.getInstance().build("/CHAT/LOGIN").navigation()
            return LoginUser(-1L, "", "", "")
        }
        return mLoginUser.value!!
    }

    fun setLoginUser(user: LoginUser) {
        mLoginUser.value = user
    }

    fun getLoginUserId(): Long {
        val user = getLoginUser()
        return user.id
    }

}