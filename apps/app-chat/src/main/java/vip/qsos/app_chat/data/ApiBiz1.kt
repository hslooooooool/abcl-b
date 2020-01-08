package vip.qsos.app_chat.data

import qsos.lib.netservice.data.BaseResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import vip.qsos.app_chat.data.entity.ChatGroupBo
import vip.qsos.app_chat.data.entity.ChatUser

/**
 * @author : 华清松
 * 主界面接口
 */
interface ApiBiz1 {

    companion object {
        const val GROUP = "/api/app/biz1"
    }

    @GET(value = "$GROUP/list.session")
    fun getSessionList(
            @Query("userId") userId: Long
    ): Call<BaseResponse<List<ChatGroupBo>>>

    @GET(value = "$GROUP/list.friend")
    fun getFriendList(
            @Query("userId") userId: Long
    ): Call<BaseResponse<List<ChatUser>>>

}