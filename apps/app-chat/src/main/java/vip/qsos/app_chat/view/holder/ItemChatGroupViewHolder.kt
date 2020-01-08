package vip.qsos.app_chat.view.holder

import android.view.View
import kotlinx.android.synthetic.main.item_chat_group.view.*
import qsos.core.lib.utils.image.ImageLoaderUtils
import qsos.lib.base.base.holder.BaseHolder
import qsos.lib.base.callback.OnListItemClickListener
import qsos.lib.base.utils.DateUtils
import vip.qsos.app_chat.data.entity.ChatGroupBo

/**
 * @author : 华清松
 * 聊天群列表项布局
 */
class ItemChatGroupViewHolder(view: View, private val mClickListener: OnListItemClickListener) : BaseHolder<ChatGroupBo>(view) {

    override fun setData(data: ChatGroupBo, position: Int) {
        itemView.apply {
            data.run {
                ImageLoaderUtils.display(context, item_chat_group_avatar, avatar)

                item_chat_group_name.text = name
                lastMessage?.timestamp?.let {
                    item_chat_group_last_send_time.text = DateUtils.getTimeToNow(
                            DateUtils.strToDate(it)
                    )
                }

                item_chat_group_desc.text = lastMessage?.getContent()?.getContentDesc()

                setOnClickListener {
                    mClickListener.onItemClick(it, position, this)
                }
            }
        }
    }
}