package qsos.base.chat.view.holder

import android.view.View
import kotlinx.android.synthetic.main.item_message_items.view.*
import kotlinx.android.synthetic.main.item_message_link.view.*
import qsos.base.chat.data.entity.MChatMessageLink
import qsos.base.chat.api.MessageViewHelper
import qsos.lib.base.callback.OnListItemClickListener

/**
 * @author : 华清松
 * 消息内容-链接布局
 */
class ItemChatMessageLinkViewHolder(session: MessageViewHelper.Session, view: View) : ItemChatMessageBaseViewHolder(session, view) {
    override fun setContent(contentView: View, data: MessageViewHelper.Message, position: Int, itemListener: OnListItemClickListener?) {
        contentView.apply {
            item_message_view_link.visibility = View.VISIBLE
            data.getRealContent<MChatMessageLink>()?.let {
                item_message_link_name.text = it.name
                item_message_link_desc.text = it.desc
                item_message_link_url.text = it.url
            }
        }
    }
}