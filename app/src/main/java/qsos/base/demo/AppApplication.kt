package qsos.base.demo

import android.annotation.SuppressLint
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import qsos.base.demo.config.PlayerConfig
import qsos.core.exception.GlobalException
import qsos.core.exception.GlobalExceptionHelper
import qsos.core.lib.config.CoreConfig
import qsos.core.player.PlayerConfigHelper
import qsos.lib.base.base.BaseApplication
import qsos.lib.base.utils.LogUtil
import qsos.lib.base.utils.rx.RxBus

/**
 * @author : 华清松
 * AppApplication
 */
open class AppApplication(
        override var debugARouter: Boolean = true,
        override var debugTimber: Boolean = true
) : BaseApplication(), LifecycleOwner {

    override fun getLifecycle(): Lifecycle {
        return LifecycleRegistry(this)
    }

    @SuppressLint("CheckResult")
    override fun onCreate() {
        super.onCreate()

        CoreConfig.DEBUG = true
        /**BASE_URL配置*/
        CoreConfig.BASE_URL = "http://192.168.3.108:8085"
        CoreConfig.PROVIDER = "qsos.base.demo.provider"

        /**Timber 日志*/
        LogUtil.open(true, GlobalExceptionHelper.CrashReportingTree())
        /**全局异常捕获处理*/
        Thread.setDefaultUncaughtExceptionHandler(GlobalExceptionHelper)
        RxBus.toFlow(GlobalExceptionHelper.ExceptionEvent::class.java).subscribe {
            dealGlobalException(it.exception)
        }

        /**配置媒体预览操作代理实现，这里不初始化，则使用默认实现*/
        PlayerConfigHelper.init(PlayerConfig())
    }

    /**TODO 统一处理异常，如重新登录、强制下线、异常反馈、网络检查*/
    private fun dealGlobalException(ex: GlobalException) {

    }
}