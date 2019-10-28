package qsos.base.chat.data.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import qsos.base.chat.data.ApiChatUser
import qsos.base.chat.data.entity.ChatUser
import qsos.lib.netservice.ApiEngine
import qsos.lib.netservice.data.BaseHttpLiveData
import qsos.lib.netservice.data.BaseResponse
import qsos.lib.netservice.expand.retrofit
import qsos.lib.netservice.expand.retrofitWithLiveDataByDef
import kotlin.coroutines.CoroutineContext

/**
 * @author : 华清松
 * 聊天用户相关接口默认实现
 */
class DefChatUserModelIml(
        override val mJob: CoroutineContext = Dispatchers.Main + Job(),
        override val mDataOfChatUserList: BaseHttpLiveData<List<ChatUser>> = BaseHttpLiveData()
) : IChatModel.IUser {

    override fun createUser(user: ChatUser, failed: (msg: String) -> Unit, success: (user: ChatUser) -> Unit) {
        CoroutineScope(mJob).retrofit<BaseResponse<ChatUser>> {
            api = ApiEngine.createService(ApiChatUser::class.java).createUser(user)
            onFailed { _, msg, error ->
                failed.invoke(msg ?: "创建失败${error.toString()}")
            }
            onSuccess {
                if (it?.data == null) {
                    failed.invoke("创建失败${it?.msg}")
                } else {
                    success.invoke(it.data!!)
                }
            }
        }
    }

    override fun getAllChatUser() {
        CoroutineScope(mJob).retrofitWithLiveDataByDef<List<ChatUser>> {
            api = ApiEngine.createService(ApiChatUser::class.java).getAllUser()
            data = mDataOfChatUserList
        }
    }

    override fun getUserById(
            userId: Int,
            failed: (msg: String) -> Unit,
            success: (user: ChatUser) -> Unit
    ) {
        CoroutineScope(mJob).retrofit<BaseResponse<ChatUser>> {
            api = ApiEngine.createService(ApiChatUser::class.java).getUserById(userId = userId)
            onSuccess {
                if (it?.data != null) {
                    success.invoke(it.data!!)
                }
            }
        }
    }

    override fun getUserListBySessionId(sessionId: Int): List<ChatUser> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteUser(sessionId: Int, userId: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun clear() {
        mJob.cancel()
    }
}