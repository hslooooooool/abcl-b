package qsos.base.chat.view.holder

import android.view.View
import kotlinx.android.synthetic.main.item_message_items.view.*
import kotlinx.android.synthetic.main.item_message_video.view.*
import qsos.base.chat.data.entity.MChatMessageVideo
import qsos.base.chat.api.IMessageListService
import qsos.core.lib.utils.image.ImageLoaderUtils
import qsos.core.player.PlayerConfigHelper
import qsos.core.player.data.PreDocumentEntity
import qsos.lib.base.callback.OnListItemClickListener

/**
 * @author : 华清松
 * 消息内容-视频布局
 */
class ItemChatMessageVideoViewHolder(session: IMessageListService.Session, view: View) : ItemChatMessageBaseViewHolder(session, view) {

    override fun setContent(contentView: View, data: IMessageListService.Message, position: Int, itemListener: OnListItemClickListener?) {
        contentView.apply {
            item_message_view_video.visibility = View.VISIBLE
            val content = data.getRealContent<MChatMessageVideo>()
            content?.let {
                ImageLoaderUtils.displayRounded(itemView.context, item_message_video_avatar, content.avatar)

                item_message_video_avatar.setOnClickListener {
                    itemListener?.onItemClick(it, position, data)
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

}