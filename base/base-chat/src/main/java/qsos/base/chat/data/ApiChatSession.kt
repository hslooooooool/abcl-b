package qsos.base.chat.data

import qsos.base.chat.data.entity.ChatMessage
import qsos.base.chat.data.entity.ChatSession
import qsos.base.core.config.BaseConfig
import qsos.lib.netservice.data.BaseResponse
import retrofit2.Call
import retrofit2.http.*

/**
 * @author : 华清松
 * 聊天消息数据网络接口
 * @see qsos.base.chat.data.model.IChatModel.ISession
 */
interface ApiChatSession {
    companion object {
        const val GROUP = "/chat/session"
    }

    @POST(value = "$GROUP/createSession")
    fun createSession(
            @Header(value = "userId") meId: Int = BaseConfig.userId,
            @Body form: FormCreateSession
    ): Call<BaseResponse<ChatSession>>

    @GET(value = "$GROUP/getSessionById")
    fun getSessionById(
            @Header(value = "userId") meId: Int = BaseConfig.userId,
            @Query(value = "sessionId") sessionId: Int
    ): Call<BaseResponse<ChatSession>>

    @GET(value = "$GROUP/getSessionListByUserId")
    fun getSessionListByUserId(
            @Header(value = "userId") meId: Int = BaseConfig.userId,
            @Query(value = "userId") userId: Int
    ): Call<BaseResponse<List<ChatSession>>>

    @POST(value = "$GROUP/addUserListToSession")
    fun addUserListToSession(
            @Header(value = "userId") meId: Int = BaseConfig.userId,
            @Query(value = "sessionId") sessionId: Int,
            @Query(value = "userIdList") userIdList: List<Int>
    ): Call<BaseResponse<ChatSession>>

    @DELETE(value = "$GROUP/deleteSession")
    fun deleteSession(
            @Header(value = "userId") meId: Int = BaseConfig.userId,
            @Query(value = "sessionId") sessionId: Int
    ): Call<BaseResponse<Boolean>>

    data class FormCreateSession(
            val userIdList: List<Int>,
            val message: ChatMessage? = null
    )
}