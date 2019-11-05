package qsos.base.chat.data.entity

/**
 * @author : 华清松
 * 消息内容接口,自定义消息实体需实现此接口
 */
abstract class MBaseChatMessageFile {
    var length: Int = -1
    var name: String = ""
    var avatar: String = ""
    var url: String = ""

    constructor()
    /**
     * @param length 文件长度,kb
     * @param name 文件名称
     * @param avatar 文件封面
     * @param url 文件链接
     */
    constructor(
            length: Int,
            name: String,
            avatar: String,
            url: String
    ) {
        this.length = length
        this.name = name
        this.avatar = avatar
        this.url = url
    }
}