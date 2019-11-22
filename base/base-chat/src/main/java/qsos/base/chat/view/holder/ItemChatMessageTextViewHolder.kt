package qsos.base.chat.view.holder

import android.view.View
import kotlinx.android.synthetic.main.item_message_items.view.*
import kotlinx.android.synthetic.main.item_message_text.view.*
import qsos.base.chat.data.entity.ChatSession
import qsos.base.chat.data.entity.MChatMessageText
import qsos.base.chat.service.IMessageService
import qsos.lib.base.callback.OnListItemClickListener

/**
 * @author : 华清松
 * 消息内容-文本布局
 */
class ItemChatMessageTextViewHolder(session: IMessageService.Session, view: View) : ItemChatMessageBaseViewHolder(session, view) {
    override fun setContent(contentView: View, data: IMessageService.Message, position: Int, itemListener: OnListItemClickListener?) {
        contentView.apply {
            item_message_view_text.visibility = View.VISIBLE
            data.getRealContent<MChatMessageText>()?.let {
                item_message_text.text = it.content
            }
        }
    }
}