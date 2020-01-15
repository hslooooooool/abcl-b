package vip.qsos.app_chat.view.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_chat_friend_list.*
import qsos.lib.base.base.fragment.BaseFragment
import vip.qsos.app_chat.R
import vip.qsos.app_chat.data.entity.AppUserBo
import vip.qsos.app_chat.data.model.FriendListViewModelImpl
import vip.qsos.app_chat.view.adapter.ChatFriendAdapter

/**
 * @author : 华清松
 * 聊天好友列表页面
 */
class FriendListFragment(
        override val layoutId: Int = R.layout.fragment_chat_friend_list,
        override val reload: Boolean = true
) : BaseFragment() {

    private lateinit var mFriendAdapter: ChatFriendAdapter
    private val mFriendListViewModel: FriendListViewModelImpl by viewModels()

    private val mList = arrayListOf<AppUserBo>()

    override fun initData(savedInstanceState: Bundle?) {}

    override fun initView(view: View) {
        mFriendAdapter = ChatFriendAdapter(mList)

        val mLinearLayoutManager = LinearLayoutManager(mContext)
        chat_friend_list.layoutManager = mLinearLayoutManager
        chat_friend_list.adapter = mFriendAdapter

        mFriendListViewModel.mFriendList.observe(this, Observer {
            mList.clear()
            mList.addAll(it)
            mFriendAdapter.notifyDataSetChanged()
        })
    }

    override fun getData() {
        mFriendListViewModel.getFriendList()
    }

    override fun onDestroy() {
        mFriendListViewModel.clear()
        super.onDestroy()
    }
}