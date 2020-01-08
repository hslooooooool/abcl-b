package vip.qsos.app_chat.data

import qsos.lib.netservice.data.BaseResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import vip.qsos.app_chat.data.entity.ChatGroupBo

/**
 * @author : 华清松
 * 聊天消息数据网络接口
 * @see vip.qsos.app_chat.data.model.ChatGroupModel
 */
interface ApiChatGroup {

    companion object {
        const val GROUP = "/api/app/group"
    }

    @GET(value = "$GROUP/info.id")
    fun getSessionById(
            @Query(value = "sessionId") groupId: Long
    ): Call<BaseResponse<ChatGroupBo>>

    @GET(value = "$GROUP/list")
    fun getGroupList(): Call<BaseResponse<List<ChatGroupBo>>>

}