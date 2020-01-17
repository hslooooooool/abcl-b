package qsos.base.chat.data.entity

import android.text.TextUtils
import com.google.gson.Gson

/**
 * @author : 华清松
 * 聊天消息内容
 */
data class ChatContent constructor(
        var type: Int = 0,
        var desc: String = ""
) {
    var data: String = ""
        get() {
            if (TextUtils.isEmpty(field)) {
                field = getContent()
            }
            return field
        }

    private var content: Content = Content()

    fun getContent(): String {
        return content.toString()
    }

    class Content : HashMap<String, Any?>() {
        override fun toString(): String {
            return Gson().toJson(this)
        }

        companion object {
            fun decode(data: String): Content {
                return Gson().fromJson(data, Content::class.java)
            }
        }
    }

    fun put(key: String, value: Any?): ChatContent {
        content[key] = value
        data = getContent()
        return this
    }

    fun get(key: String): Any? {
        if (content.isEmpty()) {
            content = Content.decode(data)
        }
        return content[key]
    }

    companion object {
        fun json(json: String): ChatContent {
            return try {
                Gson().fromJson(json, ChatContent::class.java)
            } catch (e: Exception) {
                ChatContent()
            }
        }
    }
}