package vip.qsos.app_chat.view.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_chat_group_list.*
import qsos.lib.base.base.fragment.BaseFragment
import vip.qsos.app_chat.R
import vip.qsos.app_chat.data.entity.ChatGroupBo
import vip.qsos.app_chat.data.model.ChatGroupModel
import vip.qsos.app_chat.data.model.ChatGroupModelIml
import vip.qsos.app_chat.data.model.MainViewModel
import vip.qsos.app_chat.view.adapter.ChatGroupAdapter

/**
 * @author : 华清松
 * 聊天群列表页面
 */
class ChatGroupListFragment(
        override val layoutId: Int = R.layout.fragment_chat_group_list,
        override val reload: Boolean = true
) : BaseFragment() {

    private lateinit var mGroupAdapter: ChatGroupAdapter
    private lateinit var mChatGroupModel: ChatGroupModel
    private val mMainViewModel: MainViewModel by viewModels()
    private val mGroupList = arrayListOf<ChatGroupBo>()

    override fun initData(savedInstanceState: Bundle?) {
        mChatGroupModel = ChatGroupModelIml()
    }

    override fun initView(view: View) {

        mGroupAdapter = ChatGroupAdapter(mGroupList)

        val mLinearLayoutManager = LinearLayoutManager(mContext)
        chat_group_list.layoutManager = mLinearLayoutManager
        chat_group_list.adapter = mGroupAdapter

        mChatGroupModel.mGroupListWithMeLiveData.observe(this, Observer {
            mGroupList.clear()
            it.data?.let { groups ->
                mGroupList.addAll(groups)
                mMainViewModel.mGroupList.postValue(groups)
            }
            mGroupAdapter.notifyDataSetChanged()
        })

    }

    override fun getData() {
        mChatGroupModel.getGroupListWithMe()
    }

    override fun onDestroy() {
        mChatGroupModel.clear()
        super.onDestroy()
    }
}