package vip.qsos.app_chat.data

import qsos.lib.netservice.data.BaseResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import vip.qsos.app_chat.data.entity.ChatUser

/**
 * @author : 华清松
 * 聊天消息数据网络接口
 * @see vip.qsos.app_chat.data.model.ChatUserModel
 */
interface ApiChatUser {
    companion object {
        const val GROUP = "/api/app/user"
    }

    @GET(value = "$GROUP/info")
    fun getUserById(
            @Query(value = "userId") userId: Long
    ): Call<BaseResponse<ChatUser>>

    @GET(value = "$GROUP/list")
    fun getAllUser(): Call<BaseResponse<List<ChatUser>>>

}