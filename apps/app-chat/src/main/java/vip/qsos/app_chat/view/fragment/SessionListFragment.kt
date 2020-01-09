package vip.qsos.app_chat.view.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_chat_group_list.*
import qsos.lib.base.base.fragment.BaseFragment
import vip.qsos.app_chat.R
import vip.qsos.app_chat.data.entity.ChatSessionBo
import vip.qsos.app_chat.data.model.SessionListViewModelImpl
import vip.qsos.app_chat.view.adapter.ChatSessionAdapter

/**
 * @author : 华清松
 * 会话列表页面
 */
class SessionListFragment(
        override val layoutId: Int = R.layout.fragment_chat_group_list,
        override val reload: Boolean = true
) : BaseFragment() {

    private lateinit var mSessionAdapter: ChatSessionAdapter

    private val mSessionListViewModel: SessionListViewModelImpl by viewModels()

    private val mSessionList = arrayListOf<ChatSessionBo>()

    override fun initData(savedInstanceState: Bundle?) {}

    override fun initView(view: View) {

        mSessionAdapter = ChatSessionAdapter(mSessionList)

        val mLinearLayoutManager = LinearLayoutManager(mContext)
        chat_group_list.layoutManager = mLinearLayoutManager
        chat_group_list.adapter = mSessionAdapter

        mSessionListViewModel.mSessionListLiveData.observe(this, Observer {
            mSessionList.clear()
            it.data?.let { groups ->
                mSessionList.addAll(groups)
            }
            mSessionAdapter.notifyDataSetChanged()
        })

    }

    override fun getData() {
        mSessionListViewModel.getSessionList()
    }

    override fun onDestroy() {
        mSessionListViewModel.clear()
        super.onDestroy()
    }
}