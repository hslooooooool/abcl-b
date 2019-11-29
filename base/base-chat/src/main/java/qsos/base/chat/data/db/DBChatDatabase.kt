package qsos.base.chat.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import qsos.lib.base.base.BaseApplication
import qsos.lib.base.utils.LogUtil

/**
 * @author : 华清松
 * @description : 消息数据库，当前版本【1】
 */
@Database(
        version = 1,
        entities = [DBChatSession::class]
)
abstract class DBChatDatabase : RoomDatabase() {

    abstract val chatSessionDao: DBChatSessionDao

    companion object {

        private var DB_NAME = "${DBChatDatabase::class.java.simpleName}.db"

        @Volatile
        private var INSTANCE: DBChatDatabase? = null

        fun getInstance(context: Context): DBChatDatabase =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: create(context).also { INSTANCE = it }
                }

        private fun create(context: Context): DBChatDatabase {
            return Room.databaseBuilder(context, DBChatDatabase::class.java, DB_NAME)
                    // 重建数据库
                    .fallbackToDestructiveMigration()
                    .build()
        }

    }

    object DefChatSessionDao {

        fun getChatSessionById(sessionId: Int, result: (session: DBChatSession?) -> Unit) {
            CoroutineScope(Dispatchers.IO).launch {
                LogUtil.d("会话数据库", "获取会话")
                val session = try {
                    getInstance(BaseApplication.appContext).chatSessionDao.getChatSessionById(sessionId)
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
                withContext(Dispatchers.Main) {
                    result.invoke(session)
                }
            }
        }

        fun insert(session: DBChatSession, result: (success: Boolean) -> Unit) {
            CoroutineScope(Dispatchers.IO).launch {
                LogUtil.d("会话数据库", "插入会话")
                val ok = try {
                    getInstance(BaseApplication.appContext).chatSessionDao.insert(session)
                    true
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
                withContext(Dispatchers.Main) {
                    result.invoke(ok)
                }
            }
        }

        fun update(session: DBChatSession, result: (success: Boolean) -> Unit) {
            CoroutineScope(Dispatchers.IO).launch {
                LogUtil.d("会话数据库", "更新会话")
                val line = getInstance(BaseApplication.appContext).chatSessionDao.update(session)
                withContext(Dispatchers.Main) {
                    result.invoke(line == 1)
                }
            }
        }

        fun update(sessionId: Int, nowLastMessageId: Int, nowLastMessageTimeline: Int, result: (success: Boolean) -> Unit) {
            CoroutineScope(Dispatchers.IO).launch {
                LogUtil.d("会话数据库", "更新会话")
                val line = getInstance(BaseApplication.appContext).chatSessionDao.update(sessionId, nowLastMessageId, nowLastMessageTimeline)
                withContext(Dispatchers.Main) {
                    result.invoke(line == 1)
                }
            }
        }
    }
}