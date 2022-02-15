package com.logicasur.appchoferes.auth.repository


import com.logicasur.appchoferes.localDataBase.LocalDataBase
import com.logicasur.appchoferes.network.GeoPosition
import com.logicasur.appchoferes.network.SafeApiRequest
import com.logicasur.appchoferes.network.createNewPasswordResponse.CreateNewPasswordResponse
import com.logicasur.appchoferes.network.forgotPasswordResponse.ForgotPasswordResponse
import com.logicasur.appchoferes.network.getAvatarResponse.GetAvatarResponse
import com.logicasur.appchoferes.network.loadingResponse.LoadingResponse
import com.logicasur.appchoferes.network.loadingResponse.SplashResponse
import com.logicasur.appchoferes.network.logoutResponse.MessageResponse
import com.logicasur.appchoferes.network.retrofitInterfaces.RetrofitInterface
import com.logicasur.appchoferes.network.signinResponse.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AuthRepository @Inject constructor(
    val retrofitInterface: RetrofitInterface,
    val localDataBase: LocalDataBase
) : SafeApiRequest() {
    suspend fun userSignin(
        name: String,
        password: String,
        idApp: String,
        memUsed: String,
        diskFree: String,
        diskTotal: String,
        model: String,
        operatingSystem: String,
        osVersion: String,
        appVersion: String,
        appBuild: String,
        platform: String,
        manufacturer: String,
        uuid: String,
        isVirtual: String
    ): SigninResponse {
        return apiRequest {
            retrofitInterface.userSignup(
                name,
                password,
                idApp,
                memUsed,
                diskFree,
                diskTotal,
                model,
                operatingSystem,
                osVersion,
                appVersion,
                appBuild,
                platform,
                manufacturer,
                uuid,
                isVirtual
            )
        }
    }

    suspend fun InsertSigninData(response: SigninResponse) {
        println("DB ..Response $response")
        //Profile Data Store in dataBase
        localDataBase.localDBDao().insertProfile(response.profile!!)


        // LastVar store in Room

        val LastWorkBreaklatitud: Float = response.lastVar?.LastWorkBreaklatitud!!
        val lastActivity: Int? = response.lastVar?.lastActivity
        val lastState: Int? = response.lastVar?.lastState
        val lastStateDate: String? = response.lastVar?.lastStateDate
        val lastStateLatitud: Float = response.lastVar?.lastStateLatitud
        val lastStateLongitud: Float = response.lastVar?.lastStateLongitud
        val lastWorkBreakDateEnd: String? = response.lastVar?.lastWorkBreakDateEnd
        val lastWorkBreakDateIni: String? = response.lastVar?.lastWorkBreakDateIni
        val lastWorkBreakLongitud: Float = response.lastVar?.lastWorkBreakLongitud
        val lastWorkBreakTotal: Int? = response.lastVar?.lastWorkBreakTotal
        val lastWorkedHoursDateEnd: String? = response.lastVar?.lastWorkedHoursDateEnd
        val lastWorkedHoursDateIni: String? = response.lastVar?.lastWorkedHoursDateIni
        val lastWorkedHoursLatitud: Float = response.lastVar?.lastWorkedHoursLatitud
        val lastWorkedHoursLongitud: Float = response.lastVar?.lastWorkedHoursLongitud
        val lastWorkedHoursTotal: Int? = response.lastVar?.lastWorkedHoursTotal
        var lastVarForRoom = LastVarForRoom(
            0,
            LastWorkBreaklatitud,
            lastActivity,
            lastState,
            lastStateDate,
            lastStateLatitud,
            lastStateLongitud,
            lastWorkBreakDateEnd,
            lastWorkBreakDateIni,
            lastWorkBreakLongitud,
            lastWorkBreakTotal,
            lastWorkedHoursDateEnd,
            lastWorkedHoursDateIni,
            lastWorkedHoursLatitud,
            lastWorkedHoursLongitud,
            lastWorkedHoursTotal
        )
        localDataBase.localDBDao().insertLastVar(lastVarForRoom)



        //Store states in db
        localDataBase.localDBDao().insertState(response.states!!)


        //store vehicle
        localDataBase.localDBDao().insertVehicle(response.vehicles!!)

        //store lastWork
        localDataBase.localDBDao().insertWork(response.work!!)

        //store last vehicle
        localDataBase.localDBDao().insertLastidVehicle(response.lastVar?.lastIdVehicle!!)

    }


    suspend fun logoutUser(name:String):MessageResponse{

        return apiRequest { retrofitInterface.userLogout() }
    }


    suspend fun clearData(){
        localDataBase.localDBDao().deleteLastVar()
        localDataBase.localDBDao().deleteLastidVehicle()
        localDataBase.localDBDao().deleteProfile()
        localDataBase.localDBDao().deleteState()
        localDataBase.localDBDao().deleteVehicle()
        localDataBase.localDBDao().deleteWork()
        println("Room Data is Clear Now")
    }

    suspend fun forgotPassword(name:String):ForgotPasswordResponse{

        return apiRequest { retrofitInterface.forgotPassword(name) }
    }



    suspend fun getProfile(): Profile {
        return withContext(Dispatchers.IO) {

           localDataBase.localDBDao().getProfile()
        }
    }


    suspend fun CreateNewPasswordPassword(password:String,Token:String):CreateNewPasswordResponse{

        return apiRequest { retrofitInterface.createNewPassword(password,Token) }
    }


    suspend fun getUserAvatar(user:String,Token: String):GetAvatarResponse{

        return apiRequest { retrofitInterface.getAvatar(user,Token)}
    }

    suspend fun updateAvatar(avatar:String,Token: String):MessageResponse{

        return apiRequest { retrofitInterface.updateAvatar(avatar,Token)}
    }

    suspend fun userSync(Token: String): SigninResponse {
        return apiRequest {
            retrofitInterface.sync(Token)
        }
    }

    suspend fun otp(
        otp:Int,
        name: String,
        idApp: String,
        memUsed: String,
        diskFree: String,
        diskTotal: String,
        model: String,
        operatingSystem: String,
        osVersion: String,
        appVersion: String,
        appBuild: String,
        platform: String,
        manufacturer: String,
        uuid: String,
        isVirtual: String,
        Token: String
    ): SigninResponse {
        return apiRequest {
            retrofitInterface.sendOTP(
                otp ,
                name,
                idApp,
                memUsed,
                diskFree,
                diskTotal,
                model,
                operatingSystem,
                osVersion,
                appVersion,
                appBuild,
                platform,
                manufacturer,
                uuid,
                isVirtual,
                Token
            )
        }
    }

    suspend fun updateProfile(name:String,surname:String,Token: String):MessageResponse{

        return apiRequest { retrofitInterface.updateProfile(name,surname,Token)}
    }

    suspend fun getVehicle():List<Vehicle>{
        return withContext(Dispatchers.IO) {

            localDataBase.localDBDao().getVehicles()
        }
    }

    suspend fun getState():List<State>{
        return withContext(Dispatchers.IO) {
            localDataBase.localDBDao().getState()
        }
    }


    suspend fun clearProfile(){
        localDataBase.localDBDao().deleteProfile()
    }

    suspend fun insetProfile(profile:Profile)
    {
        localDataBase.localDBDao().insertProfile(profile)
    }

    suspend fun updateState(datetime: String?,
                           totalTime: Int?,
                            state:State?,
                          geoPosition: GeoPosition?,
                        vehicle: Vehicle?,
                       sessionIdAndToken: String):MessageResponse
    {
        val wrap = Wrap(datetime,totalTime,state!!,geoPosition,vehicle)
        return apiRequest { retrofitInterface.updateStatus(wrap,sessionIdAndToken)}
    }


    suspend fun updateActivity(datetime: String?,
                            totalTime: Int?,
                               activity: Int?,
                            geoPosition: GeoPosition?,
                            vehicle: Vehicle?,
                            sessionIdAndToken: String):MessageResponse
    {
        val wrapVehicle = WrapVehicle(datetime,totalTime,activity,geoPosition,vehicle)
        return apiRequest { retrofitInterface.updateActivity(wrapVehicle,sessionIdAndToken)}
    }



    suspend fun getLoadingScreen(Token: String):LoadingResponse{

        return apiRequest { retrofitInterface.getLoadingScreen(Token)}
    }


    suspend fun getSplashScreen():SplashResponse{

        return apiRequest { retrofitInterface.getSplashScreen()}
    }


    suspend fun checkServer(token:String):MessageResponse
    {
        return apiRequest {retrofitInterface.checkServer(token)}
    }

}