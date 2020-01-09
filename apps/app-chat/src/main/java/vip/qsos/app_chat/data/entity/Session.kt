package vip.qsos.app_chat.data.entity

import qsos.base.chat.api.MessageViewHelper

data class Session(
        override var id: String,
        override var type: Int
) : MessageViewHelper.Session