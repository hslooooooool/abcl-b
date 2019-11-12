package qsos.base.chat.data.service

/**
 * @author : 华清松
 * 消息更新服务
 */
interface IMessageUpdateService {

    /**获取本地会话列表*/
    fun <SESSION> getLocalSession(): List<SESSION>

    /**保存或更新本地会话列表*/
    fun <SESSION> saveLocalSession(list: List<SESSION>)

    /**获取在线会话列表并与本地进行对比最新消息情况，更新未读数*/
    fun getOnlineSession()

    /**获取新消息：从起始时间线开始往后 size 条新消息*/
    fun getNewMessage(startTimeline: Int, size: Int)

    /**保存新消息列表并更新本地会话列表，
     * 对应会话的 lastTimeline = startTimeline+size = list.last().timeline */
    fun <MESSAGE> saveNewMessage(list: List<MESSAGE>)

}