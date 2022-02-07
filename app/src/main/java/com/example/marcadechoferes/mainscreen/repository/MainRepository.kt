package com.example.marcadechoferes.mainscreen.repository

import com.example.marcadechoferes.localDataBase.LocalDataBase
import com.example.marcadechoferes.network.SafeApiRequest
import com.example.marcadechoferes.network.logoutResponse.MassageResponse
import com.example.marcadechoferes.network.retrofitInterfaces.RetrofitInterface
import com.example.marcadechoferes.network.signinResponse.Notify
import com.example.marcadechoferes.network.unsentApis.UnsentLanguageUpdation
import com.example.marcadechoferes.network.unsentApis.UnsentNotifyStateUpload
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

    suspend fun insertUnsentLanguageUpdate(unsentLanguageUpdation: UnsentLanguageUpdation){
        localDataBase.unsentApiDao().insertUpdateLanguage(unsentLanguageUpdation)
    }

    suspend fun insertUnsentNotifyStateUpload(unsentNotifyStateUpload: UnsentNotifyStateUpload)
    {
        localDataBase.unsentApiDao().insertUnsentNotifyStateUpload(unsentNotifyStateUpload)
    }

    fun deleteAllUnsentLanguageUpdationDetails(){
        localDataBase.unsentApiDao().deleteAllUnsentLanguageUpdationDetails()
    }

    fun deleteAllUnsentNotifyStateUploadDetails(){
        localDataBase.unsentApiDao().deleteAllUnsentNotifyStateUploadDetails()
    }
    fun isExistsUpdateLanguageDB(): Boolean{
        return localDataBase.unsentApiDao().isExistsUpdateLanguageDB()
    }
    fun isExistsUnsentNotifyStateUploadDB(): Boolean{
        return localDataBase.unsentApiDao().isExistsUnsentNotifyStateUploadDB()
    }


    fun getUnsentLanguageUpdateDetails(): UnsentLanguageUpdation{
      return localDataBase.unsentApiDao().getUnsentLanguageUpdateDetails()
    }
    fun getUnsentNotifyStateUploadDetails(): UnsentNotifyStateUpload{

        return localDataBase.unsentApiDao().getUnsentNotifyStateUploadDetails()
    }

}