package qsos.base.chat.view.holder

import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import kotlinx.android.synthetic.main.item_message_image.view.*
import kotlinx.android.synthetic.main.item_message_items.view.*
import qsos.base.chat.R
import qsos.base.chat.data.entity.ChatSession
import qsos.base.chat.data.entity.MBaseChatMessageFile
import qsos.base.chat.data.entity.MChatMessageImage
import qsos.base.chat.service.IMessageService
import qsos.core.lib.utils.image.ImageLoaderUtils
import qsos.core.player.PlayerConfigHelper
import qsos.core.player.data.PreImageEntity
import qsos.lib.base.callback.OnListItemClickListener

/**
 * @author : 华清松
 * 消息内容-图片布局
 */
class ItemChatMessageImageViewHolder(session: ChatSession, view: View) : ItemChatMessageBaseFileViewHolder(session, view) {
    override fun setContent(contentView: View, data: IMessageService.Message, position: Int, itemListener: OnListItemClickListener?) {
        super.setContent(contentView, data, position, itemListener)
        contentView.apply {
            item_message_view_image.visibility = View.VISIBLE
            val content = data.realContent as MChatMessageImage

            ImageLoaderUtils.display(itemView.context, item_message_image, content.url)

            item_message_image.setOnClickListener {
                PlayerConfigHelper.previewImage(
                        context = itemView.context,
                        position = 0,
                        list = arrayListOf(
                                PreImageEntity(
                                        name = content.name,
                                        desc = content.name,
                                        path = content.url
                                )
                        )
                )
            }
        }
    }

    override fun updateFileState(contentView: View, data: IMessageService.Message, position: Int) {
        contentView.apply {
            val mMessageState = findViewById<ImageView>(R.id.item_message_state)
            val mMessageProgressBar = findViewById<ProgressBar>(R.id.item_message_progress)
            if (data.realContent is MChatMessageImage) {
                val file = data.realContent as MChatMessageImage
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