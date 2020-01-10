package vip.qsos.app_chat.data.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import qsos.base.core.config.BaseConfig
import qsos.lib.netservice.ApiEngine
import qsos.lib.netservice.expand.retrofitWithSuccessByDef
import vip.qsos.app_chat.data.MainApi
import vip.qsos.app_chat.data.entity.ChatUser
import kotlin.coroutines.CoroutineContext

/**
 * @author : 华清松
 */
class FriendListViewModelImpl(
        override val mJob: CoroutineContext = Dispatchers.Main + Job(),
        override val mFriendList: MutableLiveData<List<ChatUser>> = MutableLiveData()
) : FriendListViewModel, ViewModel() {

    override fun getFriendList() {
        CoroutineScope(mJob).retrofitWithSuccessByDef<List<ChatUser>> {
            api = ApiEngine.createService(MainApi::class.java)
                    .getFriendList(BaseConfig.getLoginUserId())
            onSuccess {
                it?.let { mFriendList.postValue(it) }
            }
        }
    }

    override fun clear() {
        mJob.cancel()
    }
}