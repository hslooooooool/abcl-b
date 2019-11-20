package qsos.base.chat.view.holder

import android.view.View
import kotlinx.android.synthetic.main.item_message_card.view.*
import kotlinx.android.synthetic.main.item_message_items.view.*
import qsos.base.chat.data.entity.ChatSession
import qsos.base.chat.data.entity.ChatMessageBo
import qsos.base.chat.data.entity.MChatMessageCard
import qsos.base.chat.service.IMessageService
import qsos.core.lib.utils.image.ImageLoaderUtils
import qsos.lib.base.callback.OnListItemClickListener

/**
 * @author : 华清松
 * 消息内容-名片布局
 */
class ItemChatMessageCardViewHolder(session: ChatSession, view: View) : ItemChatMessageBaseViewHolder(session, view) {

    override fun setContent(contentView: View, data: IMessageService.Message, position: Int, itemListener: OnListItemClickListener?) {
        contentView.apply {

            item_message_view_card.visibility = View.VISIBLE
            val content = data.realContent as MChatMessageCard

            ImageLoaderUtils.display(itemView.context, item_message_card_avatar, content.avatar)

            item_message_card_name.text = content.name
            item_message_card_desc.text = content.desc
        }
    }
}