package qsos.base.chat.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.view.View
import android.view.ViewAnimationUtils
import kotlin.math.sqrt

/**
 * @author : 华清松
 * 扩大与缩小动画
 */
object CircularAnimHelper {

    const val MINI_RADIUS = 0

    fun show(myView: View) {
        show(myView, MINI_RADIUS.toFloat(), 100)
    }

    fun hide(myView: View) {
        hide(myView, MINI_RADIUS.toFloat(), 800)
    }

    /**
     * 向四周伸张，直到完成显示。
     */
    fun show(myView: View, startRadius: Float, durationMills: Long) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
            myView.visibility = View.VISIBLE
            return
        }
        val cx = (myView.left + myView.right) / 2
        val cy = (myView.top + myView.bottom) / 2
        val w = myView.width
        val h = myView.height
        // 勾股定理 & 进一法
        val finalRadius = sqrt((w * w + h * h).toDouble()).toInt() + 1
        val anim = ViewAnimationUtils.createCircularReveal(myView, cx, cy, startRadius, finalRadius.toFloat())
        myView.visibility = View.VISIBLE
        anim.duration = durationMills
        anim.start()
    }

    /**
     * 由满向中间收缩，直到隐藏。
     */
    fun hide(myView: View, endRadius: Float, durationMills: Long) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
            myView.visibility = View.GONE
            return
        }
        val cx = (myView.left + myView.right) / 2
        val cy = (myView.top + myView.bottom) / 2
        val w = myView.width
        val h = myView.height
        // 勾股定理 & 进一法
        val initialRadius = sqrt((w * w + h * h).toDouble()).toInt() + 1
        val anim = ViewAnimationUtils.createCircularReveal(myView, cx, cy, initialRadius.toFloat(), endRadius)
        anim.duration = durationMills
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                myView.visibility = View.GONE
            }
        })
        anim.start()
    }

}
