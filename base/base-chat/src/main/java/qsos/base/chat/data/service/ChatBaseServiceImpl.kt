package qsos.base.chat.data.service

import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import qsos.base.chat.data.ApiChatMessage
import qsos.base.chat.data.db.ChatDatabase
import qsos.base.chat.data.db.DBMessage
import qsos.base.chat.data.db.DBRelationMessage
import qsos.base.chat.data.entity.ChatMessage
import qsos.base.core.config.BaseConfig
import qsos.lib.base.base.BaseApplication
import qsos.lib.base.utils.LogUtil
import qsos.lib.netservice.ApiEngine
import retrofit2.await
import kotlin.coroutines.CoroutineContext

abstract class ChatBaseServiceImpl(
        open val mJob: CoroutineContext
) : IChatMessageService.IChatBase<ChatMessage> {

    override fun getMessageList(msgList: List<IChatMessageService.IRelation>, success: () -> Unit) {
        val messageIds = arrayListOf<Int>()
        msgList.forEach {
            messageIds.add(it.messageId)
        }
        /**获取消息列表*/
        CoroutineScope(mJob).launch(Dispatchers.IO) {
            val api = ApiEngine.createService(ApiChatMessage::class.java)
                    .getMessageListByIds(messageIds = messageIds)
            val result = api.await()
            result.data?.let {
                LogUtil.i("消息列表", "获取新消息列表成功")
                /**保存到数据库*/
                it.forEach { msg ->
                    saveMessage(msg)
                }
                /**更新UI*/
                notifyUI(msgList)

                /**请求处理成功*/
                success.invoke()
            }
        }
    }

    override fun checkTimeline(
            msgForm: IChatMessageService.FormPullMsgRelation,
            result: (msgForm: IChatMessageService.FormPullMsgRelation) -> Unit
    ) {
        val mMsgForm: IChatMessageService.FormPullMsgRelation = msgForm
        CoroutineScope(mJob).launch {
            withContext(Dispatchers.IO) {
                ChatDatabase.getInstance(BaseApplication.appContext)
                        .sessionDao
                        .getSessionById(msgForm.sessionId)
            }?.let {
                mMsgForm.timeline = it.lastTimeline
            }
            result.invoke(mMsgForm)
        }
    }

    override fun saveMessage(msg: ChatMessage) {
        val msgId = ChatDatabase.getInstance(BaseApplication.appContext).messageDao
                .insert(DBMessage(messageId = msg.messageId, contentJson = Gson().toJson(msg))).toInt()
        val sessionId = msg.sessionId
        saveRelation(DBRelationMessage(
                messageId = msgId,
                sessionId = sessionId,
                userId = BaseConfig.userId,
                timeline = msg.sequence
        ))
    }

    override fun saveRelation(relation: IChatMessageService.IRelation) {
        ChatDatabase.getInstance(BaseApplication.appContext)
                .relationMessageDao
                .insert(DBRelationMessage(
                        sessionId = relation.sessionId,
                        messageId = relation.messageId,
                        userId = relation.userId,
                        timeline = relation.timeline
                ))
        ChatDatabase.getInstance(BaseApplication.appContext)
                .sessionDao
                .getSessionById(sessionId = relation.sessionId)?.let {
                    if (it.lastTimeline < relation.timeline) {
                        it.lastMessageId = relation.messageId
                        it.lastTimeline = relation.timeline
                        ChatDatabase.getInstance(BaseApplication.appContext)
                                .sessionDao
                                .update(it)
                    }
                }
    }

    override fun notifyUI(form: List<IChatMessageService.IRelation>) {

    }

    override fun updateMessageReadState() {

    }

    override fun uploadMessageReadState() {

    }

}