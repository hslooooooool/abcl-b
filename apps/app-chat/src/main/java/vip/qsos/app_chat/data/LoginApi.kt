package vip.qsos.app_chat.data

import qsos.lib.netservice.data.BaseResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import qsos.base.core.base.LoginUser

/**
 * @author : 华清松
 * 登录页接口
 */
interface LoginApi {

    @GET("/api/app/login/login")
    fun login(
            @Query("account") account: String,
            @Query("password") password: String
    ): Call<BaseResponse<LoginUser>>

    @POST("/api/app/login/register")
    fun register(
            @Query("account") account: String,
            @Query("password") password: String
    ): Call<BaseResponse<LoginUser>>

}