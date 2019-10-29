package qsos.base.chat.view.holder

import android.view.View
import kotlinx.android.synthetic.main.item_chat_friend.view.*
import qsos.base.chat.data.entity.ChatUser
import qsos.core.lib.utils.image.ImageLoaderUtils
import qsos.lib.base.base.holder.BaseHolder
import qsos.lib.base.callback.OnListItemClickListener

/**
 * @author : 华清松
 * 聊天好友列表项布局
 */
class ItemChatFriendViewHolder(
        view: View,
        private val mClickListener: OnListItemClickListener
) : BaseHolder<ChatUser>(view) {

    override fun setData(data: ChatUser, position: Int) {

        ImageLoaderUtils.display(itemView.context, itemView.item_chat_friend_avatar, data.avatar)

        itemView.item_chat_friend_name.text = data.userName

        itemView.setOnClickListener {
            mClickListener.onItemClick(it, position, data)
        }
    }
}