package vip.qsos.app_chat.view.adapter

import android.view.View
import com.alibaba.android.arouter.launcher.ARouter
import com.google.gson.Gson
import qsos.lib.base.base.adapter.BaseAdapter
import qsos.lib.base.base.holder.BaseHolder
import vip.qsos.app_chat.R
import vip.qsos.app_chat.data.entity.ChatSessionBo
import vip.qsos.app_chat.view.holder.SessionViewHolder

/**
 * @author : 华清松
 * 会话列表
 */
class ChatSessionAdapter(list: ArrayList<ChatSessionBo>) : BaseAdapter<ChatSessionBo>(list) {

    override fun getHolder(view: View, viewType: Int): BaseHolder<ChatSessionBo> {
        return SessionViewHolder(view, this)
    }

    override fun getLayoutId(viewType: Int): Int {
        return R.layout.item_chat_group
    }

    override fun onItemClick(view: View, position: Int, obj: Any?) {
        ARouter.getInstance().build("/CHAT/SESSION")
                .withString("/CHAT/SESSION_JSON", Gson().toJson(data[position]))
                .navigation()
    }

    override fun onItemLongClick(view: View, position: Int, obj: Any?) {}

}