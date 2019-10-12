package qsos.base.chat.view.holder

import android.view.View
import kotlinx.android.synthetic.main.item_message_image.view.*
import kotlinx.android.synthetic.main.item_message_items.view.*
import qsos.base.chat.data.entity.MChatMessage
import qsos.base.chat.data.entity.MChatMessageImage
import qsos.core.lib.utils.image.ImageLoaderUtils

/**
 * @author : 华清松
 * 消息内容-图片布局
 */
class ItemChatMessageImageViewHolder(view: View) : ItemChatMessageBaseViewHolder(view) {
    override fun setContent(contentView: View, data: MChatMessage, position: Int) {
        contentView.item_message_view_image.visibility = View.VISIBLE
        val content = data.content as MChatMessageImage

        ImageLoaderUtils.display(itemView.context, contentView.item_message_image, content.url)
    }
}