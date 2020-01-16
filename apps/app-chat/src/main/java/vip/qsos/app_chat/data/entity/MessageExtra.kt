package vip.qsos.app_chat.data.entity

import com.google.gson.Gson
import qsos.base.chat.data.entity.EnumSessionType

/**消息附加信息
 * @param sessionType 消息类型
 * @param sessionId 会话ID
 * @param timeline 消息时序
 * */
data class MessageExtra constructor(
        var sessionType: EnumSessionType,
        var sessionId: Long,
        var timeline: Long = -1L,
        var sender: AppUserBo? = null,
        var readNum: Int = 1
) {
    override fun toString(): String {
        return Gson().toJson(this)
    }

    companion object {
        fun json(json: String): MessageExtra {
            return Gson().fromJson(json, MessageExtra::class.java)
        }
    }
}