package qsos.base.chat.view.holder

import android.annotation.SuppressLint
import android.view.View
import kotlinx.android.synthetic.main.item_message_items.view.*
import kotlinx.android.synthetic.main.item_message_voice.view.*
import qsos.base.chat.api.MessageViewHelper
import qsos.base.chat.data.entity.EnumChatSendStatus
import qsos.base.chat.data.entity.MChatMessageAudio
import qsos.lib.base.callback.OnListItemClickListener

/**
 * @author : 华清松
 * 消息内容-音频布局
 */
class ItemChatMessageAudioViewHolder(session: MessageViewHelper.Session, view: View) : ItemChatMessageBaseViewHolder(session, view) {
    @SuppressLint("SetTextI18n")
    override fun setContent(contentView: View, data: MessageViewHelper.Message, position: Int, itemListener: OnListItemClickListener?) {
        contentView.apply {
            item_message_view_audio.visibility = View.VISIBLE
            data.getRealContent<MChatMessageAudio>()?.let {
                item_message_audio_time.text = "${it.length}`"
                item_message_view_audio.setOnClickListener { v ->
                    itemListener?.onItemClick(v, position, data)
                }
            }
        }
    }

}