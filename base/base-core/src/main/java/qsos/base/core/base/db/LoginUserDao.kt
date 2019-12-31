package qsos.base.core.base.db

import androidx.room.*

/**
 * @author : 华清松
 * 登录用户数据库查询
 */
@Dao
interface LoginUserDao {

    /**根据登录用户ID获取登录用户数据
     * @param userId 登录用户ID
     * @return 登录用户数据
     * */
    @Query("SELECT * FROM login_user where user_id=:userId")
    fun getLoginUserByUserId(userId: Long): DBLoginUser?

    /**更新登录用户数据*/
    @Update
    fun updateUserInfoByUserId(user: DBLoginUser): Int

    /**插入登录用户数据
     * @param user 登录用户数据
     * @return 登录用户数据
     * */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: DBLoginUser): Long?

    /**删除登录用户数据
     * @param userId 登录用户ID
     * @return 登录用户数据
     * */
    @Query("DELETE FROM login_user WHERE user_id=:userId")
    fun delete(userId: Long): Int

}