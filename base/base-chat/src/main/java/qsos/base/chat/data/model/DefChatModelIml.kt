package qsos.base.chat.data.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import qsos.base.chat.data.ApiChatMessage
import qsos.base.chat.data.entity.*
import qsos.lib.netservice.ApiEngine
import qsos.lib.netservice.data.BaseHttpLiveData
import qsos.lib.netservice.data.BaseResponse
import qsos.lib.netservice.expand.retrofitWithSuccess

/**
 * @author : 华清松
 * 聊天实现默认实现
 */
class DefChatModelIml : IChatModelConfig {

    private val mJob = Dispatchers.Main + Job()

    override val mDataOfChatMessageList: BaseHttpLiveData<List<MChatMessage>> = BaseHttpLiveData()

    override fun getSessionById(sessionId: Long): ChatSession {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getMessageById(messageId: Long): ChatMessage {
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

    override fun getGroupByBySessionId(sessionId: Long): ChatGroup {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getUserListBySessionId(sessionId: Long): List<ChatUser> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getMessageListBySessionId(sessionId: Long) {
        CoroutineScope(mJob).retrofitWithSuccess<BaseResponse<List<ChatMessage>>> {
            api = ApiEngine.createService(ApiChatMessage::class.java).getMessageListBySessionId(sessionId = 1)
            onSuccess {
                it?.data?.let { list ->
                    val messages: ArrayList<MChatMessage> = arrayListOf()
                    list.forEach { message ->
                        messages.add(
                                MChatMessage(
                                        user = ChatUser(userId = 1, userName = "测试"),
                                        message = message
                                )
                        )
                        mDataOfChatMessageList.postValue(BaseResponse(
                                code = it.code, msg = it.msg, data = messages
                        ))
                    }
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

    override fun addUserListToSession(userIdList: List<Long>, sessionId: Long): ChatSession {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateGroupNotice(notice: String): ChatGroup {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateGroupName(name: String): ChatGroup {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteSession(sessionId: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteUser(sessionId: Long, userId: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteMessage(messageId: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun clear() {
        mJob.cancel()
    }
}