package vip.qsos.app_chat.data

import vip.qsos.app_chat.data.entity.ChatMessage
import vip.qsos.app_chat.data.entity.ChatMessageBo
import vip.qsos.app_chat.data.entity.ChatMessageReadStatusBo
import qsos.base.core.config.BaseConfig
import qsos.lib.netservice.data.BaseResponse
import retrofit2.Call
import retrofit2.http.*

/**
 * @author : 华清松
 * 聊天消息数据网络接口
 * @see vip.qsos.app_chat.data.model.ChatModel.IMessage
 */
interface ApiChatMessage {
    companion object {
        const val GROUP = "/chat/message"
    }

    @POST(value = "$GROUP/sendMessage")
    fun sendMessage(
            @Header(value = "userId") meId: Long = BaseConfig.userId,
            @Body message: ChatMessage
    ): Call<BaseResponse<ChatMessage>>

    @GET(value = "$GROUP/getMessageListBySessionIdAndTimeline")
    fun getMessageListBySessionIdAndTimeline(
            @Header(value = "userId") meId: Long = BaseConfig.userId,
            @Query(value = "id") sessionId: Long,
            @Query(value = "timeline") timeline: Int = -1,
            @Query(value = "next") next: Boolean = true,
            @Query(value = "size") size: Int = 20
    ): Call<BaseResponse<List<ChatMessageBo>>>

    @GET(value = "$GROUP/getMessageListBySessionId")
    fun getMessageListBySessionId(
            @Header(value = "userId") meId: Long = BaseConfig.userId,
            @Query(value = "id") sessionId: Long
    ): Call<BaseResponse<List<ChatMessageBo>>>

    @POST(value = "$GROUP/readMessage")
    fun readMessage(
            @Header(value = "userId") meId: Long = BaseConfig.userId,
            @Query(value = "messageId") messageId: Int
    ): Call<BaseResponse<ChatMessageReadStatusBo>>

    @DELETE(value = "$GROUP/deleteMessage")
    fun deleteMessage(
            @Header(value = "userId") meId: Long = BaseConfig.userId,
            @Query(value = "messageId") messageId: Int
    ): Call<BaseResponse<Boolean>>

    @GET(value = "$GROUP/getMessageListByIds")
    fun getMessageListByIds(
            @Header(value = "userId") meId: Long = BaseConfig.userId,
            @Query(value = "messageIds") messageIds: List<Int>
    ): Call<BaseResponse<List<ChatMessageBo>>>

    @GET(value = "$GROUP/getMessageById")
    fun getMessageById(
            @Header(value = "userId") meId: Long = BaseConfig.userId,
            @Query(value = "messageId") messageId: Int
    ): Call<BaseResponse<ChatMessageBo>>

}