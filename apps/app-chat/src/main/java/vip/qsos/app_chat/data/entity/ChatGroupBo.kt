package vip.qsos.app_chat.data.entity

import com.google.gson.Gson
import qsos.base.chat.data.entity.ChatContent

/**
 * @author : 华清松
 * 群聊业务实体
 * @param sessionId 会话ID
 * @param groupId 群聊ID
 * @param name 群名称
 * @param avatar 群封面
 * @param lastMessage 最后一条消息
 */
data class ChatGroupBo(
        var sessionId: Long,
        var groupId: Long,
        var name: String,
        var avatar: String? = null,
        var lastMessage: LastMessage? = null
) {

    /**
     * @param senderName 发送人名称
     * @param messageId 消息ID
     * @param timestamp 发送时间
     * @param content 消息内容
     * */
    data class LastMessage(
            var senderName: String? = null,
            var messageId: Long? = null,
            var timeline: Long? = null,
            var timestamp: String,
            var content: String
    ) {
        fun getContent(): ChatContent? {
            return try {
                ChatContent(Gson().fromJson<HashMap<String, Any?>>(content, HashMap::class.java))
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

}