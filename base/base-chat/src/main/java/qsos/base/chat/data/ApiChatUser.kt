package qsos.base.chat.data

import qsos.base.chat.data.entity.ChatUser
import qsos.base.core.config.BaseConfig
import qsos.lib.netservice.data.BaseResponse
import retrofit2.Call
import retrofit2.http.*

/**
 * @author : 华清松
 * 聊天消息数据网络接口
 * @see qsos.base.chat.data.model.IChatModel.IUser
 */
interface ApiChatUser {
    companion object {
        const val GROUP = "/chat/user"
    }

    @POST(value = "$GROUP/createUser")
    fun createUser(
            @Body user: ChatUser
    ): Call<BaseResponse<ChatUser>>

    @GET(value = "$GROUP/getUserById")
    fun getUserById(
            @Header(value = "userId") meId: Int = BaseConfig.userId,
            @Query(value = "userId") userId: Int
    ): Call<BaseResponse<ChatUser>>

    @GET(value = "$GROUP/getAllUser")
    fun getAllUser(
            @Header(value = "userId") meId: Int = BaseConfig.userId
    ): Call<BaseResponse<List<ChatUser>>>

    @GET(value = "$GROUP/getUserListBySessionId")
    fun getUserListBySessionId(
            @Header(value = "userId") meId: Int = BaseConfig.userId,
            @Query(value = "sessionId") sessionId: Int
    ): Call<BaseResponse<List<ChatUser>>>

    @DELETE(value = "$GROUP/deleteUser")
    fun deleteUser(
            @Header(value = "userId") meId: Int = BaseConfig.userId,
            @Query(value = "sessionId") sessionId: Int,
            @Query(value = "userId") userId: Int
    ): Call<BaseResponse<Boolean>>

}