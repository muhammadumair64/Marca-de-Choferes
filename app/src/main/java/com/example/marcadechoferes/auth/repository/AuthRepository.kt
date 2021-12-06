package com.example.marcadechoferes.auth.repository


import com.example.marcadechoferes.localDataBase.LocalDataBase
import com.example.marcadechoferes.network.SafeApiRequest
import com.example.marcadechoferes.network.createNewPasswordResponse.CreateNewPasswordResponse
import com.example.marcadechoferes.network.forgotPasswordResponse.ForgotPasswordResponse
import com.example.marcadechoferes.network.getAvatarResponse.GetAvatarResponse
import com.example.marcadechoferes.network.logoutResponse.LogoutResponse
import com.example.marcadechoferes.network.retrofitInterfaces.AuthInterface
import com.example.marcadechoferes.network.signinResponse.LastVarForRoom
import com.example.marcadechoferes.network.signinResponse.Profile
import com.example.marcadechoferes.network.signinResponse.SigninResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AuthRepository @Inject constructor(
    val authInterface: AuthInterface,
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
            authInterface.userSignup(
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

        val LastWorkBreaklatitud: Int? = response.lastVar?.LastWorkBreaklatitud
        val lastActivity: Int? = response.lastVar?.lastActivity
        val lastState: Int? = response.lastVar?.lastState
        val lastStateDate: String? = response.lastVar?.lastStateDate
        val lastStateLatitud: Int? = response.lastVar?.lastStateLatitud
        val lastStateLongitud: Int? = response.lastVar?.lastStateLongitud
        val lastWorkBreakDateEnd: String? = response.lastVar?.lastWorkBreakDateEnd
        val lastWorkBreakDateIni: String? = response.lastVar?.lastWorkBreakDateIni
        val lastWorkBreakLongitud: Int? = response.lastVar?.lastWorkBreakLongitud
        val lastWorkBreakTotal: Int? = response.lastVar?.lastWorkBreakTotal
        val lastWorkedHoursDateEnd: String? = response.lastVar?.lastWorkedHoursDateEnd
        val lastWorkedHoursDateIni: String? = response.lastVar?.lastWorkedHoursDateIni
        val lastWorkedHoursLatitud: Int? = response.lastVar?.lastWorkedHoursLatitud
        val lastWorkedHoursLongitud: Int? = response.lastVar?.lastWorkedHoursLongitud
        val lastWorkedHoursTotal: Int? = response.lastVar?.lastWorkedHoursTotal
        var lastVarForRoom = LastVarForRoom(
            0,
            LastWorkBreaklatitud!!,
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
        localDataBase.localDBDao().insertLastidVehicle(response.lastVar.lastIdVehicle!!)

    }


    suspend fun logoutUser(name:String):LogoutResponse{

        return apiRequest { authInterface.userLogout() }
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

        return apiRequest { authInterface.forgotPassword(name) }
    }



    suspend fun getProfile(): Profile {
        return withContext(Dispatchers.IO) {

           localDataBase.localDBDao().getProfile()
        }
    }


    suspend fun CreateNewPasswordPassword(password:String):CreateNewPasswordResponse{

        return apiRequest { authInterface.createNewPassword(password) }
    }


    suspend fun getUserAvatar():GetAvatarResponse{

        return apiRequest { authInterface.getAvatar()}
    }

}