package qsos.base.chat.data.entity

import com.google.gson.Gson
import qsos.base.chat.ChatMessageViewConfig
import qsos.base.chat.service.IMessageService
import qsos.lib.base.utils.LogUtil
import java.util.*

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

    override var messageId: Int = -1
        get() {
            field = message.messageId
            return field
        }

    override var sessionId: Int = -1
        get() {
            field = message.sessionId
            return field
        }

    override var timeline: Int = -1
        get() {
            field = message.timeline
            if (field == -1) {
                field = UUID.randomUUID().hashCode()
                LogUtil.e("消息时序错误messageId=${message.messageId}")
            }
            return field
        }

    override val sendUserId: Int
        get() = user.userId

    override val sendUserName: String
        get() = user.userName

    override val sendUserAvatar: String
        get() = user.avatar ?: ""

    override val content: ChatContent
        get() = message.content

    /**消息发送状态,本地存储*/
    override var sendStatus: EnumChatSendStatus? = null
        get() = if (field == null) EnumChatSendStatus.SUCCESS else field

    override var readStatus: Boolean? = null
        get() = if (field == null) true else field

    override var readNum: Int = 1

    override fun <T> getRealContent(): T? {
        val contentType: Int = message.content.getContentType()
        return if (contentType == -1) null else {
            val gson = Gson()
            val json = gson.toJson(message.content.fields)
            val type = ChatMessageViewConfig.getContentType(contentType)
            try {
                gson.fromJson(json, type) as T?
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
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