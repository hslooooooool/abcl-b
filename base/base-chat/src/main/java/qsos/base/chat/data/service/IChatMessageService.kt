package qsos.base.chat.data.service

/**
 * @author : 华清松
 * 消息服务接口配置
 */
interface IChatMessageService {

    /**消息发送服务*/
    interface IChatSend<MSG> : IChatBase<MSG> {

        fun send(
                msg: MSG,
                failed: (notice: String, msg: MSG) -> Unit,
                success: (msg: MSG) -> Unit
        )
    }

    /**处理所有消息服务*/
    interface IChatPullAll<MSG> : IChatBase<MSG> {

        /**拉取最新消息*/
        fun pullMessage(msgForms: ArrayList<FormPullMsgRelation>)

    }

    /**处理会话内消息服务*/
    interface IChatPullSession<MSG> : IChatBase<MSG> {

        /**拉取最新消息*/
        fun pullMessage(sessionId: Int, success: () -> Unit)

    }

    /**消息通用服务*/
    interface IChatBase<MSG> {

        /**获取消息列表数据*/
        fun getMessageList(msgList: List<IRelation>, success: () -> Unit)

        /**检查消息时序正确性*/
        fun checkTimeline(msgForm: FormPullMsgRelation, result: (msgForm: FormPullMsgRelation) -> Unit)

        /**保存消息关系数据，涵盖：会话ID、消息ID、发送人ID、时序*/
        fun saveRelation(relation: IRelation)

        /**保存消息数据，涵盖：会话ID、消息内容、发送人、时序、读取状态*/
        fun saveMessage(msg: MSG)

        /**通知UI更新消息数据，根据展示需求自行从数据库获取对应数据后更新UI*/
        fun notifyUI(form: List<IRelation>)

        /**更新消息读取状态*/
        fun updateMessageReadState()

        /**上传消息读取状态*/
        fun uploadMessageReadState()
    }

    /**获取消息传参
     * @param sessionId 会话ID
     * @param timeline 会话最新时序
     * */
    data class FormPullMsgRelation(val sessionId: Int, var timeline: Int)

    data class FormPullMsgList(val msgIds: ArrayList<Int>)

    /**新消息更新传参，会话ID、消息ID、发送人ID、时序*/
    interface IRelation {
        val sessionId: Int
        val messageId: Int
        val userId: Int
        val timeline: Int
    }
}