package qsos.base.chat.view.holder

import android.annotation.SuppressLint
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.item_message.view.*
import qsos.base.chat.R
import qsos.base.chat.api.MessageViewHelper
import qsos.base.chat.data.entity.EnumChatSendStatus
import qsos.base.chat.data.entity.EnumSessionType
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
@SuppressLint("SetTextI18n")
abstract class ItemChatMessageBaseViewHolder(
        private val session: MessageViewHelper.Session, view: View
) : BaseHolder<MessageViewHelper.Message>(view) {

    private var mItemListener: OnListItemClickListener? = null

    /**设置消息列表项点击监听*/
    fun setOnListItemClickListener(itemListener: OnListItemClickListener?): ItemChatMessageBaseViewHolder {
        this.mItemListener = itemListener
        return this
    }

    /**展示消息内容数据*/
    abstract fun setContent(contentView: View, data: MessageViewHelper.Message, position: Int, itemListener: OnListItemClickListener?)

    override fun setData(data: MessageViewHelper.Message, position: Int) {
        getContentView(data, position).apply {
            ImageLoaderUtils.displayRounded(
                    context,
                    this.findViewById(R.id.item_message_user_avatar),
                    data.sendUserAvatar
            )
            this.findViewById<TextView>(R.id.item_message_user_name).text = data.sendUserName
            this.findViewById<TextView>(R.id.item_message_read_state).setOnClickListener {
                mItemListener?.onItemClick(it, position, data)
            }
            this.findViewById<ImageView>(R.id.item_message_user_avatar).setOnClickListener {
                mItemListener?.onItemClick(it, position, data)
            }
            this.findViewById<LinearLayout>(R.id.item_message_content).setOnLongClickListener {
                mItemListener?.onItemLongClick(it, position, data)
                return@setOnLongClickListener true
            }

            updateSendStatus(this, position, data)
            updateReadStatus(this, data)
            setContent(this, data, position, mItemListener)
        }
    }

    /**判断获取具体内容视图*/
    private fun getContentView(data: MessageViewHelper.Message, position: Int): View {
        if (TextUtils.isEmpty(data.createTime)) {
            itemView.item_message_time.visibility = View.GONE
        } else {
            itemView.item_message_time.visibility = View.VISIBLE
            itemView.item_message_time.text = data.createTime
        }
        val contentView: View
        if (BaseConfig.getLoginUser().imAccount == data.sendUserAccount) {
            itemView.findViewById<View>(R.id.item_message_left).visibility = View.GONE
            contentView = itemView.findViewById<View>(R.id.item_message_right)
            contentView.findViewById<TextView>(R.id.item_message_read_state).visibility = View.VISIBLE
        } else {
            itemView.findViewById<View>(R.id.item_message_right).visibility = View.GONE
            contentView = itemView.findViewById<View>(R.id.item_message_left)
            contentView.findViewById<TextView>(R.id.item_message_read_state).visibility =
                    when (session.type) {
                        EnumSessionType.GROUP.key -> {
                            View.VISIBLE
                        }
                        else -> {
                            View.INVISIBLE
                        }
                    }
        }

        contentView.visibility = View.VISIBLE

        if (itemView.getTag(R.id.item_message) == null) {
            itemView.setTag(R.id.item_message, contentView)
        }
        if (itemView.getTag(R.id.tag_of_chat_item_data) == null) {
            itemView.setTag(R.id.tag_of_chat_item_data, data)
        }
        if (contentView.getTag(R.id.item_message_state) == null) {
            contentView.setTag(R.id.item_message_state, contentView.findViewById(R.id.item_message_state))
        }
        if (contentView.getTag(R.id.item_message_progress) == null) {
            contentView.setTag(R.id.item_message_progress, contentView.findViewById(R.id.item_message_progress))
        }
        if (contentView.getTag(R.id.item_message_read_state) == null) {
            contentView.setTag(R.id.item_message_read_state, contentView.findViewById(R.id.item_message_read_state))
        }

        itemView.item_message_cancel_reedit.setOnClickListener {
            mItemListener?.onItemClick(it, position, data)
        }
        return contentView
    }

    /**更新消息发送状态*/
    private fun updateSendStatus(contentView: View, position: Int, data: MessageViewHelper.Message) {
        itemView.item_message_cancel.visibility = View.GONE
        itemView.item_message_main.visibility = View.VISIBLE

        val messageStateView = contentView.getTag(R.id.item_message_state) as View
        val messageProgressView = contentView.getTag(R.id.item_message_progress) as View
        val messageReadView = contentView.getTag(R.id.item_message_read_state) as View

        messageStateView.setOnClickListener {
            mItemListener?.onItemClick(it, position, data)
        }
        when (data.sendStatus) {
            EnumChatSendStatus.FAILED -> {
                messageStateView.visibility = View.VISIBLE
                messageProgressView.visibility = View.INVISIBLE
                messageReadView.visibility = View.INVISIBLE
            }
            EnumChatSendStatus.SENDING -> {
                messageStateView.visibility = View.INVISIBLE
                messageProgressView.visibility = View.VISIBLE
                messageReadView.visibility = View.INVISIBLE
            }
            EnumChatSendStatus.SUCCESS -> {
                messageStateView.visibility = View.INVISIBLE
                messageProgressView.visibility = View.INVISIBLE
                messageReadView.visibility = View.VISIBLE
            }
            EnumChatSendStatus.CANCEL_CAN, EnumChatSendStatus.CANCEL_OK -> {
                messageStateView.visibility = View.INVISIBLE
                messageProgressView.visibility = View.INVISIBLE
                messageReadView.visibility = View.VISIBLE

                itemView.item_message_cancel.visibility = View.VISIBLE
                itemView.item_message_main.visibility = View.GONE
                itemView.item_message_cancel_reedit.visibility =
                        if (data.sendStatus == EnumChatSendStatus.CANCEL_CAN) {
                            View.VISIBLE
                        } else {
                            View.GONE
                        }
            }
            else -> {
            }
        }
    }

    /**更新消息读取状态*/
    private fun updateReadStatus(contentView: View, data: MessageViewHelper.Message) {
        contentView.findViewById<TextView>(R.id.item_message_read_state).text =
                when (session.type) {
                    EnumSessionType.SINGLE.key -> {
                        if (data.readNum < 2) "未读" else "已读"
                    }
                    else -> {
                        "${data.readNum}人已读"
                    }
                }
    }
}