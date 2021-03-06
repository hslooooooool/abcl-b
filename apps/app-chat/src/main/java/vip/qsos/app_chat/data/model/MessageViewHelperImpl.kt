package vip.qsos.app_chat.data.model

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import qsos.base.chat.api.MessageViewHelper
import qsos.base.chat.data.db.DBChatDatabase
import qsos.base.chat.data.entity.EnumChatSendStatus
import qsos.base.chat.view.IMessageListView
import qsos.lib.base.utils.DateUtils
import qsos.lib.base.utils.LogUtil
import qsos.lib.netservice.ApiEngine
import qsos.lib.netservice.data.BaseResponse
import qsos.lib.netservice.expand.retrofitByDef
import qsos.lib.netservice.expand.retrofitWithSuccess
import vip.qsos.app_chat.data.api.MessageApi
import vip.qsos.app_chat.data.entity.ChatMessageBo
import vip.qsos.app_chat.data.entity.ChatMessageReadStatusBo
import vip.qsos.app_chat.data.entity.ChatMessageSendBo
import java.util.*
import kotlin.coroutines.CoroutineContext

/**
 * @author : 华清松
 * 消息服务配置
 */
class MessageViewHelperImpl(
        private val mJob: CoroutineContext = Dispatchers.Main + Job(),
        override var mUpdateShowMessageList: MutableLiveData<List<MessageViewHelper.Message>> = MutableLiveData()
) : MessageViewHelper {
    private var mUpdateShowMessageTimer: Timer = Timer()

    override fun getMessageListBySessionId(
            session: MessageViewHelper.Session,
            messageList: MutableLiveData<ArrayList<MessageViewHelper.Message>>
    ) {
        DBChatDatabase.DefChatSessionDao.getChatSessionById(session.id.toLong()) { oldSession ->
            oldSession?.let {
                val lastTimeline: Long = it.lastTimeline ?: -1L
                CoroutineScope(mJob).retrofitWithSuccess<BaseResponse<List<ChatMessageBo>>> {
                    /**获取此会话sessionId下最后一条消息lastTimeline及其以上20条数据*/
                    api = ApiEngine.createService(MessageApi::class.java).getMessageListBySessionIdAndTimeline(
                            sessionId = session.id.toLong(), timeline = lastTimeline + 1
                    )
                    onSuccess { result ->
                        result?.data?.let { list ->
                            var mLastTime = ""
                            val messages = list.map { bo ->
                                bo.decode()
                            }
                            messages.sortedBy { msg ->
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

                            val array = arrayListOf<MessageViewHelper.Message>()
                            array.addAll(messages)
                            /**更新当前会话消息时序记录*/
                            if (array.isEmpty()) {
                                oldSession.lastTimeline = null
                                oldSession.nowFirstTimeline = null
                            } else {
                                oldSession.lastTimeline = array.last().timeline
                                oldSession.nowFirstTimeline = array.first().timeline
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
            message: MessageViewHelper.Message,
            failed: (msg: String, message: MessageViewHelper.Message) -> Unit,
            success: (oldMessageId: String, message: MessageViewHelper.Message) -> Unit
    ) {
        val oldMessageId = message.messageId
        if (TextUtils.isEmpty(oldMessageId)) {
            message.sendStatus = EnumChatSendStatus.FAILED
            failed.invoke("发送失败，消息临时ID不能为空", message)
            return
        }
        CoroutineScope(mJob).retrofitByDef<ChatMessageSendBo> {
            api = ApiEngine.createService(MessageApi::class.java).sendMessage(
                    sessionId = message.sessionId.toLong(),
                    contentType = message.content.type,
                    data = message.content.getContent(),
                    sender = message.sendUserAccount
            )
            onFailed { _, msg, error ->
                message.sendStatus = EnumChatSendStatus.FAILED
                failed.invoke(msg ?: "发送失败${error?.message}", message)
            }
            onSuccess {
                if (it == null) {
                    message.sendStatus = EnumChatSendStatus.FAILED
                    failed.invoke("发送失败", message)
                } else {
                    message.updateSendState(it.messageId.toString(), it.timeline, EnumChatSendStatus.SUCCESS)
                    DBChatDatabase.DefChatSessionDao.update(message.sessionId.toLong(), it.timeline) { ok ->
                        success.invoke(oldMessageId, message)
                        LogUtil.d("会话更新", (if (ok) "已" else "未") + "更新会话最新消息")
                    }
                }
            }
        }
    }

    override fun readMessage(message: MessageViewHelper.Message, failed: (msg: String, message: MessageViewHelper.Message) -> Unit, success: (message: MessageViewHelper.Message) -> Unit) {
        if (message.readStatus == false) {
            CoroutineScope(mJob).retrofitByDef<ChatMessageReadStatusBo> {
                api = ApiEngine.createService(MessageApi::class.java).readMessage(messageId = message.messageId.toLong())
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
            message: MessageViewHelper.Message,
            failed: (msg: String, message: MessageViewHelper.Message) -> Unit,
            success: (message: MessageViewHelper.Message) -> Unit
    ) {
        CoroutineScope(mJob).retrofitByDef<Boolean> {
            api = ApiEngine.createService(MessageApi::class.java).deleteMessage(messageId = message.messageId.toLong())
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
        // FIXME 无用 messageListView.getShowMessageList()
    }

    override fun clear() {
        mJob.cancel()
        mUpdateShowMessageTimer.cancel()
    }
}