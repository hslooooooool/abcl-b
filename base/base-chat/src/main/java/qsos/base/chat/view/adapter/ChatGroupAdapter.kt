package qsos.base.chat.view.adapter

import android.view.View
import com.alibaba.android.arouter.launcher.ARouter
import qsos.base.chat.R
import qsos.base.chat.data.entity.ChatGroup
import qsos.base.chat.view.holder.ItemChatGroupViewHolder
import qsos.lib.base.base.adapter.BaseAdapter
import qsos.lib.base.base.holder.BaseHolder

/**
 * @author : 华清松
 * 聊天群列表
 */
class ChatGroupAdapter(list: ArrayList<ChatGroup>) : BaseAdapter<ChatGroup>(list) {
    override fun getHolder(view: View, viewType: Int): BaseHolder<ChatGroup> {
        return ItemChatGroupViewHolder(view, this)
    }

    override fun getLayoutId(viewType: Int): Int {
        return R.layout.item_chat_group
    }

    override fun onItemClick(view: View, position: Int, obj: Any?) {
        ARouter.getInstance().build("/CHAT/SESSION")
                .withInt("/CHAT/SESSION_ID", data[position].groupId)
                .navigation()
    }

    override fun onItemLongClick(view: View, position: Int, obj: Any?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}