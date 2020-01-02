package vip.qsos.app_chat.data.entity

/**
 * @author : 华清松
 * 聊天群
 * @param groupId 群ID,设计上等于会话ID
 * @param name 群名称
 * @param createTime 创建时间,毫秒数
 * @param avatar 群封面
 * @param notice 群公告
 *
 * @see ChatGroup
 */
data class ChatGroupInfo(
        /** @see ChatGroup.id */
        val groupId: Int,
        val name: String,
        val createTime: String,
        val avatar: String?,
        val notice: String? = null
) {
    /**此会话最后一条消息*/
    var lastMessage: ChatMessageBo? = null
}
