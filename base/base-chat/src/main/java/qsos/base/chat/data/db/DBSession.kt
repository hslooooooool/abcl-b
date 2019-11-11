package qsos.base.chat.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @author : 华清松
 * 消息会话数据库
 * @param sessionId 会话ID
 * @param lastMessageId 最后会话消息ID
 * @param lastTimeline 最后会话消息时间线
 */
@Entity(
        tableName = "session"
)
data class DBSession(
        @ColumnInfo(name = "id")
        @PrimaryKey(autoGenerate = true)
        val sessionId: Int? = -1,
        @ColumnInfo(name = "message_id")
        val lastMessageId: Int,
        @ColumnInfo(name = "timeline")
        val lastTimeline: Int = -1
)
