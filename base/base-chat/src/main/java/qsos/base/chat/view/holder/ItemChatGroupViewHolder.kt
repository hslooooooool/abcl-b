package qsos.base.chat.view.holder

import android.view.View
import kotlinx.android.synthetic.main.item_chat_group.view.*
import qsos.base.chat.data.entity.ChatGroup
import qsos.core.lib.utils.image.ImageLoaderUtils
import qsos.lib.base.base.holder.BaseHolder
import qsos.lib.base.callback.OnListItemClickListener
import qsos.lib.base.utils.DateUtils
import java.util.*

/**
 * @author : 华清松
 * 聊天群列表项布局
 */
class ItemChatGroupViewHolder(view: View, private val mClickListener: OnListItemClickListener) : BaseHolder<ChatGroup>(view) {

    override fun setData(data: ChatGroup, position: Int) {

        ImageLoaderUtils.display(itemView.context, itemView.item_chat_group_avatar, data.avatar)

        itemView.item_chat_group_name.text = data.name
        itemView.item_chat_group_last_send_time.text = getTimeToNow(data.lastMessage?.createTime)
        itemView.item_chat_group_desc.text = data.lastMessage?.message?.content?.getContentDesc()
                ?: ""

        itemView.setOnClickListener {
            mClickListener.onItemClick(it, position, data)
        }
    }

    private fun getTimeToNow(createTime: Long?): String {
        val time: Date? = createTime?.let {
            try {
                Date(it)
            } catch (e: Exception) {
                null
            }
        }
        return DateUtils.getTimeToNow(time)
    }
}