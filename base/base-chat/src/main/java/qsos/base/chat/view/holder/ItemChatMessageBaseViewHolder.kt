package qsos.base.chat.view.holder

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import kotlinx.android.synthetic.main.item_message.view.*
import qsos.base.chat.R
import qsos.base.chat.data.entity.ChatType
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

    /**更新数据*/
    data class Update(
            /**更新类型
             * 1 更新发送状态
             * 2 更新读取状态
             * */
            val type: Int,
            /**更新数据
             * 1 发送状态
             * 2 读取状态
             * */
            val data: Any
    )

    private var mItemListener: OnListItemClickListener? = null

    /**设置消息列表项点击监听*/
    fun setOnListItemClickListener(itemListener: OnListItemClickListener?): ItemChatMessageBaseViewHolder {
        this.mItemListener = itemListener
        return this
    }

    /**展示消息内容数据*/
    abstract fun setContent(contentView: View, data: IMessageService.Message, position: Int, itemListener: OnListItemClickListener?)

    override fun setData(data: IMessageService.Message, position: Int) {
        getContentView(data, position).apply {
            ImageLoaderUtils.display(
                    context,
                    this.findViewById(R.id.item_message_user_avatar),
                    data.sendUserAvatar
            )
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
            this.findViewById<ImageView>(R.id.item_message_user_avatar).setOnClickListener {
                mItemListener?.onItemClick(it, position, data)
            }
            this.findViewById<LinearLayout>(R.id.item_message_content).setOnLongClickListener {
                mItemListener?.onItemLongClick(it, position, data)
                return@setOnLongClickListener true
            }

            /**设置消息发送状态*/
            updateSendStatus(this, data.sendStatus)

            /**设置消息内容数据*/
            setContent(this, data, position, mItemListener)
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
        if (itemView.getTag(R.id.item_message) == null) {
            val stateLiveData = MutableLiveData<EnumChatSendStatus>()
            stateLiveData.value = data.sendStatus
            itemView.setTag(R.id.item_message_state, stateLiveData)
        }
        if (contentView.getTag(R.id.item_message_state) == null) {
            contentView.setTag(R.id.item_message_state, contentView.findViewById(R.id.item_message_state))
        }
        if (contentView.getTag(R.id.item_message_progress) == null) {
            contentView.setTag(R.id.item_message_progress, contentView.findViewById(R.id.item_message_progress))
        }

        itemView.item_message_cancel_reedit.setOnClickListener {
            mItemListener?.onItemClick(it, position, data)
        }
        return contentView
    }

    /**更新消息状态*/
    fun updateState(update: Update) {
        itemView.getTag(R.id.item_message)?.let { contentView ->
            when (update.type) {
                1 -> {
                    updateSendStatus(contentView as View, update.data as EnumChatSendStatus)
                }
                2 -> {
                    updateReadStatus(contentView as View, update.data as Int)
                }
                else -> {

                }
            }
        }
    }

    /**更新消息发送状态*/
    private fun updateSendStatus(contentView: View, state: EnumChatSendStatus?) {

        itemView.item_message_cancel.visibility = View.GONE
        itemView.item_message_main.visibility = View.VISIBLE
        val messageStateView = contentView.getTag(R.id.item_message_state) as View
        val messageProgressView = contentView.getTag(R.id.item_message_progress) as View
        when (state) {
            EnumChatSendStatus.FAILED -> {
                messageStateView.visibility = View.VISIBLE
                messageProgressView.visibility = View.INVISIBLE
            }
            EnumChatSendStatus.SENDING -> {
                messageStateView.visibility = View.INVISIBLE
                messageProgressView.visibility = View.VISIBLE
            }
            EnumChatSendStatus.SUCCESS -> {
                messageStateView.visibility = View.INVISIBLE
                messageProgressView.visibility = View.INVISIBLE
            }
            EnumChatSendStatus.CANCEL_CAN, EnumChatSendStatus.CANCEL_OK -> {
                messageStateView.visibility = View.INVISIBLE
                messageProgressView.visibility = View.INVISIBLE

                itemView.item_message_cancel.visibility = View.VISIBLE
                itemView.item_message_main.visibility = View.GONE
                itemView.item_message_cancel_reedit.visibility =
                        if (state == EnumChatSendStatus.CANCEL_CAN) {
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
    private fun updateReadStatus(contentView: View, readNum: Int) {
        contentView.findViewById<TextView>(R.id.item_message_read_state).text = when (session.sessionType) {
            ChatType.GROUP.key -> {
                if (readNum < 1) "" else "${readNum}人已读"
            }
            ChatType.SINGLE.key -> {
                if (readNum == 0) "未读" else "已读"
            }
            else -> ""
        }
    }
}