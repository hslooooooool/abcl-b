package vip.qsos.app_chat.data

import qsos.lib.netservice.data.BaseResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import vip.qsos.app_chat.data.entity.LoginUser

/**
 * @author : 华清松
 * 用户相关接口
 */
interface ApiLoginUser {

    companion object {
        const val GROUP = "/api/app/user"
    }

    @GET("$GROUP/login")
    fun login(
            @Query("account") account: String,
            @Query("password") password: String
    ): Call<BaseResponse<LoginUser>>

    @POST("$GROUP/register")
    fun register(
            @Query("account") account: String,
            @Query("password") password: String
    ): Call<BaseResponse<LoginUser>>

    @GET("$GROUP/list")
    fun list(): Call<BaseResponse<List<LoginUser>>>

}