package qsos.base.chat.view

import qsos.base.chat.api.MessageViewHelper

/**
 * @author : 华清松
 * 聊天消息列表页功能接口
 */
interface IMessageListView {

    /**发送消息，自动滑到底部
     * @param msg 消息数据
     * @param new 是否新消息（新消息需要上屏），反之为文件类消息，上传成功后执行发送消息动作，前期已上屏
     * */
    fun sendMessage(msg: MessageViewHelper.Message, new: Boolean)

    /**刷新所有消息*/
    fun refreshMessage(data: ArrayList<MessageViewHelper.Message>)

    /**更新消息状态（发送状态、读取状态）*/
    fun notifyMessage(oldMessageId: String, message: MessageViewHelper.Message)

    /**追加历史消息*/
    fun notifyOldMessage(messageList: List<MessageViewHelper.Message>)

    /**追加新消息
     * @param toBottom 更新后是否自动滑到底部
     * */
    fun addNewMessage(message: MessageViewHelper.Message, toBottom: Boolean = true)

    /**更新文件消息状态*/
    fun notifyFileMessage(message: MessageViewHelper.Message)

    /**更新当前用户消息读取状态*/
    fun readMessage(adapterPosition: Int)

    /**消息列表滚动到底部*/
    fun scrollToBottom()

    /**当前页面显示的消息列表*/
    fun getShowMessageList(): List<MessageViewHelper.Message>

}