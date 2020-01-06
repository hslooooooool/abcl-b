package vip.qsos.app_chat.data

import vip.qsos.app_chat.data.entity.ChatGroupInfo
import qsos.base.core.config.BaseConfig
import qsos.lib.netservice.data.BaseResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

/**
 * @author : 华清松
 * 聊天消息数据网络接口
 * @see vip.qsos.app_chat.data.model.ChatModel.IGroup
 */
interface ApiChatGroup {

    companion object {
        const val GROUP = "/api/im/group"
    }

    @GET(value = "$GROUP/info.id")
    fun getGroupById(
            @Header(value = "userId") meId: Long = BaseConfig.userId,
            @Query(value = "groupId") groupId: String
    ): Call<BaseResponse<ChatGroupInfo>>

    @GET(value = "$GROUP/list")
    fun getGroupWithMe(
            @Header(value = "userId") meId: Long = BaseConfig.userId
    ): Call<BaseResponse<List<ChatGroupInfo>>>

}