package qsos.base.chat

import android.view.View
import com.google.gson.reflect.TypeToken
import qsos.base.chat.data.entity.MChatMessage
import qsos.base.chat.data.entity.MChatMessageImage
import qsos.base.chat.data.entity.MChatMessageText
import qsos.base.chat.data.entity.MChatMessageType
import qsos.base.chat.view.holder.ItemChatMessageImageViewHolder
import qsos.base.chat.view.holder.ItemChatMessageTextViewHolder
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
            MChatMessageType.VIDEO.contentType -> ItemChatMessageTextViewHolder(view)
            MChatMessageType.AUDIO.contentType -> ItemChatMessageTextViewHolder(view)
            MChatMessageType.FILE.contentType -> ItemChatMessageTextViewHolder(view)
            MChatMessageType.LINK.contentType -> ItemChatMessageTextViewHolder(view)
            MChatMessageType.CARD.contentType -> ItemChatMessageTextViewHolder(view)
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
            MChatMessageType.VIDEO.contentType -> object : TypeToken<MChatMessageText>() {}.type
            MChatMessageType.AUDIO.contentType -> object : TypeToken<MChatMessageText>() {}.type
            MChatMessageType.FILE.contentType -> object : TypeToken<MChatMessageText>() {}.type
            MChatMessageType.LINK.contentType -> object : TypeToken<MChatMessageText>() {}.type
            MChatMessageType.CARD.contentType -> object : TypeToken<MChatMessageText>() {}.type
            MChatMessageType.LOCATION.contentType -> object : TypeToken<MChatMessageText>() {}.type
            else -> String::class.java
        }
    }

}