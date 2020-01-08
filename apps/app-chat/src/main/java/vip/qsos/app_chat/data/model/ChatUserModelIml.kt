package vip.qsos.app_chat.data.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import qsos.lib.netservice.ApiEngine
import qsos.lib.netservice.data.BaseHttpLiveData
import qsos.lib.netservice.data.BaseResponse
import qsos.lib.netservice.expand.retrofit
import qsos.lib.netservice.expand.retrofitWithLiveDataByDef
import vip.qsos.app_chat.data.ApiBiz1
import vip.qsos.app_chat.data.ApiChatFriend
import vip.qsos.app_chat.data.ApiChatUser
import vip.qsos.app_chat.data.entity.ChatFriend
import vip.qsos.app_chat.data.entity.ChatUser
import kotlin.coroutines.CoroutineContext

/**
 * @author : 华清松
 * 聊天用户相关接口默认实现
 */
class ChatUserModelIml(
        override val mJob: CoroutineContext = Dispatchers.Main + Job(),
        override val mDataOfChatUserList: BaseHttpLiveData<List<ChatUser>> = BaseHttpLiveData()
) : ChatUserModel {

    override fun getFriendList() {
        CoroutineScope(mJob).retrofitWithLiveDataByDef<List<ChatUser>> {
            api = ApiEngine.createService(ApiBiz1::class.java).getFriendList(ChatModel.mLoginUser.value!!.userId)
            data = mDataOfChatUserList
        }
    }

    override fun getUserById(
            userId: Long,
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

    override fun addFriend(
            userId: Long, friendId: Long,
            failed: (msg: String) -> Unit,
            success: (user: ChatFriend) -> Unit
    ) {
        CoroutineScope(mJob).retrofit<BaseResponse<ChatFriend>> {
            api = ApiEngine.createService(ApiChatFriend::class.java).addChatFriend(
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
            success: (user: ChatFriend?) -> Unit
    ) {
        CoroutineScope(mJob).retrofit<BaseResponse<ChatFriend>> {
            api = ApiEngine.createService(ApiChatFriend::class.java).findChatFriend(
                    userId = userId, friendId = friendId
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