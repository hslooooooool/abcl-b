package vip.qsos.app_chat.data.model

import qsos.lib.netservice.data.BaseHttpLiveData
import vip.qsos.app_chat.data.entity.ChatGroupBo
import kotlin.coroutines.CoroutineContext

/**
 * @author : 华清松
 * 群聊接口
 */
interface ChatGroupModel {

    val mJob: CoroutineContext
    fun clear()
    val mGroupListWithMeLiveData: BaseHttpLiveData<List<ChatGroupBo>>

    /**获取聊天群数据
     * @param groupId 聊天群ID
     * @param success 聊天群数据
     * */
    fun getSessionById(
            groupId: Long,
            success: (message: ChatGroupBo) -> Unit
    )

    //TODO 建立消息数据库，读取最新消息并进行未读统计后入库，消息列表与群列表从数据库获取经过排序的数据并更新未读数
    /**获取当前用户所在的所有聊天群列表数据*/
    fun getGroupListWithMe()

    /**获取群对应的聊天群数据
     * @param sessionId 群ID
     * @return 聊天群数据
     * */
    fun getGroupBySessionId(sessionId: Long): ChatGroupBo

}