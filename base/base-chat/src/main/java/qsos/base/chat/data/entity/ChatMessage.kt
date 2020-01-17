package qsos.base.chat.data.entity

import com.google.gson.Gson
import qsos.base.chat.ChatMessageViewConfig
import qsos.base.chat.api.MessageViewHelper
import qsos.base.core.config.BaseConfig
import qsos.lib.base.utils.DateUtils
import java.util.*

/**
 * @author : 华清松
 * 消息实体
 */
class ChatMessage(
        override var messageId: String,
        override var sessionId: String,
        override var timeline: Long,
        override val sendUserAccount: String,
        override val sendUserName: String,
        override val sendUserAvatar: String,
        override var createTime: String,
        override var extra: String,
        override var content: ChatContent,
        override var sendStatus: EnumChatSendStatus?,
        override var readStatus: Boolean?,
        override var readNum: Int
) : MessageViewHelper.Message {

    override fun updateSendState(messageId: String, timeline: Long, sendStatus: EnumChatSendStatus, readNum: Int, readState: Boolean?) {
        this.messageId = messageId
        this.timeline = timeline
        this.sendStatus = sendStatus
        this.readNum = readNum
        this.readStatus = readState
    }

    override fun <T> getRealContent(): T? {
        val contentType: Int = this.content.type
        return if (contentType == -1) null else {
            val gson = Gson()
            val json = this.content.data
            val type = ChatMessageViewConfig.getContentType(contentType)
            try {
                gson.fromJson(json, type) as T?
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    companion object {
        /**创建用于发送的消息实体*/
        fun createSendMessage(sessionId: String, content: ChatContent, extra: String): ChatMessage {
            return ChatMessage(
                    messageId = UUID.randomUUID().hashCode().toString(),
                    sessionId = sessionId,
                    timeline = -1L,
                    sendUserAccount = BaseConfig.getLoginUser().imAccount,
                    sendUserName = BaseConfig.getLoginUser().name,
                    sendUserAvatar = BaseConfig.getLoginUser().avatar ?: "",
                    createTime = DateUtils.format(date = Date()),
                    extra = extra,
                    content = content,
                    sendStatus = EnumChatSendStatus.SENDING,
                    readStatus = true,
                    readNum = 1
            )
        }

        /**获取真实消息内容对象*/
        fun getRealContent(content: ChatContent): Any? {
            val contentType: Int = content.type
            return if (contentType == -1) null else {
                val gson = Gson()
                val json = content.data
                val type = ChatMessageViewConfig.getContentType(contentType)
                try {
                    gson.fromJson<Any>(json, type)
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
        }

        /**获取真实消息内容摘要*/
        fun getRealContentDesc(content: ChatContent): String {
            return when (val data = getRealContent(content)) {
                is MChatMessageText -> data.content

                is MChatMessageImage -> data.name
                is MChatMessageVideo -> data.name
                is MChatMessageAudio -> data.name
                is MChatMessageFile -> data.name

                is MChatMessageLink -> data.name
                is MChatMessageCard -> data.name
                is MChatMessageLocation -> data.name
                else -> ""
            }
        }

        /**获取真实消息内容摘要*/
        fun getRealContentDesc(json: String): String {
            val data = ChatContent.json(json)
            return getRealContentDesc(data)
        }
    }
}