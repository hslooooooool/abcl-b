package qsos.base.chat.data.entity

/**
 * @author : 华清松
 * 消息发送状态
 * @sample EnumChatSendStatus.SENDING
 * @sample EnumChatSendStatus.SUCCESS
 * @sample EnumChatSendStatus.FAILED
 * @sample EnumChatSendStatus.CANCEL_CAN
 * @sample EnumChatSendStatus.CANCEL_OK
 */
enum class EnumChatSendStatus(val key: String) {
    SENDING("发送中"),
    SUCCESS("发送成功"),
    FAILED("发送失败"),
    CANCEL_CAN("可撤销发送"),
    CANCEL_OK("已撤销发送");
}