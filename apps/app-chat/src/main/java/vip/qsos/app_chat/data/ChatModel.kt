package vip.qsos.app_chat.data

import androidx.lifecycle.MutableLiveData
import vip.qsos.app_chat.data.entity.LoginUser

/**
 * @author : 华清松
 * 聊天接口定义
 */
object ChatModel {

    val mLoginUser: MutableLiveData<LoginUser> = MutableLiveData()

}