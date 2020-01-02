package vip.qsos.app_chat.view.adapter

import android.view.View
import com.alibaba.android.arouter.launcher.ARouter
import vip.qsos.app_chat.data.entity.ChatUser
import qsos.lib.base.base.adapter.BaseAdapter
import qsos.lib.base.base.holder.BaseHolder
import vip.qsos.app_chat.R
import vip.qsos.app_chat.view.holder.ItemChatFriendViewHolder

/**
 * @author : 华清松
 * 聊天好友列表
 */
class ChatFriendAdapter(list: ArrayList<ChatUser>) : BaseAdapter<ChatUser>(list) {
    override fun getHolder(view: View, viewType: Int): BaseHolder<ChatUser> {
        return ItemChatFriendViewHolder(view, this)
    }

    override fun getLayoutId(viewType: Int): Int {
        return R.layout.item_chat_friend
    }

    override fun onItemClick(view: View, position: Int, obj: Any?) {
        ARouter.getInstance().build("/CHAT/USER")
                .withLong("/CHAT/USER_ID", data[position].userId)
                .navigation()
    }

    override fun onItemLongClick(view: View, position: Int, obj: Any?) {

    }
}