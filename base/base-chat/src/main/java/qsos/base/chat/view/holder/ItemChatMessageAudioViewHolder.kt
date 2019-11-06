package qsos.base.chat.view.holder

import android.annotation.SuppressLint
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import kotlinx.android.synthetic.main.item_message_audio.view.*
import kotlinx.android.synthetic.main.item_message_items.view.*
import qsos.base.chat.R
import qsos.base.chat.data.entity.ChatSession
import qsos.base.chat.data.entity.MBaseChatMessageFile
import qsos.base.chat.data.entity.MChatMessage
import qsos.base.chat.data.entity.MChatMessageAudio
import qsos.lib.base.callback.OnListItemClickListener

/**
 * @author : 华清松
 * 消息内容-音频布局
 */
class ItemChatMessageAudioViewHolder(session: ChatSession, view: View) : ItemChatMessageBaseFileViewHolder(session, view) {
    @SuppressLint("SetTextI18n")
    override fun setContent(contentView: View, data: MChatMessage, position: Int, itemListener: OnListItemClickListener?) {
        super.setContent(contentView, data, position, itemListener)
        contentView.apply {
            item_message_view_audio.visibility = View.VISIBLE
            val content = data.content as MChatMessageAudio
            item_message_audio_time.text = "${content.length}`"

            item_message_view_audio.setOnClickListener {
                itemListener?.onItemClick(it, position, data)
            }
        }
    }

    override fun updateFileState(contentView: View, data: MChatMessage, position: Int) {
        contentView.apply {
            val mMessageState = findViewById<ImageView>(R.id.item_message_state)
            val mMessageProgressBar = findViewById<ProgressBar>(R.id.item_message_progress)
            if (data.content is MChatMessageAudio) {
                val file = data.content as MChatMessageAudio
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