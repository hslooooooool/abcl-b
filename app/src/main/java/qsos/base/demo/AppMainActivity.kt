package qsos.base.demo

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
import qsos.base.chat.data.model.DefChatUserModelIml
import qsos.base.chat.data.model.IChatModel
import qsos.base.chat.view.fragment.ChatFriendListFragment
import qsos.base.chat.view.fragment.ChatGroupListFragment
import qsos.base.core.config.BaseConfig
import qsos.base.user.view.fragment.UserCenterFragment
import qsos.lib.base.base.activity.BaseActivity
import qsos.lib.base.base.adapter.BaseFragmentAdapter
import qsos.lib.base.utils.ToastUtils

/**
 * @author : 华清松
 * 聊天群列表页面
 */
@Route(group = "APP", path = "/APP/MAIN")
class AppMainActivity(
        override val layoutId: Int = R.layout.activity_main,
        override val reload: Boolean = false
) : BaseActivity() {

    private val fragments = arrayListOf<Fragment>()
    private var mFragmentAdapter: BaseFragmentAdapter? = null

    private var mGroupListTab: View? = null
    private var mFriendListTab: View? = null
    private var mUserCenterTab: View? = null

    private var mChatUserModel: IChatModel.IUser? = null

    override fun initData(savedInstanceState: Bundle?) {
        mChatUserModel = DefChatUserModelIml()

        val fragment1 = ChatGroupListFragment()
        val fragment2 = ChatFriendListFragment()
        val fragment3 = UserCenterFragment(mChatUserModel!!.mJob)
        fragments.clear()
        fragments.add(fragment1)
        fragments.add(fragment2)
        fragments.add(fragment3)
        mFragmentAdapter = BaseFragmentAdapter(supportFragmentManager, fragments)

        mGroupListTab = getTabItem("群列表", R.color.red)
        mFriendListTab = getTabItem("通讯录", R.color.orange)
        mUserCenterTab = getTabItem("我的", R.color.yellow)

    }

    override fun initView() {

        main_vp.adapter = mFragmentAdapter
        main_vp.offscreenPageLimit = fragments.size

        main_tab.addTab(main_tab.newTab().setCustomView(mGroupListTab))
        main_tab.addTab(main_tab.newTab().setCustomView(mFriendListTab))
        main_tab.addTab(main_tab.newTab().setCustomView(mUserCenterTab))

        main_vp.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(main_tab))
        main_tab.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(main_vp))

        mChatUserModel?.getUserById(
                userId = BaseConfig.userId,
                failed = {
                    ToastUtils.showToast(this, it)
                },
                success = {
                    IChatModel.mLoginUser.postValue(it)
                }
        )
    }

    override fun getData() {}

    private fun getTabItem(name: String, iconID: Int?): View {
        val tabItemView = LayoutInflater.from(mContext).inflate(R.layout.tab_chat, null)
        tabItemView.findViewById<TextView>(R.id.tab_chat_name).text = name
        tabItemView.findViewById<ImageView>(R.id.tab_chat_icon).setImageResource(iconID
                ?: R.color.orange)
        return tabItemView
    }

    override fun onDestroy() {
        mChatUserModel?.clear()
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val allFragments = supportFragmentManager.fragments
        for (fragment in allFragments) {
            fragment.onActivityResult(requestCode, resultCode, data)
        }
    }
}