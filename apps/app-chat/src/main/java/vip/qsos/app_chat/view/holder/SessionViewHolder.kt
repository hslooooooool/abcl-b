package vip.qsos.app_chat.view.holder

import android.view.View
import kotlinx.android.synthetic.main.item_chat_group.view.*
import qsos.core.lib.utils.image.ImageLoaderUtils
import qsos.lib.base.base.holder.BaseHolder
import qsos.lib.base.callback.OnListItemClickListener
import vip.qsos.app_chat.data.entity.ChatSessionBo

/**
 * @author : 华清松
 * 会话列表项布局
 */
class SessionViewHolder(
        view: View,
        private val mClickListener: OnListItemClickListener
) : BaseHolder<ChatSessionBo>(view) {

    override fun setData(data: ChatSessionBo, position: Int) {
        itemView.apply {
            data.run {
                ImageLoaderUtils.display(context, item_chat_group_avatar, avatar)
                item_chat_group_name.text = title
                item_chat_group_desc.text = desc
                setOnClickListener {
                    mClickListener.onItemClick(it, position, this)
                }
            }
        }
    }
}