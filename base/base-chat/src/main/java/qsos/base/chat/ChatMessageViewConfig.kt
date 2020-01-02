package qsos.base.chat

import android.view.View
import com.google.gson.reflect.TypeToken
import qsos.base.chat.data.entity.*
import qsos.base.chat.api.IMessageListService
import qsos.base.chat.view.IMessageViewConfig
import qsos.base.chat.view.holder.*
import java.lang.reflect.Type

/**
 * @author : 华清松
 * 消息内容多类型布局配置
 */
object ChatMessageViewConfig : IMessageViewConfig {

    private var mChatMessageConfig: IMessageViewConfig? = null

    /**初始化聊天消息列表项配置*/
    fun initConfig(config: IMessageViewConfig) {
        this.mChatMessageConfig = config
    }

    override fun getHolder(group: IMessageListService.Group, view: View, viewType: Int): ItemChatMessageBaseViewHolder {
        return mChatMessageConfig?.getHolder(group, view, viewType)
                ?: getDefHolder(group, view, viewType)
    }

    override fun getContentType(contentType: Int): Type {
        return mChatMessageConfig?.getContentType(contentType)
                ?: getDefContentType(contentType)
    }

    private fun getDefHolder(group: IMessageListService.Group, view: View, viewType: Int): ItemChatMessageBaseViewHolder {
        return when (viewType) {
            EnumChatMessageType.TEXT.contentType -> ItemChatMessageTextViewHolder(group, view)
            EnumChatMessageType.IMAGE.contentType -> ItemChatMessageImageViewHolder(group, view)
            EnumChatMessageType.VIDEO.contentType -> ItemChatMessageVideoViewHolder(group, view)
            EnumChatMessageType.AUDIO.contentType -> ItemChatMessageAudioViewHolder(group, view)
            EnumChatMessageType.FILE.contentType -> ItemChatMessageFileViewHolder(group, view)
            EnumChatMessageType.LINK.contentType -> ItemChatMessageLinkViewHolder(group, view)
            EnumChatMessageType.CARD.contentType -> ItemChatMessageCardViewHolder(group, view)
            EnumChatMessageType.LOCATION.contentType -> ItemChatMessageLocationViewHolder(group, view)
            else -> {
                ItemChatMessageTextViewHolder(group, view)
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