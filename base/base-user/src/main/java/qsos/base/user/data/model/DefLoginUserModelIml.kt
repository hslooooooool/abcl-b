package qsos.base.user.data.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import qsos.base.user.data.ApiLoginUser
import qsos.base.user.data.entity.LoginUser
import qsos.lib.netservice.ApiEngine
import qsos.lib.netservice.data.BaseResponse
import qsos.lib.netservice.expand.retrofit
import kotlin.coroutines.CoroutineContext

class DefLoginUserModelIml(
        override val mJob: CoroutineContext = Dispatchers.Main + Job()
) : ILoginUserModel {

    override fun login(account: String, password: String, failed: (msg: String) -> Unit, success: (user: LoginUser) -> Unit) {
        CoroutineScope(mJob).retrofit<BaseResponse<LoginUser>> {
            api = ApiEngine.createService(ApiLoginUser::class.java).login(account, password)
            onFailed { code, msg, error ->
                failed.invoke(msg ?: "$code 登录失败${error?.message}")
            }
            onSuccess {
                if (it?.data == null) {
                    failed.invoke("登录失败 ${it?.msg}")
                } else {
                    success.invoke(it.data!!)
                }
            }
        }
    }

    override fun register(account: String, password: String, failed: (msg: String) -> Unit, success: (user: LoginUser) -> Unit) {
        CoroutineScope(mJob).retrofit<BaseResponse<LoginUser>> {
            api = ApiEngine.createService(ApiLoginUser::class.java).register(account, password)
            onFailed { code, msg, error ->
                failed.invoke(msg ?: "$code 注册失败${error?.message}")
            }
            onSuccess {
                it?.data?.let { user ->
                    success.invoke(user)
                } ?: failed.invoke("注册失败 ${it?.msg}")
            }
        }
    }

    override fun clear() {
        mJob.cancel()
    }
}