package qsos.base.chat.data.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import qsos.base.chat.data.ApiChatMessage
import qsos.base.chat.data.ApiChatSession
import qsos.base.chat.data.entity.*
import qsos.lib.netservice.ApiEngine
import qsos.lib.netservice.data.BaseHttpLiveData
import qsos.lib.netservice.data.BaseResponse
import qsos.lib.netservice.expand.retrofit
import qsos.lib.netservice.expand.retrofitWithLiveDataByDef
import qsos.lib.netservice.expand.retrofitWithSuccess
import kotlin.coroutines.CoroutineContext

/**
 * @author : 华清松
 * 聊天实现默认实现
 */
class DefChatModelIml : IChatModelConfig {

    override val mJob: CoroutineContext = Dispatchers.Main + Job()

    override val mDataOfChatMessageList: BaseHttpLiveData<List<MChatMessage>> = BaseHttpLiveData()
    override val mDataOfChatSession: BaseHttpLiveData<ChatSession> = BaseHttpLiveData()

    override fun getSessionById(sessionId: Int) {
        CoroutineScope(mJob).retrofitWithLiveDataByDef<ChatSession> {
            api = ApiEngine.createService(ApiChatSession::class.java).getSessionById(sessionId)
            data = mDataOfChatSession
        }
    }

    override fun getMessageById(messageId: Int): ChatMessage {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getUserById(userId: Int): ChatUser {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getGroupById(groupId: Int): ChatGroup {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getContentById(contentId: Int): ChatContent {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getGroupByBySessionId(sessionId: Int): ChatGroup {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getUserListBySessionId(sessionId: Int): List<ChatUser> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getMessageListBySessionId(sessionId: Int) {
        CoroutineScope(mJob).retrofitWithSuccess<BaseResponse<List<MChatMessage>>> {
            api = ApiEngine.createService(ApiChatMessage::class.java).getMessageListBySessionId(sessionId = 1)
            onSuccess {
                it?.data?.let { list ->
                    mDataOfChatMessageList.postValue(BaseResponse(
                            code = it.code, msg = it.msg, data = list
                    ))
                }
            }
        }
    }

    override fun getMessageListByUserId(userId: Int): List<ChatMessage> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getSessionListByUserId(userId: Int): List<ChatSession> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createUser(user: ChatUser, failed: (msg: String) -> Unit, success: (user: ChatUser) -> Unit) {
        CoroutineScope(mJob).retrofit<BaseResponse<ChatUser>> {
            api = ApiEngine.createService(ApiChatSession::class.java).createUser(user)
            onFailed { _, msg, error ->
                failed.invoke(msg ?: "创建失败${error.toString()}")
            }
            onSuccess {
                if (it?.data == null) {
                    failed.invoke("创建失败${it?.msg}")
                } else {
                    success.invoke(it.data!!)
                }
            }
        }
    }

    override fun sendMessage(message: ChatMessage): ChatMessage {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createSession(userIdList: List<Int>, message: ChatMessage?, failed: (msg: String) -> Unit, success: () -> Unit) {
        CoroutineScope(mJob).retrofit<BaseResponse<ChatSession>> {
            api = ApiEngine.createService(ApiChatSession::class.java).createSession(form = ApiChatSession.FormCreateSession(
                    userIdList = userIdList, message = message
            ))
            onFailed { _, msg, error ->
                failed.invoke(msg ?: "创建失败${error.toString()}")
            }
            onSuccess {
                success.invoke()
            }
        }
    }

    override fun addUserListToSession(userIdList: List<Int>, sessionId: Int): ChatSession {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateGroupNotice(notice: String): ChatGroup {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateGroupName(name: String): ChatGroup {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteSession(sessionId: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteUser(sessionId: Int, userId: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteMessage(messageId: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun clear() {
        mJob.cancel()
    }
}