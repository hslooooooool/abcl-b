package qsos.base.chat.data.entity

/**
 * @author : 华清松
 * 聊天类型
 * @sample EnumChatType.SINGLE
 * @sample EnumChatType.GROUP
 * @sample EnumChatType.NOTICE
 * @sample EnumChatType.SUBSCRIPTION
 * @sample EnumChatType.SYSTEM
 */
enum class EnumChatType(val key: Int) {
    /**单聊*/
    SINGLE(1),
    /**群聊*/
    GROUP(2),
    /**公告*/
    NOTICE(3),
    /**订阅*/
    SUBSCRIPTION(4),
    /**系统指令*/
    SYSTEM(5);
}