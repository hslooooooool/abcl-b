package qsos.base.chat.view.adapter

import android.view.View
import qsos.base.chat.ChatMessageHelper
import qsos.base.chat.data.entity.MChatMessage
import qsos.lib.base.base.adapter.BaseAdapter
import qsos.lib.base.base.holder.BaseHolder

/**
 * @author : 华清松
 * 聊天消息列表
 */
class ChatMessageAdapter(list: ArrayList<MChatMessage>) : BaseAdapter<MChatMessage>(list) {

    override fun getLayoutId(viewType: Int): Int = viewType

    override fun getHolder(view: View, viewType: Int): BaseHolder<MChatMessage> {
        return ChatMessageHelper.configHolder(view, viewType)
    }

    override fun onItemClick(view: View, position: Int, obj: Any?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onItemLongClick(view: View, position: Int, obj: Any?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}