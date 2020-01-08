package vip.qsos.app_chat.data.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import vip.qsos.app_chat.data.entity.ChatGroupBo
import vip.qsos.app_chat.data.entity.ChatUser

/**
 * @author : 华清松
 * 聊天主页数据缓存
 */
class MainViewModel : ViewModel() {

    val mGroupList: MutableLiveData<List<ChatGroupBo>> = MutableLiveData()
    val mFriendList: MutableLiveData<List<ChatUser>> = MutableLiveData()

}