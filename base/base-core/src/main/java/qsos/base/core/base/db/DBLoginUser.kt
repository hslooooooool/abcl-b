package qsos.base.core.base.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * @author : 华清松
 * 登录用户信息
 */
@Entity(
        tableName = "login_user",
        indices = [Index(value = ["user_id"], unique = true)]
)
data class DBLoginUser(
        @PrimaryKey(autoGenerate = true)
        var id: Long? = null,
        @ColumnInfo(name = "user_id")
        var userId: Int = -1,
        @ColumnInfo(name = "user_name")
        var userName: String,
        @ColumnInfo(name = "account")
        var account: String,
        @ColumnInfo(name = "password")
        var password: String,
        @ColumnInfo(name = "avatar")
        var avatar: String? = null,
        @ColumnInfo(name = "birth")
        var birth: String? = null,
        @ColumnInfo(name = "sexuality")
        var sexuality: Int = -1
) {
    companion object {
        fun create(): DBLoginUser {
            return DBLoginUser(null, -1, "", "", "", "", "", -1)
        }
    }
}