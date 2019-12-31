package qsos.base.chat.data.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import qsos.base.chat.data.ApiChatSession
import qsos.base.chat.data.entity.ChatMessage
import qsos.base.chat.data.entity.ChatSession
import qsos.lib.netservice.ApiEngine
import qsos.lib.netservice.data.BaseResponse
import qsos.lib.netservice.expand.retrofit
import qsos.lib.netservice.expand.retrofitWithSuccessByDef
import kotlin.coroutines.CoroutineContext

/**
 * @author : 华清松
 * 聊天会话接口默认实现
 */
class DefChatSessionModelIml(
        override val mJob: CoroutineContext = Dispatchers.Main + Job()
) : IChatModel.ISession {

    override fun getSessionById(
            sessionId: Int,
            failed: (msg: String) -> Unit,
            success: (session: ChatSession) -> Unit
    ) {
        CoroutineScope(mJob).retrofitWithSuccessByDef<ChatSession> {
            api = ApiEngine.createService(ApiChatSession::class.java).getSessionById(
                    sessionId = sessionId
            )
            onSuccess {
                it?.let {
                    success.invoke(it)
                } ?: failed.invoke("会话请求错误")
            }
        }
    }

    override fun createSession(
            userIdList: List<Long>,
            message: ChatMessage?,
            failed: (msg: String) -> Unit,
            success: (session: ChatSession) -> Unit
    ) {
        CoroutineScope(mJob).retrofit<BaseResponse<ChatSession>> {
            api = ApiEngine.createService(ApiChatSession::class.java).createSession(
                    form = ApiChatSession.FormCreateSession(
                            userIdList = userIdList, message = message
                    )
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

    override fun getSessionListByUserId(userId: Long): List<ChatSession> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addUserListToSession(
            userIdList: List<Long>, sessionId: Int,
            failed: (msg: String) -> Unit,
            success: (session: ChatSession) -> Unit
    ) {
        CoroutineScope(mJob).retrofit<BaseResponse<ChatSession>> {
            api = ApiEngine.createService(ApiChatSession::class.java).addUserListToSession(
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

    override fun deleteSession(sessionId: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun clear() {
        mJob.cancel()
    }
}