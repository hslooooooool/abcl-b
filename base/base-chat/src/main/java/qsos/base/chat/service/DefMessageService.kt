package qsos.base.chat.service

import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import qsos.base.chat.data.ApiChatMessage
import qsos.base.chat.data.db.DBChatDatabase
import qsos.base.chat.data.entity.ChatMessage
import qsos.base.chat.data.entity.ChatMessageBo
import qsos.base.chat.data.entity.ChatMessageReadStatusBo
import qsos.base.chat.data.entity.EnumChatSendStatus
import qsos.lib.base.utils.LogUtil
import qsos.lib.netservice.ApiEngine
import qsos.lib.netservice.data.BaseResponse
import qsos.lib.netservice.expand.retrofitByDef
import qsos.lib.netservice.expand.retrofitWithSuccess
import java.util.*
import kotlin.coroutines.CoroutineContext

/**
 * @author : 华清松
 * 消息服务配置
 */
class DefMessageService(
        private val mJob: CoroutineContext = Dispatchers.Main + Job()
) : AbsMessageService() {
    private var mPullMessageTimer: Timer? = null

    override fun getMessageList(session: IMessageService.Session, messageList: MutableLiveData<ArrayList<IMessageService.Message>>) {
        DBChatDatabase.DefChatSessionDao.getChatSessionById(session.sessionId) { oldSession ->
            oldSession?.let {
                val lastTimeline: Int = it.lastMessageTimeline ?: -1
                CoroutineScope(mJob).retrofitWithSuccess<BaseResponse<List<ChatMessageBo>>> {
                    /**获取此会话sessionId下最后一条消息lastTimeline及其以上20条数据*/
                    api = ApiEngine.createService(ApiChatMessage::class.java).getMessageListBySessionIdAndTimeline(
                            sessionId = session.sessionId, timeline = lastTimeline + 1, next = false, size = 20
                    )
                    onSuccess { result ->
                        result?.data?.let { list ->
                            list.sortedBy { msg ->
                                msg.timeline
                            }
                            val array = arrayListOf<IMessageService.Message>()
                            array.addAll(list)
                            if (array.isEmpty()) {
                                oldSession.nowLastMessageId = -1
                                oldSession.nowLastMessageTimeline = -1
                                oldSession.nowFirstMessageId = -1
                                oldSession.nowFirstMessageTimeline = -1
                            } else {
                                oldSession.nowFirstMessageId = array.first().messageId
                                oldSession.nowFirstMessageTimeline = array.first().timeline
                                oldSession.nowLastMessageId = array.last().messageId
                                oldSession.nowLastMessageTimeline = array.last().timeline
                            }
                            DBChatDatabase.DefChatSessionDao.update(oldSession) { ok ->
                                messageList.postValue(array)
                                LogUtil.d("会话更新", (if (ok) "已" else "未") + "更新会话消息")
                            }
                        }
                    }
                }
            }
        }
    }

    override fun sendMessage(
            message: IMessageService.Message,
            failed: (msg: String, message: IMessageService.Message) -> Unit,
            success: (oldMessageId: Int, message: IMessageService.Message) -> Unit
    ) {
        val oldMessageId = message.messageId
        if (oldMessageId == -1) {
            message.sendStatus = EnumChatSendStatus.FAILED
            failed.invoke("发送失败，消息临时ID不能为-1", message)
            return
        }
        val sendMessage = ChatMessage(
                sessionId = message.sessionId,
                content = message.content
        )
        CoroutineScope(mJob).retrofitByDef<ChatMessage> {
            api = ApiEngine.createService(ApiChatMessage::class.java).sendMessage(message = sendMessage)
            onFailed { _, msg, error ->
                message.sendStatus = EnumChatSendStatus.FAILED
                failed.invoke(msg ?: "发送失败${error?.message}", message)
            }
            onSuccess {
                if (it == null) {
                    message.sendStatus = EnumChatSendStatus.FAILED
                    failed.invoke("发送失败", message)
                } else {
                    message.updateSendState(it.messageId, it.timeline, EnumChatSendStatus.SUCCESS)
                    DBChatDatabase.DefChatSessionDao.update(it.sessionId, it.messageId, it.timeline) { ok ->
                        success.invoke(oldMessageId, message)
                        LogUtil.d("会话更新", (if (ok) "已" else "未") + "更新会话最新消息")
                    }
                }
            }
        }
    }

    override fun readMessage(message: IMessageService.Message, failed: (msg: String, message: IMessageService.Message) -> Unit, success: (message: IMessageService.Message) -> Unit) {
        CoroutineScope(mJob).retrofitByDef<ChatMessageReadStatusBo> {
            api = ApiEngine.createService(ApiChatMessage::class.java).readMessage(messageId = message.messageId)
            onFailed { _, msg, error ->
                failed.invoke(msg ?: "更新已读失败${error?.message}", message)
            }
            onSuccess {
                if (it?.readStatus == true) {
                    message.readStatus = it.readStatus
                    message.readNum = it.readNum
                    success.invoke(message)
                } else {
                    failed.invoke("更新已读失败", message)
                }
            }
        }
    }

    override fun revokeMessage(
            message: IMessageService.Message,
            failed: (msg: String, message: IMessageService.Message) -> Unit,
            success: (message: IMessageService.Message) -> Unit
    ) {
        CoroutineScope(mJob).retrofitByDef<Boolean> {
            api = ApiEngine.createService(ApiChatMessage::class.java).deleteMessage(messageId = message.messageId)
            onFailed { _, msg, error ->
                failed.invoke(msg ?: "撤回失败${error?.message}", message)
            }
            onSuccess {
                if (it == true) {
                    message.sendStatus = EnumChatSendStatus.CANCEL_OK
                    success.invoke(message)
                } else {
                    failed.invoke("撤回失败", message)
                }
            }
        }
    }

    override fun clear() {
        mPullMessageTimer?.cancel()
        mJob.cancel()
    }
}