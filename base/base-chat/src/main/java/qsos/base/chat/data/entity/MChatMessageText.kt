package qsos.base.chat.data.entity

import qsos.base.chat.R

/**
 * @author : 华清松
 * 消息内容-文本消息
 * @param content 文本内容
 */
data class MChatMessageText(
        val content: String
) : IChatMessage {

    override val contentType: Int = 0

    override val layoutId: Int = R.layout.item_message_text
}