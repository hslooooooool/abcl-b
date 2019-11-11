package qsos.base.chat.data.service

import kotlinx.coroutines.CoroutineScope
import qsos.base.chat.data.ApiChatMessage
import qsos.base.chat.data.db.DBRelationMessage
import qsos.base.chat.data.entity.MChatMessage
import qsos.lib.base.utils.LogUtil
import qsos.lib.netservice.ApiEngine
import qsos.lib.netservice.expand.retrofitByDef

class ChatPullServiceImpl : ChatBaseServiceImpl(), IChatMessageService.IChatPullSession<MChatMessage> {

    override fun pullMessage(msgForm: IChatMessageService.FormPullMsgRelation) {
        CoroutineScope(mJob).retrofitByDef<List<MChatMessage>> {
            api = ApiEngine.createService(ApiChatMessage::class.java).getMessageListBySessionIdAndTimeline(
                    sessionId = msgForm.sessionId, timeline = msgForm.timeline
            )
            onFailed { _, msg, _ ->
                LogUtil.e(msg ?: "会话 ${msgForm.sessionId} 消息拉取失败")
            }
            onSuccess {
                it?.map { msg ->
                    DBRelationMessage(
                            sessionId = msg.message.sessionId,
                            messageId = msg.message.messageId,
                            userId = msg.user.userId,
                            timeline = msg.message.sequence
                    )
                }?.let { msgList ->
                    getMessageList(msgList)
                }
            }
        }
    }

    private fun doDb(index: Int, msgList: List<MChatMessage>) {
        val size = msgList.size
        if (size > 0 && size - index > 1) {
            val msg = msgList[index]
            val timeline = msg.message.sequence
            checkTimeline(
                    msgForm = IChatMessageService.FormPullMsgRelation(sessionId = msg.message.sessionId, timeline = msg.message.sequence),
                    result = { relation ->
                        if (relation.timeline == timeline) {
                            saveMessage(msg)
                            doDb(index + 1, msgList)
                        }
                    }
            )
        }
    }
}