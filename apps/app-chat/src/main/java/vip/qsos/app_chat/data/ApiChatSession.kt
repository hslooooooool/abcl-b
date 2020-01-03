package vip.qsos.app_chat.data

import qsos.base.core.config.BaseConfig
import qsos.lib.netservice.data.BaseResponse
import retrofit2.Call
import retrofit2.http.*
import vip.qsos.app_chat.data.entity.ChatGroup
import vip.qsos.app_chat.data.entity.ChatMessage

/**
 * @author : 华清松
 * 聊天消息数据网络接口
 * @see vip.qsos.app_chat.data.model.ChatModel.ISession
 */
interface ApiChatSession {
    companion object {
        const val GROUP = "/api/im/group"
    }

    @POST(value = "$GROUP/create")
    fun createSession(
            @Header(value = "userId") meId: Long = BaseConfig.userId,
            @Query("name") name: String,
            @Query("creator") creator: String,
            @Query("members") memberList: List<String>
    ): Call<BaseResponse<ChatGroup>>

    @GET(value = "$GROUP/info.single")
    fun findSingle(
            @Header(value = "userId") meId: Long = BaseConfig.userId,
            @Query("sender") sender: String,
            @Query("receiver") receiver: String
    ): Call<BaseResponse<ChatGroup>>

    @POST(value = "$GROUP/create")
    fun create(
            @Header(value = "userId") meId: Long = BaseConfig.userId,
            @Body form: FormCreateSession
    ): Call<BaseResponse<ChatGroup>>

    @GET(value = "$GROUP/info.id")
    fun getSessionById(
            @Header(value = "userId") meId: Long = BaseConfig.userId,
            @Query(value = "groupId") groupId: Long
    ): Call<BaseResponse<ChatGroup>>

    @GET(value = "$GROUP/getSessionListByUserId")
    fun getSessionListByUserId(
            @Header(value = "userId") meId: Long = BaseConfig.userId,
            @Query(value = "userId") userId: Int
    ): Call<BaseResponse<List<ChatGroup>>>

    @POST(value = "$GROUP/addUserListToSession")
    fun addUserListToSession(
            @Header(value = "userId") meId: Long = BaseConfig.userId,
            @Query(value = "id") sessionId: Long,
            @Query(value = "userIdList") userIdList: List<Long>
    ): Call<BaseResponse<ChatGroup>>

    @DELETE(value = "$GROUP/deleteSession")
    fun deleteSession(
            @Header(value = "userId") meId: Long = BaseConfig.userId,
            @Query(value = "id") sessionId: Long
    ): Call<BaseResponse<Boolean>>

    data class FormCreateSession(
            val accountList: List<String>,
            val message: ChatMessage? = null
    )
}