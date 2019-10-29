package qsos.base.user.view.fragment

import android.os.Bundle
import android.view.View
import androidx.navigation.Navigation
import com.google.android.material.internal.NavigationMenuPresenter
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.fragment_login.*
import qsos.base.user.R
import qsos.lib.base.base.fragment.BaseFragment

/**
 * @author : 华清松
 * 登录页面
 */
class LoginFragment(
        override val layoutId: Int = R.layout.fragment_login,
        override val reload: Boolean = false
) : BaseFragment() {

    override fun getData() {

    }

    override fun initData(savedInstanceState: Bundle?) {

    }

    override fun initView(view: View) {
        register.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }
}