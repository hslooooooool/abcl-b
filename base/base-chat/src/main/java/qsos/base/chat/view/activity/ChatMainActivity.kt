package qsos.base.chat.view.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.Fragment
import com.alibaba.android.arouter.facade.annotation.Route
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_chat_main.*
import kotlinx.android.synthetic.main.tab_chat.view.*
import qsos.base.chat.R
import qsos.base.chat.view.fragment.ChatGroupListFragment
import qsos.lib.base.base.activity.BaseActivity
import qsos.lib.base.base.adapter.BaseFragmentAdapter

/**
 * @author : 华清松
 * 聊天群列表页面
 */
@Route(group = "CHAT", path = "/CHAT/MAIN")
class ChatMainActivity(
        override val layoutId: Int = R.layout.activity_chat_main,
        override val reload: Boolean = false
) : BaseActivity() {

    private val fragments = arrayListOf<Fragment>()
    private var mFragmentAdapter: BaseFragmentAdapter? = null

    private var mGroupListTab: View? = null
    private var mFriendListTab: View? = null

    override fun initData(savedInstanceState: Bundle?) {
        val fragment1 = ChatGroupListFragment()
        val fragment2 = ChatGroupListFragment()
        fragments.clear()
        fragments.add(fragment1)
        fragments.add(fragment2)
        mFragmentAdapter = BaseFragmentAdapter(supportFragmentManager, fragments)

        mGroupListTab = getTabItem("群列表", R.color.red)
        mFriendListTab = getTabItem("通讯录", R.color.orange)
    }

    override fun initView() {

        chat_group_vp.adapter = mFragmentAdapter
        chat_group_vp.offscreenPageLimit = fragments.size

        chat_group_tab.addTab(chat_group_tab.newTab().setCustomView(mGroupListTab))
        chat_group_tab.addTab(chat_group_tab.newTab().setCustomView(mFriendListTab))

        chat_group_vp.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(chat_group_tab))
        chat_group_tab.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(chat_group_vp))

    }

    override fun getData() {}

    private fun getTabItem(name: String, iconID: Int?): View {
        val tabItemView = LayoutInflater.from(mContext).inflate(R.layout.tab_chat, null)
        tabItemView.tab_chat_name.text = name
        tabItemView.tab_chat_icon.setImageResource(iconID ?: R.color.orange)
        return tabItemView
    }
}