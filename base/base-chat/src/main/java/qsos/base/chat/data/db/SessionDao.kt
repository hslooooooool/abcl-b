package qsos.base.chat.data.db

import androidx.room.*

/**
 * @author : 华清松
 * 消息会话数据库操作
 */
@Dao
interface SessionDao {

    /**插入消息会话数据
     * @param session 消息会话数据
     * */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(session: DBSession)

    /**更新消息会话数据
     * @param session 消息会话数据
     * */
    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(session: DBSession)

    /**获取消息会话数据
     * @param sessionId 消息会话ID
     * */
    @Query("SELECT * FROM session WHERE id=:sessionId")
    fun getSessionById(sessionId: Int): DBSession?

}