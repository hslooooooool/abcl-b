package vip.qsos.app_chat.data.model

import androidx.lifecycle.ViewModel
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
import vip.qsos.app_chat.data.ChatModel
import vip.qsos.app_chat.data.MainApi
import vip.qsos.app_chat.data.entity.ChatSessionBo
import kotlin.coroutines.CoroutineContext

/**
 * @author : 华清松
 */
class SessionListViewModelImpl(
        override val mJob: CoroutineContext = Dispatchers.Main + Job(),
        override val mSessionListLiveData: BaseHttpLiveData<List<ChatSessionBo>> = BaseHttpLiveData()
) : SessionListViewModel, ViewModel() {

    override fun getSessionList() {
        CoroutineScope(mJob).retrofit<BaseResponse<List<ChatSessionBo>>> {
            api = ApiEngine.createService(MainApi::class.java)
                    .getSessionList(ChatModel.mLoginUser.value!!.userId)

            onSuccess {
                it?.let {
                    mSessionListLiveData.postValue(it)
                    /**保存或更新会话数据，用于新消息获取*/
                    it.data?.forEach { session ->
                        DBChatDatabase.DefChatSessionDao.getChatSessionById(session.id) { oldSession ->
                            val newSession = DBChatSession(
                                    sessionId = session.id,
                                    lastMessageId = null,
                                    lastMessageTimeline = null,
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

    override fun clear() {
        mJob.cancel()
    }
}