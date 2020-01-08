package vip.qsos.app_chat.data.entity

import com.google.gson.Gson
import qsos.base.chat.data.entity.EnumSessionType
import java.io.Serializable

/**
 * @author : 华清松
 * TODO 类说明，描述此类的类型和用途
 */
data class ChatMessageExtra constructor(
        var sessionType: EnumSessionType,
        var belongId: String
) : Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }

    override fun toString(): String {
        return Gson().toJson(this)
    }
}