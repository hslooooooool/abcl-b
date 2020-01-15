package vip.qsos.app_chat.data.entity

import com.google.gson.Gson
import qsos.base.chat.ChatMessageViewConfig
import qsos.base.chat.api.MessageViewHelper
import qsos.base.chat.data.entity.ChatContent
import qsos.base.chat.data.entity.EnumChatSendStatus
import qsos.lib.base.utils.DateUtils
import qsos.lib.base.utils.LogUtil
import vip.qsos.im.lib.model.Message

/**
 * @author : 华清松
 * 聊天消息列表项展示数据
 * @param user 消息发送用户
 * @param message 消息数据
 * @param createTime 创建时间
 */
data class ChatMessageBo constructor(
        var user: AppUserBo,
        override var createTime: String,
        var message: ChatMessage
) : MessageViewHelper.Message {

    companion object {

        fun formatMessage(user: AppUserBo, msg: Message): MessageViewHelper.Message? {
            val extra: MessageBo.MessageExtra
            val content: ChatContent
            try {
                extra = Gson().fromJson(msg.extra, MessageBo.MessageExtra::class.java)
                content = ChatContent(Gson().fromJson<HashMap<String, Any?>>(msg.content, HashMap::class.java))
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
            // TODO 丢弃Content方式
            return ChatMessageBo(
                    user = user,
                    createTime = DateUtils.format(millis = msg.timestamp, date = null),
                    message = ChatMessage(
                            sessionId = extra.sessionId,
                            messageId = msg.id,
                            content = content
                    )
            )
        }
    }

    override var messageId: String = ""
        get() {
            field = message.messageId.toString()
            return field
        }
        set(value) {
            message.messageId = value.toLong()
        }

    override var sessionId: String = ""
        get() {
            field = message.sessionId.toString()
            return field
        }
        set(value) {
            message.sessionId = value.toLong()
        }

    override var timeline: Long = -1L
        get() {
            field = message.timeline
            if (field == -1L) {
                LogUtil.e("消息时序错误messageId=${message.messageId}")
            }
            return field
        }
        set(value) {
            message.timeline = value
        }

    override val sendUserAccount: String
        get() = user.imAccount

    override val sendUserName: String
        get() = user.name

    override val sendUserAvatar: String
        get() = user.avatar ?: ""

    override val content: ChatContent
        get() = message.content

    override var sendStatus: EnumChatSendStatus? = null
        get() = if (field == null) EnumChatSendStatus.SUCCESS else field

    override var readStatus: Boolean? = null
        get() = if (field == null) true else field

    override var readNum: Int = 1

    override fun updateSendState(
            messageId: String, timeline: Long, sendStatus: EnumChatSendStatus, readNum: Int,
            readState: Boolean?
    ) {
        this.message.messageId = messageId.toLong()
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