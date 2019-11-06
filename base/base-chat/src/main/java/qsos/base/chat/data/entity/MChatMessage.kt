package qsos.base.chat.data.entity

import android.view.View
import androidx.annotation.LayoutRes
import com.google.gson.Gson
import qsos.base.chat.ChatMessageHelper
import qsos.base.chat.view.holder.ItemChatMessageBaseViewHolder
import qsos.lib.base.base.holder.BaseHolder
import java.lang.reflect.Type

/**
 * @author : 华清松
 * 聊天消息列表项展示数据
 * @param user 消息发送用户
 * @param message 消息数据
 * @param createTime 创建时间
 */
data class MChatMessage(
        val user: ChatUser,
        val createTime: String,
        val message: ChatMessage
) {
    /**消息发送状态,本地存储*/
    var sendStatus: MChatSendStatus = MChatSendStatus.SUCCESS
    /**消息读取人数,本地存储*/
    var readStatus: Int = 0
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
                        val type = ChatMessageHelper.getContentType(contentType)
                        gson.fromJson(json, type)
                    } catch (e: Exception) {
                        e.printStackTrace()

                        ""
                    }
                }
            }
            return field
        }

    /**消息唯一判定值*/
    var hashCode: Int? = null

    /**消息内容配置接口*/
    interface MessageConfig {

        /**判定消息内容布局*/
        fun getHolder(session: ChatSession, view: View, @LayoutRes viewType: Int): ItemChatMessageBaseViewHolder

        /**判定消息内容解析类型*/
        fun getContentType(contentType: Int): Type
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
enum class MChatMessageType(val k: String) : IChatMessageType {
    TEXT("文本消息") {
        override var contentDesc: String = "文本消息"
        override var contentType: Int = this.ordinal
    },
    IMAGE("图片消息") {
        override var contentDesc: String = "图片消息"
        override var contentType: Int = this.ordinal
    },
    VIDEO("视频消息") {
        override var contentDesc: String = "视频消息"
        override var contentType: Int = this.ordinal
    },
    AUDIO("语音消息") {
        override var contentDesc: String = "语音消息"
        override var contentType: Int = this.ordinal
    },
    FILE("附件消息") {
        override var contentDesc: String = "附件消息"
        override var contentType: Int = this.ordinal
    },
    LINK("链接消息") {
        override var contentDesc: String = "链接消息"
        override var contentType: Int = this.ordinal
    },
    CARD("名片消息") {
        override var contentDesc: String = "名片消息"
        override var contentType: Int = this.ordinal
    },
    LOCATION("位置消息") {
        override var contentDesc: String = "位置消息"
        override var contentType: Int = this.ordinal
    };
}

/**
 * @author : 华清松
 * 消息内容接口,自定义消息实体需实现此接口
 */
interface IChatMessageType {
    /**消息摘要*/
    var contentDesc: String
    /**消息内容类型值,属性名需要和 VIEW_TYPE_KEY 保持相同
     * @see MChatMessage.VIEW_TYPE_KEY
     * */
    var contentType: Int
}