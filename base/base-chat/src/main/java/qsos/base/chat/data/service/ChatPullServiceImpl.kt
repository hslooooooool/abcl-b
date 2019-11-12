package qsos.base.chat.data.service

import kotlinx.coroutines.*
import qsos.base.chat.data.ApiChatMessage
import qsos.base.chat.data.db.ChatDatabase
import qsos.base.chat.data.db.DBRelationMessage
import qsos.base.chat.data.db.DBSession
import qsos.base.chat.data.entity.ChatMessage
import qsos.lib.base.base.BaseApplication
import qsos.lib.netservice.ApiEngine
import retrofit2.await
import kotlin.coroutines.CoroutineContext

class ChatPullServiceImpl(
        override val mJob: CoroutineContext = Dispatchers.Main + Job()
) : ChatBaseServiceImpl(mJob), IChatMessageService.IChatPullSession<ChatMessage> {

    override fun pullMessage(sessionId: Int, success: () -> Unit) {
        CoroutineScope(mJob).launch {
            val call = withContext(Dispatchers.IO) {
                var session = ChatDatabase.getInstance(BaseApplication.appContext)
                        .sessionDao
                        .getSessionById(sessionId)
                if (session == null) {
                    session = DBSession(sessionId = sessionId, lastMessageId = -1, lastTimeline = -1)
                    ChatDatabase.getInstance(BaseApplication.appContext)
                            .sessionDao
                            .insert(session)
                }
                ApiEngine.createService(ApiChatMessage::class.java)
                        .getMessageListBySessionIdAndTimeline(
                                sessionId = sessionId,
                                timeline = session.lastTimeline
                        )
            }
            val result = call.await()
            result.data?.let { msgList ->
                msgList.map { msg ->
                    DBRelationMessage(
                            sessionId = msg.message.sessionId,
                            messageId = msg.message.messageId,
                            userId = msg.user.userId,
                            timeline = msg.message.sequence
                    )
                }.let { list ->
                    getMessageList(list) {
                        success.invoke()
                    }
                }
            }
        }
    }

}