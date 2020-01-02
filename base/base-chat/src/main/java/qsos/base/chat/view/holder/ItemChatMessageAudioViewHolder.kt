package qsos.base.chat.view.holder

import android.annotation.SuppressLint
import android.view.View
import kotlinx.android.synthetic.main.item_message_voice.view.*
import kotlinx.android.synthetic.main.item_message_items.view.*
import qsos.base.chat.data.entity.MChatMessageAudio
import qsos.base.chat.api.IMessageListService
import qsos.lib.base.callback.OnListItemClickListener

/**
 * @author : 华清松
 * 消息内容-音频布局
 */
class ItemChatMessageAudioViewHolder(group: IMessageListService.Group, view: View) : ItemChatMessageBaseViewHolder(group, view) {
    @SuppressLint("SetTextI18n")
    override fun setContent(contentView: View, data: IMessageListService.Message, position: Int, itemListener: OnListItemClickListener?) {
        contentView.apply {
            item_message_view_audio.visibility = View.VISIBLE
            data.getRealContent<MChatMessageAudio>()?.let {
                item_message_audio_time.text = "${it.length}`"
                item_message_view_audio.setOnClickListener {
                    itemListener?.onItemClick(it, position, data)
                }
            }
        }
    }

}