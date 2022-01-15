package com.logicasur.appchoferes.mainscreen.repository

import com.logicasur.appchoferes.localDataBase.LocalDataBase
import com.logicasur.appchoferes.network.SafeApiRequest
import com.logicasur.appchoferes.network.logoutResponse.MassageResponse
import com.logicasur.appchoferes.network.retrofitInterfaces.RetrofitInterface
import com.logicasur.appchoferes.network.signinResponse.Notify
import javax.inject.Inject

class MainRepository @Inject constructor(
    val retrofitInterface: RetrofitInterface,
    val localDataBase: LocalDataBase
) : SafeApiRequest() {

    suspend fun updateLanguage(language:Int,Token: String): MassageResponse {

        return apiRequest { retrofitInterface.updateLanguage(language,Token)}
    }


    suspend fun updateNotification(notification:Boolean,Token: String): MassageResponse {
     var notify =Notify(notification)
        return apiRequest { retrofitInterface.updateNotification(notify,Token)}
    }

}