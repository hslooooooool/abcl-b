package qsos.base.chat.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @author : 华清松
 * 消息数据库
 * @param messageId 消息ID
 * @param contentJson 消息内容Json数据
 * @see
 */
@Entity(
        tableName = "msg"
)
data class DBMessage(
        @ColumnInfo(name = "id")
        @PrimaryKey(autoGenerate = true)
        var id: Int = -1,
        @ColumnInfo(name = "message_id")
        var messageId: Int,
        @ColumnInfo(name = "content_json")
        var contentJson: String
)
