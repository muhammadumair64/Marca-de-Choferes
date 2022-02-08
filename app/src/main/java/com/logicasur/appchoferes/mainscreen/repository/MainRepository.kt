package com.logicasur.appchoferes.mainscreen.repository

import com.logicasur.appchoferes.localDataBase.LocalDataBase
import com.logicasur.appchoferes.network.SafeApiRequest
import com.logicasur.appchoferes.network.logoutResponse.MassageResponse
import com.logicasur.appchoferes.network.retrofitInterfaces.RetrofitInterface
import com.logicasur.appchoferes.network.signinResponse.Notify
import com.logicasur.appchoferes.network.unsentApis.*
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
    fun insertUnsentStateUpdate(unsentStateUpdate: UnsentStateUpdate)
    {
        localDataBase.unsentApiDao().insertUnsentStateUpdate(unsentStateUpdate)
    }

    fun insertUnsentUploadActivity(unsentUploadActivity: UnsentUploadActivity)
    {
        localDataBase.unsentApiDao().insertUnsentUploadActivity(unsentUploadActivity)
    }

    fun insertUnsentAvatarUpdate(unsentUpdateAvatar: UnsentUpdateAvatar){
        localDataBase.unsentApiDao().insertUnsentAvatarUpdate(unsentUpdateAvatar)
    }

    fun insertUnsentProfileUpdate(unsentProfileUpdate: UnsentProfileUpdate){
        localDataBase.unsentApiDao().insertUnsentProfileUpdate(unsentProfileUpdate)
    }



    fun deleteAllUUnsentStateUpdateDetails(){
        localDataBase.unsentApiDao().deleteAllUUnsentStateUpdateDetails()
    }

    fun deleteAllUnsentUploadActivityDetails(){
        localDataBase.unsentApiDao().deleteAllUnsentUploadActivityDetails()
    }

    fun deleteAllUnsentUpdateAvatarDetails(){
        localDataBase.unsentApiDao().deleteAllUnsentUpdateAvatarDetails()
    }

    fun deleteAllUnsentProfileUpdateDetails(){
        localDataBase.unsentApiDao().deleteAllUnsentProfileUpdateDetails()
    }

    fun isExistsUnsentStateUpdateDB(): Boolean{
        return localDataBase.unsentApiDao().isExistsUnsentStateUpdateDB()
    }

    fun isExistsUnsentUploadActivityDB(): Boolean{
        return localDataBase.unsentApiDao().isExistsUnsentUploadActivityDB()
    }
    fun isExistsUnsentUpdateAvatarDB(): Boolean{
        return localDataBase.unsentApiDao().isExistsUnsentUpdateAvatarDB()
    }
    fun isExistsUnsentProfileUpdateDB(): Boolean{
        return localDataBase.unsentApiDao().isExistsUnsentProfileUpdateDB()
    }


    fun getUnsentStateUpdateDetails():List< UnsentStateUpdate>{
        return localDataBase.unsentApiDao().getUnsentStateUpdateDetails()
    }

    fun getUnsentUploadActivityDetails(): List<UnsentUploadActivity> {
        return localDataBase.unsentApiDao().getUnsentUploadActivityDetails()
    }

    fun getUnsentUpdateAvatarDetails(): UnsentUpdateAvatar {
        return localDataBase.unsentApiDao().getUnsentUpdateAvatarDetails()
    }

    fun getUnsentProfileUpdateDetails(): UnsentProfileUpdate {
        return localDataBase.unsentApiDao().getUnsentProfileUpdateDetails()
    }

}