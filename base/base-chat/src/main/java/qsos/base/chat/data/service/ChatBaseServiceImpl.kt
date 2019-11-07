package qsos.base.chat.data.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import qsos.base.chat.data.ApiChatMessage
import qsos.base.chat.data.entity.MChatMessage
import qsos.lib.base.utils.LogUtil
import qsos.lib.netservice.ApiEngine
import qsos.lib.netservice.expand.retrofitByDef
import kotlin.coroutines.CoroutineContext

class ChatBaseServiceImpl : IChatMessageService.IChatBase<MChatMessage> {
    private val mJob: CoroutineContext = Dispatchers.Main + Job()

    override fun getMessageList(msgList: ArrayList<IChatMessageService.IRelation>) {
        val messageIds = arrayListOf<Int>()
        msgList.forEach {
            messageIds.add(it.messageId)
        }
        /**获取消息列表*/
        CoroutineScope(mJob).retrofitByDef<List<MChatMessage>> {
            api = ApiEngine.createService(ApiChatMessage::class.java).getMessageListByIds(messageIds = messageIds)
            onFailed { _, _, _ ->
                LogUtil.e("消息列表", "获取消息列表失败")
            }
            onSuccess {
                if (it == null) {
                    LogUtil.e("消息列表", "获取消息列表失败")
                } else {
                    /**保存到数据库*/
                    it.forEach {
                        saveMessage(it)
                    }
                    /**更新UI*/
                    notifyUI(msgList)
                }
            }
        }
    }

    override fun checkTimeline(msgForm: IChatMessageService.FormPullMsgRelation): IChatMessageService.FormPullMsgRelation {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun saveRelation(relation: IChatMessageService.IRelation) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun saveMessage(msg: MChatMessage) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun notifyUI(form: ArrayList<IChatMessageService.IRelation>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateMessageReadState() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun uploadMessageReadState() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}