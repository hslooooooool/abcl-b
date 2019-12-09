package qsos.base.demo

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.alibaba.android.arouter.launcher.ARouter
import qsos.lib.base.base.activity.BaseActivity
import qsos.lib.base.utils.ActivityManager

/**
 * @author : 华清松
 * 闪屏界面
 */
class SplashActivity(
        override val layoutId: Int = R.layout.activity_splash,
        override val reload: Boolean = false
) : BaseActivity() {

    override fun initData(savedInstanceState: Bundle?) {
    }

    override fun initView() {
        ActivityManager.finishAllButNotMe(this)

        Handler(Looper.getMainLooper()).postDelayed({
            ARouter.getInstance().build("/LOGIN/MAIN").navigation()
            finish()
        }, 2000)
    }

    override fun getData() {}
}