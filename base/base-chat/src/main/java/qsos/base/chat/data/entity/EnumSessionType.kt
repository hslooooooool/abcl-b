package qsos.base.chat.data.entity

/**
 * @author : 华清松
 * 会话类型
 * @sample EnumSessionType.SINGLE
 * @sample EnumSessionType.GROUP
 * @sample EnumSessionType.NOTICE
 * @sample EnumSessionType.SUBSCRIPTION
 * @sample EnumSessionType.SYSTEM
 */
enum class EnumSessionType(val key: Int) {
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