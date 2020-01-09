package vip.qsos.app_chat.data.model

import vip.qsos.app_chat.data.entity.ChatMessage
import vip.qsos.app_chat.data.entity.ChatSessionBo
import kotlin.coroutines.CoroutineContext

/**
 * @author : 华清松
 * 聊天会话接口
 */
interface ChatSessionModel {

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
            success: (session: ChatSessionBo) -> Unit
    )

    /**获取单聊会话信息
     * @param sender 发送方聊天账号
     * @param receiver 接收方聊天账号
     * @return 会话数据
     * */
    fun findSessionOfSingle(
            sender: String,
            receiver: String,
            failed: (msg: String) -> Unit,
            success: (session: ChatSessionBo) -> Unit
    )

    /**获取群数据
     * @param sessionId 会话ID
     * @return 群数据
     * */
    fun getSessionById(
            sessionId: Long,
            failed: (msg: String) -> Unit,
            success: (session: ChatSessionBo) -> Unit
    )

    /**往已有群中增加用户
     * @param userIdList 被添加用户ID集合
     * @param sessionId 群ID
     * @return 加入的群数据
     * */
    fun addUserListToSession(
            userIdList: List<Long>, sessionId: Long,
            failed: (msg: String) -> Unit,
            success: (session: ChatSessionBo) -> Unit
    )

    /**解散群
     * @param sessionId 群ID
     * */
    fun deleteSession(sessionId: Long)

}