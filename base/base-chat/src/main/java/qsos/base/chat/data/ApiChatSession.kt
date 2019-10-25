package qsos.base.chat.data

import qsos.base.chat.data.entity.ChatSession
import qsos.lib.netservice.data.BaseResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * @author : 华清松
 * 聊天消息数据网络接口
 */
interface ApiChatSession {

    /**获取会话数据
     * @param sessionId 会话ID
     * @return 会话数据
     * */
    @GET(value = "/chat/session")
    fun getSessionById(
            @Query(value = "sessionId") sessionId: Int
    ): Call<BaseResponse<ChatSession>>

}