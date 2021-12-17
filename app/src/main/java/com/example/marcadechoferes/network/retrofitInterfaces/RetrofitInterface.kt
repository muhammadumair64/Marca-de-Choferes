package com.example.marcadechoferes.network.retrofitInterfaces

import com.example.marcadechoferes.network.createNewPasswordResponse.CreateNewPasswordResponse
import com.example.marcadechoferes.network.forgotPasswordResponse.ForgotPasswordResponse
import com.example.marcadechoferes.network.getAvatarResponse.GetAvatarResponse
import com.example.marcadechoferes.network.logoutResponse.MassageResponse
import com.example.marcadechoferes.network.signinResponse.*
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
    ): Response<MassageResponse>


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
    ): Response<MassageResponse>


    @POST("sync")
    suspend fun sync(
        @Header("Cookie") sessionIdAndToken: String,
    ): Response<SigninResponse>


    @FormUrlEncoded
    @POST("profile/update/language")
    suspend fun updateLanguage(
        @Field("language") language: Int,
        @Header("Cookie") sessionIdAndToken: String
    ): Response<MassageResponse>


    @POST("profile/update/notify")
    suspend fun updateNotification(
        @Body  notify: Notify,
        @Header("Cookie") sessionIdAndToken: String
    ): Response<MassageResponse>


    @FormUrlEncoded
    @POST("profile/update")
    suspend fun updateProfile(
        @Field("name") name: String,
        @Field("surname") surname: String,
        @Header("Cookie") sessionIdAndToken: String
    ): Response<MassageResponse>




    @POST("state/set")
    suspend fun updateStatus(
//        @Body("datetime") datetime: String?,
//        @Body("totalTime") totalTime: Int?,
//        @Field("state") state:State,
//        @Field("geoPosition") geoPosition: GeoPosition?,
//        @Field("vehicle") vehicle: Vehicle?,

        @Body wrap: Wrap,
        @Header("Cookie") sessionIdAndToken: String
    ): Response<MassageResponse>


    @POST("activity/set")
    suspend fun updateActivity(
        @Body wrapVehicle: WrapVehicle,
        @Header("Cookie") sessionIdAndToken: String
    ): Response<MassageResponse>

}