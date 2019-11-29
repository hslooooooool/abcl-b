package qsos.base.chat.data.model

import qsos.base.chat.data.entity.*
import qsos.lib.netservice.data.BaseHttpLiveData
import kotlin.coroutines.CoroutineContext

/**
 * @author : 华清松
 * 聊天接口定义
 */
interface IChatModel {

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
                userId: Int,
                failed: (msg: String) -> Unit,
                success: (user: ChatUser) -> Unit
        )

        /**获取会话下的用户列表
         * @param sessionId 会话ID
         * @return 用户列表
         * */
        fun getUserListBySessionId(sessionId: Int): List<ChatUser>

        /**将用户移除会话
         * @param sessionId 会话ID
         * @param userId 需要移除的用户ID
         * */
        fun deleteUser(sessionId: Int, userId: Int)

    }

    interface ISession {

        val mJob: CoroutineContext
        fun clear()

        /**创建会话,可同时往会话发送一条消息,适用于发起单聊/群聊/分享等场景
         * @param userIdList 用户ID集合
         * @param message 发送的消息
         * @return 会话数据
         * */
        fun createSession(
                userIdList: List<Int>,
                message: ChatMessage? = null,
                failed: (msg: String) -> Unit,
                success: (session: ChatSession) -> Unit
        )

        /**获取会话数据
         * @param sessionId 会话ID
         * @return 会话数据
         * */
        fun getSessionById(
                sessionId: Int,
                failed: (msg: String) -> Unit,
                success: (session: ChatSession) -> Unit
        )

        /**获取用户订阅的会话
         * @param userId 用户ID
         * @return 用户订阅的会话
         * */
        fun getSessionListByUserId(userId: Int): List<ChatSession>

        /**往已有会话中增加用户
         * @param userIdList 被添加用户ID集合
         * @param sessionId 会话ID
         * @return 加入的会话数据
         * */
        fun addUserListToSession(
                userIdList: List<Int>, sessionId: Int,
                failed: (msg: String) -> Unit,
                success: (session: ChatSession) -> Unit
        )

        /**解散会话
         * @param sessionId 会话ID
         * */
        fun deleteSession(sessionId: Int)

    }

    interface IMessage {

        val mJob: CoroutineContext
        fun clear()
        val mDataOfNewMessage: BaseHttpLiveData<List<ChatMessageBo>>

        /**获取会话下的新消息列表
         * @param sessionId 会话ID
         * */
        fun getNewMessageBySessionId(sessionId: Int)

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
        val mGroupListWithMeLiveData: BaseHttpLiveData<List<ChatGroup>>

        /**获取聊天群数据
         * @param groupId 聊天群ID
         * @param success 聊天群数据
         * */
        fun getGroupById(
                groupId: Int,
                success: (message: ChatGroup) -> Unit
        )

        //TODO 建立消息数据库，读取最新消息并进行未读统计后入库，消息列表与群列表从数据库获取经过排序的数据并更新未读数
        /**获取当前用户所在的所有聊天群列表数据*/
        fun getGroupListWithMe()

        /**获取会话对应的聊天群数据
         * @param sessionId 会话ID
         * @return 聊天群数据
         * */
        fun getGroupBySessionId(sessionId: Int): ChatGroup

        /**更新聊天群公告
         * @param notice 需更新的聊天群公告
         * @return 已更新的聊天群数据
         * */
        fun updateGroupNotice(notice: String): ChatGroup

        /**更新聊天群名称
         * @param name 需更新的聊天群名称
         * @return 已更新的聊天群数据
         * */
        fun updateGroupName(name: String): ChatGroup

    }
}