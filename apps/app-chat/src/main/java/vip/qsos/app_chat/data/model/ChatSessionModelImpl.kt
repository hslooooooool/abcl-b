package vip.qsos.app_chat.data.model

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import qsos.lib.netservice.ApiEngine
import qsos.lib.netservice.data.BaseResponse
import qsos.lib.netservice.expand.retrofit
import qsos.lib.netservice.expand.retrofitWithSuccessByDef
import vip.qsos.app_chat.data.SessionApi
import vip.qsos.app_chat.data.entity.ChatMessage
import vip.qsos.app_chat.data.entity.ChatSessionBo
import kotlin.coroutines.CoroutineContext

/**
 * @author : 华清松
 */
class ChatSessionModelImpl(
        override val mJob: CoroutineContext = Dispatchers.Main + Job()
) : ChatSessionModel, ViewModel() {

    override fun findSessionOfSingle(
            sender: String,
            receiver: String,
            failed: (msg: String) -> Unit,
            success: (group: ChatSessionBo) -> Unit
    ) {
        CoroutineScope(mJob).retrofitWithSuccessByDef<ChatSessionBo> {
            api = ApiEngine.createService(SessionApi::class.java).findSessionOfSingle(
                    sender = sender, receiver = receiver
            )
            onSuccess {
                it?.let {
                    success.invoke(it)
                } ?: failed.invoke("非好友")
            }
        }
    }

    override fun getSessionById(
            sessionId: Long,
            failed: (msg: String) -> Unit,
            success: (group: ChatSessionBo) -> Unit
    ) {
        CoroutineScope(mJob).retrofitWithSuccessByDef<ChatSessionBo> {
            api = ApiEngine.createService(SessionApi::class.java).getSessionById(sessionId)
            onSuccess {
                it?.let {
                    success.invoke(it)
                } ?: failed.invoke("会话请求错误")
            }
        }
    }

    override fun createSession(
            creator: String,
            accountList: List<String>,
            message: ChatMessage?,
            failed: (msg: String) -> Unit,
            success: (group: ChatSessionBo) -> Unit
    ) {
        CoroutineScope(mJob).retrofit<BaseResponse<ChatSessionBo>> {
            api = ApiEngine.createService(SessionApi::class.java).createSession(
                    name = "測試群", creator = creator, memberList = accountList
            )
            onFailed { _, msg, error ->
                failed.invoke(msg ?: "创建失败${error.toString()}")
            }
            onSuccess {
                it?.let {
                    success.invoke(it.data!!)
                } ?: failed.invoke(it?.msg ?: "创建失败")
            }
        }
    }

    override fun addUserListToSession(
            userIdList: List<Long>, sessionId: Long,
            failed: (msg: String) -> Unit,
            success: (group: ChatSessionBo) -> Unit
    ) {
        CoroutineScope(mJob).retrofit<BaseResponse<ChatSessionBo>> {
            api = ApiEngine.createService(SessionApi::class.java).addUserListToSession(
                    sessionId = sessionId, userIdList = userIdList
            )
            onFailed { _, msg, error ->
                failed.invoke(msg ?: "添加失败${error.toString()}")
            }
            onSuccess {
                it?.let {
                    success.invoke(it.data!!)
                } ?: failed.invoke(it?.msg ?: "添加失败")
            }
        }
    }

    override fun deleteSession(sessionId: Long) {

    }

    override fun clear() {
        mJob.cancel()
    }
}