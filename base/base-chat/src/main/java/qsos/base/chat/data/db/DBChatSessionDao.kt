package qsos.base.chat.data.db

import androidx.room.*

@Dao
interface DBChatSessionDao {

    @Query("SELECT * FROM chat_session where session_id=:sessionId")
    fun getChatSessionById(sessionId: String): DBChatSession?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(session: DBChatSession)

    @Update(onConflict = OnConflictStrategy.ABORT)
    fun update(session: DBChatSession): Int

    @Query("UPDATE chat_session SET now_last_message_id=:nowLastMessageId AND now_last_message_timeline=:nowLastMessageTimeline WHERE session_id=:sessionId ")
    fun update(sessionId: String, nowLastMessageId: String, nowLastMessageTimeline: Long): Int

}