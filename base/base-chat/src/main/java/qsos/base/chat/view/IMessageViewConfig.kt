package qsos.base.chat.view

import android.view.View
import androidx.annotation.LayoutRes
import qsos.base.chat.api.MessageViewHelper
import qsos.base.chat.view.holder.ItemChatMessageBaseViewHolder
import java.lang.reflect.Type

/**
 * @author : 华清松
 * 消息视图配置接口
 */
interface IMessageViewConfig {

    /**判定消息内容布局*/
    fun getHolder(
            session: MessageViewHelper.Session,
            view: View,
            @LayoutRes viewType: Int
    ): ItemChatMessageBaseViewHolder

    /**判定消息内容解析类型*/
    fun getContentType(contentType: Int): Type
}