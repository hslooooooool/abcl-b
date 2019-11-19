package qsos.base.chat.service

import qsos.base.chat.data.entity.ChatContent
import qsos.lib.base.utils.DateUtils
import java.util.*
import kotlin.collections.HashMap
import kotlin.concurrent.timerTask

/**
 * @author : 华清松
 * 消息服务配置
 */
class DefMessageService : AbsMessageService() {
    class DefSession(override var id: Int = 4, override var name: String = "会话1") : IMessageService.Session
    class DefMessage(
            override var messageId: Int,
            override var sessionId: Int = 4,
            override var sendUserId: Int = 3,
            override var sendUserName: String = "99976767",
            override var sendUserAvatar: String = "http://www.qsos.vip/upload/2018/11/ic_launcher20181225044818498.png",
            override var timeline: Int,
            override var content: ChatContent,
            override var createTime: String
    ) : IMessageService.Message

    private var index = 10000

    init {
        val content = HashMap<String, Any?>()
        content["contentDesc"] = "contentDesc"
        content["contentType"] = 0
        content["content"] = "自动发送文本"
        Timer().schedule(timerTask {
            notifyMessage(DefSession(), arrayListOf(
                    DefMessage(index++, timeline = index++, content = ChatContent(
                            fields = content
                    ), createTime = DateUtils.getTimeToNow(Date()))
            ))
        }, 1000L, 4000L)
    }

    override fun sendMessage(
            message: IMessageService.Message,
            failed: (msg: String, message: IMessageService.Message) -> Unit,
            success: (message: IMessageService.Message) -> Unit
    ) {


    }

    override fun revokeMessage(
            message: IMessageService.Message,
            failed: (msg: String, message: IMessageService.Message) -> Unit,
            success: (message: IMessageService.Message) -> Unit
    ) {


    }

}