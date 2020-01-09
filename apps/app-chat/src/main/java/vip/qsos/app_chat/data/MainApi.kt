package vip.qsos.app_chat.data

import qsos.lib.netservice.data.BaseResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import vip.qsos.app_chat.data.entity.ChatSessionBo
import vip.qsos.app_chat.data.entity.ChatUser

/**
 * @author : 华清松
 * 主页接口
 */
interface MainApi {

    /**获取会话列表*/
    @GET(value = "/api/app/main/list.session")
    fun getSessionList(
            @Query("userId") userId: Long
    ): Call<BaseResponse<List<ChatSessionBo>>>

    /**获取好友列表*/
    @GET(value = "/api/app/main/list.friend")
    fun getFriendList(
            @Query("userId") userId: Long
    ): Call<BaseResponse<List<ChatUser>>>

}