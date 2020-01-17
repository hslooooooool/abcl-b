package qsos.base.chat

import android.view.View
import com.google.gson.reflect.TypeToken
import qsos.base.chat.data.entity.*
import qsos.base.chat.api.MessageViewHelper
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

    override fun getHolder(session: MessageViewHelper.Session, view: View, viewType: Int): ItemChatMessageBaseViewHolder {
        return mChatMessageConfig?.getHolder(session, view, viewType)
                ?: getDefHolder(session, view, viewType)
    }

    override fun getContentType(contentType: Int): Type {
        return mChatMessageConfig?.getContentType(contentType)
                ?: getDefContentType(contentType)
    }

    private fun getDefHolder(session: MessageViewHelper.Session, view: View, viewType: Int): ItemChatMessageBaseViewHolder {
        return when (viewType) {
            EnumChatMessageType.TEXT.type -> ItemChatMessageTextViewHolder(session, view)
            EnumChatMessageType.IMAGE.type -> ItemChatMessageImageViewHolder(session, view)
            EnumChatMessageType.VIDEO.type -> ItemChatMessageVideoViewHolder(session, view)
            EnumChatMessageType.AUDIO.type -> ItemChatMessageAudioViewHolder(session, view)
            EnumChatMessageType.FILE.type -> ItemChatMessageFileViewHolder(session, view)
            EnumChatMessageType.LINK.type -> ItemChatMessageLinkViewHolder(session, view)
            EnumChatMessageType.CARD.type -> ItemChatMessageCardViewHolder(session, view)
            EnumChatMessageType.LOCATION.type -> ItemChatMessageLocationViewHolder(session, view)
            else -> {
                ItemChatMessageTextViewHolder(session, view)
            }
        }
    }

    private fun getDefContentType(contentType: Int): Type {
        return when (contentType) {
            EnumChatMessageType.TEXT.type -> object : TypeToken<MChatMessageText>() {}.type
            EnumChatMessageType.IMAGE.type -> object : TypeToken<MChatMessageImage>() {}.type
            EnumChatMessageType.VIDEO.type -> object : TypeToken<MChatMessageVideo>() {}.type
            EnumChatMessageType.AUDIO.type -> object : TypeToken<MChatMessageAudio>() {}.type
            EnumChatMessageType.FILE.type -> object : TypeToken<MChatMessageFile>() {}.type
            EnumChatMessageType.LINK.type -> object : TypeToken<MChatMessageLink>() {}.type
            EnumChatMessageType.CARD.type -> object : TypeToken<MChatMessageCard>() {}.type
            EnumChatMessageType.LOCATION.type -> object : TypeToken<MChatMessageLocation>() {}.type
            else -> String::class.java
        }
    }
}