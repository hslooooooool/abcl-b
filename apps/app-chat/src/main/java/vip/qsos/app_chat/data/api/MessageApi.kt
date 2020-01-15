package vip.qsos.app_chat.data.api

import qsos.lib.netservice.data.BaseResponse
import retrofit2.Call
import retrofit2.http.*
import vip.qsos.app_chat.data.entity.ChatMessageBo
import vip.qsos.app_chat.data.entity.ChatMessageReadStatusBo
import vip.qsos.app_chat.data.entity.MessageOfGroupBo

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
            //TODO 消息对象错误 MessageOfGroupBo
    ): Call<BaseResponse<MessageOfGroupBo>>

    @GET(value = "/api/app/chat/single/message/list/{sessionId}/{timeline}")
    fun getMessageListBySessionIdAndTimeline(
            @Path(value = "sessionId") sessionId: Long,
            @Path(value = "timeline") timeline: Long = -1L,
            @Query(value = "next") next: Boolean = true,
            @Query(value = "size") size: Int = 20
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