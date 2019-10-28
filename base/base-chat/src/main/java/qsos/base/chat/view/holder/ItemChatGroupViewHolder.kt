package qsos.base.chat.view.holder

import android.view.View
import kotlinx.android.synthetic.main.item_chat_group.view.*
import qsos.base.chat.data.entity.ChatGroup
import qsos.core.lib.utils.image.ImageLoaderUtils
import qsos.lib.base.base.holder.BaseHolder

/**
 * @author : 华清松
 * 聊天群列表项布局
 */
class ItemChatGroupViewHolder(view: View) : BaseHolder<ChatGroup>(view) {

    override fun setData(data: ChatGroup, position: Int) {

        ImageLoaderUtils.display(itemView.context, itemView.item_chat_group_avatar, data.avatar)

        itemView.item_chat_group_name.text = data.name
    }
}