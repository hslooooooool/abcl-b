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
        @PrimaryKey
        @ColumnInfo(name = "user_id")
        var userId: Long = -1L,
        @ColumnInfo(name = "name")
        var name: String = "",
        @ColumnInfo(name = "account")
        var account: String = "",
        @ColumnInfo(name = "password")
        var password: String = "",
        @ColumnInfo(name = "avatar")
        var avatar: String? = null
)