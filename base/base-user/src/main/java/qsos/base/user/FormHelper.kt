package qsos.base.user

import android.annotation.SuppressLint
import android.text.TextUtils
import qsos.base.core.base.db.DBLoginUser
import qsos.core.form.db.entity.*
import qsos.core.form.utils.FormValueHelper
import qsos.lib.base.utils.DateUtils
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.*

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
            /**单选-性别*/
            val man = user.sexuality == 1
            formItemList.add(FormItem.newFormItemValue(
                    item = FormItem(
                            valueType = FormItemType.CHOOSE.tag, notice = "用户性别",
                            title = "性别", require = true
                    ),
                    value = FormItemValue(limitMin = 1, limitMax = 1, values = arrayListOf(
                            Value().newCheck(FormValueOfCheck("0", "女", "0", !man)),
                            Value().newCheck(FormValueOfCheck("1", "男", "1", man))
                    ))
            ))
            /**出身日期*/
            val now = Date()
            val c = Calendar.getInstance()
            c.time = now
            c.add(Calendar.YEAR, -100)// 当前年往前100年
            val birth = if (TextUtils.isEmpty(user.birth)) {
                now
            } else {
                val formatter = SimpleDateFormat(DateUtils.TimeType.YMD.type)
                val pos = ParsePosition(0)
                formatter.parse(user.birth!!, pos) ?: now
            }
            formItemList.add(FormItem.newFormItemValue(
                    item = FormItem(
                            valueType = FormItemType.TIME.tag, notice = "出身日期",
                            title = "出身日期", require = true
                    ),
                    value = FormItemValue(limitMin = 1, limitMax = 1, limitType = "yyyy-MM-dd", values = arrayListOf(
                            Value().newTime(FormValueOfTime(
                                    timeStart = birth.time,
                                    timeEnd = now.time,
                                    timeLimitMin = c.time.time,
                                    timeLimitMax = now.time))
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
            user.sexuality = FormValueHelper.GetValue.singleChose(form.formItems!![2])!!.toInt()
            user.birth = DateUtils.format(millis = FormValueHelper.GetValue.time(form.formItems!![3])!!.timeStart, date = null, timeType = DateUtils.TimeType.YMD)
            return user
        }
    }
}