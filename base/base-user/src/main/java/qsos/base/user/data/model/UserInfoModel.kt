package qsos.base.user.data.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import qsos.base.core.base.db.DBLoginUser
import qsos.base.core.base.db.LoginUserDatabase
import qsos.base.core.config.BaseConfig
import qsos.base.user.FormHelper
import qsos.core.form.data.FormRepository
import qsos.core.form.db
import qsos.core.form.db.entity.FormEntity
import qsos.lib.base.base.BaseApplication
import kotlin.coroutines.CoroutineContext

/**
 * @author : 华清松
 * 用户信息操作接口
 */
class UserInfoModel(
        override val mJob: CoroutineContext = Dispatchers.Main + Job()
) : IUserInfoModel {
    private val mFormRepository: FormRepository = FormRepository()

    override fun getUserInfoByDB(userId: Int, back: (user: DBLoginUser?) -> Unit) {
        LoginUserDatabase.DefLoginUserDao.getLoginUserByUserId(userId) {
            back.invoke(it)
        }
    }

    override fun getForm(formId: Long, back: (form: FormEntity?) -> Unit) {
        mFormRepository.getForm(formId).let {
            back.invoke(it)
        }
    }

    override fun getUserInfoByForm(back: (form: FormEntity?) -> Unit) {
        CoroutineScope(mJob).db<FormEntity> {
            db = {
                var user: FormEntity? = null
                LoginUserDatabase.getInstance(BaseApplication.appContext).loginUserDao
                        .getLoginUserByUserId(BaseConfig.userId)?.let {
                            user = mFormRepository.insertForm(FormHelper.Create.userInfoForm(it))
                        }
                user
            }
            onSuccess = {
                back.invoke(it)
            }
        }
    }
}