package vip.qsos.app_chat.view.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.alibaba.android.arouter.facade.annotation.Route
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_main.*
import qsos.lib.base.base.activity.BaseActivity
import qsos.lib.base.base.adapter.BaseFragmentAdapter
import vip.qsos.app_chat.R
import vip.qsos.app_chat.view.fragment.FriendListFragment
import vip.qsos.app_chat.view.fragment.SessionListFragment

/**
 * @author : 华清松
 * 主页
 */
@Route(group = "APP", path = "/CHAT/MAIN")
class ChatMainActivity(
        override val layoutId: Int = R.layout.activity_main,
        override val reload: Boolean = false
) : BaseActivity() {

    private val fragments = arrayListOf<Fragment>()
    private lateinit var mFragmentAdapter: BaseFragmentAdapter

    private lateinit var mGroupListTab: View
    private lateinit var mFriendListTab: View

    override fun initData(savedInstanceState: Bundle?) {
        val fragment1 = SessionListFragment()
        val fragment2 = FriendListFragment()
        fragments.clear()
        fragments.add(fragment1)
        fragments.add(fragment2)
        mFragmentAdapter = BaseFragmentAdapter(supportFragmentManager, fragments)
        mGroupListTab = getTabItem("会话", R.color.red)
        mFriendListTab = getTabItem("好友", R.color.orange)
    }

    override fun initView() {

        main_vp.adapter = mFragmentAdapter
        main_vp.offscreenPageLimit = fragments.size

        main_tab.addTab(main_tab.newTab().setCustomView(mGroupListTab))
        main_tab.addTab(main_tab.newTab().setCustomView(mFriendListTab))

        main_vp.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(main_tab))
        main_tab.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(main_vp))
    }

    override fun getData() {}

    @SuppressLint("InflateParams")
    private fun getTabItem(name: String, iconID: Int?): View {
        val tabItemView = LayoutInflater.from(mContext).inflate(R.layout.tab_chat, null)
        tabItemView.findViewById<TextView>(R.id.tab_chat_name).text = name
        tabItemView.findViewById<ImageView>(R.id.tab_chat_icon).setImageResource(iconID
                ?: R.color.orange)
        return tabItemView
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val allFragments = supportFragmentManager.fragments
        for (fragment in allFragments) {
            fragment.onActivityResult(requestCode, resultCode, data)
        }
    }
}