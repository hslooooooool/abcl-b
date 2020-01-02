package qsos.base.chat.data.entity

/**
 * @author : 华清松
 * 自定义消息内容类型，0->7
 * @sample EnumChatMessageType.TEXT
 * @sample EnumChatMessageType.IMAGE
 * @sample EnumChatMessageType.VIDEO
 * @sample EnumChatMessageType.AUDIO
 * @sample EnumChatMessageType.FILE
 * @sample EnumChatMessageType.LINK
 * @sample EnumChatMessageType.CARD
 * @sample EnumChatMessageType.LOCATION
 */
enum class EnumChatMessageType(val key: String) : IMessageType {
    TEXT("文本消息") {
        override var contentDesc: String = "文本消息"
        override var contentType: Int = this.ordinal
    },
    IMAGE("图片消息") {
        override var contentDesc: String = "图片消息"
        override var contentType: Int = this.ordinal
    },
    VIDEO("视频消息") {
        override var contentDesc: String = "视频消息"
        override var contentType: Int = this.ordinal
    },
    AUDIO("语音消息") {
        override var contentDesc: String = "语音消息"
        override var contentType: Int = this.ordinal
    },
    FILE("附件消息") {
        override var contentDesc: String = "附件消息"
        override var contentType: Int = this.ordinal
    },
    LINK("链接消息") {
        override var contentDesc: String = "链接消息"
        override var contentType: Int = this.ordinal
    },
    CARD("名片消息") {
        override var contentDesc: String = "名片消息"
        override var contentType: Int = this.ordinal
    },
    LOCATION("位置消息") {
        override var contentDesc: String = "位置消息"
        override var contentType: Int = this.ordinal
    };
}