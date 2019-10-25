package qsos.base.chat.view.holder

import android.view.View
import kotlinx.android.synthetic.main.item_message_items.view.*
import kotlinx.android.synthetic.main.item_message_text.view.*
import qsos.base.chat.data.entity.ChatSession
import qsos.base.chat.data.entity.MChatMessage
import qsos.base.chat.data.entity.MChatMessageText

/**
 * @author : 华清松
 * 消息内容-文本布局
 */
class ItemChatMessageTextViewHolder(session: ChatSession, view: View) : ItemChatMessageBaseViewHolder(session, view) {
    override fun setContent(contentView: View, data: MChatMessage, position: Int, chatMessageItemListener: IChatMessageItemListener?) {
        contentView.item_message_view_text.visibility = View.VISIBLE
        val content = data.content as MChatMessageText
        contentView.item_message_text.text = content.content
    }
}