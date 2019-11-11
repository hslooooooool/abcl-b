package qsos.base.chat.data

import qsos.base.chat.data.entity.ChatContent
import qsos.base.chat.data.entity.ChatMessage
import qsos.base.chat.data.entity.MChatMessage
import qsos.base.core.config.BaseConfig
import qsos.lib.netservice.data.BaseResponse
import retrofit2.Call
import retrofit2.http.*

/**
 * @author : 华清松
 * 聊天消息数据网络接口
 * @see qsos.base.chat.data.model.IChatModel.IMessage
 */
interface ApiChatMessage {
    companion object {
        const val GROUP = "/chat/message"
    }

    @POST(value = "$GROUP/sendMessage")
    fun sendMessage(
            @Header(value = "userId") meId: Int = BaseConfig.userId,
            @Body message: ChatMessage
    ): Call<BaseResponse<ChatMessage>>

    @GET(value = "$GROUP/getMessageById")
    fun getMessageById(
            @Header(value = "userId") meId: Int = BaseConfig.userId,
            @Query(value = "messageId") messageId: Int
    ): Call<BaseResponse<MChatMessage>>

    @GET(value = "$GROUP/getMessageListByIds")
    fun getMessageListByIds(
            @Header(value = "userId") meId: Int = BaseConfig.userId,
            @Query(value = "messageIds") messageIds: List<Int>
    ): Call<BaseResponse<List<MChatMessage>>>

    @GET(value = "$GROUP/getMessageListBySessionIdAndTimeline")
    fun getMessageListBySessionIdAndTimeline(
            @Header(value = "userId") meId: Int = BaseConfig.userId,
            @Query(value = "sessionId") sessionId: Int,
            @Query(value = "timeline") timeline: Int
    ): Call<BaseResponse<List<MChatMessage>>>

    @GET(value = "$GROUP/getMessageListByUserId")
    fun getMessageListByUserId(
            @Header(value = "userId") meId: Int = BaseConfig.userId,
            @Query(value = "userId") userId: Int
    ): Call<BaseResponse<List<MChatMessage>>>

    @GET(value = "$GROUP/getMessageListBySessionId")
    fun getMessageListBySessionId(
            @Header(value = "userId") meId: Int = BaseConfig.userId,
            @Query(value = "sessionId") sessionId: Int
    ): Call<BaseResponse<List<MChatMessage>>>

    @GET(value = "$GROUP/getContentById")
    fun getContentById(
            @Header(value = "userId") meId: Int = BaseConfig.userId,
            @Query(value = "contentId") contentId: Int
    ): Call<BaseResponse<ChatContent>>

    @DELETE(value = "$GROUP/deleteMessage")
    fun deleteMessage(
            @Header(value = "userId") meId: Int = BaseConfig.userId,
            @Query(value = "messageId") messageId: Int
    ): Call<BaseResponse<Boolean>>

}