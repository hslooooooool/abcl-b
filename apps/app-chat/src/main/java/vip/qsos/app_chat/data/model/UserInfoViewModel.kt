package vip.qsos.app_chat.data.model

import vip.qsos.app_chat.data.entity.ChatFriendBo
import vip.qsos.app_chat.data.entity.ChatSessionBo
import vip.qsos.app_chat.data.entity.AppUserBo
import kotlin.coroutines.CoroutineContext

/**
 * @author : 华清松
 * 用户资料页接口
 */
interface UserInfoViewModel {

    val mJob: CoroutineContext
    fun clear()

    /**获取用户数据
     * @param userId 用户ID
     * @return 用户数据
     * */
    fun getUserById(
            userId: Long,
            failed: (msg: String) -> Unit,
            success: (user: AppUserBo) -> Unit
    )

    /**加好友
     * @param userId 申请用户ID
     * @param friendId 待加好友ID
     * */
    fun addFriend(
            userId: Long,
            friendId: Long,
            failed: (msg: String) -> Unit,
            success: (user: ChatFriendBo) -> Unit
    )

    /**判断好友关系
     * @param userId 用户ID
     * @param friendId 好友ID
     * */
    fun findFriend(
            userId: Long,
            friendId: Long,
            failed: (msg: String) -> Unit,
            success: (user: ChatFriendBo?) -> Unit
    )

    /**获取单聊会话信息*/
    fun getSessionOfSingle(
            sender: String,
            receiver: String,
            failed: (msg: String) -> Unit,
            success: (session: ChatSessionBo?) -> Unit
    )

}