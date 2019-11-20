package qsos.base.chat.service

import qsos.base.chat.data.entity.ChatContent
import qsos.base.chat.data.entity.EnumChatMessageType
import qsos.base.chat.data.entity.EnumChatSendStatus
import qsos.lib.base.utils.DateUtils
import java.util.*
import kotlin.concurrent.timerTask

/**
 * @author : 华清松
 * 消息服务配置
 */
class DefMessageService : AbsMessageService() {

    class DefSession(override var sessionId: Int = 4, override var sessionName: String = "会话1") : IMessageService.Session
    class DefMessage(
            override var messageId: Int,
            override var sessionId: Int = 4,
            override var sendUserId: Int = 3,
            override var sendUserName: String = "99976767",
            override var sendUserAvatar: String = "http://www.qsos.vip/upload/2018/11/ic_launcher20181225044818498.png",
            override var timeline: Int,
            override var content: ChatContent,
            override var createTime: String,
            override var sendStatus: EnumChatSendStatus = EnumChatSendStatus.SUCCESS,
            override var readNum: Int = 1,
            override var realContent: Any? = null
    ) : IMessageService.Message

    private var index = 10000

    init {
        var mChatContent: ChatContent
        Timer().schedule(timerTask {
            index++
            mChatContent = ChatContent()
                    .create(0, "自动发送文本$index")
                    .put("realContent", "自动发送文本$index")

            notifyMessage(DefSession(), arrayListOf(
                    DefMessage(
                            index,
                            timeline = index,
                            content = mChatContent,
                            createTime = DateUtils.getTimeToNow(Date()))
            ))
        }, 1000L, 1000L)
    }

    override fun sendMessage(
            message: IMessageService.Message,
            failed: (msg: String, message: IMessageService.Message) -> Unit,
            success: (message: IMessageService.Message) -> Unit
    ) {
        when (message.content.getContentType()) {
            EnumChatMessageType.TEXT.contentType -> {

            }
        }

    }

    override fun revokeMessage(
            message: IMessageService.Message,
            failed: (msg: String, message: IMessageService.Message) -> Unit,
            success: (message: IMessageService.Message) -> Unit
    ) {


    }

}