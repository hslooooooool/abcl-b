package qsos.base.chat.data.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import qsos.base.chat.data.ApiChatMessage
import qsos.base.chat.data.db.DBChatDatabase
import qsos.base.chat.data.entity.ChatMessageBo
import qsos.base.chat.data.entity.EnumChatSendStatus
import qsos.lib.base.utils.LogUtil
import qsos.lib.netservice.ApiEngine
import qsos.lib.netservice.data.BaseHttpLiveData
import qsos.lib.netservice.data.BaseResponse
import qsos.lib.netservice.expand.retrofitByDef
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

/**
 * @author : 华清松
 * 聊天消息相关接口默认实现
 */
class DefChatMessageModelIml(
        override val mJob: CoroutineContext = Dispatchers.Main + Job(),
        override val mDataOfNewMessage: BaseHttpLiveData<List<ChatMessageBo>> = BaseHttpLiveData()
) : IChatModel.IMessage {

    /**是否正在获取新消息*/
    private var pullNewMessageIng = false

    override fun getNewMessageBySessionId(sessionId: Int) {
        if (pullNewMessageIng) {
            return
        }
        pullNewMessageIng = true
        DBChatDatabase.DefChatSessionDao.getChatSessionById(sessionId) { oldSession ->
            val nowLastMessageTimeline = oldSession?.nowLastMessageTimeline
            /**本地最新消息以获取过!=null,可能为-1，但依然比服务器最新消息Timeline小，则获取新的消息*/
            if (nowLastMessageTimeline != null && nowLastMessageTimeline < oldSession.lastMessageTimeline ?: -1) {
                CoroutineScope(mJob).retrofitByDef<List<ChatMessageBo>> {
                    api = ApiEngine.createService(ApiChatMessage::class.java).getMessageListBySessionIdAndTimeline(
                            sessionId = sessionId, timeline = nowLastMessageTimeline, next = true
                    )
                    onFailed { code, msg, error ->
                        mDataOfNewMessage.postValue(BaseResponse(
                                code = code, msg = msg, data = null
                        ))
                        pullNewMessageIng = false
                        Timber.e(error)
                    }
                    onSuccess {
                        it?.let { list ->
                            list.sortedBy { msg ->
                                msg.timeline
                            }
                            oldSession.nowLastMessageId = list.last().messageId
                            oldSession.nowLastMessageTimeline = list.last().timeline
                            DBChatDatabase.DefChatSessionDao.update(oldSession) { ok ->
                                mDataOfNewMessage.postValue(BaseResponse(
                                        code = 200, msg = "请求成功", data = list
                                ))
                                pullNewMessageIng = false
                                LogUtil.d("会话更新", (if (ok) "已" else "未") + "更新会话最新消息")
                            }
                        }
                    }
                }
            } else {
                pullNewMessageIng = false
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