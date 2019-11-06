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
import qsos.lib.base.callback.OnListItemClickListener

/**
 * @author : 华清松
 * 消息内容-基础布局
 *
 * @param session 消息会话数据
 * @param view 消息布局
 */
abstract class ItemChatMessageBaseViewHolder(
        private val session: ChatSession, view: View
) : BaseHolder<MChatMessage>(view) {

    enum class UpdateType(val str: String) {
        UPLOAD_STATE("上传状态"),
        READ_STATE("读取状态"),
        SEND_STATE("发送状态");
    }

    private var mItemListener: OnListItemClickListener? = null

    /**设置消息列表项点击监听*/
    fun setOnListItemClickListener(itemListener: OnListItemClickListener?): ItemChatMessageBaseViewHolder {
        this.mItemListener = itemListener
        return this
    }

    /**更新消息状态*/
    fun updateState(position: Int, data: MChatMessage, type: UpdateType) {
        val contentView = itemView.getTag(R.id.item_message_time) as View
        when (type) {
            UpdateType.UPLOAD_STATE -> {
                /**更新消息文件上传状态*/
                if (this is ItemChatMessageBaseFileViewHolder) {
                    // 注意这行代码，置 null 后让程序重新解析 json 数据
                    data.content = null
                    this.updateFileState(contentView, data, position)
                }
            }
            else -> {

            }
        }
    }

    /**展示消息内容数据*/
    abstract fun setContent(contentView: View, data: MChatMessage, position: Int, itemListener: OnListItemClickListener?)

    override fun setData(data: MChatMessage, position: Int) {

        itemView.item_message_time.text = data.createTime

        if (BaseConfig.userId == data.user.userId) {
            itemView.findViewById<View>(R.id.item_message_left).visibility = View.GONE
            itemView.findViewById<View>(R.id.item_message_right)
        } else {
            itemView.findViewById<View>(R.id.item_message_right).visibility = View.GONE
            itemView.findViewById<View>(R.id.item_message_left)
        }.apply {
            if (itemView.getTag(R.id.item_message_time) == null) {
                itemView.setTag(R.id.item_message_time, this)
            }

            visibility = View.VISIBLE

            findViewById<TextView>(R.id.item_message_user_name).text = data.user.userName

            findViewById<ImageView>(R.id.item_message_state).visibility =
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
                mItemListener?.onItemClick(it, position, data)
            }

            ImageLoaderUtils.display(
                    context,
                    findViewById(R.id.item_message_user_avatar),
                    data.user.avatar
            )

            findViewById<ImageView>(R.id.item_message_user_avatar).setOnClickListener {
                mItemListener?.onItemClick(it, position, data)
            }

            findViewById<View>(R.id.item_message_content).setOnLongClickListener {
                mItemListener?.onItemLongClick(it, position, data)
                return@setOnLongClickListener true
            }

            setContent(this, data, position, mItemListener)
        }
    }
}