package qsos.base.chat.view.holder

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.item_message.view.*
import qsos.base.chat.R
import qsos.base.chat.data.entity.ChatSession
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
        private val session: ChatSession, view: View
) : BaseHolder<IMessageService.Message>(view) {

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
    fun updateState(position: Int, data: IMessageService.Message, type: UpdateType) {
        val contentView = itemView.getTag(R.id.item_message_time) as View
        when (type) {
            UpdateType.UPLOAD_STATE -> {
                /**更新消息文件上传状态*/
                if (this is ItemChatMessageBaseFileViewHolder) {
                    // 注意这行代码，置 null 后让程序重新解析 json 数据
                    data.realContent = null
                    this.updateFileState(contentView, data, position)
                }
            }
            else -> {

            }
        }
    }

    /**展示消息内容数据*/
    abstract fun setContent(contentView: View, data: IMessageService.Message, position: Int, itemListener: OnListItemClickListener?)

    override fun setData(data: IMessageService.Message, position: Int) {

        itemView.item_message_time.text = data.createTime

        if (BaseConfig.userId == data.sendUserId) {
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

            findViewById<TextView>(R.id.item_message_user_name).text = data.sendUserName

            findViewById<ImageView>(R.id.item_message_state).visibility =
                    if (data.sendStatus == EnumChatSendStatus.FAILED) {
                        View.VISIBLE
                    } else {
                        View.INVISIBLE
                    }

            findViewById<TextView>(R.id.item_message_read_state).text = when (session.type) {
                ChatType.GROUP -> {
                    if (data.readNum < 1) "" else "${data.readNum}人已读"
                }
                ChatType.SINGLE -> {
                    if (data.readNum == 0) "未读" else "已读"
                }
                else -> ""
            }

            findViewById<TextView>(R.id.item_message_read_state).setOnClickListener {
                mItemListener?.onItemClick(it, position, data)
            }

            ImageLoaderUtils.display(
                    context,
                    findViewById(R.id.item_message_user_avatar),
                    data.sendUserAvatar
            )

            findViewById<ImageView>(R.id.item_message_user_avatar).setOnClickListener {
                mItemListener?.onItemClick(it, position, data)
            }

            findViewById<LinearLayout>(R.id.item_message_content).setOnLongClickListener {
                mItemListener?.onItemLongClick(it, position, data)
                return@setOnLongClickListener true
            }

            if (data.sendStatus == EnumChatSendStatus.CANCEL_CAN || data.sendStatus == EnumChatSendStatus.CANCEL_OK) {
                itemView.item_message_cancel.visibility = View.VISIBLE
                itemView.item_message_main.visibility = View.GONE
                itemView.item_message_cancel_reedit.visibility =
                        if (
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
            } else {
                itemView.item_message_cancel.visibility = View.GONE
                itemView.item_message_main.visibility = View.VISIBLE

                setContent(this, data, position, mItemListener)
            }
        }
    }
}