package com.logicasur.appchoferes.network.retrofitInterfaces

import com.logicasur.appchoferes.network.createNewPasswordResponse.CreateNewPasswordResponse
import com.logicasur.appchoferes.network.forgotPasswordResponse.ForgotPasswordResponse
import com.logicasur.appchoferes.network.getAvatarResponse.GetAvatarResponse
import com.logicasur.appchoferes.network.loadingResponse.LoadingResponse
import com.logicasur.appchoferes.network.loadingResponse.SplashResponse
import com.logicasur.appchoferes.network.logoutResponse.MessageResponse
import com.logicasur.appchoferes.network.signinResponse.*
import retrofit2.Response
import retrofit2.http.*

interface RetrofitInterface {

    @FormUrlEncoded
    @POST("auth/login")
    suspend fun userSignup(
        @Field("username") name: String,
        @Field("password") password: String,
        @Field("idApp") idApp: String,
        @Field("memUsed") memUsed: String,
        @Field("diskFree") diskFree: String,
        @Field("diskTotal") diskTotal: String,
        @Field("model") model: String,
        @Field("operatingSystem") operatingSystem: String,
        @Field("osVersion") osVersion: String,
        @Field("appVersion") appVersion: String,
        @Field("platform") platform: String,
        @Field("appBuild") appBuild: String,
        @Field("manufacturer") manufacturer: String,
        @Field("uuid") uuid: String,
        @Field("isVirtual") isVirtual: String

    ): Response<SigninResponse>


    @FormUrlEncoded
    @POST("auth/recover")
    suspend fun forgotPassword(
        @Field("username") name: String
    ): Response<ForgotPasswordResponse>


    @POST("auth/logout")
    suspend fun userLogout(
    ): Response<MessageResponse>


    @FormUrlEncoded
    @POST("auth/change")
    suspend fun createNewPassword(
        @Field("password") password: String,
        @Header("Cookie") sessionIdAndToken: String
    ): Response<CreateNewPasswordResponse>


    @FormUrlEncoded
    @POST("auth/reset/{otp}")
    suspend fun sendOTP(
        @Path("otp") otp: Int,
        @Field("username") name: String,
        @Field("idApp") idApp: String,
        @Field("memUsed") memUsed: String,
        @Field("diskFree") diskFree: String,
        @Field("diskTotal") diskTotal: String,
        @Field("model") model: String,
        @Field("operatingSystem") operatingSystem: String,
        @Field("osVersion") osVersion: String,
        @Field("appVersion") appVersion: String,
        @Field("platform") platform: String,
        @Field("appBuild") appBuild: String,
        @Field("manufacturer") manufacturer: String,
        @Field("uuid") uuid: String,
        @Field("isVirtual") isVirtual: String,
        @Header("Cookie") sessionIdAndToken: String
    ): Response<SigninResponse>


    @FormUrlEncoded
    @POST("profile/get/avatar")
    suspend fun getAvatar(
        @Field("username") name: String,
        @Header("Cookie") sessionIdAndToken: String
    ): Response<GetAvatarResponse>


    @FormUrlEncoded
    @POST("profile/update/avatar")
    suspend fun updateAvatar(
        @Field("avatar") avatar: String,
        @Header("Cookie") sessionIdAndToken: String
    ): Response<MessageResponse>


    @POST("sync")
    suspend fun sync(
        @Header("Cookie") sessionIdAndToken: String,
    ): Response<SigninResponse>


    @FormUrlEncoded
    @POST("profile/update/language")
    suspend fun updateLanguage(
        @Field("language") language: Int,
        @Header("Cookie") sessionIdAndToken: String
    ): Response<MessageResponse>


    @POST("profile/update/notify")
    suspend fun updateNotification(
        @Body  notify: Notify,
        @Header("Cookie") sessionIdAndToken: String
    ): Response<MessageResponse>


    @FormUrlEncoded
    @POST("profile/update")
    suspend fun updateProfile(
        @Field("name") name: String,
        @Field("surname") surname: String,
        @Header("Cookie") sessionIdAndToken: String
    ): Response<MessageResponse>




    @POST("state/set")
    suspend fun updateStatus(
//        @Body("datetime") datetime: String?,
//        @Body("totalTime") totalTime: Int?,
//        @Field("state") state:State,
//        @Field("geoPosition") geoPosition: GeoPosition?,
//        @Field("vehicle") vehicle: Vehicle?,

        @Body wrap: Wrap,
        @Header("Cookie") sessionIdAndToken: String
    ): Response<MessageResponse>


    @POST("activity/set")
    suspend fun updateActivity(
        @Body wrapVehicle: WrapVehicle,
        @Header("Cookie") sessionIdAndToken: String
    ): Response<MessageResponse>

    @POST("config/get/loadingscreen")
    suspend fun  getLoadingScreen(
        @Header("Cookie") sessionIdAndToken: String
    ):Response<LoadingResponse>

    @POST("config/get/splashscreen")
    suspend fun  getSplashScreen(
    ):Response<SplashResponse>

//        @POST("http://localhost:4000/serverCheck")
    @POST("check")
    suspend fun checkServer(
        @Header("Cookie") sessionIdAndToken: String
    ): Response<MessageResponse>
}