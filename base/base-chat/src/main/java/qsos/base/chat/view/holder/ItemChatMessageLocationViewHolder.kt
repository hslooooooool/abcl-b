package qsos.base.chat.view.holder

import android.annotation.SuppressLint
import android.view.View
import kotlinx.android.synthetic.main.item_message_items.view.*
import kotlinx.android.synthetic.main.item_message_location.view.*
import qsos.base.chat.data.entity.ChatSession
import qsos.base.chat.data.entity.MChatMessageLocation
import qsos.base.chat.service.IMessageService
import qsos.core.lib.utils.image.ImageLoaderUtils
import qsos.lib.base.callback.OnListItemClickListener

/**
 * @author : 华清松
 * 消息内容-位置布局
 */
class ItemChatMessageLocationViewHolder(session: IMessageService.Session, view: View) : ItemChatMessageBaseViewHolder(session, view) {
    @SuppressLint("SetTextI18n")
    override fun setContent(contentView: View, data: IMessageService.Message, position: Int, itemListener: OnListItemClickListener?) {
        contentView.apply {
            item_message_view_location.visibility = View.VISIBLE
            data.getRealContent<MChatMessageLocation>()?.let {
                ImageLoaderUtils.display(contentView.context, item_message_location_avatar, it.avatar)
                item_message_location_name.text = it.name
                item_message_location_desc.text = it.lat + "," + it.lng
            }
        }
    }
}