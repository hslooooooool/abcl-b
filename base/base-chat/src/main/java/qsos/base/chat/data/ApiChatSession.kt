package qsos.base.chat.data

import qsos.base.chat.data.entity.ChatMessage
import qsos.base.chat.data.entity.ChatSession
import qsos.base.chat.data.entity.ChatUser
import qsos.base.core.config.BaseConfig
import qsos.lib.netservice.data.BaseResponse
import retrofit2.Call
import retrofit2.http.*

/**
 * @author : 华清松
 * 聊天消息数据网络接口
 */
interface ApiChatSession {

    /**创建用户
     * @param user 用户
     * */
    @POST(value = "/chat/user/createUser")
    fun createUser(
            @Body user: ChatUser
    ): Call<BaseResponse<ChatUser>>

    /**获取会话数据
     * @param sessionId 会话ID
     * @return 会话数据
     * */
    @GET(value = "/chat/session")
    fun getSessionById(
            @Query(value = "sessionId") sessionId: Int
    ): Call<BaseResponse<ChatSession>>

    /**创建会话
     * @param userId 登录用户ID
     * @param form 创建会话表单
     * @return 会话数据
     * */
    @POST(value = "/chat/createSession")
    fun createSession(
            @Header(value = "userId") userId: Int = BaseConfig.userId,
            @Body form: FormCreateSession
    ): Call<BaseResponse<ChatSession>>

    /**创建会话表单
     * @param userIdList 用户ID集合
     * @param message 发送的消息，可为空
     * */
    data class FormCreateSession(
            val userIdList: List<Int>,
            val message: ChatMessage? = null
    )
}