package qsos.base.chat.data.service

import qsos.base.chat.data.entity.MChatMessage

class ChatPullServiceImpl : ChatBaseServiceImpl(), IChatMessageService.IChatPullSession<MChatMessage> {

    override fun pullMessage(msgForm: IChatMessageService.FormPullMsgRelation) {

    }

}