package qsos.base.chat.view.holder

import android.annotation.SuppressLint
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import kotlinx.android.synthetic.main.item_message_file.view.*
import kotlinx.android.synthetic.main.item_message_items.view.*
import qsos.base.chat.R
import qsos.base.chat.data.entity.ChatSession
import qsos.base.chat.data.entity.MBaseChatMessageFile
import qsos.base.chat.data.entity.MChatMessageFile
import qsos.base.chat.service.IMessageService
import qsos.core.lib.utils.image.ImageLoaderUtils
import qsos.core.player.PlayerConfigHelper
import qsos.core.player.data.PreDocumentEntity
import qsos.lib.base.callback.OnListItemClickListener

/**
 * @author : 华清松
 * 消息内容-文件布局
 */
class ItemChatMessageFileViewHolder(session: ChatSession, view: View) : ItemChatMessageBaseFileViewHolder(session, view) {
    @SuppressLint("SetTextI18n")
    override fun setContent(contentView: View, data: IMessageService.Message, position: Int, itemListener: OnListItemClickListener?) {
        super.setContent(contentView, data, position, itemListener)
        contentView.apply {
            item_message_view_file.visibility = View.VISIBLE
            data.getRealContent<MChatMessageFile>()?.let {
                ImageLoaderUtils.display(itemView.context, item_message_file_avatar, it.url)
                item_message_file_name.text = it.name
                item_message_file_length.text = "${it.length} kb"
                item_message_file_avatar.setOnClickListener { _ ->
                    PlayerConfigHelper.previewDocument(
                            context = itemView.context,
                            data = PreDocumentEntity(
                                    name = it.name,
                                    desc = it.name,
                                    path = it.url
                            )
                    )
                }
            }
        }
    }

    override fun updateFileState(contentView: View, data: IMessageService.Message, position: Int) {
        contentView.apply {
            val mMessageState = findViewById<ImageView>(R.id.item_message_state)
            val mMessageProgressBar = findViewById<ProgressBar>(R.id.item_message_progress)
            data.getRealContent<MChatMessageFile>()?.let {
                when (it.uploadState) {
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