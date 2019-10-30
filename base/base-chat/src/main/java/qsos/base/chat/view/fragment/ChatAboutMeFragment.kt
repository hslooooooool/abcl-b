package qsos.base.chat.view.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_chat_about_me.*
import qsos.base.chat.R
import qsos.base.chat.view.activity.ChatMainActivity
import qsos.core.lib.utils.image.ImageLoaderUtils
import qsos.lib.base.base.fragment.BaseFragment

/**
 * @author : 华清松
 * 关于我页面
 */
class ChatAboutMeFragment(
        override val layoutId: Int = R.layout.fragment_chat_about_me,
        override val reload: Boolean = true
) : BaseFragment() {

    override fun initData(savedInstanceState: Bundle?) {}

    @SuppressLint("SetTextI18n")
    override fun initView(view: View) {
        ChatMainActivity.mLoginUser.observe(this, Observer {
            context?.let { context ->
                ImageLoaderUtils.display(context, chat_about_me_avatar, it.avatar)
                chat_about_me_name.text = it.userName
                chat_about_me_desc.text = "${it.birth}\n${if (it.sexuality == 0) "男" else "女"}"
            }
        })
    }

    override fun getData() {
    }
}