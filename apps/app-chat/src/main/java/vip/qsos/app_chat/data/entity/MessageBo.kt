package vip.qsos.app_chat.data.entity

import com.google.gson.Gson
import qsos.base.chat.data.entity.ChatContent
import qsos.base.chat.data.entity.EnumChatType
import vip.qsos.im.lib.model.Message

/**
 * @author : 华清松
 * TODO 类说明，描述此类的类型和用途
 */
data class MessageBo(
        /**消息ID*/
        var id: String,
        /**消息标题*/
        var title: String,
        /**消息发送者账号*/
        var sender: String,
        /**消息发送者接收者*/
        var receiver: String,
        /**消息发送时间*/
        var timestamp: Long,
        /**消息附加信息*/
        var extra: MessageExtra,
        /**消息内容*/
        var content: ChatContent
) {

    /**消息内容格式*/
    enum class Format(val value: String) {
        PROTOBUF("protobuf"),
        JSON("json"),
        XML("xml"),
        TEXT("text");
    }

    /**消息附加信息
     * @param chatType 消息类型
     * @param belongId ID，一般为群ID
     * */
    data class MessageExtra constructor(
            var chatType: EnumChatType,
            var belongId: String
    )

    companion object {

        fun decode(msg: Message): MessageBo {
            /**附加信息转换*/
            val extra = decodeExtra(msg.extra!!)
            /**真是内容转换*/
            val content = decodeContent(extra.chatType, msg.action!!, msg.format!!, msg.content!!)
            return MessageBo(
                    /**基本信息*/
                    msg.id.toString(), msg.title ?: "", msg.sender!!, msg.receiver!!, msg.timestamp,
                    /**附加信息*/
                    extra,
                    /**内容信息*/
                    content
            )
        }

        private fun decodeContent(type: EnumChatType, action: String, format: String, content: String): ChatContent {
            // todo 根据 format 解析
            return decodeContentByJson(type, content)
        }

        private fun decodeExtra(extra: String): MessageExtra {
            return Gson().fromJson(extra, MessageExtra::class.java)
        }

        private fun decodeContentByJson(type: EnumChatType, content: String): ChatContent {
            val sContent = ChatContent()
            val fields = Gson().fromJson<HashMap<String, Any?>>(content, HashMap::class.java)
            sContent.fields = fields
            return sContent
        }
    }
}