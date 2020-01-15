package vip.qsos.app_chat.data.model

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import qsos.base.chat.api.MessageViewHelper
import qsos.base.chat.data.db.DBChatDatabase
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

    override fun getOldMessageBySessionId(sessionId: Long, success: (messageList: List<ChatMessageBo>) -> Unit) {
        DBChatDatabase.DefChatSessionDao.getChatSessionById(sessionId) { oldSession ->
            val nowFirstMessageTimeline = oldSession?.nowFirstMessageTimeline
            if (nowFirstMessageTimeline != null && nowFirstMessageTimeline < oldSession.nowLastMessageTimeline ?: -1) {
                CoroutineScope(mJob).retrofitByDef<List<ChatMessageBo>> {
                    /**获取当前会话下第一条已获取的消息以上20条消息*/
                    api = ApiEngine.createService(MessageApi::class.java).getMessageListBySessionIdAndTimeline(
                            sessionId = sessionId, timeline = nowFirstMessageTimeline, next = false, size = 20
                    )
                    onFailed { _, _, _ ->
                        success.invoke(arrayListOf())
                    }
                    onSuccess {
                        when {
                            it == null || it.isEmpty() -> {
                                success.invoke(arrayListOf())
                            }
                            else -> {
                                oldSession.nowFirstMessageId = it.first().messageId.toLong()
                                oldSession.nowFirstMessageTimeline = it.first().timeline
                                /**按时序正序排列*/
                                var mLastTime = ""
                                it.sortedBy { msg ->
                                    msg.timeline
                                }.forEachIndexed { index, messageBo ->
                                    /**校对时间，第一条时间显示，其余时间以上一条显示的时间差度3分钟以内，忽略（不显示）*/
                                    if (index == 0) {
                                        mLastTime = messageBo.createTime
                                    } else {
                                        val lastTime = DateUtils.strToDate(mLastTime)?.time
                                                ?: -1L
                                        val thisTime = DateUtils.strToDate(messageBo.createTime)?.time
                                                ?: -1L
                                        if (thisTime > lastTime && (thisTime - lastTime) >= MessageViewHelper.showTimeLimit) {
                                            mLastTime = messageBo.createTime
                                        } else {
                                            messageBo.createTime = ""
                                        }
                                    }
                                }

                                val array = arrayListOf<ChatMessageBo>()
                                array.addAll(it)
                                /**更新本地最新消息记录*/
                                DBChatDatabase.DefChatSessionDao.update(oldSession) { ok ->
                                    success.invoke(array)
                                    LogUtil.d("会话更新", (if (ok) "已" else "未") + "更新会话历史消息")
                                }
                            }
                        }
                    }
                }
            } else {
                success.invoke(arrayListOf())
            }
        }
    }

    override fun deleteMessage(
            message: ChatMessageBo,
            failed: (msg: String, message: ChatMessageBo) -> Unit,
            success: (message: ChatMessageBo) -> Unit
    ) {
        CoroutineScope(mJob).retrofitByDef<Boolean> {
            api = ApiEngine.createService(MessageApi::class.java).deleteMessage(
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