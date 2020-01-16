package qsos.base.chat.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * @author : 华清松
 * 聊天会话数据表
 * @param sessionId 会话ID
 * @param lastTimeline 服务器最后一条消息时序
 * @param nowFirstTimeline 本地第一条消息时序
 */
@Entity(
        tableName = "chat_session",
        indices = [Index(value = ["session_id"], unique = true)]
)
data class DBChatSession(
        @PrimaryKey
        @ColumnInfo(name = "session_id")
        var sessionId: Long? = -1L,
        @ColumnInfo(name = "last_timeline")
        var lastTimeline: Long? = -1L,
        @ColumnInfo(name = "now_first_timeline")
        var nowFirstTimeline: Long? = -1L
)