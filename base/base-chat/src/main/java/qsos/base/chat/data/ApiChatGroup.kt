package qsos.base.chat.data

import qsos.base.chat.data.entity.ChatGroup
import qsos.base.core.config.BaseConfig
import qsos.lib.netservice.data.BaseResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * @author : 华清松
 * 聊天消息数据网络接口
 * @see qsos.base.chat.data.model.IChatModel.IGroup
 */
interface ApiChatGroup {

    companion object {
        const val GROUP = "/chat/group"
    }

    @POST(value = "$GROUP/getGroupById")
    fun getGroupById(
            @Header(value = "userId") meId: Int = BaseConfig.userId,
            @Query(value = "groupId") groupId: Int
    ): Call<BaseResponse<ChatGroup>>

    @GET(value = "$GROUP/getGroupWithMe")
    fun getGroupWithMe(
            @Header(value = "userId") meId: Int = BaseConfig.userId
    ): Call<BaseResponse<List<ChatGroup>>>

    @POST(value = "$GROUP/getGroupByBySessionId")
    fun getGroupByBySessionId(
            @Header(value = "userId") meId: Int = BaseConfig.userId,
            @Query(value = "sessionId") sessionId: Int
    ): Call<BaseResponse<ChatGroup>>

    @POST(value = "$GROUP/updateGroupNotice")
    fun updateGroupNotice(
            @Header(value = "userId") meId: Int = BaseConfig.userId,
            @Query(value = "notice") notice: String
    ): Call<BaseResponse<Boolean>>

    @POST(value = "$GROUP/updateGroupName")
    fun updateGroupName(
            @Header(value = "userId") meId: Int = BaseConfig.userId,
            @Query(value = "name") name: String
    ): Call<BaseResponse<Boolean>>
}