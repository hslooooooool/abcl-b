package qsos.base.user.view.fragment

import android.os.Bundle
import android.view.View
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_register.*
import qsos.base.user.R
import qsos.lib.base.base.fragment.BaseFragment

/**
 * @author : 华清松
 * 注册页面
 */
class RegisterFragment(
        override val layoutId: Int = R.layout.fragment_register,
        override val reload: Boolean = false
) : BaseFragment() {

    override fun getData() {

    }

    override fun initData(savedInstanceState: Bundle?) {

    }

    override fun initView(view: View) {
        login.setOnClickListener {
            Navigation.findNavController(it).popBackStack()
        }
    }
}