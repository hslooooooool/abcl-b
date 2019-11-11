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
import qsos.base.chat.data.entity.MChatMessage
import qsos.base.core.config.BaseConfig
import qsos.lib.base.base.BaseApplication
import qsos.lib.base.utils.LogUtil
import qsos.lib.netservice.ApiEngine
import qsos.lib.netservice.expand.retrofitByDef
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
        CoroutineScope(mJob).retrofitByDef<List<ChatMessage>> {
            api = ApiEngine.createService(ApiChatMessage::class.java).getMessageListByIds(messageIds = messageIds)
            onFailed { _, _, _ ->
                LogUtil.e("消息列表", "获取消息列表失败")
            }
            onSuccess {
                if (it == null) {
                    LogUtil.e("消息列表", "获取消息列表失败")
                } else {
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

    override fun saveRelation(relation: IChatMessageService.IRelation) {
        CoroutineScope(mJob).launch {
            withContext(Dispatchers.IO) {
                ChatDatabase.getInstance(BaseApplication.appContext)
                        .relationMessageDao
                        .insert(DBRelationMessage(
                                sessionId = relation.sessionId,
                                messageId = relation.messageId,
                                userId = relation.userId,
                                timeline = relation.timeline
                        ))
                val session = ChatDatabase.getInstance(BaseApplication.appContext)
                        .sessionDao
                        .getSessionById(sessionId = relation.sessionId)
                session?.let {
                    if (session.lastTimeline < relation.timeline) {
                        session.lastMessageId = relation.messageId
                        session.lastTimeline = relation.timeline
                        ChatDatabase.getInstance(BaseApplication.appContext)
                                .sessionDao
                                .update(session)
                    }
                }
            }
        }
    }

    override fun saveMessage(msg: ChatMessage) {
        ChatDatabase.DefMessageDao.insert(
                msg = DBMessage(contentJson = Gson().toJson(msg))
        ) { msgId ->
            val sessionId = msg.sessionId
            saveRelation(DBRelationMessage(
                    messageId = msgId,
                    sessionId = sessionId,
                    userId = BaseConfig.userId,
                    timeline = msg.sequence
            ))
        }
    }

    override fun notifyUI(form: List<IChatMessageService.IRelation>) {

    }

    override fun updateMessageReadState() {

    }

    override fun uploadMessageReadState() {

    }

}