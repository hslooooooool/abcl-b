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

        /**创建用户
         * @param user 用户
         * */
        fun createUser(user: ChatUser, failed: (msg: String) -> Unit, success: (user: ChatUser) -> Unit)

        /**获取用户数据
         * @param userId 用户ID
         * @return 用户数据
         * */
        fun getUserById(userId: Int): ChatUser

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
        val mDataOfChatSession: BaseHttpLiveData<ChatSession>

        /**创建会话,可同时往会话发送一条消息,适用于发起单聊/群聊/分享等场景
         * @param userIdList 用户ID集合
         * @param message 发送的消息
         * @return 会话数据
         * */
        fun createSession(userIdList: List<Int>, message: ChatMessage? = null, failed: (msg: String) -> Unit, success: () -> Unit)

        /**获取会话数据
         * @param sessionId 会话ID
         * @return 会话数据
         * */
        fun getSessionById(sessionId: Int)

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
        fun addUserListToSession(userIdList: List<Int>, sessionId: Int): ChatSession

        /**解散会话
         * @param sessionId 会话ID
         * */
        fun deleteSession(sessionId: Int)

    }

    interface IMessage {

        val mJob: CoroutineContext
        fun clear()
        val mDataOfChatMessageList: BaseHttpLiveData<List<MChatMessage>>

        /**发送消息
         * @param message 消息数据
         * @return 消息数据
         * */
        fun sendMessage(message: ChatMessage): ChatMessage

        /**获取消息数据
         * @param messageId 消息ID
         * @return 消息数据
         * */
        fun getMessageById(messageId: Int): ChatMessage

        /**获取用户发送的消息
         * @param userId 用户ID
         * @return 用户发送的消息
         * */
        fun getMessageListByUserId(userId: Int): List<ChatMessage>

        /**获取会话下的消息列表
         * @param sessionId 会话ID
         * @return 会话下的消息列表 List<ChatMessage>
         * */
        fun getMessageListBySessionId(sessionId: Int)

        /**获取消息内容数据
         * @param contentId 消息内容ID
         * @return 消息内容数据
         * */
        fun getContentById(contentId: Int): ChatContent

        /**撤回消息
         * @param messageId 消息ID
         * */
        fun deleteMessage(messageId: Int)

    }

    interface IGroup {

        val mJob: CoroutineContext
        fun clear()
        val mGroupListWithMeLiveData: BaseHttpLiveData<List<ChatGroup>>

        /**获取聊天群数据
         * @param groupId 聊天群ID
         * @return 聊天群数据
         * */
        fun getGroupById(groupId: Int): ChatGroup

        /**获取当前用户所在的所有聊天群列表数据*/
        fun getGroupListWithMe()

        /**获取会话对应的聊天群数据
         * @param sessionId 会话ID
         * @return 聊天群数据
         * */
        fun getGroupByBySessionId(sessionId: Int): ChatGroup

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