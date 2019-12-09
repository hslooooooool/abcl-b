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
import qsos.base.chat.view.IMessageListView
import qsos.lib.base.utils.DateUtils
import qsos.lib.base.utils.LogUtil
import qsos.lib.netservice.ApiEngine
import qsos.lib.netservice.data.BaseResponse
import qsos.lib.netservice.expand.retrofitByDef
import qsos.lib.netservice.expand.retrofitWithSuccess
import qsos.lib.netservice.expand.retrofitWithSuccessByDef
import java.util.*
import kotlin.concurrent.timerTask
import kotlin.coroutines.CoroutineContext

/**
 * @author : 华清松
 * 消息服务配置
 */
class DefMessageListService(
        private val mJob: CoroutineContext = Dispatchers.Main + Job(),
        override var mUpdateShowMessageList: MutableLiveData<List<IMessageListService.Message>> = MutableLiveData()
) : IMessageListService {
    private var mUpdateShowMessageTimer: Timer = Timer()

    override fun getMessageListBySessionId(
            session: IMessageListService.Session,
            messageList: MutableLiveData<ArrayList<IMessageListService.Message>>
    ) {
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
                            var mLastTime = ""
                            list.sortedBy { msg ->
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

                            val array = arrayListOf<IMessageListService.Message>()
                            array.addAll(list)
                            /**更新当前会话消息时序记录*/
                            if (array.isEmpty()) {
                                oldSession.nowFirstMessageId = -1
                                oldSession.nowFirstMessageTimeline = -1
                                oldSession.nowLastMessageId = -1
                                oldSession.nowLastMessageTimeline = -1
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
            message: IMessageListService.Message,
            failed: (msg: String, message: IMessageListService.Message) -> Unit,
            success: (oldMessageId: Int, message: IMessageListService.Message) -> Unit
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

    override fun readMessage(message: IMessageListService.Message, failed: (msg: String, message: IMessageListService.Message) -> Unit, success: (message: IMessageListService.Message) -> Unit) {
        if (message.readStatus == false) {
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
    }

    override fun revokeMessage(
            message: IMessageListService.Message,
            failed: (msg: String, message: IMessageListService.Message) -> Unit,
            success: (message: IMessageListService.Message) -> Unit
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

    override fun updateShowMessage(messageListView: IMessageListView) {
        mUpdateShowMessageTimer.schedule(timerTask {
            messageListView.getShowMessageList().also {
                if (it.isNotEmpty()) {
                    val messageIdList = arrayListOf<Int>()
                    it.forEach { msg ->
                        messageIdList.add(msg.messageId)
                    }
                    CoroutineScope(mJob).retrofitWithSuccessByDef<List<ChatMessageBo>> {
                        api = ApiEngine.createService(ApiChatMessage::class.java).getMessageListByIds(messageIds = messageIdList)
                        onSuccess { list ->
                            list?.let {
                                mUpdateShowMessageList.postValue(list)
                            }
                        }
                    }
                }
            }
        }, 2000L, 2000L)
    }

    override fun clear() {
        mJob.cancel()
        mUpdateShowMessageTimer.cancel()
    }
}