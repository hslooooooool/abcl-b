package vip.qsos.app_chat.data.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import qsos.lib.netservice.ApiEngine
import qsos.lib.netservice.data.BaseResponse
import qsos.lib.netservice.expand.retrofit
import vip.qsos.app_chat.data.api.UserInfoApi
import vip.qsos.app_chat.data.entity.ChatFriendBo
import vip.qsos.app_chat.data.entity.ChatSessionBo
import vip.qsos.app_chat.data.entity.AppUserBo
import kotlin.coroutines.CoroutineContext

/**
 * @author : 华清松
 */
class UserInfoViewModelImpl(
        override val mJob: CoroutineContext = Dispatchers.Main + Job()
) : UserInfoViewModel, ViewModel() {

    val mChatUser: MutableLiveData<AppUserBo> = MutableLiveData()
    val mChatSession: MutableLiveData<ChatSessionBo> = MutableLiveData()
    val mChatFriend: MutableLiveData<ChatFriendBo> = MutableLiveData()

    override fun getUserById(
            userId: Long,
            failed: (msg: String) -> Unit,
            success: (user: AppUserBo) -> Unit
    ) {
        CoroutineScope(mJob).retrofit<BaseResponse<AppUserBo>> {
            api = ApiEngine.createService(UserInfoApi::class.java).getUserById(userId = userId)
            onSuccess {
                if (it?.data != null) {
                    success.invoke(it.data!!)
                }
            }
        }
    }

    override fun addFriend(
            userId: Long, friendId: Long,
            failed: (msg: String) -> Unit,
            success: (user: ChatFriendBo) -> Unit
    ) {
        CoroutineScope(mJob).retrofit<BaseResponse<ChatFriendBo>> {
            api = ApiEngine.createService(UserInfoApi::class.java).addFriend(
                    userId = userId, friendId = friendId
            )
            onSuccess {
                if (it?.data != null) {
                    success.invoke(it.data!!)
                }
            }
        }
    }

    override fun findFriend(
            userId: Long, friendId: Long,
            failed: (msg: String) -> Unit,
            success: (user: ChatFriendBo?) -> Unit
    ) {
        CoroutineScope(mJob).retrofit<BaseResponse<ChatFriendBo>> {
            api = ApiEngine.createService(UserInfoApi::class.java).findFriend(
                    userId = userId, friendId = friendId
            )
            onSuccess {
                success.invoke(it!!.data)
            }
        }
    }

    override fun getSessionOfSingle(
            sender: String,
            receiver: String,
            failed: (msg: String) -> Unit,
            success: (session: ChatSessionBo?) -> Unit
    ) {
        CoroutineScope(mJob).retrofit<BaseResponse<ChatSessionBo>> {
            api = ApiEngine.createService(UserInfoApi::class.java).getSessionOfSingle(
                    sender, receiver
            )
            onSuccess {
                success.invoke(it!!.data)
            }
        }
    }

    override fun clear() {
        mJob.cancel()
    }
}