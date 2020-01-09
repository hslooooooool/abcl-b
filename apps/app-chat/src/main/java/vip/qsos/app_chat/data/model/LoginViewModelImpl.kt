package vip.qsos.app_chat.data.model

import android.content.Context
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import qsos.base.core.base.db.DBLoginUser
import qsos.base.core.base.db.LoginUserDatabase
import qsos.base.core.config.BaseConfig
import qsos.lib.base.base.BaseApplication
import qsos.lib.netservice.ApiEngine
import qsos.lib.netservice.data.BaseResponse
import qsos.lib.netservice.expand.retrofit
import vip.qsos.app_chat.data.LoginApi
import vip.qsos.app_chat.data.entity.LoginUser
import kotlin.coroutines.CoroutineContext

class LoginViewModelImpl : LoginViewModel, ViewModel() {

    override val mJob: CoroutineContext = Dispatchers.Main + Job()

    override fun login(account: String, password: String, failed: (msg: String) -> Unit, success: (user: LoginUser) -> Unit) {
        CoroutineScope(mJob).retrofit<BaseResponse<LoginUser>> {
            api = ApiEngine.createService(LoginApi::class.java).login(account, password)
            onFailed { code, msg, error ->
                failed.invoke(msg ?: "$code 登录失败${error?.message}")
            }
            onSuccess {
                it?.data?.let { user ->
                    saveLoginUser(user, failed, success)
                } ?: failed.invoke("登录失败 ${it?.msg}")
            }
        }
    }

    override fun register(account: String, password: String, failed: (msg: String) -> Unit, success: (user: LoginUser) -> Unit) {
        CoroutineScope(mJob).retrofit<BaseResponse<LoginUser>> {
            api = ApiEngine.createService(LoginApi::class.java).register(account, password)
            onFailed { code, msg, error ->
                failed.invoke(msg ?: "$code 注册失败${error?.message}")
            }
            onSuccess {
                it?.data?.let { user ->
                    saveLoginUser(user, failed, success)
                } ?: failed.invoke("注册失败 ${it?.msg}")
            }
        }
    }

    private fun saveLoginUser(user: LoginUser, failed: (msg: String) -> Unit, success: (user: LoginUser) -> Unit) {
        LoginUserDatabase.DefLoginUserDao.insert(
                user = DBLoginUser(
                        userId = user.userId, name = user.name,
                        account = user.imAccount, password = user.password,
                        avatar = user.avatar
                ),
                result = {
                    if (it == null) {
                        failed.invoke("失败，APP异常")
                    } else {
                        BaseConfig.userId = user.userId
                        BaseApplication.appContext.getSharedPreferences("SHARED_PRE", Context.MODE_PRIVATE)
                                .edit().putLong("LAST_LOGIN_USER_ID", BaseConfig.userId).apply()
                        success.invoke(user)
                    }
                }
        )
    }

    override fun clear() {
        mJob.cancel()
    }
}