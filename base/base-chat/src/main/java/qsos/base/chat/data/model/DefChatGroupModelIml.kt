package qsos.base.chat.data.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import qsos.base.chat.data.ApiChatGroup
import qsos.base.chat.data.entity.ChatGroup
import qsos.lib.netservice.ApiEngine
import qsos.lib.netservice.data.BaseHttpLiveData
import qsos.lib.netservice.expand.retrofitWithLiveDataByDef
import kotlin.coroutines.CoroutineContext

/**
 * @author : 华清松
 * 聊天群组相关接口默认实现
 */
class DefChatGroupModelIml(
        override val mJob: CoroutineContext = Dispatchers.Main + Job(),
        override val mGroupListWithMeLiveData: BaseHttpLiveData<List<ChatGroup>> = BaseHttpLiveData()
) : IChatModel.IGroup {


    override fun getGroupById(groupId: Int): ChatGroup {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getGroupByBySessionId(sessionId: Int): ChatGroup {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getGroupListWithMe() {
        CoroutineScope(mJob).retrofitWithLiveDataByDef<List<ChatGroup>> {
            api = ApiEngine.createService(ApiChatGroup::class.java).getGroupWithMe()
            data = mGroupListWithMeLiveData
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