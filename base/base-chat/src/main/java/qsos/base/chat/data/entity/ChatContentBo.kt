package qsos.base.chat.data.entity

import com.google.gson.Gson

/**
 * @author : 华清松
 * 聊天消息内容
 * @param fields 内容map集合
 *  FIXME 待取消此实体
 */
data class ChatContentBo constructor(
        var fields: HashMap<String, Any?> = HashMap()
) {

    fun create(type: Int, desc: String): ChatContentBo {
        setContentType(type)
        setContentDesc(desc)
        return this
    }

    fun put(key: String, value: Any?): ChatContentBo {
        this.fields[key] = value
        return this
    }

    /**设置消息类型*/
    fun setContentType(contentType: Int): ChatContentBo {
        this.fields["type"] = contentType
        return this
    }

    /**设置消息摘要*/
    fun setContentDesc(contentDesc: String): ChatContentBo {
        var desc: String = contentDesc
        val length = contentDesc.length
        if (length > 20) {
            desc = contentDesc.substring(0, 20) + "..."
        }
        this.fields["description"] = desc
        return this
    }

    /**获取消息类型*/
    fun getContentType(): Int {
        var type: Int?
        try {
            type = (this.fields["type"] as Number?)?.toInt()
            this.fields["type"] = type
        } catch (e: Exception) {
            type = -1
        }
        return type ?: -1
    }

    /**获取消息摘要*/
    fun getContentDesc(): String {
        var contentDesc: String = this.fields["description"]?.toString() ?: ""
        val length = contentDesc.length
        if (length > 20) {
            contentDesc = contentDesc.substring(0, 20) + "..."
        }
        this.fields["description"] = contentDesc
        return contentDesc
    }

    /**获取文本内容，仅文本消息有效*/
    fun getContent(): String {
        return this.fields["data"]?.toString() ?: ""
    }

    override fun toString(): String {
        return Gson().toJson(fields)
    }
}