package qsos.base.chat.data.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import qsos.base.chat.data.ApiChatGroup
import qsos.base.chat.data.entity.ChatGroup
import qsos.lib.netservice.ApiEngine
import qsos.lib.netservice.data.BaseHttpLiveData
import qsos.lib.netservice.data.BaseResponse
import qsos.lib.netservice.expand.retrofit
import kotlin.coroutines.CoroutineContext

/**
 * @author : 华清松
 * 聊天群组相关接口默认实现
 */
class DefChatGroupModelIml(
        override val mJob: CoroutineContext = Dispatchers.Main + Job(),
        override val mGroupListWithMeLiveData: BaseHttpLiveData<List<ChatGroup>> = BaseHttpLiveData()
) : IChatModel.IGroup {

    override fun getGroupById(groupId: Int, success: (message: ChatGroup) -> Unit) {
        CoroutineScope(mJob).retrofit<BaseResponse<ChatGroup>> {
            api = ApiEngine.createService(ApiChatGroup::class.java).getGroupById(groupId = groupId)
            onSuccess {
                it?.data?.let { group ->
                    success.invoke(group)
                }
            }
        }
    }

    override fun getGroupBySessionId(sessionId: Int): ChatGroup {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getGroupListWithMe() {
        CoroutineScope(mJob).retrofit<BaseResponse<List<ChatGroup>>> {
            api = ApiEngine.createService(ApiChatGroup::class.java).getGroupWithMe()
            onSuccess {
                it?.let {
                    mGroupListWithMeLiveData.postValue(it)
                    // TODO 查询各个群下未读消息列表listA按消息序列排序，以各群最新消息的消息序列减去listA最早一条消息的序列，将得到未读消息数，更新

                    // TODO 开启消息获取服务，通知群内新消息上屏，通知群外新消息提醒

                }
            }
        }
    }

    override fun updateGroupNotice(notice: String): ChatGroup {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateGroupName(name: String): ChatGroup {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun clear() {
        mJob.cancel()
    }
}