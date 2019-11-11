package qsos.base.chat.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * @author : 华清松
 * 消息与其它数据关联表
 * @param messageId 消息ID，唯一
 * @param sessionId 会话ID
 * @param userId 用户ID
 * @param timeline 消息时序，同一会话下唯一，递增
 */
@Entity(
        tableName = "msg_and_user_and_session",
        indices = [Index(value = ["message_id"], unique = true)]
)
data class DBRelationMessage(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "id")
        val id: Long = -1,
        @ColumnInfo(name = "session_id")
        val sessionId: Int,
        @ColumnInfo(name = "message_id")
        val messageId: Int,
        @ColumnInfo(name = "user_id")
        val userId: Int,
        @ColumnInfo(name = "timeline")
        val timeline: Int
)
