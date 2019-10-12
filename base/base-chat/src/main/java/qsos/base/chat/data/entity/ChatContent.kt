package qsos.base.chat.data.entity

/**
 * @author : 华清松
 * 聊天消息内容
 * @param fields 内容map集合
 */
data class ChatContent(
        val fields: HashMap<String, Any?>
) {
    /**设置消息类型*/
    fun setContentType(contentType: Int) {
        this.fields["contentType"] = contentType
    }
}