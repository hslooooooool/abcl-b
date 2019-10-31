package qsos.base.chat.view.fragment

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_chat_group_list.*
import qsos.base.chat.R
import qsos.base.chat.data.entity.ChatGroup
import qsos.base.chat.data.model.DefChatGroupModelIml
import qsos.base.chat.data.model.IChatModel
import qsos.base.chat.view.adapter.ChatGroupAdapter
import qsos.lib.base.base.fragment.BaseFragment
import java.util.*
import kotlin.concurrent.timerTask

/**
 * @author : 华清松
 * 聊天群列表页面
 */
class ChatGroupListFragment(
        override val layoutId: Int = R.layout.fragment_chat_group_list,
        override val reload: Boolean = false
) : BaseFragment() {

    private var mGroupAdapter: ChatGroupAdapter? = null

    private var mChatGroupModel: IChatModel.IGroup? = null

    private val mGroupList = arrayListOf<ChatGroup>()
    private val mGetMessageTimer = Timer()

    override fun initData(savedInstanceState: Bundle?) {
        mChatGroupModel = DefChatGroupModelIml()
    }

    override fun initView(view: View) {

        mGroupAdapter = ChatGroupAdapter(mGroupList)

        val mLinearLayoutManager = LinearLayoutManager(mContext)
        chat_group_list.layoutManager = mLinearLayoutManager
        chat_group_list.adapter = mGroupAdapter

        mChatGroupModel?.mGroupListWithMeLiveData?.observe(this, Observer {
            mGroupList.clear()
            it.data?.let { groups ->
                mGroupList.addAll(groups)
            }
            mGroupAdapter?.notifyDataSetChanged()
        })

        mGetMessageTimer.schedule(timerTask {
            getData()
        }, 1000L, 1000L)
    }

    override fun getData() {
        mChatGroupModel?.getGroupListWithMe()
    }

    override fun onDestroy() {
        mChatGroupModel?.clear()
        super.onDestroy()
    }
}