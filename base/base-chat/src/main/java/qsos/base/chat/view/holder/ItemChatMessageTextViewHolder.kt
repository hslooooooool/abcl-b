package qsos.base.chat.view.holder

import android.text.method.LinkMovementMethod
import android.view.View
import androidx.core.text.HtmlCompat
import kotlinx.android.synthetic.main.item_message_items.view.*
import kotlinx.android.synthetic.main.item_message_text.view.*
import qsos.base.chat.data.entity.MChatMessageText
import qsos.base.chat.service.IMessageListService
import qsos.lib.base.callback.OnListItemClickListener

/**
 * @author : 华清松
 * 消息内容-文本布局
 */
class ItemChatMessageTextViewHolder(session: IMessageListService.Session, view: View) : ItemChatMessageBaseViewHolder(session, view) {
    override fun setContent(contentView: View, data: IMessageListService.Message, position: Int, itemListener: OnListItemClickListener?) {
        contentView.apply {
            item_message_view_text.visibility = View.VISIBLE
            data.getRealContent<MChatMessageText>()?.let {
                item_message_text.text = ""
                item_message_text.append(HtmlCompat.fromHtml(it.content, HtmlCompat.FROM_HTML_MODE_LEGACY))
                item_message_text.movementMethod = LinkMovementMethod.getInstance()
            }
            item_message_text.setOnClickListener {
                itemListener?.onItemClick(it, position, data)
            }
            item_message_text.setOnLongClickListener {
                itemListener?.onItemLongClick(it, position, data)
                true
            }
        }
    }
}