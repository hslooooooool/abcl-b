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
        override var desc: String = "文本消息"
        override var type: Int = this.ordinal
    },
    IMAGE("图片消息") {
        override var desc: String = "图片消息"
        override var type: Int = this.ordinal
    },
    VIDEO("视频消息") {
        override var desc: String = "视频消息"
        override var type: Int = this.ordinal
    },
    AUDIO("语音消息") {
        override var desc: String = "语音消息"
        override var type: Int = this.ordinal
    },
    FILE("附件消息") {
        override var desc: String = "附件消息"
        override var type: Int = this.ordinal
    },
    LINK("链接消息") {
        override var desc: String = "链接消息"
        override var type: Int = this.ordinal
    },
    CARD("名片消息") {
        override var desc: String = "名片消息"
        override var type: Int = this.ordinal
    },
    LOCATION("位置消息") {
        override var desc: String = "位置消息"
        override var type: Int = this.ordinal
    };
}