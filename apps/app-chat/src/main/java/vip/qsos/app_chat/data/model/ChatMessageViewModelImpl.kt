package vip.qsos.app_chat.data.model

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import qsos.base.chat.api.MessageViewHelper
import qsos.base.chat.data.db.DBChatDatabase
import qsos.base.chat.data.entity.ChatMessage
import qsos.base.chat.data.entity.EnumChatSendStatus
import qsos.lib.base.utils.DateUtils
import qsos.lib.base.utils.LogUtil
import qsos.lib.netservice.ApiEngine
import qsos.lib.netservice.expand.retrofitByDef
import vip.qsos.app_chat.data.api.MessageApi
import vip.qsos.app_chat.data.entity.ChatMessageBo
import kotlin.coroutines.CoroutineContext

/**
 * @author : 华清松
 */
class ChatMessageViewModelImpl(
        override val mJob: CoroutineContext = Dispatchers.Main + Job()
) : ChatMessageViewModel, ViewModel() {

    override fun getOldMessageBySessionId(sessionId: Long, success: (messageList: List<ChatMessage>) -> Unit) {
        DBChatDatabase.DefChatSessionDao.getChatSessionById(sessionId) { oldSession ->
            val nowFirstTimeline = oldSession?.nowFirstTimeline
            if (nowFirstTimeline == null || nowFirstTimeline < 1L) {
                success.invoke(arrayListOf())
            } else {
                CoroutineScope(mJob).retrofitByDef<List<ChatMessageBo>> {
                    api = ApiEngine.createService(MessageApi::class.java).getMessageListBySessionIdAndTimeline(
                            sessionId = sessionId, timeline = nowFirstTimeline - 1
                    )
                    onFailed { _, _, _ ->
                        success.invoke(arrayListOf())
                    }
                    onSuccess { list ->
                        when {
                            list == null || list.isEmpty() -> {
                                success.invoke(arrayListOf())
                            }
                            else -> {
                                val messages: List<ChatMessage> = list.map {
                                    it.decode()
                                }
                                /**按时序正序排列*/
                                var mLastTime = ""
                                messages.sortedBy { bo ->
                                    bo.timeline
                                }.forEachIndexed { index, bo ->
                                    /**校对时间，第一条时间显示，其余时间以上一条显示的时间差度3分钟以内，忽略（不显示）*/
                                    if (index == 0) {
                                        mLastTime = bo.createTime
                                    } else {
                                        val lastTime = DateUtils.strToDate(mLastTime)?.time
                                                ?: -1L
                                        val thisTime = DateUtils.strToDate(bo.createTime)?.time
                                                ?: -1L
                                        if (thisTime > lastTime && (thisTime - lastTime) >= MessageViewHelper.showTimeLimit) {
                                            mLastTime = bo.createTime
                                        } else {
                                            bo.createTime = ""
                                        }
                                    }
                                }
                                /**更新本地最新消息记录*/
                                oldSession.nowFirstTimeline = messages.first().timeline
                                DBChatDatabase.DefChatSessionDao.update(oldSession) { ok ->
                                    success.invoke(messages)
                                    LogUtil.d("会话更新", (if (ok) "已" else "未") + "更新会话历史消息")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun deleteMessage(
            message: ChatMessage,
            failed: (msg: String, message: ChatMessage) -> Unit,
            success: (message: ChatMessage) -> Unit
    ) {
        CoroutineScope(mJob).retrofitByDef<Boolean> {
            api = ApiEngine.createService(MessageApi::class.java).deleteMessage(
                    messageId = message.messageId.toLong()
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