package qsos.base.chat.data.entity

import com.google.gson.Gson
import qsos.base.chat.DefChatMessageViewConfig
import qsos.base.chat.service.IMessageService

/**
 * @author : 华清松
 * 聊天消息列表项展示数据
 * @param user 消息发送用户
 * @param message 消息数据
 * @param createTime 创建时间
 */
data class ChatMessageBo(
        var user: ChatUser,
        override var createTime: String,
        var message: ChatMessage
) : IMessageService.Message {

    override var messageId: Int
        get() = message.messageId
        set(value) {}
    override var sessionId: Int
        get() = message.sessionId
        set(value) {}
    override var timeline: Int = message.sequence
        get() {
            if (field == -1) {
                field = message.content.hashCode()
            }
            return field
        }
        set(value) {}
    override var sendUserId: Int
        get() = user.userId
        set(value) {}
    override var sendUserName: String
        get() = user.userName
        set(value) {}
    override var sendUserAvatar: String
        get() = user.avatar ?: ""
        set(value) {}
    override var content: ChatContent
        get() = message.content
        set(value) {}

    /**消息发送状态,本地存储*/
    override var sendStatus: EnumChatSendStatus = EnumChatSendStatus.SUCCESS
    /**消息读取人数,单聊时1即为已读，群聊时代表读取人数，本地存储*/
    override var readNum: Int = 0
    /**消息内容类型*/
    var contentType: Int = -1
        get() {
            field = when (val type = message.content.getContentType()) {
                is Number -> type.toInt()
                else -> -1
            }
            return field
        }

    /**消息内容实体*/
    override var realContent: Any? = null
        get() {
            if (contentType == -1) field = "" else {
                if (field == null) {
                    val gson = Gson()
                    field = try {
                        val json = gson.toJson(message.content.fields)
                        val type = DefChatMessageViewConfig.getContentType(contentType)
                        gson.fromJson(json, type)
                    } catch (e: Exception) {
                        e.printStackTrace()

                        ""
                    }
                }
            }
            return field
        }

    /**消息唯一判定值*/
    var hashCode: Int? = null
        get() {
            if (field == null) {
                field = this.hashCode()
            }
            return field
        }

}

/**
 * @author : 华清松
 * 消息内容接口,自定义消息实体需实现此接口
 */
interface IChatMessageType {
    /**消息摘要*/
    var contentDesc: String
    /**消息内容类型值
     * @see EnumChatMessageType*/
    var contentType: Int
}