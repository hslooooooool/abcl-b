package qsos.base.chat.data.entity

/**
 * @author : 华清松
 * 消息内容接口,自定义消息实体需实现此接口
 */
abstract class AbsChatMessageFile {
    var length: Int = -1
    var name: String = ""
    var avatar: String = ""
    var url: String = ""
}