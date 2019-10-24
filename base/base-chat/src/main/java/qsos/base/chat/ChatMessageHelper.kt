package qsos.base.chat

import android.view.View
import com.google.gson.reflect.TypeToken
import qsos.base.chat.data.entity.*
import qsos.base.chat.view.holder.*
import qsos.lib.base.base.holder.BaseHolder
import java.lang.reflect.Type

/**
 * @author : 华清松
 * 消息内容多类型布局配置
 */
object ChatMessageHelper : MChatMessage.MessageConfig {

    private var mChatMessageConfig: MChatMessage.MessageConfig? = null

    /**初始化聊天消息列表项配置*/
    fun initConfig(config: MChatMessage.MessageConfig) {
        this.mChatMessageConfig = config
    }

    override fun getHolder(view: View, viewType: Int): BaseHolder<MChatMessage> {
        return mChatMessageConfig?.getHolder(view, viewType)
                ?: getDefHolder(view, viewType)
    }

    override fun getContentType(contentType: Int): Type {
        return mChatMessageConfig?.getContentType(contentType)
                ?: getDefContentType(contentType)
    }

    private fun getDefHolder(view: View, viewType: Int): BaseHolder<MChatMessage> {
        return when (viewType) {
            MChatMessageType.TEXT.contentType -> ItemChatMessageTextViewHolder(view)
            MChatMessageType.IMAGE.contentType -> ItemChatMessageImageViewHolder(view)
            MChatMessageType.VIDEO.contentType -> ItemChatMessageVideoViewHolder(view)
            MChatMessageType.AUDIO.contentType -> ItemChatMessageAudioViewHolder(view)
            MChatMessageType.FILE.contentType -> ItemChatMessageFileViewHolder(view)
            MChatMessageType.LINK.contentType -> ItemChatMessageLinkViewHolder(view)
            MChatMessageType.CARD.contentType -> ItemChatMessageCardViewHolder(view)
            MChatMessageType.LOCATION.contentType -> ItemChatMessageTextViewHolder(view)
            else -> {
                ItemChatMessageTextViewHolder(view)
            }
        }
    }

    private fun getDefContentType(contentType: Int): Type {
        return when (contentType) {
            MChatMessageType.TEXT.contentType -> object : TypeToken<MChatMessageText>() {}.type
            MChatMessageType.IMAGE.contentType -> object : TypeToken<MChatMessageImage>() {}.type
            MChatMessageType.VIDEO.contentType -> object : TypeToken<MChatMessageVideo>() {}.type
            MChatMessageType.AUDIO.contentType -> object : TypeToken<MChatMessageAudio>() {}.type
            MChatMessageType.FILE.contentType -> object : TypeToken<MChatMessageFile>() {}.type
            MChatMessageType.LINK.contentType -> object : TypeToken<MChatMessageLink>() {}.type
            MChatMessageType.CARD.contentType -> object : TypeToken<MChatMessageCard>() {}.type
            MChatMessageType.LOCATION.contentType -> object : TypeToken<MChatMessageText>() {}.type
            else -> String::class.java
        }
    }

}