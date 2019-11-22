package qsos.base.chat.data.entity

/**
 * @author : 华清松
 * 聊天类型
 * @sample ChatType.SINGLE 单聊
 * @sample ChatType.GROUP 群聊
 * @sample ChatType.NOTICE 公告
 * @sample ChatType.SUBSCRIPTION 订阅
 */
enum class ChatType(val key: Int) {
    SINGLE(1),
    GROUP(2),

    NOTICE(3),
    SUBSCRIPTION(4);
}