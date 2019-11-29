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
        var sessionId: Int = -1,
        @ColumnInfo(name = "last_message_id")
        var lastMessageId: Int? = null,
        @ColumnInfo(name = "last_message_timeline")
        var lastMessageTimeline: Int? = null,
        @ColumnInfo(name = "now_last_message_id")
        var nowLastMessageId: Int? = null,
        @ColumnInfo(name = "now_last_message_timeline")
        var nowLastMessageTimeline: Int? = null
)