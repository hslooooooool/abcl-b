package vip.qsos.app_chat.data.model

import qsos.lib.netservice.data.BaseHttpLiveData
import vip.qsos.app_chat.data.entity.ChatSessionBo
import kotlin.coroutines.CoroutineContext

/**
 * @author : 华清松
 * 会话列表接口
 */
interface SessionListViewModel {

    val mJob: CoroutineContext
    fun clear()
    val mSessionListLiveData: BaseHttpLiveData<List<ChatSessionBo>>

    //TODO 建立消息数据库，读取最新消息并进行未读统计后入库，消息列表与群列表从数据库获取经过排序的数据并更新未读数
    /**获取当前用户所在的所有聊天群列表数据*/
    fun getSessionList()

}