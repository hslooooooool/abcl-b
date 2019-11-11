package qsos.base.chat.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.*
import qsos.lib.base.base.BaseApplication
import kotlin.coroutines.CoroutineContext

/**
 * @author : 华清松
 * @description : 聊天数据库，当前版本【1】
 */
@Database(
        version = 1,
        entities = [DBMessage::class, DBRelationMessage::class, DBSession::class]
)
abstract class ChatDatabase : RoomDatabase() {

    abstract val relationMessageDao: RelationMessageDao
    abstract val messageDao: MessageDao
    abstract val sessionDao: SessionDao

    companion object {

        private var DB_NAME = "${ChatDatabase::class.java.simpleName}.db"

        @Volatile
        private var INSTANCE: ChatDatabase? = null

        fun getInstance(context: Context): ChatDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: create(context).also { INSTANCE = it }
        }

        private fun create(context: Context): ChatDatabase {
            return Room.databaseBuilder(context, ChatDatabase::class.java, DB_NAME)
                    // 重建数据库
                    .fallbackToDestructiveMigration()
                    .build()
        }

    }

    object DefRelationMessageDao {
        private val mJob: CoroutineContext = Dispatchers.Main + Job()

        fun getMessageListBySessionId(sessionId: Int, result: (rMsgList: List<DBRelationMessage>) -> Unit) {
            CoroutineScope(mJob).launch {
                val rMsgList = withContext(Dispatchers.IO) {
                    getInstance(BaseApplication.appContext).relationMessageDao
                            .getMessageListBySessionId(sessionId)
                }
                result.invoke(rMsgList)
            }
        }

        fun insert(rMsg: DBRelationMessage, result: (id: Long) -> Unit) {
            CoroutineScope(mJob).launch {
                val id = withContext(Dispatchers.IO) {
                    getInstance(BaseApplication.appContext).relationMessageDao
                            .insert(rMsg)
                }
                result.invoke(id)
            }
        }

    }

    object DefMessageDao {
        private val mJob: CoroutineContext = Dispatchers.Main + Job()

        fun getMessageListBySessionId(sessionId: Int, result: (rMsgList: List<DBRelationMessage>) -> Unit) {
            CoroutineScope(mJob).launch {
                val rMsgList = withContext(Dispatchers.IO) {
                    getInstance(BaseApplication.appContext).relationMessageDao
                            .getMessageListBySessionId(sessionId)
                }
                result.invoke(rMsgList)
            }
        }

        fun insert(msg: DBMessage, result: (id: Int) -> Unit) {
            CoroutineScope(mJob).launch {
                val messageId = withContext(Dispatchers.IO) {
                    getInstance(BaseApplication.appContext).messageDao
                            .insert(msg)
                }
                result.invoke(messageId as Int)
            }
        }

    }

    object DefSessionDao {
        private val mJob: CoroutineContext = Dispatchers.Main + Job()

        fun insert(session: DBSession, result: () -> Unit) {
            CoroutineScope(mJob).launch {
                withContext(Dispatchers.IO) {
                    getInstance(BaseApplication.appContext).sessionDao
                            .insert(session)
                }
                result.invoke()
            }
        }

        fun update(session: DBSession, result: () -> Unit) {
            CoroutineScope(mJob).launch {
                withContext(Dispatchers.IO) {
                    getInstance(BaseApplication.appContext).sessionDao
                            .update(session)
                }
                result.invoke()
            }
        }

        fun getSessionById(sessionId: Int, result: (session: DBSession?) -> Unit) {
            CoroutineScope(mJob).launch {
                val session = withContext(Dispatchers.IO) {
                    getInstance(BaseApplication.appContext).sessionDao
                            .getSessionById(sessionId)
                }
                result.invoke(session)
            }
        }

    }
}