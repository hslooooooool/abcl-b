package vip.qsos.app_chat.view.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.alibaba.android.arouter.launcher.ARouter
import qsos.lib.base.utils.ActivityManager
import vip.qsos.app_chat.BuildConfig
import vip.qsos.app_chat.R
import vip.qsos.app_chat.config.Constants
import vip.qsos.im.lib.IMManagerHelper

/**
 * @author : 华清松
 * 闪屏界面
 */
class SplashActivity(
        override val layoutId: Int = R.layout.activity_splash,
        override val reload: Boolean = false
) : AbsIMActivity() {

    override fun initData(savedInstanceState: Bundle?) {}

    override fun initView() {
        ActivityManager.finishAllButNotMe(this)

        Handler(Looper.getMainLooper()).postDelayed({
            ARouter.getInstance().build("/CHAT/LOGIN", "APP").navigation()
            finish()
        }, 2000)

        getData()
    }

    override fun getData() {
        IMManagerHelper.setLoggerEnable(this, BuildConfig.DEBUG)
        IMManagerHelper.connect(this, Constants.IM_SERVER_HOST, Constants.IM_SERVER_PORT)
    }

    override fun onBackPressed() {
        IMManagerHelper.destroy(this)
        finish()
    }

    override fun onConnectionFailed() {
        Toast.makeText(this, "连接服务器失败，请检查当前设备是否能连接上服务器IP和端口", Toast.LENGTH_LONG).show()
    }

}