package vip.qsos.app_chat.data

import qsos.lib.netservice.data.BaseResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import vip.qsos.app_chat.data.entity.ChatFriend

/**
 * @author : 华清松
 * 聊天消息数据网络接口
 * @see vip.qsos.app_chat.data.model.ChatUserModel
 */
interface ApiChatFriend {
    companion object {
        const val GROUP = "/api/app/friend"
    }

    @POST("$GROUP/friend.add")
    fun addChatFriend(
            @Query(value = "userId") userId: Long,
            @Query(value = "friendId") friendId: Long
    ): Call<BaseResponse<ChatFriend>>

    @GET("$GROUP/friend")
    fun findChatFriend(
            @Query(value = "userId") userId: Long,
            @Query(value = "friendId") friendId: Long
    ): Call<BaseResponse<ChatFriend>>

}