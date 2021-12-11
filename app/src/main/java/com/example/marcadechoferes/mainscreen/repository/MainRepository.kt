package com.example.marcadechoferes.mainscreen.repository

import com.example.marcadechoferes.localDataBase.LocalDataBase
import com.example.marcadechoferes.network.SafeApiRequest
import com.example.marcadechoferes.network.logoutResponse.MassageResponse
import com.example.marcadechoferes.network.retrofitInterfaces.RetrofitInterface
import javax.inject.Inject

class MainRepository @Inject constructor(
    val retrofitInterface: RetrofitInterface,
    val localDataBase: LocalDataBase
) : SafeApiRequest() {

    suspend fun updateLanguage(language:Int,Token: String): MassageResponse {

        return apiRequest { retrofitInterface.updateLanguage(language,Token)}
    }


    suspend fun updateNotification(notify:Boolean,Token: String): MassageResponse {

        return apiRequest { retrofitInterface.updateNotification(notify,Token)}
    }

}