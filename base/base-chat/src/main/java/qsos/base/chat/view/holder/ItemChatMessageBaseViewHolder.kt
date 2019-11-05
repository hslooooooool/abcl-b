package qsos.base.chat.view.holder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.item_message.view.*
import qsos.base.chat.R
import qsos.base.chat.data.entity.ChatSession
import qsos.base.chat.data.entity.ChatType
import qsos.base.chat.data.entity.MChatMessage
import qsos.base.chat.data.entity.MChatSendStatus
import qsos.base.core.config.BaseConfig
import qsos.core.lib.utils.image.ImageLoaderUtils
import qsos.lib.base.base.holder.BaseHolder

/**
 * @author : 华清松
 * 消息内容-基础布局
 *
 * @param session 消息会话数据
 * @param view 消息布局
 */
abstract class ItemChatMessageBaseViewHolder(private val session: ChatSession, view: View) : BaseHolder<MChatMessage>(view) {

    private var mIChatMessageItemListener: IChatMessageItemListener? = null

    interface IChatMessageItemListener {
        /**点击触发
         * @param position 点击位置
         * @param data 点击项数据
         * @param view 点击项View
         * @param longClick 是否长按，默认 false
         * */
        fun onClick(position: Int, data: MChatMessage, view: View, longClick: Boolean = false)
    }

    /**设置消息列表项点击监听*/
    fun setChatMessageItemListener(chatMessageItemListener: IChatMessageItemListener) {
        this.mIChatMessageItemListener = chatMessageItemListener
    }

    /**展示消息内容数据*/
    abstract fun setContent(contentView: View, data: MChatMessage, position: Int, chatMessageItemListener: IChatMessageItemListener?)

    override fun setData(data: MChatMessage, position: Int) {

        itemView.item_message_time.text = data.createTime

        if (BaseConfig.userId == data.user.userId) {
            itemView.findViewById<View>(R.id.item_message_left).visibility = View.GONE
            itemView.findViewById<View>(R.id.item_message_right)
        } else {
            itemView.findViewById<View>(R.id.item_message_right).visibility = View.GONE
            itemView.findViewById<View>(R.id.item_message_left)
        }.apply {

            visibility = View.VISIBLE

            findViewById<TextView>(R.id.item_message_user_name).text = data.user.userName

            findViewById<ImageView>(R.id.item_message_send_state).visibility =
                    if (data.sendStatus == MChatSendStatus.FAILED) {
                        View.VISIBLE
                    } else {
                        View.INVISIBLE
                    }

            findViewById<TextView>(R.id.item_message_read_state).text = when (session.type) {
                ChatType.GROUP -> {
                    if (data.readStatus < 1) "" else "${data.readStatus}人已读"
                }
                ChatType.SINGLE -> {
                    if (data.readStatus == 0) "未读" else "已读"
                }
                else -> ""
            }

            findViewById<TextView>(R.id.item_message_read_state).setOnClickListener {
                mIChatMessageItemListener?.onClick(position, data, it)
            }

            ImageLoaderUtils.display(
                    context,
                    findViewById(R.id.item_message_user_avatar),
                    data.user.avatar
            )

            findViewById<ImageView>(R.id.item_message_user_avatar).setOnClickListener {
                mIChatMessageItemListener?.onClick(position, data, it)
            }

            findViewById<View>(R.id.item_message_content).setOnLongClickListener {
                mIChatMessageItemListener?.onClick(position, data, it, true)
                return@setOnLongClickListener true
            }

            setContent(this, data, position, mIChatMessageItemListener)
        }
    }
}