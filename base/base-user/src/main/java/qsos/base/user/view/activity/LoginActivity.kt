package qsos.base.user.view.activity

import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import com.alibaba.android.arouter.facade.annotation.Route
import qsos.base.user.R
import qsos.lib.base.base.activity.BaseActivity

/**
 * @author : 华清松
 * 登录页面
 */
@Route(group = "LOGIN", path = "/LOGIN/MAIN")
class LoginActivity(
        override val layoutId: Int = R.layout.activity_login,
        override val reload: Boolean = false
) : BaseActivity() {

    override fun getData() {

    }

    override fun initData(savedInstanceState: Bundle?) {

    }

    override fun initView() {

    }

    override fun onSupportNavigateUp(): Boolean {
        val fragment = supportFragmentManager.findFragmentById(R.id.login_fragment)!!
        return NavHostFragment.findNavController(fragment).navigateUp();
    }
}