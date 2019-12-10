package qsos.base.user.data.model

import qsos.base.core.base.db.DBLoginUser
import qsos.core.form.db.entity.FormEntity
import kotlin.coroutines.CoroutineContext

/**
 * @author : 华清松
 * 用户信息操作接口
 */
interface IUserInfoModel {
    val mJob: CoroutineContext

    /**从数据库获取用户信息*/
    fun getUserInfoByDB(userId: Int, back: (user: DBLoginUser?) -> Unit)

    /**从表单数据库获取用户信息*/
    fun getUserInfoByForm(back: (form: FormEntity?) -> Unit)

    /**更新当前用户信息*/
    fun updateMineInfo(formId: Long, back: (result: Boolean) -> Unit)
}