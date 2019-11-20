package qsos.base.chat.data.entity

/**
 * @author : 华清松
 * 聊天消息内容
 * @param fields 内容map集合
 */
data class ChatContent(
        val fields: HashMap<String, Any?> = HashMap()
) {

    fun create(type: Int, desc: String): ChatContent {
        setContentType(type)
        setContentDesc(desc)
        return this
    }

    fun put(key: String, value: Any?): ChatContent {
        this.fields[key] = value
        return this
    }

    /**设置消息类型*/
    fun setContentType(contentType: Int): ChatContent {
        this.fields["contentType"] = contentType
        return this
    }

    /**设置消息摘要*/
    fun setContentDesc(contentDesc: String): ChatContent {
        this.fields["contentDesc"] = contentDesc
        return this
    }

    /**获取消息类型*/
    fun getContentType(): Int {
        val type = (this.fields["contentType"] as Number?)?.toInt()
        this.fields["contentType"] = type
        return type ?: -1
    }

    /**获取消息摘要*/
    fun getContentDesc(): String {
        return this.fields["contentDesc"]?.toString() ?: ""
    }
}