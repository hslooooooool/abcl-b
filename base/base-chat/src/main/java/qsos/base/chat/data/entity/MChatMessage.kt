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
 * @param createTime 创建时间,毫秒数
 *
 * @param sendStatus 消息发送状态,本地存储
 * @param readStatus 消息读取状态,本地存储
 */
data class MChatMessage(
        val user: ChatUser,
        val createTime: Long,
        val message: ChatMessage,
        val sendStatus: MChatSendStatus = MChatSendStatus.SUCCESS,
        val readStatus: Boolean = true
) {

    /**消息内容类型*/
    var contentType: Int = -1
        get() {
            field = when (val type = message.content.fields[VIEW_TYPE_KEY]) {
                is Number -> type.toInt()
                else -> -1
            }
            return field
        }

    /**消息内容实体*/
    var content: Any? = null
        get() {
            if (contentType == -1) field = "" else {
                if (field == null) {
                    val gson = Gson()
                    field = try {
                        val json = gson.toJson(message.content.fields)
                        val type = ChatMessageHelper.configBeenType(contentType)
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
        /**配置消息内容类型*/
        fun configViewType(contentType: Int): Int

        /**配置布局*/
        fun configHolder(view: View, @LayoutRes viewType: Int): BaseHolder<MChatMessage>

        /**配置消息内容类型转化*/
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

    companion object {
        /**通过内容类型获取对应布局*/
        fun getValueByContentType(contentType: Int): Int {
            var v: Int = -1
            for (e in values()) {
                if (e.contentType == contentType) {
                    v = e.v
                    break
                }
            }
            return v
        }
    }
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