package vip.qsos.app_chat.data

import qsos.base.core.config.BaseConfig
import qsos.lib.netservice.data.BaseResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import vip.qsos.app_chat.data.entity.ChatMessage
import vip.qsos.app_chat.data.entity.ChatSession
import vip.qsos.app_chat.data.entity.ChatSessionBo

/**
 * @author : 华清松
 * 聊天消息数据网络接口
 * @see vip.qsos.app_chat.data.model.ChatSessionModel
 */
interface ApiChatSession {
    companion object {
        const val GROUP = "/api/im/session"
    }

    @POST(value = "$GROUP/create")
    fun createSession(
            @Query("name") name: String,
            @Query("creator") creator: String,
            @Query("members") memberList: List<String>
    ): Call<BaseResponse<ChatSessionBo>>

    @GET(value = "$GROUP/info.single")
    fun findSessionOfSingle(
            @Query("sender") sender: String,
            @Query("receiver") receiver: String
    ): Call<BaseResponse<ChatSessionBo>>

    @GET(value = "$GROUP/info.id")
    fun getSessionById(
            @Query(value = "sessionId") groupId: Long
    ): Call<BaseResponse<ChatSessionBo>>

    @POST(value = "$GROUP/addUserListToSession")
    fun addUserListToSession(
            @Query(value = "id") sessionId: Long,
            @Query(value = "userIdList") userIdList: List<Long>
    ): Call<BaseResponse<ChatSession>>

    data class FormCreateSession(
            val accountList: List<String>,
            val message: ChatMessage? = null
    )

}