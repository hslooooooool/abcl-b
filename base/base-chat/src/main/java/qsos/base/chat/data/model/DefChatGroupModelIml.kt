package qsos.base.chat.data.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import qsos.base.chat.data.ApiChatGroup
import qsos.base.chat.data.db.DBChatDatabase
import qsos.base.chat.data.db.DBChatSession
import qsos.base.chat.data.entity.ChatGroup
import qsos.lib.base.utils.LogUtil
import qsos.lib.netservice.ApiEngine
import qsos.lib.netservice.data.BaseHttpLiveData
import qsos.lib.netservice.data.BaseResponse
import qsos.lib.netservice.expand.retrofit
import kotlin.coroutines.CoroutineContext

/**
 * @author : 华清松
 * 聊天群组相关接口默认实现
 */
class DefChatGroupModelIml(
        override val mJob: CoroutineContext = Dispatchers.Main + Job(),
        override val mGroupListWithMeLiveData: BaseHttpLiveData<List<ChatGroup>> = BaseHttpLiveData()
) : IChatModel.IGroup {

    override fun getGroupById(groupId: Int, success: (message: ChatGroup) -> Unit) {
        CoroutineScope(mJob).retrofit<BaseResponse<ChatGroup>> {
            api = ApiEngine.createService(ApiChatGroup::class.java).getGroupById(groupId = groupId)
            onSuccess {
                it?.data?.let { group ->
                    success.invoke(group)
                }
            }
        }
    }

    override fun getGroupBySessionId(sessionId: Int): ChatGroup {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getGroupListWithMe() {
        CoroutineScope(mJob).retrofit<BaseResponse<List<ChatGroup>>> {
            api = ApiEngine.createService(ApiChatGroup::class.java).getGroupWithMe()
            onSuccess {
                it?.let {
                    mGroupListWithMeLiveData.postValue(it)
                    /**保存或更新会话数据，用于新消息获取*/
                    it.data?.forEach { group ->
                        DBChatDatabase.DefChatSessionDao.getChatSessionById(group.groupId) { oldSession ->
                            val newSession = DBChatSession(
                                    sessionId = group.groupId,
                                    lastMessageId = group.lastMessage?.message?.messageId,
                                    lastMessageTimeline = group.lastMessage?.message?.timeline,
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

    override fun updateGroupNotice(notice: String): ChatGroup {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateGroupName(name: String): ChatGroup {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun clear() {
        mJob.cancel()
    }
}