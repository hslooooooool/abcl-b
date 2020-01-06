package vip.qsos.app_chat.data.model

import androidx.lifecycle.MutableLiveData
import qsos.lib.netservice.data.BaseHttpLiveData
import vip.qsos.app_chat.data.entity.*
import kotlin.coroutines.CoroutineContext

/**
 * @author : 华清松
 * 聊天接口定义
 */
interface ChatModel {

    companion object {
        val mLoginUser: MutableLiveData<ChatUser> = MutableLiveData()
    }

    interface IUser {

        val mJob: CoroutineContext
        fun clear()
        val mDataOfChatUserList: BaseHttpLiveData<List<ChatUser>>

        /**创建用户
         * @param user 用户
         * */
        fun createUser(user: ChatUser, failed: (msg: String) -> Unit, success: (user: ChatUser) -> Unit)

        /**获取所有用户列表数据
         * @return 用户数据
         * */
        fun getAllChatUser()

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
                success: (user: ChatFriend) -> Unit
        )

        /**获取群下的用户列表
         * @param sessionId 群ID
         * @return 用户列表
         * */
        fun getUserListBySessionId(sessionId: Long): List<ChatUser>

        /**将用户移除群
         * @param sessionId 群ID
         * @param userId 需要移除的用户ID
         * */
        fun deleteUser(sessionId: Long, userId: Long)

    }

    interface ISession {

        val mJob: CoroutineContext
        fun clear()

        /**创建群,可同时往群发送一条消息,适用于发起单聊/群聊/分享等场景
         * @param creator 创建人聊天账号
         * @param accountList 用户聊天账号集合
         * @param message 发送的消息
         * @return 群数据
         * */
        fun createSession(
                creator: String,
                accountList: List<String>,
                message: ChatMessage? = null,
                failed: (msg: String) -> Unit,
                success: (group: ChatGroup) -> Unit
        )

        /**获取单聊群信息
         * @param sender 创建人聊天账号
         * @param receiver 群成员，单人聊天账号
         * @return 群数据
         * */
        fun findSingle(
                sender: String,
                receiver: String,
                failed: (msg: String) -> Unit,
                success: (group: ChatGroup) -> Unit
        )

        /**获取群数据
         * @param groupId 群ID
         * @return 群数据
         * */
        fun getGroupById(
                groupId: String,
                failed: (msg: String) -> Unit,
                success: (group: ChatGroup) -> Unit
        )

        /**获取用户订阅的群
         * @param userId 用户ID
         * @return 用户订阅的群
         * */
        fun getSessionListByUserId(userId: Long): List<ChatGroup>

        /**往已有群中增加用户
         * @param userIdList 被添加用户ID集合
         * @param sessionId 群ID
         * @return 加入的群数据
         * */
        fun addUserListToSession(
                userIdList: List<Long>, sessionId: String,
                failed: (msg: String) -> Unit,
                success: (group: ChatGroup) -> Unit
        )

        /**解散群
         * @param sessionId 群ID
         * */
        fun deleteSession(sessionId: String)

    }

    interface IMessage {

        val mJob: CoroutineContext
        fun clear()

        /**获取群下的历史消息列表，即当前第一条消息时序前【20】条消息
         * @param sessionId 群ID
         * */
        fun getOldMessageBySessionId(sessionId: String, success: (messageList: List<ChatMessageBo>) -> Unit)

        /**获取群下的新消息列表
         * @param sessionId 群ID
         * */
        fun getNewMessageBySessionId(sessionId: String, success: (messageList: List<ChatMessageBo>) -> Unit)

        /**撤回消息
         * @param message 消息
         * */
        fun deleteMessage(
                message: ChatMessageBo,
                failed: (msg: String, message: ChatMessageBo) -> Unit,
                success: (message: ChatMessageBo) -> Unit
        )

    }

    interface IGroup {

        val mJob: CoroutineContext
        fun clear()
        val mGroupListWithMeLiveData: BaseHttpLiveData<List<ChatGroupInfo>>

        /**获取聊天群数据
         * @param groupId 聊天群ID
         * @param success 聊天群数据
         * */
        fun getGroupById(
                groupId: String,
                success: (message: ChatGroupInfo) -> Unit
        )

        //TODO 建立消息数据库，读取最新消息并进行未读统计后入库，消息列表与群列表从数据库获取经过排序的数据并更新未读数
        /**获取当前用户所在的所有聊天群列表数据*/
        fun getGroupListWithMe()

        /**获取群对应的聊天群数据
         * @param sessionId 群ID
         * @return 聊天群数据
         * */
        fun getGroupBySessionId(sessionId: String): ChatGroupInfo

        /**更新聊天群公告
         * @param notice 需更新的聊天群公告
         * @return 已更新的聊天群数据
         * */
        fun updateGroupNotice(notice: String): ChatGroupInfo

        /**更新聊天群名称
         * @param name 需更新的聊天群名称
         * @return 已更新的聊天群数据
         * */
        fun updateGroupName(name: String): ChatGroupInfo

    }
}