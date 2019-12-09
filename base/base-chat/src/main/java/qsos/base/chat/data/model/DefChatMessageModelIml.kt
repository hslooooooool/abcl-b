package qsos.base.chat.data.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import qsos.base.chat.data.ApiChatMessage
import qsos.base.chat.data.db.DBChatDatabase
import qsos.base.chat.data.entity.ChatMessageBo
import qsos.base.chat.data.entity.EnumChatSendStatus
import qsos.base.chat.service.IMessageListService
import qsos.lib.base.utils.DateUtils
import qsos.lib.base.utils.LogUtil
import qsos.lib.netservice.ApiEngine
import qsos.lib.netservice.expand.retrofitByDef
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

/**
 * @author : 华清松
 * 聊天消息相关接口默认实现
 */
class DefChatMessageModelIml(
        override val mJob: CoroutineContext = Dispatchers.Main + Job()
) : IChatModel.IMessage {

    /**是否正在获取新消息*/
    private var pullNewMessageIng = false

    override fun getOldMessageBySessionId(sessionId: Int, success: (messageList: List<ChatMessageBo>) -> Unit) {
        DBChatDatabase.DefChatSessionDao.getChatSessionById(sessionId) { oldSession ->
            val nowFirstMessageTimeline = oldSession?.nowFirstMessageTimeline
            if (nowFirstMessageTimeline != null && nowFirstMessageTimeline < oldSession.nowLastMessageTimeline ?: -1) {
                CoroutineScope(mJob).retrofitByDef<List<ChatMessageBo>> {
                    /**获取当前会话下第一条已获取的消息以上20条消息*/
                    api = ApiEngine.createService(ApiChatMessage::class.java).getMessageListBySessionIdAndTimeline(
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
                                oldSession.nowFirstMessageId = it.first().messageId
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
                                        if (thisTime > lastTime && (thisTime - lastTime) >= IMessageListService.showTimeLimit) {
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

    override fun getNewMessageBySessionId(sessionId: Int, success: (messageList: List<ChatMessageBo>) -> Unit) {
        if (pullNewMessageIng) {
            return
        }
        pullNewMessageIng = true
        DBChatDatabase.DefChatSessionDao.getChatSessionById(sessionId) { oldSession ->
            val nowLastMessageTimeline = oldSession?.nowLastMessageTimeline
            /**本地最新消息已获取过（!=null）,可能为-1，但依然比服务器最新消息Timeline小，则获取新的消息*/
            if (nowLastMessageTimeline != null && nowLastMessageTimeline < oldSession.lastMessageTimeline ?: -1) {
                CoroutineScope(mJob).retrofitByDef<List<ChatMessageBo>> {
                    api = ApiEngine.createService(ApiChatMessage::class.java).getMessageListBySessionIdAndTimeline(
                            sessionId = sessionId, timeline = nowLastMessageTimeline, next = true
                    )
                    onFailed { _, _, error ->
                        pullNewMessageIng = false
                        Timber.e(error)
                    }
                    onSuccess {
                        if (it == null || it.isEmpty()) {
                            pullNewMessageIng = false
                        } else {
                            oldSession.nowLastMessageId = it.last().messageId
                            oldSession.nowLastMessageTimeline = it.last().timeline
                            /**排除登录用户发送的消息并按时序正序排列*/
                            val messageList = it.filterNot { msg ->
                                msg.user.userId == IChatModel.mLoginUser.value?.userId
                            }.sortedBy { msg ->
                                msg.timeline
                            }
                            /**更新本地最新消息记录*/
                            DBChatDatabase.DefChatSessionDao.update(oldSession) { ok ->
                                pullNewMessageIng = false
                                if (messageList.isNotEmpty()) {
                                    success.invoke(messageList)
                                }
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