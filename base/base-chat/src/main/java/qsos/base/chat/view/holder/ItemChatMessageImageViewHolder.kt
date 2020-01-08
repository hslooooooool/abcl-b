package qsos.base.chat.view.holder

import android.view.View
import kotlinx.android.synthetic.main.item_message_image.view.*
import kotlinx.android.synthetic.main.item_message_items.view.*
import qsos.base.chat.data.entity.MChatMessageImage
import qsos.base.chat.api.IMessageListService
import qsos.core.lib.utils.image.ImageLoaderUtils
import qsos.core.player.PlayerConfigHelper
import qsos.core.player.data.PreImageEntity
import qsos.lib.base.callback.OnListItemClickListener

/**
 * @author : 华清松
 * 消息内容-图片布局
 */
class ItemChatMessageImageViewHolder(session: IMessageListService.Session, view: View) : ItemChatMessageBaseViewHolder(session, view) {

    override fun setContent(contentView: View, data: IMessageListService.Message, position: Int, itemListener: OnListItemClickListener?) {
        contentView.apply {
            item_message_view_image.visibility = View.VISIBLE
            data.getRealContent<MChatMessageImage>()?.let {
                ImageLoaderUtils.displayRounded(itemView.context, item_message_image, it.url)
                item_message_image.setOnClickListener { _ ->
                    val char = "min-"
                    val url = it.url.replace(char, "")
                    PlayerConfigHelper.previewImage(
                            context = itemView.context,
                            position = 0,
                            list = arrayListOf(
                                    PreImageEntity(
                                            name = it.name,
                                            desc = it.name,
                                            path = url
                                    )
                            )
                    )
                }
            }
        }
    }

}