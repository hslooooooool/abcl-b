package vip.qsos.app_chat.data.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import qsos.base.chat.data.db.DBChatDatabase
import qsos.base.chat.data.db.DBChatSession
import qsos.lib.base.utils.LogUtil
import qsos.lib.netservice.ApiEngine
import qsos.lib.netservice.data.BaseHttpLiveData
import qsos.lib.netservice.data.BaseResponse
import qsos.lib.netservice.expand.retrofit
import vip.qsos.app_chat.data.entity.ChatGroupBo
import kotlin.coroutines.CoroutineContext

/**
 * @author : 华清松
 * 聊天群组相关接口默认实现
 */
class ChatGroupModelIml(
        override val mJob: CoroutineContext = Dispatchers.Main + Job(),
        override val mGroupListWithMeLiveData: BaseHttpLiveData<List<ChatGroupBo>> = BaseHttpLiveData()
) : ChatModel.IGroup {

    override fun getGroupById(groupId: Long, success: (message: ChatGroupBo) -> Unit) {
        CoroutineScope(mJob).retrofit<BaseResponse<ChatGroupBo>> {
            api = ApiEngine.createService(vip.qsos.app_chat.data.ApiChatGroup::class.java).getSessionById(groupId = groupId)
            onSuccess {
                it?.data?.let { group ->
                    success.invoke(group)
                }
            }
        }
    }

    override fun getGroupBySessionId(sessionId: Long): ChatGroupBo {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getGroupListWithMe() {
        CoroutineScope(mJob).retrofit<BaseResponse<List<ChatGroupBo>>> {
            api = ApiEngine.createService(vip.qsos.app_chat.data.ApiChatGroup::class.java).getGroupWithMe()
            onSuccess {
                it?.let {
                    mGroupListWithMeLiveData.postValue(it)
                    /**保存或更新会话数据，用于新消息获取*/
                    it.data?.forEach { group ->
                        DBChatDatabase.DefChatSessionDao.getChatSessionById(group.groupId) { oldSession ->
                            val newSession = DBChatSession(
                                    sessionId = group.groupId,
                                    lastMessageId = group.lastMessage?.messageId,
                                    lastMessageTimeline = group.lastMessage?.timeline,
                                    nowFirstMessageId = oldSession?.nowFirstMessageId,
                                    nowFirstMessageTimeline = oldSession?.nowFirstMessageTimeline,
                                    nowLastMessageId = oldSession?.nowLastMessageId,
                                    nowLastMessageTimeline = oldSession?.nowLastMessageTimeline
                            )
                            if (oldSession == null) {
                                DBChatDatabase.DefChatSessionDao.insert(newSession) { ok ->
                                    LogUtil.d("会话更新", if (ok) "已增加" else "未增加")
                                }
                            } else if (
                                    newSession.lastMessageId != null
                                    && oldSession.lastMessageId != newSession.lastMessageId
                            ) {
                                DBChatDatabase.DefChatSessionDao.update(newSession) { ok ->
                                    LogUtil.d("会话更新", if (ok) "已更新" else "未更新")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun updateGroupNotice(notice: String): ChatGroupBo {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateGroupName(name: String): ChatGroupBo {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun clear() {
        mJob.cancel()
    }
}