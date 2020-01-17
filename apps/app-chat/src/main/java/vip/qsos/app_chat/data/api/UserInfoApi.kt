package vip.qsos.app_chat.data.api

import qsos.lib.netservice.data.BaseResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import vip.qsos.app_chat.data.entity.ChatFriendBo
import vip.qsos.app_chat.data.entity.ChatSessionBo
import vip.qsos.app_chat.data.entity.AppUserBo

/**
 * @author : 华清松
 * 用户资料页接口
 */
interface UserInfoApi {

    @GET(value = "/api/app/user/info")
    fun getUserById(
            @Query(value = "userId") userId: Long
    ): Call<BaseResponse<AppUserBo>>

    @POST("/api/app/user/friend.put")
    fun addFriend(
            @Query(value = "userId") userId: Long,
            @Query(value = "friendId") friendId: Long
    ): Call<BaseResponse<ChatFriendBo>>

    @GET("/api/app/user/friend.info")
    fun findFriend(
            @Query(value = "userId") userId: Long,
            @Query(value = "friendId") friendId: Long
    ): Call<BaseResponse<ChatFriendBo>>

    @GET("/api/app/user/session.single")
    fun getSessionOfSingle(
            @Query(value = "sender") sender: String,
            @Query(value = "receiver") receiver: String
    ): Call<BaseResponse<ChatSessionBo>>

}