package qsos.base.chat.data

import qsos.base.chat.data.entity.ChatMessage
import qsos.base.core.config.BaseConfig
import qsos.lib.netservice.data.BaseResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

/**
 * @author : 华清松
 * 聊天消息数据网络接口
 */
interface ApiChatMessage {

    /**获取会话下的消息列表
     * @param sessionId 会话ID
     * @return 会话下的消息列表
     * */
    @GET(value = "/chat/message/getMessageListBySessionId")
    fun getMessageListBySessionId(
            @Query(value = "sessionId") sessionId: Int
    ): Call<BaseResponse<List<ChatMessage>>>

    /**获取用户发送的消息列表
     * @param userId 用户ID
     * @return 用户发送的消息列表
     * */
    @GET(value = "/chat/message/getMessageListByUserId")
    fun getMessageListByUserId(
            @Header(value = "userId") userId: Int = BaseConfig.userId
    ): Call<BaseResponse<List<ChatMessage>>>

}