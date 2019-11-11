package qsos.base.chat.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @author : 华清松
 * 消息数据库
 * @param messageId 消息ID
 * @param contentJson 消息内容Json数据
 */
@Entity(
        tableName = "msg"
)
data class DBMessage(
        @ColumnInfo(name = "id")
        @PrimaryKey(autoGenerate = true)
        val messageId: Int? = -1,
        @ColumnInfo(name = "content_json")
        val contentJson: String
)
