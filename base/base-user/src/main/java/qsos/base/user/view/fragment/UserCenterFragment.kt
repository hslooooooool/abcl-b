package qsos.base.user.view.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.alibaba.android.arouter.launcher.ARouter
import kotlinx.android.synthetic.main.fragment_user_center.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import qsos.base.core.config.BaseConfig
import qsos.base.user.R
import qsos.base.user.data.model.IUserInfoModel
import qsos.base.user.data.model.UserInfoModel
import qsos.core.form.FormPath
import qsos.core.lib.utils.image.ImageLoaderUtils
import qsos.lib.base.base.fragment.BaseFragment
import qsos.lib.base.utils.ToastUtils
import kotlin.coroutines.CoroutineContext

/**
 * @author : 华清松
 * 用户中心界面
 */
class UserCenterFragment(
        mJob: CoroutineContext = Dispatchers.Main + Job(),
        override val layoutId: Int? = R.layout.fragment_user_center,
        override val reload: Boolean = true
) : BaseFragment() {

    private val mUserInfoModel: IUserInfoModel = UserInfoModel(mJob)

    override fun initData(savedInstanceState: Bundle?) {
    }

    override fun initView(view: View) {
        user_center_login_out.setOnClickListener {
            ARouter.getInstance().build("/USER/LOGIN").navigation()
        }
        user_center_change.setOnClickListener {
            goToUserInfo(it)
        }
    }

    override fun getData() {
        mUserInfoModel.getUserInfoByDB(BaseConfig.userId) {
            it?.let {
                ImageLoaderUtils.display(mContext, user_center_avatar, it.avatar)
                user_center_name.text = it.userName
            }
        }
    }

    private fun goToUserInfo(view: View) {
        view.isEnabled = false

        mUserInfoModel.getUserInfoByForm {
            view.isEnabled = true
            it?.let {
                ARouter.getInstance().build(FormPath.MAIN)
                        .withLong(FormPath.FORM_ID, it.id!!)
                        .navigation(activity, FormPath.FORM_REQUEST_CODE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when {
            requestCode == FormPath.FORM_REQUEST_CODE && resultCode == Activity.RESULT_OK -> {
                data?.getLongExtra(FormPath.FORM_ID, -1L)?.let { id ->
                    if (id != -1L) {
                        mUserInfoModel.updateMineInfo(id) { result ->
                            if (result) {
                                ToastUtils.showToast(mContext, "更新成功")
                                getData()
                            } else {
                                ToastUtils.showToast(mContext, "更新失败")
                            }
                        }
                    }
                }
            }
        }
    }
}