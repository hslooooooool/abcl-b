package qsos.base.chat.view.holder

import android.view.View
import kotlinx.android.synthetic.main.item_message_card.view.*
import kotlinx.android.synthetic.main.item_message_items.view.*
import qsos.base.chat.data.entity.MChatMessageCard
import qsos.base.chat.api.MessageViewHelper
import qsos.core.lib.utils.image.ImageLoaderUtils
import qsos.lib.base.callback.OnListItemClickListener

/**
 * @author : 华清松
 * 消息内容-名片布局
 */
class ItemChatMessageCardViewHolder(session: MessageViewHelper.Session, view: View) : ItemChatMessageBaseViewHolder(session, view) {

    override fun setContent(contentView: View, data: MessageViewHelper.Message, position: Int, itemListener: OnListItemClickListener?) {
        contentView.apply {
            item_message_view_card.visibility = View.VISIBLE
            data.getRealContent<MChatMessageCard>()?.let {
                ImageLoaderUtils.display(itemView.context, item_message_card_avatar, it.avatar)
                item_message_card_name.text = it.name
                item_message_card_desc.text = it.desc
            }
        }
    }
}