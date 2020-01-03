package vip.qsos.app_chat.view.adapter

import android.view.View
import com.alibaba.android.arouter.launcher.ARouter
import vip.qsos.app_chat.data.entity.ChatGroupInfo
import qsos.lib.base.base.adapter.BaseAdapter
import qsos.lib.base.base.holder.BaseHolder
import vip.qsos.app_chat.R
import vip.qsos.app_chat.view.holder.ItemChatGroupViewHolder

/**
 * @author : 华清松
 * 聊天群列表
 */
class ChatGroupAdapter(list: ArrayList<ChatGroupInfo>) : BaseAdapter<ChatGroupInfo>(list) {

    override fun getHolder(view: View, viewType: Int): BaseHolder<ChatGroupInfo> {
        return ItemChatGroupViewHolder(view, this)
    }

    override fun getLayoutId(viewType: Int): Int {
        return R.layout.item_chat_group
    }

    override fun onItemClick(view: View, position: Int, obj: Any?) {
        ARouter.getInstance().build("/CHAT/SESSION")
                .withLong("/CHAT/GROUP_ID", data[position].groupId)
                .navigation()
    }

    override fun onItemLongClick(view: View, position: Int, obj: Any?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}