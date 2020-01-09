package qsos.base.chat.view.holder

import android.annotation.SuppressLint
import android.view.View
import kotlinx.android.synthetic.main.item_message_file.view.*
import kotlinx.android.synthetic.main.item_message_items.view.*
import qsos.base.chat.data.entity.MChatMessageFile
import qsos.base.chat.api.MessageViewHelper
import qsos.core.lib.utils.image.ImageLoaderUtils
import qsos.core.player.PlayerConfigHelper
import qsos.core.player.data.PreDocumentEntity
import qsos.lib.base.callback.OnListItemClickListener

/**
 * @author : 华清松
 * 消息内容-文件布局
 */
class ItemChatMessageFileViewHolder(session: MessageViewHelper.Session, view: View) : ItemChatMessageBaseViewHolder(session, view) {
    @SuppressLint("SetTextI18n")
    override fun setContent(contentView: View, data: MessageViewHelper.Message, position: Int, itemListener: OnListItemClickListener?) {
        contentView.apply {
            item_message_view_file.visibility = View.VISIBLE
            data.getRealContent<MChatMessageFile>()?.let {
                ImageLoaderUtils.display(itemView.context, item_message_file_avatar, it.url)
                item_message_file_name.text = it.name
                item_message_file_length.text = "${it.length/1024} kb"
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

}