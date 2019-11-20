package qsos.base.chat

import android.view.View
import com.google.gson.reflect.TypeToken
import qsos.base.chat.data.entity.*
import qsos.base.chat.view.IMessageViewConfig
import qsos.base.chat.view.holder.*
import java.lang.reflect.Type

/**
 * @author : 华清松
 * 消息内容多类型布局配置
 */
object DefChatMessageViewConfig : IMessageViewConfig {

    private var mChatMessageConfig: IMessageViewConfig? = null

    /**初始化聊天消息列表项配置*/
    fun initConfig(config: IMessageViewConfig) {
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
            EnumChatMessageType.TEXT.contentType -> ItemChatMessageTextViewHolder(session, view)
            EnumChatMessageType.IMAGE.contentType -> ItemChatMessageImageViewHolder(session, view)
            EnumChatMessageType.VIDEO.contentType -> ItemChatMessageVideoViewHolder(session, view)
            EnumChatMessageType.AUDIO.contentType -> ItemChatMessageAudioViewHolder(session, view)
            EnumChatMessageType.FILE.contentType -> ItemChatMessageFileViewHolder(session, view)
            EnumChatMessageType.LINK.contentType -> ItemChatMessageLinkViewHolder(session, view)
            EnumChatMessageType.CARD.contentType -> ItemChatMessageCardViewHolder(session, view)
            EnumChatMessageType.LOCATION.contentType -> ItemChatMessageLocationViewHolder(session, view)
            else -> {
                ItemChatMessageTextViewHolder(session, view)
            }
        }
    }

    private fun getDefContentType(contentType: Int): Type {
        return when (contentType) {
            EnumChatMessageType.TEXT.contentType -> object : TypeToken<MChatMessageText>() {}.type
            EnumChatMessageType.IMAGE.contentType -> object : TypeToken<MChatMessageImage>() {}.type
            EnumChatMessageType.VIDEO.contentType -> object : TypeToken<MChatMessageVideo>() {}.type
            EnumChatMessageType.AUDIO.contentType -> object : TypeToken<MChatMessageAudio>() {}.type
            EnumChatMessageType.FILE.contentType -> object : TypeToken<MChatMessageFile>() {}.type
            EnumChatMessageType.LINK.contentType -> object : TypeToken<MChatMessageLink>() {}.type
            EnumChatMessageType.CARD.contentType -> object : TypeToken<MChatMessageCard>() {}.type
            EnumChatMessageType.LOCATION.contentType -> object : TypeToken<MChatMessageLocation>() {}.type
            else -> String::class.java
        }
    }
}