package vip.qsos.app_chat.data

import qsos.lib.netservice.data.BaseResponse
import retrofit2.Call
import retrofit2.http.*
import vip.qsos.app_chat.data.entity.ChatMessage
import vip.qsos.app_chat.data.entity.ChatMessageBo
import vip.qsos.app_chat.data.entity.ChatMessageReadStatusBo

/**
 * @author : 华清松
 * 单聊页接口
 */
interface MessageApi {

    companion object {
        const val GROUP = "/api/app/chat/single"
    }

    @POST(value = "$GROUP/message/send")
    fun sendMessage(
            @Body message: ChatMessage
    ): Call<BaseResponse<ChatMessage>>

    @GET(value = "$GROUP/message/list/{sessionId}/{timeline}")
    fun getMessageListBySessionIdAndTimeline(
            @Path(value = "sessionId") sessionId: Long,
            @Path(value = "timeline") timeline: Long = -1L,
            @Query(value = "next") next: Boolean = true,
            @Query(value = "size") size: Int = 20
    ): Call<BaseResponse<List<ChatMessageBo>>>

    @POST(value = "$GROUP/message/read")
    fun readMessage(
            @Query(value = "messageId") messageId: Long
    ): Call<BaseResponse<ChatMessageReadStatusBo>>

    @DELETE(value = "$GROUP/message")
    fun deleteMessage(
            @Query(value = "messageId") messageId: Long
    ): Call<BaseResponse<Boolean>>

    @GET(value = "$GROUP/message/list")
    fun getMessageListByIds(
            @Query(value = "messageIds") messageIds: List<Long>
    ): Call<BaseResponse<List<ChatMessageBo>>>

}