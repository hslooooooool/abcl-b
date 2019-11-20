package qsos.base.chat.data.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import qsos.base.chat.data.ApiChatMessage
import qsos.base.chat.data.entity.ChatMessageBo
import qsos.base.chat.data.entity.EnumChatSendStatus
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
        override val mDataOfChatMessageList: BaseHttpLiveData<List<ChatMessageBo>> = BaseHttpLiveData()
) : IChatModel.IMessage {

    override fun getMessageListBySessionId(sessionId: Int) {
        CoroutineScope(mJob).retrofitWithSuccess<BaseResponse<List<ChatMessageBo>>> {
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

    override fun deleteMessage(
            message: ChatMessageBo,
            failed: (msg: String, message: ChatMessageBo) -> Unit,
            success: (message: ChatMessageBo) -> Unit
    ) {
        CoroutineScope(mJob).retrofitByDef<Boolean> {
            api = ApiEngine.createService(ApiChatMessage::class.java).deleteMessage(
                    messageId = message.message.messageId
            )
            onFailed { _, msg, error ->
                failed.invoke(msg ?: "撤销失败${error?.message}", message)
            }
            onSuccess {
                if (it == true) {
                    message.sendStatus = EnumChatSendStatus.CANCEL_OK
                    success.invoke(message)
                } else {
                    failed.invoke("撤销失败", message)
                }
            }
        }
    }

    override fun clear() {
        mJob.cancel()
    }
}