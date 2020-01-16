package qsos.base.chat.data.entity

import com.google.gson.Gson

/**
 * @author : 华清松
 * 聊天消息内容
 */
class ChatContent : HashMap<String, Any?> {
    var contentType: Int = 0
    var contentDesc: String = ""

    constructor()

    /**
     * @param contentType 消息类型
     * @param contentDesc 消息摘要
     * */
    constructor(contentType: Int, contentDesc: String = "") {
        this.contentType = contentType
        this.contentDesc = contentDesc
        add("contentType", contentType)
        add("contentDesc", contentDesc)
    }

    fun add(key: String, value: Any?): ChatContent {
        put(key, value)
        return this
    }

    override fun toString(): String {
        return Gson().toJson(this)
    }

    companion object {
        fun json(json: String): ChatContent {
            return Gson().fromJson(json, ChatContent::class.java)
        }
    }
}