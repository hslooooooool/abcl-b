package qsos.base.chat.view.adapter

import android.view.View
import qsos.base.chat.ChatMessageHelper
import qsos.base.chat.R
import qsos.base.chat.data.entity.ChatSession
import qsos.base.chat.data.entity.MChatMessage
import qsos.base.chat.view.holder.ItemChatMessageBaseViewHolder
import qsos.lib.base.base.adapter.BaseAdapter
import qsos.lib.base.base.holder.BaseHolder
import qsos.lib.base.callback.OnListItemClickListener

/**
 * @author : 华清松
 * 聊天消息列表
 */
class ChatMessageAdapter(
        val session: ChatSession, list: ArrayList<MChatMessage>,
        val itemListener: OnListItemClickListener? = null
) : BaseAdapter<MChatMessage>(list) {

    override fun onBindViewHolder(holder: BaseHolder<MChatMessage>, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            holder as ItemChatMessageBaseViewHolder
            payloads.forEach {
                /**更新消息状态*/
                if (it is ItemChatMessageBaseViewHolder.UpdateType) {
                    holder.updateState(position, data[position], it)
                }
            }
        }
    }

    override fun getLayoutId(viewType: Int): Int = R.layout.item_message

    override fun getHolder(view: View, viewType: Int): BaseHolder<MChatMessage> {
        return ChatMessageHelper.getHolder(session, view, viewType)
                .setOnListItemClickListener(itemListener)
    }

    override fun getItemViewType(position: Int): Int {
        return data[position].contentType
    }

    override fun onItemClick(view: View, position: Int, obj: Any?) {}

    override fun onItemLongClick(view: View, position: Int, obj: Any?) {}
}