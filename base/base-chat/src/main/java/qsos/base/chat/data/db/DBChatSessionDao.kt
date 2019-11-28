package qsos.base.chat.data.db

import androidx.room.*

@Dao
interface DBChatSessionDao {

    @Query("SELECT * FROM chat_session where session_id=:sessionId")
    fun getChatSessionById(sessionId: Int): DBChatSession?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(session: DBChatSession)

    @Update(onConflict = OnConflictStrategy.ABORT)
    fun update(session: DBChatSession): Int

}