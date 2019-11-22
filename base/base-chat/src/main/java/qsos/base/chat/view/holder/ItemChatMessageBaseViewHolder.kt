package qsos.base.chat.view.holder

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import kotlinx.android.synthetic.main.item_message.view.*
import qsos.base.chat.R
import qsos.base.chat.data.entity.ChatType
import qsos.base.chat.data.entity.EnumChatMessageType
import qsos.base.chat.data.entity.EnumChatSendStatus
import qsos.base.chat.service.IMessageService
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
        private val session: IMessageService.Session, view: View
) : BaseHolder<IMessageService.Message>(view) {

    enum class UpdateType(val str: String) {
        READ_STATE("读取状态"),
        SEND_STATE("发送状态");
    }

    private var mItemListener: OnListItemClickListener? = null

    /**设置消息列表项点击监听*/
    fun setOnListItemClickListener(itemListener: OnListItemClickListener?): ItemChatMessageBaseViewHolder {
        this.mItemListener = itemListener
        return this
    }

    /**展示消息内容数据*/
    abstract fun setContent(contentView: View, data: IMessageService.Message, position: Int, itemListener: OnListItemClickListener?)

    /**更新消息状态*/
    fun updateState(position: Int, data: IMessageService.Message, type: UpdateType) {
        val contentView = itemView.getTag(R.id.item_message) as View?
        when (type) {
            UpdateType.SEND_STATE -> {
                contentView?.let {
                    updateSendStatus(contentView, data)
                }
            }
            else -> {

            }
        }
    }

    /**判断获取具体内容视图*/
    private fun getContentView(data: IMessageService.Message, position: Int): View {
        itemView.item_message_time.text = data.createTime
        val contentView: View
        if (BaseConfig.userId == data.sendUserId) {
            itemView.findViewById<View>(R.id.item_message_left).visibility = View.GONE
            contentView = itemView.findViewById<View>(R.id.item_message_right)
        } else {
            itemView.findViewById<View>(R.id.item_message_right).visibility = View.GONE
            contentView = itemView.findViewById<View>(R.id.item_message_left)
        }

        contentView.visibility = View.VISIBLE

        if (itemView.getTag(R.id.item_message) == null) {
            itemView.setTag(R.id.item_message, contentView)
        }
        return contentView
    }

    override fun setData(data: IMessageService.Message, position: Int) {
        getContentView(data, position).apply {

            this.findViewById<TextView>(R.id.item_message_user_name).text = data.sendUserName

            this.findViewById<TextView>(R.id.item_message_read_state).text = when (session.sessionType) {
                ChatType.GROUP.key -> {
                    if (data.readNum < 1) "" else "${data.readNum}人已读"
                }
                ChatType.SINGLE.key -> {
                    if (data.readNum == 0) "未读" else "已读"
                }
                else -> ""
            }

            this.findViewById<TextView>(R.id.item_message_read_state).setOnClickListener {
                mItemListener?.onItemClick(it, position, data)
            }

            ImageLoaderUtils.display(
                    context,
                    this.findViewById(R.id.item_message_user_avatar),
                    data.sendUserAvatar
            )

            this.findViewById<ImageView>(R.id.item_message_user_avatar).setOnClickListener {
                mItemListener?.onItemClick(it, position, data)
            }

            this.findViewById<LinearLayout>(R.id.item_message_content).setOnLongClickListener {
                mItemListener?.onItemLongClick(it, position, data)
                return@setOnLongClickListener true
            }

            updateSendStatus(this, data)

            setContent(this, data, position, mItemListener)
        }
    }

    /**更新消息发送状态*/
    private fun updateSendStatus(contentView: View, data: IMessageService.Message) {

        itemView.item_message_cancel.visibility = View.GONE
        itemView.item_message_main.visibility = View.VISIBLE

        when (data.sendStatus) {
            EnumChatSendStatus.FAILED -> {
                contentView.findViewById<ImageView>(R.id.item_message_state).visibility = View.VISIBLE
                contentView.findViewById<ProgressBar>(R.id.item_message_progress).visibility = View.INVISIBLE
            }
            EnumChatSendStatus.SENDING -> {
                contentView.findViewById<ImageView>(R.id.item_message_state).visibility = View.INVISIBLE
                contentView.findViewById<ProgressBar>(R.id.item_message_progress).visibility = View.VISIBLE
            }
            EnumChatSendStatus.SUCCESS -> {
                contentView.findViewById<ImageView>(R.id.item_message_state).visibility = View.INVISIBLE
                contentView.findViewById<ProgressBar>(R.id.item_message_progress).visibility = View.INVISIBLE
            }
            EnumChatSendStatus.CANCEL_CAN, EnumChatSendStatus.CANCEL_OK -> {
                contentView.findViewById<ImageView>(R.id.item_message_state).visibility = View.INVISIBLE
                contentView.findViewById<ProgressBar>(R.id.item_message_progress).visibility = View.INVISIBLE

                itemView.item_message_cancel.visibility = View.VISIBLE
                itemView.item_message_main.visibility = View.GONE
                itemView.item_message_cancel_reedit.visibility = if (
                        data.sendStatus == EnumChatSendStatus.CANCEL_CAN
                        && data.content.getContentType() == EnumChatMessageType.TEXT.contentType
                ) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

                itemView.item_message_cancel_reedit.setOnClickListener {
                    mItemListener?.onItemClick(it, position, data)
                }
            }
        }
    }
}