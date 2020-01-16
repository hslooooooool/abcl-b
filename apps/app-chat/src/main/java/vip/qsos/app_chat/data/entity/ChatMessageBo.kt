package vip.qsos.app_chat.data.entity

import qsos.base.chat.data.entity.ChatContent
import qsos.base.chat.data.entity.ChatMessage
import qsos.base.chat.data.entity.EnumChatSendStatus
import qsos.lib.base.utils.DateUtils
import vip.qsos.im.lib.model.Message

/**
 * @author : 华清松
 * @param user 消息发送人
 * @param message 消息对象
 */
data class ChatMessageBo(
        var user: AppUserBo,
        var message: Message
) {
    fun decode(): ChatMessage {
        val extra = MessageExtra.json(message.extra!!)
        return ChatMessage(
                messageId = message.id.toString(),
                sessionId = extra.sessionId.toString(),
                timeline = extra.timeline,
                sendUserAccount = user.imAccount,
                sendUserName = user.name,
                sendUserAvatar = user.avatar ?: "",
                createTime = DateUtils.format(millis = message.timestamp, date = null),
                extra = message.extra!!,
                content = ChatContent.json(message.content!!),
                sendStatus = EnumChatSendStatus.SUCCESS,
                readStatus = false,
                readNum = extra.readNum
        )
    }

    companion object {
        fun decode(message: Message): ChatMessage {
            val extra = MessageExtra.json(message.extra!!)
            return ChatMessage(
                    messageId = message.id.toString(),
                    sessionId = extra.sessionId.toString(),
                    timeline = extra.timeline,
                    sendUserAccount = extra.sender?.imAccount ?: "",
                    sendUserName = extra.sender?.name ?: "未知用户",
                    sendUserAvatar = extra.sender?.avatar ?: "",
                    createTime = DateUtils.format(millis = message.timestamp, date = null),
                    extra = message.extra!!,
                    content = ChatContent.json(message.content!!),
                    sendStatus = EnumChatSendStatus.SUCCESS,
                    readStatus = false,
                    readNum = extra.readNum
            )
        }
    }
}