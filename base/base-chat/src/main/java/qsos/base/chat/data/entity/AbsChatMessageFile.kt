package qsos.base.chat.data.entity

/**
 * @author : 华清松
 * 消息内容接口,自定义消息实体需实现此接口
 */
abstract class AbsChatMessageFile {
    companion object {
        var HOST: String = ""
    }

    var length: Int = -1
    var name: String = ""
    var avatar: String = ""
    var url: String = ""

    fun getHttpUrl(online: Boolean): String {
        if (this.url.startsWith("http://")) {
            return this.url
        }
        if (online) {
            this.url = HOST + this.url
        }
        return this.url
    }

    fun getHttpAvatar(online: Boolean): String {
        if (this.avatar.startsWith("http://")) {
            return this.avatar
        }
        if (online) {
            this.avatar = HOST + this.avatar
        }
        return this.avatar
    }

}