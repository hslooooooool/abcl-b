package qsos.base.chat

import android.view.View
import com.google.gson.reflect.TypeToken
import qsos.base.chat.data.entity.*
import qsos.base.chat.view.holder.*
import java.lang.reflect.Type

/**
 * @author : 华清松
 * 消息内容多类型布局配置
 */
object ChatMessageHelper : MChatMessage.IMessageConfig {

    private var mChatMessageConfig: MChatMessage.IMessageConfig? = null

    /**初始化聊天消息列表项配置*/
    fun initConfig(config: MChatMessage.IMessageConfig) {
        this.mChatMessageConfig = config
    }

    override fun getHolder(session: ChatSession, view: View, viewType: Int): ItemChatMessageBaseViewHolder {
        return mChatMessageConfig?.getHolder(session, view, viewType)
                ?: getDefHolder(session, view, viewType)
    }

    override fun getContentType(contentType: Int): Type {
        return mChatMessageConfig?.getContentType(contentType)
                ?: getDefContentType(contentType)
    }

    private fun getDefHolder(session: ChatSession, view: View, viewType: Int): ItemChatMessageBaseViewHolder {
        return when (viewType) {
            MChatMessageType.TEXT.contentType -> ItemChatMessageTextViewHolder(session, view)
            MChatMessageType.IMAGE.contentType -> ItemChatMessageImageViewHolder(session, view)
            MChatMessageType.VIDEO.contentType -> ItemChatMessageVideoViewHolder(session, view)
            MChatMessageType.AUDIO.contentType -> ItemChatMessageAudioViewHolder(session, view)
            MChatMessageType.FILE.contentType -> ItemChatMessageFileViewHolder(session, view)
            MChatMessageType.LINK.contentType -> ItemChatMessageLinkViewHolder(session, view)
            MChatMessageType.CARD.contentType -> ItemChatMessageCardViewHolder(session, view)
            MChatMessageType.LOCATION.contentType -> ItemChatMessageLocationViewHolder(session, view)
            else -> {
                ItemChatMessageTextViewHolder(session, view)
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
            MChatMessageType.LOCATION.contentType -> object : TypeToken<MChatMessageLocation>() {}.type
            else -> String::class.java
        }
    }
}