package qsos.base.chat.api

import androidx.lifecycle.MutableLiveData
import qsos.base.chat.data.entity.ChatContent
import qsos.base.chat.data.entity.EnumChatSendStatus
import qsos.base.chat.api.IMessageListService.EventType.*
import qsos.base.chat.view.IMessageListView
import qsos.lib.base.utils.rx.RxBus

/**
 * @author : 华清松
 * 消息服务配置接口
 */
interface IMessageListService {

    companion object {
        /**消息时间显示间隔，低于此值间的消息不显示时间*/
        var showTimeLimit = 1 * 60 * 1000
    }

    /**消息更新策略
     * @sample SEND
     * @sample SHOW
     * @sample SHOW_MORE
     * @sample SEND_SHOWED
     * @sample UPDATE_SHOWED
     * @sample SHOW_NEW
     * */
    enum class EventType(val notice: String) {
        SEND("发送消息，底部上屏，滚动到底部，用于消息发送并上屏"),
        SHOW("底部上屏，滚动到底部，用于消息推后发送并上屏，如文件消息发送"),
        SHOW_MORE("顶部上屏，用于历史消息推送"),
        SEND_SHOWED("发送已上屏消息，用于文件消息上传成功后消息发送"),
        UPDATE_SHOWED("更新已上屏消息，用于消息发送状态更新"),
        SHOW_NEW("底部上屏，自动判断是否滚动，用于新消息推送"),
    }

    /**消息发送事件实体
     * @param group 会话实体
     * @param message 消息实体
     * @param eventType 消息更新策略
     * */
    data class MessageEvent(
            val group: Group,
            val message: List<Message>,
            val eventType: EventType
    ) : RxBus.RxBusEvent<MessageEvent> {
        override fun message(): MessageEvent? {
            return this
        }

        override fun name(): String {
            return "消息发送"
        }
    }

    /**会话实体属性*/
    interface Group {
        /**群号*/
        var id: Long
        /**群名称*/
        var name: String
        /**群类型*/
        var type: Int
    }

    /**消息实体属性*/
    interface Message {
        /**消息ID*/
        var messageId: Int
        /**会话ID*/
        var sessionId: Long
        /**消息时序，由服务器统一设置*/
        var timeline: Int
        /**发送人ID*/
        val sendUserId: Long
        /**发送人名称*/
        val sendUserName: String
        /**发送人头像*/
        val sendUserAvatar: String
        /**创建时间*/
        var createTime: String
        /**消息内容*/
        val content: ChatContent
        /**发送状态*/
        var sendStatus: EnumChatSendStatus?
        /**当前用户消息读取状态*/
        var readStatus: Boolean?
        /**消息读取人数,单聊时2即为已读，群聊时代表读取人数*/
        var readNum: Int

        /**消息更新发送状态*/
        fun updateSendState(
                messageId: Int, timeline: Int, sendStatus: EnumChatSendStatus,
                readNum: Int = 1, readState: Boolean? = false
        )

        /**消息转换后实体*/
        fun <T> getRealContent(): T?
    }

    var mUpdateShowMessageList: MutableLiveData<List<Message>>

    /**获取消息列表（进入会话页第一次请求）
     * @param group 会话实体
     * @param messageList 消息列表
     * */
    fun getMessageListBySessionId(group: Group, messageList: MutableLiveData<ArrayList<Message>>)

    /**发送消息
     * @param message 消息实体
     * @param failed 失败回执
     * @param success 成功回执
     * - oldMessageId 为发送消息时本地配置的id，用于消息位子标记
     * - message 为更新后的消息，更新 messageId timeline sendStatus
     * */
    fun sendMessage(
            message: Message,
            failed: (msg: String, message: Message) -> Unit,
            success: (oldMessageId: Int, message: Message) -> Unit
    )

    /**读取消息，用户读取消息后通知服务器消息已读
     * @param message 已读的消息实体
     * @param failed 失败回执
     * @param success 成功回执
     * - message 已读的消息实体
     * */
    fun readMessage(
            message: Message,
            failed: (msg: String, message: Message) -> Unit,
            success: (message: Message) -> Unit
    )

    /**撤销消息
     * @param message 消息实体
     * @param failed 失败回执
     * @param success 成功回执
     * - message 已撤销的消息实体
     * */
    fun revokeMessage(
            message: Message,
            failed: (msg: String, message: Message) -> Unit,
            success: (message: Message) -> Unit
    )

    /**更新当前展示消息*/
    fun updateShowMessage(messageListView: IMessageListView)

    /**释放资源*/
    fun clear()
}