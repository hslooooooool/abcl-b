package vip.qsos.app_chat.data.model

import qsos.lib.netservice.data.BaseHttpLiveData
import vip.qsos.app_chat.data.entity.ChatFriend
import vip.qsos.app_chat.data.entity.ChatUser
import kotlin.coroutines.CoroutineContext

/**
 * @author : 华清松
 * 聊天用户接口
 */
interface ChatUserModel {

    val mJob: CoroutineContext
    fun clear()
    val mDataOfChatUserList: BaseHttpLiveData<List<ChatUser>>

    /**获取好友列表数据
     * @return 好友列表
     * */
    fun getFriendList()

    /**获取用户数据
     * @param userId 用户ID
     * @return 用户数据
     * */
    fun getUserById(
            userId: Long,
            failed: (msg: String) -> Unit,
            success: (user: ChatUser) -> Unit
    )

    /**加好友
     * @param userId 申请用户ID
     * @param friendId 待加好友ID
     * */
    fun addFriend(
            userId: Long,
            friendId: Long,
            failed: (msg: String) -> Unit,
            success: (user: ChatFriend) -> Unit
    )

    /**判断好友关系
     * @param userId 用户ID
     * @param friendId 好友ID
     * */
    fun findFriend(
            userId: Long,
            friendId: Long,
            failed: (msg: String) -> Unit,
            success: (user: ChatFriend?) -> Unit
    )

}