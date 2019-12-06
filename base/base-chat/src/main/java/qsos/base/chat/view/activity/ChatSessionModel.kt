package qsos.base.chat.view.activity

import android.app.Activity
import android.view.View
import androidx.appcompat.widget.PopupMenu
import qsos.base.chat.R
import qsos.base.chat.service.IMessageService

/**
 * @author : 华清松
 * 聊天会话功能接口
 */
class ChatSessionModel(private val activity: Activity) : IChatSessionModel {
    override fun clickTextMessage(
            view: View, message: IMessageService.Message,
            back: (action: Int) -> Unit
    ) {
        val popup = PopupMenu(activity, view)
        val inflater = popup.menuInflater
        inflater.inflate(R.menu.chat_message_item_click, popup.menu)
        popup.menu.removeGroup(R.id.menu_message_2)
        popup.setOnMenuItemClickListener { item ->
            back.invoke(item.itemId)
            true
        }
        popup.show()
    }

    override fun longClickTextMessage(
            view: View, message: IMessageService.Message,
            back: (action: Int) -> Unit
    ) {
        val popup = PopupMenu(activity, view)
        val inflater = popup.menuInflater
        inflater.inflate(R.menu.chat_message_item_click, popup.menu)
        popup.menu.removeGroup(R.id.menu_message_1)
        when {
            message.sendUserId == ChatMainActivity.mLoginUser.value?.userId -> {
                popup.menu.removeItem(R.id.menu_message_reply)
                if (message.readNum >= 2) {
                    popup.menu.removeItem(R.id.menu_message_cancel)
                }
            }
            message.sendUserId != ChatMainActivity.mLoginUser.value?.userId -> {
                popup.menu.removeItem(R.id.menu_message_cancel)
            }
        }
        popup.setOnMenuItemClickListener { item ->
            back.invoke(item.itemId)
            true
        }
        popup.show()
    }

}