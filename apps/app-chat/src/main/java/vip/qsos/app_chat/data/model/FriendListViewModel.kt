package vip.qsos.app_chat.data.model

import androidx.lifecycle.MutableLiveData
import vip.qsos.app_chat.data.entity.ChatUser
import kotlin.coroutines.CoroutineContext

/**
 * @author : 华清松
 * 好友列表接口
 */
interface FriendListViewModel {

    val mJob: CoroutineContext
    fun clear()
    val mFriendList: MutableLiveData<List<ChatUser>>

    /**获取好友列表数据
     * @return 好友列表
     * */
    fun getFriendList()

}