package qsos.base.chat.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy

/**
 * @author : 华清松
 * 消息关系数据库操作
 */
@Dao
interface MessageDao {

    /**插入消息数据
     * @param msg 消息数据
     * @return 消息ID
     * */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(msg: DBMessage): Long

}