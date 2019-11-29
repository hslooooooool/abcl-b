package qsos.base.chat.view.adapter

import android.annotation.SuppressLint
import android.view.View
import qsos.base.chat.ChatMessageViewConfig
import qsos.base.chat.R
import qsos.base.chat.service.IMessageService
import qsos.base.chat.view.holder.ItemChatMessageBaseViewHolder
import qsos.lib.base.base.adapter.BaseAdapter
import qsos.lib.base.base.holder.BaseHolder
import qsos.lib.base.callback.OnListItemClickListener
import qsos.lib.base.callback.OnTListener
import qsos.lib.base.utils.LogUtil

/**
 * @author : 华清松
 * 聊天消息列表
 */
class ChatMessageAdapter(
        /**会话数据*/
        val session: IMessageService.Session,
        /**消息列表数据*/
        list: ArrayList<IMessageService.Message>,
        /**消息列表项点击监听*/
        private val onItemClickListener: OnListItemClickListener? = null,
        /**消息列表项显示监听，返回消息列表项视图位置=adapterPosition*/
        private val onItemShowedListener: OnTListener<Int>? = null
) : BaseAdapter<IMessageService.Message>(list) {

    @SuppressLint("UseSparseArrays")
    val mStateLiveDataMap = HashMap<Int, BaseHolder<*>>()

    override fun onViewAttachedToWindow(holder: BaseHolder<IMessageService.Message>) {
        super.onViewAttachedToWindow(holder)
        val position = holder.adapterPosition
        onItemShowedListener?.back(position)
        LogUtil.d("聊天列表", "${session.sessionId}显示了消息位$position")
    }

    override fun onBindViewHolder(holder: BaseHolder<IMessageService.Message>, position: Int, payloads: MutableList<Any>) {
        super.onBindViewHolder(holder, position, payloads)
        if (payloads.isEmpty()) {
            mStateLiveDataMap[data[position].timeline] = holder
        } else {
            holder as ItemChatMessageBaseViewHolder
            payloads.forEach {
                when (it) {
                    is ItemChatMessageBaseViewHolder.Update -> {
                        /**更新消息状态*/
                        holder.updateState(it)
                    }
                }
            }
        }
    }

    override fun getLayoutId(viewType: Int): Int = R.layout.item_message

    override fun getHolder(view: View, viewType: Int): BaseHolder<IMessageService.Message> {
        return ChatMessageViewConfig.getHolder(session, view, viewType)
                .setOnListItemClickListener(onItemClickListener)
    }

    override fun getItemViewType(position: Int): Int {
        return data[position].content.getContentType()
    }

    override fun onItemClick(view: View, position: Int, obj: Any?) {}

    override fun onItemLongClick(view: View, position: Int, obj: Any?) {}
}