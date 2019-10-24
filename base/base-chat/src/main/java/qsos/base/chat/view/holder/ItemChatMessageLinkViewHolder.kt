package qsos.base.chat.view.holder

import android.view.View
import kotlinx.android.synthetic.main.item_message_items.view.*
import kotlinx.android.synthetic.main.item_message_link.view.*
import qsos.base.chat.data.entity.MChatMessage
import qsos.base.chat.data.entity.MChatMessageLink

/**
 * @author : 华清松
 * 消息内容-链接布局
 */
class ItemChatMessageLinkViewHolder(view: View) : ItemChatMessageBaseViewHolder(view) {
    override fun setContent(contentView: View, data: MChatMessage, position: Int) {
        contentView.item_message_view_text.visibility = View.VISIBLE
        val content = data.content as MChatMessageLink
        contentView.item_message_link_name.text = content.name
        contentView.item_message_link_desc.text = content.desc
        contentView.item_message_link_url.text = content.url
    }
}