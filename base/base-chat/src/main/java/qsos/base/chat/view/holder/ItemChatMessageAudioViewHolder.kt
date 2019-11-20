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
import qsos.base.chat.data.entity.MChatMessageAudio
import qsos.base.chat.service.IMessageService
import qsos.lib.base.callback.OnListItemClickListener

/**
 * @author : 华清松
 * 消息内容-音频布局
 */
class ItemChatMessageAudioViewHolder(session: ChatSession, view: View) : ItemChatMessageBaseFileViewHolder(session, view) {
    @SuppressLint("SetTextI18n")
    override fun setContent(contentView: View, data: IMessageService.Message, position: Int, itemListener: OnListItemClickListener?) {
        super.setContent(contentView, data, position, itemListener)
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

    override fun updateFileState(contentView: View, data: IMessageService.Message, position: Int) {
        contentView.apply {
            val mMessageState = findViewById<ImageView>(R.id.item_message_state)
            val mMessageProgressBar = findViewById<ProgressBar>(R.id.item_message_progress)
            data.getRealContent<MChatMessageAudio>()?.let {
                when (it.uploadState) {
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