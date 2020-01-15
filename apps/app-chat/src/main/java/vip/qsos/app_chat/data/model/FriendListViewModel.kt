package vip.qsos.app_chat.data.model

import androidx.lifecycle.MutableLiveData
import vip.qsos.app_chat.data.entity.AppUserBo
import kotlin.coroutines.CoroutineContext

/**
 * @author : 华清松
 * 好友列表接口
 */
interface FriendListViewModel {

    val mJob: CoroutineContext
    fun clear()
    val mFriendList: MutableLiveData<List<AppUserBo>>

    /**获取好友列表数据
     * @return 好友列表
     * */
    fun getFriendList()

}