package qsos.base.chat.view.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.alibaba.android.arouter.facade.annotation.Route
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_chat_main.*
import kotlinx.android.synthetic.main.tab_chat.view.*
import qsos.base.chat.R
import qsos.base.chat.data.entity.ChatUser
import qsos.base.chat.data.model.DefChatUserModelIml
import qsos.base.chat.data.model.IChatModel
import qsos.base.chat.data.service.MessageUpdateService
import qsos.base.chat.view.fragment.ChatAboutMeFragment
import qsos.base.chat.view.fragment.ChatFriendListFragment
import qsos.base.chat.view.fragment.ChatGroupListFragment
import qsos.base.core.config.BaseConfig
import qsos.lib.base.base.activity.BaseActivity
import qsos.lib.base.base.adapter.BaseFragmentAdapter
import qsos.lib.base.utils.ToastUtils

/**
 * @author : 华清松
 * 聊天群列表页面
 */
@Route(group = "CHAT", path = "/CHAT/MAIN")
class ChatMainActivity(
        override val layoutId: Int = R.layout.activity_chat_main,
        override val reload: Boolean = false
) : BaseActivity() {

    companion object {
        val mLoginUser: MutableLiveData<ChatUser> = MutableLiveData()
    }

    private val fragments = arrayListOf<Fragment>()
    private var mFragmentAdapter: BaseFragmentAdapter? = null

    private var mGroupListTab: View? = null
    private var mFriendListTab: View? = null
    private var mAboutMeTab: View? = null

    private var mChatUserModel: IChatModel.IUser? = null
    private var mMessageUpdateService: Intent? = null

    override fun initData(savedInstanceState: Bundle?) {
        val fragment1 = ChatGroupListFragment()
        val fragment2 = ChatFriendListFragment()
        val fragment3 = ChatAboutMeFragment()
        fragments.clear()
        fragments.add(fragment1)
        fragments.add(fragment2)
        fragments.add(fragment3)
        mFragmentAdapter = BaseFragmentAdapter(supportFragmentManager, fragments)

        mGroupListTab = getTabItem("群列表", R.color.red)
        mFriendListTab = getTabItem("通讯录", R.color.orange)
        mAboutMeTab = getTabItem("关于我", R.color.blue_light)

        mChatUserModel = DefChatUserModelIml()

        mMessageUpdateService = Intent(this, MessageUpdateService::class.java)
        startService(mMessageUpdateService)

    }

    override fun initView() {

        chat_group_vp.adapter = mFragmentAdapter
        chat_group_vp.offscreenPageLimit = fragments.size

        chat_group_tab.addTab(chat_group_tab.newTab().setCustomView(mGroupListTab))
        chat_group_tab.addTab(chat_group_tab.newTab().setCustomView(mFriendListTab))
        chat_group_tab.addTab(chat_group_tab.newTab().setCustomView(mAboutMeTab))

        chat_group_vp.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(chat_group_tab))
        chat_group_tab.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(chat_group_vp))

        mChatUserModel?.getUserById(
                userId = BaseConfig.userId,
                failed = {
                    ToastUtils.showToast(this, it)
                },
                success = {
                    mLoginUser.postValue(it)
                }
        )
    }

    override fun getData() {}

    private fun getTabItem(name: String, iconID: Int?): View {
        val tabItemView = LayoutInflater.from(mContext).inflate(R.layout.tab_chat, null)
        tabItemView.tab_chat_name.text = name
        tabItemView.tab_chat_icon.setImageResource(iconID ?: R.color.orange)
        return tabItemView
    }

    override fun onDestroy() {
        super.onDestroy()

        mChatUserModel?.clear()
        mMessageUpdateService?.let {
            stopService(it)
        }
    }
}