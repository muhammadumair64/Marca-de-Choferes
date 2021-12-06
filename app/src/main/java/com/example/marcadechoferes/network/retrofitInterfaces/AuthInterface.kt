package com.example.marcadechoferes.network.retrofitInterfaces

import com.example.marcadechoferes.network.createNewPasswordResponse.CreateNewPasswordResponse
import com.example.marcadechoferes.network.forgotPasswordResponse.ForgotPasswordResponse
import com.example.marcadechoferes.network.getAvatarResponse.GetAvatarResponse
import com.example.marcadechoferes.network.logoutResponse.LogoutResponse
import com.example.marcadechoferes.network.signinResponse.SigninResponse
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Path

interface AuthInterface {

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

        ) : Response<SigninResponse>



    @FormUrlEncoded
    @POST("auth/recover")
    suspend fun forgotPassword(
        @Field("username") name: String
    ) : Response<ForgotPasswordResponse>


    @POST("auth/logout")
    suspend fun userLogout(
    ) : Response<LogoutResponse>

    @FormUrlEncoded
    @POST("auth/change")
    suspend fun createNewPassword(
        @Field("password") password: String,
    ) : Response<CreateNewPasswordResponse>


    @FormUrlEncoded
    @POST("reset/{otp}")
    suspend fun sendOTP(
        @Path("otp") otp:Int,
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

    ) : Response<SigninResponse>



   @POST("profile/get/avatar")
    suspend fun getAvatar(
    ) : Response<GetAvatarResponse>

}