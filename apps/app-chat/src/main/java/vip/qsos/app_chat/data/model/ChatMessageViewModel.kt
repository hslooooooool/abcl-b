package vip.qsos.app_chat.data.model

import vip.qsos.app_chat.data.entity.ChatMessageBo
import kotlin.coroutines.CoroutineContext

/**
 * @author : 华清松
 * 消息接口
 */
interface ChatMessageViewModel {

    val mJob: CoroutineContext
    fun clear()

    /**获取群下的历史消息列表，即当前第一条消息时序前【20】条消息
     * @param sessionId 群ID
     * */
    fun getOldMessageBySessionId(sessionId: Long, success: (messageList: List<ChatMessageBo>) -> Unit)

    /**撤回消息
     * @param message 消息
     * */
    fun deleteMessage(
            message: ChatMessageBo,
            failed: (msg: String, message: ChatMessageBo) -> Unit,
            success: (message: ChatMessageBo) -> Unit
    )

}