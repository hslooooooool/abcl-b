package qsos.base.user

import android.annotation.SuppressLint
import qsos.base.core.base.db.DBLoginUser
import qsos.core.form.db.entity.*
import qsos.core.form.utils.FormValueHelper

/**表单转换帮助类*/
@SuppressLint("SimpleDateFormat")
object FormHelper {

    /**创建一个表单*/
    object Create {
        /**用户表单*/
        fun userInfoForm(user: DBLoginUser): FormEntity {
            val form = FormEntity(notice = "用户信息", submitName = "保存", title = "用户信息")
            val formItemList = arrayListOf<FormItem>()

            /**用户头像*/
            formItemList.add(FormItem.newFormItemValue(
                    item = FormItem(
                            valueType = FormItemType.FILE.tag, notice = "用户头像",
                            title = "用户头像", require = true
                    ),
                    value = FormItemValue(limitMin = 1, limitMax = 1, limitType = ".png;.jpg;.jpeg", values = arrayListOf(
                            Value().newFile(FormValueOfFile(
                                    fileId = user.avatar,
                                    fileUrl = user.avatar,
                                    fileCover = user.avatar,
                                    fileName = ""
                            ))
                    ))
            ))
            /**输入姓名*/
            formItemList.add(FormItem.newFormItemValue(
                    item = FormItem(
                            valueType = FormItemType.INPUT.tag, notice = "请输入姓名",
                            title = "姓名", require = true
                    ),
                    value = FormItemValue(limitMin = 1, limitMax = 10, values = arrayListOf(
                            Value().newText(FormValueOfText(user.userName))
                    ))
            ))
            return FormEntity.newFormItems(form = form, formItems = formItemList)
        }
    }

    object Getter {
        /**用户表单*/
        fun getUserInfo(form: FormEntity): DBLoginUser? {
            if (form.formItems == null || form.formItems?.size != 4) {
                return null
            }
            val user = DBLoginUser.create()
            user.avatar = FormValueHelper.GetValue.fileIds(form.formItems!![0])[0]
            user.userName = FormValueHelper.GetValue.input(form.formItems!![1])
            return user
        }
    }
}