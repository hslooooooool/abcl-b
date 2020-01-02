package vip.qsos.app_chat.view.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_chat_friend_list.*
import vip.qsos.app_chat.data.entity.ChatUser
import vip.qsos.app_chat.data.model.ChatUserModelIml
import vip.qsos.app_chat.data.model.ChatModel
import qsos.lib.base.base.fragment.BaseFragment
import vip.qsos.app_chat.R
import vip.qsos.app_chat.data.model.MainViewModel
import vip.qsos.app_chat.view.adapter.ChatFriendAdapter

/**
 * @author : 华清松
 * 聊天好友列表页面
 */
class ChatFriendListFragment(
        override val layoutId: Int = R.layout.fragment_chat_friend_list,
        override val reload: Boolean = true
) : BaseFragment() {

    private lateinit var mFriendAdapter: ChatFriendAdapter
    private lateinit var mChatUserModel: ChatModel.IUser
    private val mMainViewModel: MainViewModel by viewModels()
    private val mList = arrayListOf<ChatUser>()

    override fun initData(savedInstanceState: Bundle?) {
        mChatUserModel = ChatUserModelIml()
    }

    override fun initView(view: View) {

        mFriendAdapter = ChatFriendAdapter(mList)

        val mLinearLayoutManager = LinearLayoutManager(mContext)
        chat_friend_list.layoutManager = mLinearLayoutManager
        chat_friend_list.adapter = mFriendAdapter

        mChatUserModel.mDataOfChatUserList.observe(this, Observer {
            mList.clear()
            it.data?.let { users ->
                mList.addAll(users)
                mMainViewModel.mFriendList.postValue(users)
            }
            mFriendAdapter.notifyDataSetChanged()
        })

        getData()
    }

    override fun getData() {
        mChatUserModel.getAllChatUser()
    }

    override fun onDestroy() {
        mChatUserModel.clear()
        super.onDestroy()
    }
}