package qsos.base.core.base.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.*
import qsos.lib.base.base.BaseApplication
import kotlin.coroutines.CoroutineContext

/**
 * @author : 华清松
 * @description : 登录用户数据库，当前版本【1】
 */
@Database(
        version = 1,
        entities = [DBLoginUser::class]
)
abstract class LoginUserDatabase : RoomDatabase() {

    abstract val loginUserDao: LoginUserDao

    companion object {

        private var DB_NAME = "${LoginUserDatabase::class.java.simpleName}.db"

        @Volatile
        private var INSTANCE: LoginUserDatabase? = null

        fun getInstance(context: Context): LoginUserDatabase =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: create(context).also { INSTANCE = it }
                }

        private fun create(context: Context): LoginUserDatabase {
            return Room.databaseBuilder(context, LoginUserDatabase::class.java, DB_NAME)
                    // 重建数据库
                    .fallbackToDestructiveMigration()
                    .build()
        }

    }

    object DefLoginUserDao {
        private val mJob: CoroutineContext = Dispatchers.Main + Job()
        fun getLoginUserByUserId(userId: Int, result: (user: DBLoginUser?) -> Unit) {
            CoroutineScope(mJob).launch {
                val user = withContext(Dispatchers.IO) {
                    getInstance(BaseApplication.appContext).loginUserDao.getLoginUserByUserId(userId)
                }
                result.invoke(user)
            }
        }

        fun insert(user: DBLoginUser, result: (id: Long?) -> Unit) {
            CoroutineScope(mJob).launch {
                val id = withContext(Dispatchers.IO) {
                    getInstance(BaseApplication.appContext).loginUserDao.delete(user.userId)
                    getInstance(BaseApplication.appContext).loginUserDao.insert(user)
                }
                result.invoke(id)
            }
        }

        fun update(
                userId: Int, userName: String, avatar: String?, birth: String?, sexuality: Int = -1,
                back: (result: Int) -> Unit
        ) {
            CoroutineScope(mJob).launch {
                val result = withContext(Dispatchers.IO) {
                    val user = getInstance(BaseApplication.appContext).loginUserDao.getLoginUserByUserId(userId)
                    user!!.userName = userName
                    user.avatar = avatar
                    user.birth = birth
                    user.sexuality = sexuality
                    getInstance(BaseApplication.appContext).loginUserDao.updateUserInfoByUserId(user)
                }
                back.invoke(result)
            }
        }

    }
}