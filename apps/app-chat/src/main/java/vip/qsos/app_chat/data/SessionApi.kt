package vip.qsos.app_chat.data

import qsos.lib.netservice.data.BaseResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import vip.qsos.app_chat.data.entity.ChatGroupBo
import vip.qsos.app_chat.data.entity.ChatSessionBo

/**
 * @author : 华清松
 * 会话页接口
 */
interface SessionApi {

    companion object {
        const val SESSION = "/api/app/session"
        const val GROUP = "/api/app/group"
    }

    @POST(value = "$SESSION/create")
    fun createSession(
            @Query("name") name: String,
            @Query("creator") creator: String,
            @Query("members") memberList: List<String>
    ): Call<BaseResponse<ChatSessionBo>>

    @GET(value = "$SESSION/info.single")
    fun findSessionOfSingle(
            @Query("sender") sender: String,
            @Query("receiver") receiver: String
    ): Call<BaseResponse<ChatSessionBo>>

    @GET(value = "$SESSION/info.id")
    fun getSessionById(
            @Query(value = "sessionId") sessionId: Long
    ): Call<BaseResponse<ChatSessionBo>>

    @POST(value = "$SESSION/addUserListToSession")
    fun addUserListToSession(
            @Query(value = "id") sessionId: Long,
            @Query(value = "userIdList") userIdList: List<Long>
    ): Call<BaseResponse<ChatSessionBo>>

    @GET(value = "$GROUP/info.id")
    fun getGroupById(
            @Query(value = "groupId") groupId: Long
    ): Call<BaseResponse<ChatGroupBo>>

}