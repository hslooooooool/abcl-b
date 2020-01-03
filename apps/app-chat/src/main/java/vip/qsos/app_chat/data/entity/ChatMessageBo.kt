package vip.qsos.app_chat.data.entity

import com.google.gson.Gson
import qsos.base.chat.ChatMessageViewConfig
import qsos.base.chat.data.entity.ChatContent
import qsos.base.chat.data.entity.EnumChatSendStatus
import qsos.base.chat.api.IMessageListService
import qsos.lib.base.utils.LogUtil

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
) : IMessageListService.Message {

    override var messageId: Int = -1
        get() {
            field = message.messageId
            return field
        }
        set(value) {
            message.messageId = value
        }

    override var sessionId: Long = -1L
        get() {
            field = message.sessionId
            return field
        }
        set(value) {
            message.sessionId = value
        }

    override var timeline: Int = -1
        get() {
            field = message.timeline
            if (field == -1) {
                LogUtil.e("消息时序错误messageId=${message.messageId}")
            }
            return field
        }
        set(value) {
            message.timeline = value
        }

    override val sendUserId: Long
        get() = user.userId

    override val sendUserName: String
        get() = user.name

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

    override fun updateSendState(messageId: Int, timeline: Int, sendStatus: EnumChatSendStatus,
                                 readNum: Int, readState: Boolean?) {
        this.message.messageId = messageId
        this.message.timeline = timeline
        this.sendStatus = sendStatus
        this.readNum = readNum
        this.readStatus = readStatus
    }

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