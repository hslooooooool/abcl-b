package qsos.base.chat

import android.view.View
import androidx.annotation.LayoutRes
import com.google.gson.reflect.TypeToken
import qsos.base.chat.data.entity.MChatMessage
import qsos.base.chat.data.entity.MChatMessageType
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

    override fun configHolder(view: View, viewType: Int): BaseHolder<MChatMessage> {
        return mChatMessageConfig?.configHolder(view, viewType)
                ?: defConfigHolder(view, viewType)
    }

    override fun configBeenType(contentType: Int): Type {
        return mChatMessageConfig?.configBeenType(contentType)
                ?: defConfigBeenType(contentType)
    }

    private fun defConfigHolder(view: View, @LayoutRes viewType: Int): BaseHolder<MChatMessage> {
        return when (viewType) {
            MChatMessageType.TEXT.v -> ItemChatMessageTextViewHolder(view)
            MChatMessageType.IMAGE.v -> ItemChatMessageTextViewHolder(view)
            MChatMessageType.VIDEO.v -> ItemChatMessageTextViewHolder(view)
            MChatMessageType.AUDIO.v -> ItemChatMessageTextViewHolder(view)
            MChatMessageType.FILE.v -> ItemChatMessageTextViewHolder(view)
            MChatMessageType.LINK.v -> ItemChatMessageTextViewHolder(view)
            MChatMessageType.CARD.v -> ItemChatMessageTextViewHolder(view)
            MChatMessageType.LOCATION.v -> ItemChatMessageTextViewHolder(view)
            else -> {
                ItemChatMessageTextViewHolder(view)
            }
        }
    }

    private fun defConfigBeenType(contentType: Int): Type {
        return when (contentType) {
            MChatMessageType.TEXT.contentType -> object : TypeToken<MChatMessage>() {}.type
            MChatMessageType.IMAGE.contentType -> object : TypeToken<MChatMessage>() {}.type
            MChatMessageType.VIDEO.contentType -> object : TypeToken<MChatMessage>() {}.type
            MChatMessageType.AUDIO.contentType -> object : TypeToken<MChatMessage>() {}.type
            MChatMessageType.FILE.contentType -> object : TypeToken<MChatMessage>() {}.type
            MChatMessageType.LINK.contentType -> object : TypeToken<MChatMessage>() {}.type
            MChatMessageType.CARD.contentType -> object : TypeToken<MChatMessage>() {}.type
            MChatMessageType.LOCATION.contentType -> object : TypeToken<MChatMessage>() {}.type
            else -> String::class.java
        }
    }
}