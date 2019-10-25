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

    override fun getUserById(userId: Long): ChatUser {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getGroupById(groupId: Long): ChatGroup {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getContentById(contentId: Long): ChatContent {
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

    override fun getMessageListByUserId(userId: Long): List<ChatMessage> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getSessionListByUserId(userId: Long): List<ChatSession> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun sendMessage(message: ChatMessage): ChatMessage {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createSession(userIdList: List<Long>, message: ChatMessage?): ChatSession {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addUserListToSession(userIdList: List<Long>, sessionId: Int): ChatSession {
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

    override fun deleteUser(sessionId: Int, userId: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteMessage(messageId: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun clear() {
        mJob.cancel()
    }
}