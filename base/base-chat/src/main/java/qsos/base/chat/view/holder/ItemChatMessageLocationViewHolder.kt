package qsos.base.chat.view.holder

import android.annotation.SuppressLint
import android.view.View
import kotlinx.android.synthetic.main.item_message_items.view.*
import kotlinx.android.synthetic.main.item_message_location.view.*
import qsos.base.chat.data.entity.ChatSession
import qsos.base.chat.data.entity.MChatMessage
import qsos.base.chat.data.entity.MChatMessageLocation
import qsos.core.lib.utils.image.ImageLoaderUtils

/**
 * @author : 华清松
 * 消息内容-位置布局
 */
class ItemChatMessageLocationViewHolder(session: ChatSession, view: View) : ItemChatMessageBaseViewHolder(session, view) {
    @SuppressLint("SetTextI18n")
    override fun setContent(contentView: View, data: MChatMessage, position: Int, chatMessageItemListener: IChatMessageItemListener?) {
        contentView.item_message_view_location.visibility = View.VISIBLE
        val content = data.content as MChatMessageLocation

        ImageLoaderUtils.display(contentView.context, contentView.item_message_location_avatar, content.avatar)

        contentView.item_message_location_name.text = content.name
        contentView.item_message_location_desc.text = content.lat + "," + content.lng
    }
}