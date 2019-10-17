package qsos.base.chat.view.holder

import android.annotation.SuppressLint
import android.view.View
import kotlinx.android.synthetic.main.item_message_audio.view.*
import kotlinx.android.synthetic.main.item_message_items.view.*
import qsos.base.chat.data.entity.MChatMessage
import qsos.base.chat.data.entity.MChatMessageAudio
import qsos.core.lib.utils.image.ImageLoaderUtils
import qsos.core.player.PlayerConfigHelper
import qsos.core.player.data.PreAudioEntity

/**
 * @author : 华清松
 * 消息内容-音频布局
 */
class ItemChatMessageAudioViewHolder(view: View) : ItemChatMessageBaseViewHolder(view) {
    @SuppressLint("SetTextI18n")
    override fun setContent(contentView: View, data: MChatMessage, position: Int) {
        contentView.item_message_view_audio.visibility = View.VISIBLE
        val content = data.content as MChatMessageAudio
        contentView.item_message_audio_name.text = content.name
        contentView.item_message_audio_time.text = "${content.length.toFloat() * 0.001}`"
        ImageLoaderUtils.display(itemView.context, contentView.item_message_audio_avatar, content.avatar)

        contentView.item_message_audio_avatar.setOnClickListener {
            PlayerConfigHelper.previewAudio(
                    context = itemView.context,
                    position = 0,
                    list = arrayListOf(
                            PreAudioEntity(
                                    name = content.name,
                                    desc = content.name,
                                    path = content.url
                            )
                    )
            )
        }
    }
}