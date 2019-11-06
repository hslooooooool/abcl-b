package qsos.base.chat.view.holder

import android.annotation.SuppressLint
import android.view.View
import kotlinx.android.synthetic.main.item_message_file.view.*
import kotlinx.android.synthetic.main.item_message_items.view.*
import qsos.base.chat.data.entity.ChatSession
import qsos.base.chat.data.entity.MChatMessage
import qsos.base.chat.data.entity.MChatMessageFile
import qsos.core.lib.utils.image.ImageLoaderUtils
import qsos.core.player.PlayerConfigHelper
import qsos.core.player.data.PreDocumentEntity
import qsos.lib.base.callback.OnListItemClickListener

/**
 * @author : 华清松
 * 消息内容-文件布局
 */
class ItemChatMessageFileViewHolder(session: ChatSession, view: View) : ItemChatMessageBaseViewHolder(session, view) {
    @SuppressLint("SetTextI18n")
    override fun setContent(contentView: View, data: MChatMessage, position: Int, itemListener: OnListItemClickListener?) {
        contentView.apply {

            item_message_view_file.visibility = View.VISIBLE
            val content = data.content as MChatMessageFile

            ImageLoaderUtils.display(itemView.context, item_message_file_avatar, content.url)

            item_message_file_name.text = content.name
            item_message_file_length.text = "${content.length} kb"

            item_message_file_avatar.setOnClickListener {
                PlayerConfigHelper.previewDocument(
                        context = itemView.context,
                        data = PreDocumentEntity(
                                name = content.name,
                                desc = content.name,
                                path = content.url
                        )
                )
            }
        }
    }
}