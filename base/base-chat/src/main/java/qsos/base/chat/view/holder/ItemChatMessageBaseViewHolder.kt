package qsos.base.chat.view.holder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import qsos.base.chat.R
import qsos.base.chat.data.entity.MChatMessage
import qsos.base.core.config.BaseConfig
import qsos.core.lib.utils.image.ImageLoaderUtils
import qsos.lib.base.base.holder.BaseHolder

/**
 * @author : 华清松
 * 消息内容-基础布局
 */
abstract class ItemChatMessageBaseViewHolder(view: View) : BaseHolder<MChatMessage>(view) {
    abstract fun setContent(contentView: View, data: MChatMessage, position: Int)
    override fun setData(data: MChatMessage, position: Int) {
        val messageView = if (BaseConfig.userId == data.user.userId) {
            itemView.findViewById<View>(R.id.item_message_left).visibility = View.GONE
            itemView.findViewById<View>(R.id.item_message_right)
        } else {
            itemView.findViewById<View>(R.id.item_message_right).visibility = View.GONE
            itemView.findViewById<View>(R.id.item_message_left)
        }
        messageView.visibility = View.VISIBLE

        messageView.findViewById<TextView>(R.id.item_message_user_name)
                .text = data.user.userName

        ImageLoaderUtils.display(
                messageView.context,
                messageView.findViewById(R.id.item_message_user_avatar),
                data.user.avatar
        )
        messageView.findViewById<ImageView>(R.id.item_message_user_avatar)
                .setOnClickListener {

                }

        setContent(messageView, data, position)
    }
}