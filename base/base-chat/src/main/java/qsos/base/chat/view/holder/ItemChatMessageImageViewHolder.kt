package qsos.base.chat.view.holder

import android.view.View
import kotlinx.android.synthetic.main.item_message_image.view.*
import kotlinx.android.synthetic.main.item_message_items.view.*
import qsos.base.chat.api.MessageViewHelper
import qsos.base.chat.data.entity.EnumChatSendStatus
import qsos.base.chat.data.entity.MChatMessageImage
import qsos.core.lib.utils.image.ImageLoaderUtils
import qsos.core.player.PlayerConfigHelper
import qsos.core.player.data.PreImageEntity
import qsos.lib.base.callback.OnListItemClickListener

/**
 * @author : 华清松
 * 消息内容-图片布局
 */
class ItemChatMessageImageViewHolder(session: MessageViewHelper.Session, view: View) : ItemChatMessageBaseViewHolder(session, view) {

    override fun setContent(contentView: View, data: MessageViewHelper.Message, position: Int, itemListener: OnListItemClickListener?) {
        contentView.apply {
            item_message_view_image.visibility = View.VISIBLE
            data.getRealContent<MChatMessageImage>()?.let {
                val url = it.getHttpUrl(
                        EnumChatSendStatus.SUCCESS == data.sendStatus
                )
                ImageLoaderUtils.displayRounded(itemView.context, item_message_image, url)
                item_message_image.setOnClickListener { _ ->
                    val char = "min-"
                    val url2 = url.replace(char, "")
                    PlayerConfigHelper.previewImage(
                            context = itemView.context,
                            position = 0,
                            list = arrayListOf(
                                    PreImageEntity(
                                            name = it.name,
                                            desc = it.name,
                                            path = url2
                                    )
                            )
                    )
                }
            }
        }
    }

}