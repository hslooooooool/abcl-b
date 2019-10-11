package qsos.base.chat.view.holder

import android.view.View
import kotlinx.android.synthetic.main.item_message_text.view.*
import qsos.base.chat.data.entity.MChatMessage
import qsos.base.chat.data.entity.MChatMessageText
import qsos.lib.base.base.holder.BaseHolder

/**
 * @author : 华清松
 * 消息内容-文本布局
 */
class ItemChatMessageTextViewHolder(view: View) : BaseHolder<MChatMessage>(view) {
    override fun setData(data: MChatMessage, position: Int) {
        val content = data.content as MChatMessageText
        itemView.item_message_text.text = content.content
    }
}