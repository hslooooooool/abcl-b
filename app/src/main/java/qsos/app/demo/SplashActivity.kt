package qsos.app.demo

import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import com.alibaba.android.arouter.launcher.ARouter
import kotlinx.android.synthetic.main.app_activity_splash.*
import kotlinx.android.synthetic.main.app_item_component.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import qsos.lib.base.base.activity.BaseActivity
import qsos.lib.base.base.adapter.BaseNormalAdapter
import qsos.lib.base.utils.ActivityManager

/**
 * @author : 华清松
 * 闪屏界面
 */
class SplashActivity(
        override val layoutId: Int = R.layout.app_activity_splash,
        override val reload: Boolean = false
) : BaseActivity() {

    private val mList = arrayListOf("朋友圈", "表单", "表单2")
    private val mJob = Dispatchers.Main + Job()

    override fun initData(savedInstanceState: Bundle?) {}

    override fun initView() {
        ActivityManager.finishAllButNotMe(this)

        splash_rv.layoutManager = GridLayoutManager(this, 3)
        splash_rv.adapter = BaseNormalAdapter(R.layout.app_item_component, mList, setHolder = { holder, data, _ ->
            holder.itemView.tv_item_component.text = data
            holder.itemView.tv_item_component.setOnClickListener {
                when (data) {
                    "朋友圈" -> {
                        ARouter.getInstance().build(AppPath.TWEET).navigation()
                    }
                }
            }
        })
        splash_rv.adapter?.notifyDataSetChanged()
    }

    override fun getData() {}
}