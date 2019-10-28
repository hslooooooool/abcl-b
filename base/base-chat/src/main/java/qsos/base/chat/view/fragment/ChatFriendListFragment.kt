package qsos.base.chat.view.fragment

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.fragment_chat_friend_list.*
import qsos.base.chat.R
import qsos.base.chat.data.entity.ChatUser
import qsos.base.chat.data.model.DefChatUserModelIml
import qsos.base.chat.data.model.IChatModel
import qsos.base.chat.view.adapter.ChatFriendAdapter
import qsos.lib.base.base.fragment.BaseFragment

/**
 * @author : 华清松
 * 聊天好友列表页面
 */
class ChatFriendListFragment(
        override val layoutId: Int = R.layout.fragment_chat_friend_list,
        override val reload: Boolean = false
) : BaseFragment() {

    private var mFriendAdapter: ChatFriendAdapter? = null

    private var mChatUserModel: IChatModel.IUser? = null

    private val mList = arrayListOf<ChatUser>()

    override fun initData(savedInstanceState: Bundle?) {
        mChatUserModel = DefChatUserModelIml()
    }

    override fun initView(view: View) {

        mFriendAdapter = ChatFriendAdapter(mList)

        val mLinearLayoutManager = GridLayoutManager(mContext, 2)
        chat_friend_list.layoutManager = mLinearLayoutManager
        chat_friend_list.adapter = mFriendAdapter

        mChatUserModel?.mDataOfChatUserList?.observe(this, Observer {
            mList.clear()
            it.data?.let { users ->
                mList.addAll(users)
            }
            mFriendAdapter?.notifyDataSetChanged()
        })

        getData()
    }

    override fun getData() {
        mChatUserModel?.getAllChatUser()
    }

    override fun onDestroy() {
        mChatUserModel?.clear()
        super.onDestroy()
    }
}