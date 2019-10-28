package qsos.base.demo

import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import com.alibaba.android.arouter.launcher.ARouter
import kotlinx.android.synthetic.main.app_activity_splash.*
import kotlinx.android.synthetic.main.app_item_component.view.*
import qsos.base.chat.data.entity.ChatUser
import qsos.base.chat.data.model.DefChatUserModelIml
import qsos.base.chat.data.model.IChatModel
import qsos.base.core.config.BaseConfig
import qsos.lib.base.base.activity.BaseActivity
import qsos.lib.base.base.adapter.BaseNormalAdapter
import qsos.lib.base.utils.ActivityManager
import qsos.lib.base.utils.ToastUtils

/**
 * @author : 华清松
 * 闪屏界面
 */
class SplashActivity(
        override val layoutId: Int = R.layout.app_activity_splash,
        override val reload: Boolean = false
) : BaseActivity() {

    private val mList = arrayListOf("聊天")

    private var mChatUserModel: IChatModel.IUser? = null

    override fun initData(savedInstanceState: Bundle?) {
        mChatUserModel = DefChatUserModelIml()
    }

    override fun initView() {
        ActivityManager.finishAllButNotMe(this)

        splash_rv.layoutManager = GridLayoutManager(this, 3)
        splash_rv.adapter = BaseNormalAdapter(R.layout.app_item_component, mList, setHolder = { holder, data, _ ->
            holder.itemView.tv_item_component.text = data
            holder.itemView.tv_item_component.setOnClickListener {
                when (data) {
                    "聊天" -> {
                        mChatUserModel?.createUser(
                                user = ChatUser(
                                        userName = "测试用户" + System.currentTimeMillis(),
                                        avatar = "http://www.qsos.vip/upload/2018/11/ic_launcher20181225044818498.png",
                                        birth = "2019-11-11 11:11:11",
                                        sexuality = true
                                ),
                                failed = {
                                    ToastUtils.showToastLong(this, it)
                                },
                                success = { user ->
                                    BaseConfig.userId = user.userId
                                    ARouter.getInstance()
                                            .build("/CHAT/MAIN")
                                            .navigation()
                                })
                    }
                }
            }
        })
        splash_rv.adapter?.notifyDataSetChanged()
    }

    override fun getData() {}
}