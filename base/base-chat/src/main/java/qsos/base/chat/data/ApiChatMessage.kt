package qsos.base.chat.data

import qsos.base.chat.data.entity.ChatMessage
import qsos.lib.netservice.data.BaseResponse
import retrofit2.Call
import retrofit2.http.GET
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
            @Query("sessionId") sessionId: Long
    ): Call<BaseResponse<List<ChatMessage>>>

    @GET(value = "/chat/test")
    fun test(): Call<BaseResponse<String>>

}