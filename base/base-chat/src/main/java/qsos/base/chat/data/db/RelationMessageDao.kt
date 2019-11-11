package qsos.base.chat.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * @author : 华清松
 * 消息关系数据库操作
 */
@Dao
interface RelationMessageDao {

    /**根据会话ID获取消息关系数据列表
     * @param sessionId 会话ID
     * @return 消息关系数据列表
     * */
    @Query("SELECT * FROM msg_and_user_and_session WHERE session_id=:sessionId")
    fun getMessageListBySessionId(sessionId: Int): List<DBRelationMessage>

    /**插入消息数据
     * @param rMsg 消息关系数据
     * @return 消息关系ID
     * */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(rMsg: DBRelationMessage): Long

}