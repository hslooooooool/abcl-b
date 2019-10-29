package qsos.base.chat.data.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import qsos.base.chat.data.ApiChatMessage
import qsos.base.chat.data.entity.ChatContent
import qsos.base.chat.data.entity.ChatMessage
import qsos.base.chat.data.entity.MChatMessage
import qsos.base.chat.data.entity.MChatSendStatus
import qsos.lib.netservice.ApiEngine
import qsos.lib.netservice.data.BaseHttpLiveData
import qsos.lib.netservice.data.BaseResponse
import qsos.lib.netservice.expand.retrofitByDef
import qsos.lib.netservice.expand.retrofitWithSuccess
import kotlin.coroutines.CoroutineContext

/**
 * @author : 华清松
 * 聊天消息相关接口默认实现
 */
class DefChatMessageModelIml(
        override val mJob: CoroutineContext = Dispatchers.Main + Job(),
        override val mDataOfChatMessageList: BaseHttpLiveData<List<MChatMessage>> = BaseHttpLiveData()
) : IChatModel.IMessage {

    override fun sendMessage(
            message: MChatMessage,
            failed: (msg: String, message: MChatMessage) -> Unit,
            success: (message: MChatMessage) -> Unit
    ) {
        CoroutineScope(mJob).retrofitByDef<ChatMessage> {
            api = ApiEngine.createService(ApiChatMessage::class.java).sendMessage(message = message.message)
            onFailed { _, msg, error ->
                message.sendStatus = MChatSendStatus.FAILED
                failed.invoke(msg ?: "发送失败${error?.message}", message)
            }
            onSuccess {
                if (it == null) {
                    message.sendStatus = MChatSendStatus.FAILED
                    failed.invoke("发送失败", message)
                } else {
                    message.sendStatus = MChatSendStatus.SUCCESS
                    message.message.messageId = it.messageId
                    success.invoke(message)
                }
            }
        }
    }

    override fun getMessageById(messageId: Int): ChatMessage {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getMessageListBySessionId(sessionId: Int) {
        CoroutineScope(mJob).retrofitWithSuccess<BaseResponse<List<MChatMessage>>> {
            api = ApiEngine.createService(ApiChatMessage::class.java).getMessageListBySessionId(
                    sessionId = sessionId
            )
            onSuccess {
                it?.data?.let { list ->
                    mDataOfChatMessageList.postValue(BaseResponse(
                            code = it.code, msg = it.msg, data = list
                    ))
                }
            }
        }
    }

    override fun getMessageListByUserId(userId: Int): List<ChatMessage> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getContentById(contentId: Int): ChatContent {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteMessage(messageId: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun clear() {
        mJob.cancel()
    }
}