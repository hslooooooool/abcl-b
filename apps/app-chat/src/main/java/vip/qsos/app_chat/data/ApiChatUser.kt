package vip.qsos.app_chat.data

import qsos.base.core.config.BaseConfig
import qsos.lib.netservice.data.BaseResponse
import retrofit2.Call
import retrofit2.http.*
import vip.qsos.app_chat.data.entity.ChatFriend
import vip.qsos.app_chat.data.entity.ChatUser

/**
 * @author : 华清松
 * 聊天消息数据网络接口
 * @see vip.qsos.app_chat.data.model.ChatModel.IUser
 */
interface ApiChatUser {
    companion object {
        const val GROUP = "/api/app/user"
    }

    @POST(value = "$GROUP/createUser")
    fun createUser(
            @Body user: ChatUser
    ): Call<BaseResponse<ChatUser>>

    @GET(value = "$GROUP/user.id")
    fun getUserById(
            @Header(value = "userId") meId: Long = BaseConfig.userId,
            @Query(value = "userId") userId: Long
    ): Call<BaseResponse<ChatUser>>

    @POST("$GROUP/friend.add")
    fun addFriend(
            @Header(value = "userId") meId: Long = BaseConfig.userId,
            @Query(value = "userId") userId: Long,
            @Query(value = "friendId") friendId: Long
    ): Call<BaseResponse<ChatFriend>>

    @GET("$GROUP/friend")
    fun findFriend(
            @Header(value = "userId") meId: Long = BaseConfig.userId,
            @Query(value = "userId") userId: Long,
            @Query(value = "friendId") friendId: Long
    ): Call<BaseResponse<ChatFriend>>

    @GET(value = "$GROUP/list")
    fun getAllUser(
            @Header(value = "userId") meId: Long = BaseConfig.userId
    ): Call<BaseResponse<List<ChatUser>>>

    @GET(value = "$GROUP/getUserListBySessionId")
    fun getUserListBySessionId(
            @Header(value = "userId") meId: Long = BaseConfig.userId,
            @Query(value = "id") sessionId: Int
    ): Call<BaseResponse<List<ChatUser>>>

    @DELETE(value = "$GROUP/deleteUser")
    fun deleteUser(
            @Header(value = "userId") meId: Long = BaseConfig.userId,
            @Query(value = "id") sessionId: Int,
            @Query(value = "userId") userId: Int
    ): Call<BaseResponse<Boolean>>

}