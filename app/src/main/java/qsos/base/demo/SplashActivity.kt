package qsos.base.demo

import android.os.Bundle
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.launcher.ARouter
import kotlinx.android.synthetic.main.app_activity_splash.*
import kotlinx.android.synthetic.main.app_item_component.view.*
import qsos.base.chat.data.entity.ChatUser
import qsos.base.chat.data.model.DefChatUserModelIml
import qsos.base.chat.data.model.IChatModel
import qsos.base.core.config.BaseConfig
import qsos.core.lib.utils.dialog.AbsBottomDialog
import qsos.core.lib.utils.dialog.BottomDialog
import qsos.core.lib.utils.dialog.BottomDialogUtils
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

    private val mList = arrayListOf("聊天注册", "聊天登录", "登录")

    private var mChatUserModel: IChatModel.IUser? = null

    override fun initData(savedInstanceState: Bundle?) {
        mChatUserModel = DefChatUserModelIml()
    }

    override fun initView() {
        ActivityManager.finishAllButNotMe(this)

        splash_rv.layoutManager = GridLayoutManager(this, 3) as RecyclerView.LayoutManager?
        splash_rv.adapter = BaseNormalAdapter(R.layout.app_item_component, mList,
                setHolder = { holder, data, _ ->
                    holder.itemView.tv_item_component.text = data
                    holder.itemView.tv_item_component.setOnClickListener {
                        when (data) {
                            "聊天注册" -> {
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
                                            ToastUtils.showToastLong(this, "已注册用户" + user.userId)
                                        })
                            }
                            "聊天登录" -> {
                                mChatUserModel?.getAllChatUser()
                            }
                            "登录" -> {
                                ARouter.getInstance().build("/LOGIN/MAIN").navigation()
                            }
                        }
                    }
                })
        splash_rv.adapter?.notifyDataSetChanged()

        mChatUserModel?.mDataOfChatUserList?.observe(this, Observer { result ->
            val userList: ArrayList<ChatUser> = arrayListOf()
            result.data?.let {
                userList.addAll(it)
            }
            BottomDialogUtils.showCustomerView(
                    this, R.layout.dialog_chat_user_list,
                    object : BottomDialog.ViewListener {
                        override fun bindView(dialog: AbsBottomDialog) {
                            val mUserRecyclerView = dialog.findViewById<RecyclerView>(R.id.dialog_chat_user_list)
                            mUserRecyclerView.layoutManager = LinearLayoutManager(mContext)
                            mUserRecyclerView.adapter = BaseNormalAdapter(
                                    layoutId = R.layout.dialog_chat_user,
                                    list = userList,
                                    setHolder = { holder, data, _ ->
                                        holder.itemView.findViewById<TextView>(R.id.dialog_chat_user_name).text = data.userName
                                        holder.itemView.setOnClickListener {
                                            BaseConfig.userId = data.userId
                                            ARouter.getInstance()
                                                    .build("/CHAT/MAIN")
                                                    .navigation()
                                        }
                                    }
                            )
                        }
                    }, true)
        })
    }

    override fun getData() {}
}