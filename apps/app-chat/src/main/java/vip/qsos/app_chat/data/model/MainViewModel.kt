package vip.qsos.app_chat.data.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import vip.qsos.app_chat.data.entity.ChatGroupInfo
import vip.qsos.app_chat.data.entity.ChatUser

/**
 * @author : 华清松
 * 聊天主页数据缓存
 */
class MainViewModel : ViewModel() {

    val mGroupList: MutableLiveData<List<ChatGroupInfo>> = MutableLiveData()
    val mFriendList: MutableLiveData<List<ChatUser>> = MutableLiveData()

}