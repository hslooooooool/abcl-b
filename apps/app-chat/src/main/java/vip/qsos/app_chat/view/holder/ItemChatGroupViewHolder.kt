package vip.qsos.app_chat.view.holder

import android.view.View
import kotlinx.android.synthetic.main.item_chat_group.view.*
import vip.qsos.app_chat.data.entity.ChatGroupInfo
import qsos.core.lib.utils.image.ImageLoaderUtils
import qsos.lib.base.base.holder.BaseHolder
import qsos.lib.base.callback.OnListItemClickListener
import qsos.lib.base.utils.DateUtils

/**
 * @author : 华清松
 * 聊天群列表项布局
 */
class ItemChatGroupViewHolder(view: View, private val mClickListener: OnListItemClickListener) : BaseHolder<ChatGroupInfo>(view) {

    override fun setData(data: ChatGroupInfo, position: Int) {
        itemView.apply {
            data.run {
                ImageLoaderUtils.display(context, item_chat_group_avatar, avatar)

                item_chat_group_name.text = name
                lastMessage?.createTime?.let {
                    item_chat_group_last_send_time.text = DateUtils.getTimeToNow(
                            DateUtils.strToDate(it)
                    )
                }

                item_chat_group_desc.text = lastMessage?.message?.content?.getContentDesc() ?: ""

                setOnClickListener {
                    mClickListener.onItemClick(it, position, this)
                }
            }
        }
    }
}