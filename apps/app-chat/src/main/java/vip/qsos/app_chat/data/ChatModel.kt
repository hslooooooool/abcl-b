package vip.qsos.app_chat.data

import androidx.lifecycle.MutableLiveData
import com.alibaba.android.arouter.launcher.ARouter
import qsos.core.exception.GlobalException
import vip.qsos.app_chat.data.entity.LoginUser

/**
 * @author : 华清松
 * 聊天接口定义
 */
object ChatModel {

    private val mLoginUser: MutableLiveData<LoginUser> = MutableLiveData()

    fun getLoginUser(): LoginUser {
        if (mLoginUser.value == null) {
            ARouter.getInstance().build("/USER/LOGIN").navigation()
            throw GlobalException(403, "请重新登录")
        }
        return mLoginUser.value!!
    }

    fun setLoginUser(user: LoginUser) {
        mLoginUser.value = user
    }
}