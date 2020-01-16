package vip.qsos.app_chat.data.api

import qsos.lib.netservice.data.BaseResponse
import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import vip.qsos.app_chat.data.entity.ChatMessageBo
import vip.qsos.app_chat.data.entity.ChatMessageReadStatusBo
import vip.qsos.app_chat.data.entity.ChatMessageSendBo

/**
 * @author : 华清松
 * 单聊页接口
 */
interface MessageApi {

    @POST(value = "/api/app/session/single/message/send")
    fun sendMessage(
            @Query("sessionId")
            sessionId: Long,
            @Query("contentType")
            contentType: Int,
            @Query("content")
            content: String,
            @Query("sender")
            sender: String
    ): Call<BaseResponse<ChatMessageSendBo>>

    @GET(value = "/api/app/session/single/message/list")
    fun getMessageListBySessionIdAndTimeline(
            @Query(value = "sessionId") sessionId: Long,
            @Query(value = "timeline") timeline: Long = 1L,
            @Query(value = "size") size: Int = 10,
            @Query(value = "previous") previous: Boolean = true
    ): Call<BaseResponse<List<ChatMessageBo>>>

    @POST(value = "/api/app/chat/single/message/read")
    fun readMessage(
            @Query(value = "messageId") messageId: Long
    ): Call<BaseResponse<ChatMessageReadStatusBo>>

    @DELETE(value = "/api/app/chat/single/message")
    fun deleteMessage(
            @Query(value = "messageId") messageId: Long
    ): Call<BaseResponse<Boolean>>

}