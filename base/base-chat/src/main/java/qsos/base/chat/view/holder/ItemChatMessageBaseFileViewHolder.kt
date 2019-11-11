package qsos.base.chat.view.holder

import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import qsos.base.chat.R
import qsos.base.chat.data.entity.ChatSession
import qsos.base.chat.data.entity.MBaseChatMessageFile
import qsos.base.chat.data.entity.MChatMessage
import qsos.lib.base.callback.OnListItemClickListener

/**
 * @author : 华清松
 * 消息内容-文件消息布局
 *
 * @param session 消息会话数据
 * @param view 消息布局
 */
abstract class ItemChatMessageBaseFileViewHolder(
        private val session: ChatSession, view: View
) : ItemChatMessageBaseViewHolder(session, view) {

    /**更新消息文件上传状态*/
    abstract fun updateFileState(contentView: View, data: MChatMessage, position: Int)

    override fun setContent(contentView: View, data: MChatMessage, position: Int, itemListener: OnListItemClickListener?) {
        contentView.apply {
            val mMessageState = findViewById<ImageView>(R.id.item_message_state)
            val mMessageProgressBar = findViewById<ProgressBar>(R.id.item_message_progress)
            if (data.content is MBaseChatMessageFile) {
                val file = data.content as MBaseChatMessageFile
                when (file.uploadState) {
                    MBaseChatMessageFile.UpLoadState.SUCCESS -> {
                        mMessageState.visibility = View.INVISIBLE
                        mMessageProgressBar.visibility = View.INVISIBLE
                    }
                    MBaseChatMessageFile.UpLoadState.LOADING -> {
                        mMessageState.visibility = View.INVISIBLE
                        mMessageProgressBar.visibility = View.VISIBLE
                    }
                    MBaseChatMessageFile.UpLoadState.FAILED -> {
                        mMessageState.visibility = View.VISIBLE
                        mMessageProgressBar.visibility = View.INVISIBLE
                    }
                }
            }
        }
    }
}