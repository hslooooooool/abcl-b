package vip.qsos.app_chat.view.holder

import android.view.View
import kotlinx.android.synthetic.main.item_chat_friend.view.*
import vip.qsos.app_chat.data.entity.ChatUser
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
        itemView.apply {

            ImageLoaderUtils.display(context, item_chat_friend_avatar, data.avatar)

            item_chat_friend_name.text = data.name

            setOnClickListener {
                mClickListener.onItemClick(it, position, data)
            }
        }
    }
}