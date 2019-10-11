package qsos.base.chat.data.entity

import android.view.View
import androidx.annotation.LayoutRes
import com.google.gson.Gson
import qsos.base.chat.ChatMessageHelper
import qsos.base.chat.R
import qsos.lib.base.base.holder.BaseHolder
import java.lang.reflect.Type

/**
 * @author : 华清松
 * 聊天消息列表项展示数据
 * @param user 消息发送用户
 * @param message 消息数据
 * @param sendStatus 消息发送状态
 * @param readStatus 消息读取状态
 */
data class MChatMessage(
        val user: ChatUser,
        val message: ChatMessage,
        val sendStatus: MChatSendStatus = MChatSendStatus.SUCCESS,
        val readStatus: Boolean = true
) {

    /**布局类型*/
    var viewType: Int = -1
        get() {
            field = when (val type = message.content.fields[VIEW_TYPE_KEY]) {
                is Int -> type
                else -> -1
            }
            return field
        }

    /**消息内容实体*/
    var content: Any? = null
        get() {
            if (viewType == -1) field = "" else {
                if (field == null) {
                    val gson = Gson()
                    field = try {
                        val json = gson.toJson(message.content.fields)
                        val type = ChatMessageHelper.configBeenType(viewType)
                        gson.fromJson(json, type)
                    } catch (e: Exception) {
                        e.printStackTrace()

                        ""
                    }
                }
            }
            return field
        }

    /**消息内容配置接口*/
    interface MessageConfig {
        /**配置布局类型判定逻辑*/
        fun configHolder(view: View, @LayoutRes viewType: Int): BaseHolder<MChatMessage>

        /**消息内容类型判定逻辑*/
        fun configBeenType(contentType: Int): Type
    }

    companion object {
        /**布局类型 KEY */
        const val VIEW_TYPE_KEY = "contentType"
    }
}

/**
 * @author : 华清松
 * 消息发送状态
 * @sample MChatSendStatus.SENDING
 * @sample MChatSendStatus.SUCCESS
 * @sample MChatSendStatus.FAILED
 */
enum class MChatSendStatus(val k: String) {
    SENDING("发送中"),
    SUCCESS("发送成功"),
    FAILED("发送失败");
}

/**
 * @author : 华清松
 * 自定义消息内容类型
 */
enum class MChatMessageType(val k: String, @LayoutRes val v: Int) : IChatMessage {
    TEXT("文本消息", R.layout.item_message_text) {
        override val contentType: Int
            get() = this.ordinal
        override val layoutId: Int
            get() = this.v
    },
    IMAGE("图片消息", R.layout.item_message_text) {
        override val contentType: Int
            get() = this.ordinal
        override val layoutId: Int
            get() = this.v
    },
    VIDEO("视频消息", R.layout.item_message_text) {
        override val contentType: Int
            get() = this.ordinal
        override val layoutId: Int
            get() = this.v
    },
    AUDIO("语音消息", R.layout.item_message_text) {
        override val contentType: Int
            get() = this.ordinal
        override val layoutId: Int
            get() = this.v
    },
    FILE("附件消息", R.layout.item_message_text) {
        override val contentType: Int
            get() = this.ordinal
        override val layoutId: Int
            get() = this.v
    },
    LINK("链接消息", R.layout.item_message_text) {
        override val contentType: Int
            get() = this.ordinal
        override val layoutId: Int
            get() = this.v
    },
    CARD("名片消息", R.layout.item_message_text) {
        override val contentType: Int
            get() = this.ordinal
        override val layoutId: Int
            get() = this.v
    },
    LOCATION("位置消息", R.layout.item_message_text) {
        override val contentType: Int
            get() = this.ordinal
        override val layoutId: Int
            get() = this.v
    };
}

/**
 * @author : 华清松
 * 消息内容接口,自定义消息实体需实现此接口
 */
interface IChatMessage {
    /**消息内容类型值,属性名需要和 VIEW_TYPE_KEY 保持相同
     * @see MChatMessage.VIEW_TYPE_KEY
     * */
    val contentType: Int
    /**消息内容类型对应的布局ID*/
    val layoutId: Int
}