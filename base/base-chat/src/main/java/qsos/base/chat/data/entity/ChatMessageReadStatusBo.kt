package qsos.base.chat.data.entity

/**消息已读记录
 * @param readStatus 当前用户是否已读
 * @param readNum 消息被多少人读取
 * */
data class ChatMessageReadStatusBo(
        val readStatus: Boolean,
        val readNum: Int
)