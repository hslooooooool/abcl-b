package qsos.base.chat.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * @author : 华清松
 * 聊天会话数据表
 * @param sessionId 会话ID
 * @param lastMessageId 服务器最后一条消息ID
 * @param lastMessageTimeline 服务器最后一条消息Timeline
 * @param nowFirstMessageId 本地第一条消息ID
 * @param nowFirstMessageTimeline 本地第一条消息Timeline
 * @param nowLastMessageId 本地最后一条消息ID
 * @param nowLastMessageTimeline 本地最后一条消息Timeline
 */
@Entity(
        tableName = "chat_session",
        indices = [Index(value = ["session_id"], unique = true)]
)
data class DBChatSession(
        @PrimaryKey
        @ColumnInfo(name = "session_id")
        var sessionId: Long? = -1L,
        @ColumnInfo(name = "last_message_id")
        var lastMessageId: Long? = -1L,
        @ColumnInfo(name = "last_message_timeline")
        var lastMessageTimeline: Long? = -1L,
        @ColumnInfo(name = "now_first_message_id")
        var nowFirstMessageId: Long? = -1L,
        @ColumnInfo(name = "now_first_message_timeline")
        var nowFirstMessageTimeline: Long? = -1L,
        @ColumnInfo(name = "now_last_message_id")
        var nowLastMessageId: Long? = -1L,
        @ColumnInfo(name = "now_last_message_timeline")
        var nowLastMessageTimeline: Long? = -1L
)