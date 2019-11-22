package qsos.base.chat.view.adapter

import android.view.View
import qsos.base.chat.DefChatMessageViewConfig
import qsos.base.chat.R
import qsos.base.chat.service.IMessageService
import qsos.base.chat.view.holder.ItemChatMessageBaseViewHolder
import qsos.lib.base.base.adapter.BaseAdapter
import qsos.lib.base.base.holder.BaseHolder
import qsos.lib.base.callback.OnListItemClickListener

/**
 * @author : 华清松
 * 聊天消息列表
 */
class ChatMessageAdapter(
        val session: IMessageService.Session, list: ArrayList<IMessageService.Message>,
        val itemListener: OnListItemClickListener? = null
) : BaseAdapter<IMessageService.Message>(list) {

    private val mMessageMap: HashMap<Int, View> = HashMap()

    override fun onBindViewHolder(holder: BaseHolder<IMessageService.Message>, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            holder as ItemChatMessageBaseViewHolder
            payloads.forEach {
                /**更新消息状态*/
                if (it is ItemChatMessageBaseViewHolder.UpdateType) {
                    mMessageMap[data[position].timeline]?.let { v ->
                        holder.updateState(v, data[position], it)
                    }
                }
            }
        }
    }

    override fun onBindViewHolder(holder: BaseHolder<IMessageService.Message>, position: Int) {
        super.onBindViewHolder(holder, position)
        mMessageMap[data[position].timeline] = holder.itemView
    }

    override fun getLayoutId(viewType: Int): Int = R.layout.item_message

    override fun getHolder(view: View, viewType: Int): BaseHolder<IMessageService.Message> {
        return DefChatMessageViewConfig.getHolder(session, view, viewType)
                .setOnListItemClickListener(itemListener)
    }

    override fun getItemViewType(position: Int): Int {
        return data[position].content.getContentType()
    }

    override fun onItemClick(view: View, position: Int, obj: Any?) {}

    override fun onItemLongClick(view: View, position: Int, obj: Any?) {}
}