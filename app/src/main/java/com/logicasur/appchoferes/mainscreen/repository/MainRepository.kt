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

    suspend fun updateLanguage(language: Int, Token: String): MassageResponse {

        return apiRequest { retrofitInterface.updateLanguage(language, Token) }
    }


    suspend fun updateNotification(notification: Boolean, Token: String): MassageResponse {
        var notify = Notify(notification)
        return apiRequest { retrofitInterface.updateNotification(notify, Token) }
    }


    suspend fun insertUnsentLanguageUpdate(unsentLanguageUpdation: UnsentLanguageUpdation) {
        localDataBase.unsentApiDao().insertUpdateLanguage(unsentLanguageUpdation)
    }

    suspend fun insertUnsentNotifyStateUpload(unsentNotifyStateUpload: UnsentNotifyStateUpload) {
        localDataBase.unsentApiDao().insertUnsentNotifyStateUpload(unsentNotifyStateUpload)
    }

   suspend fun deleteAllUnsentLanguageUpdationDetails(){
        localDataBase.unsentApiDao().deleteAllUnsentLanguageUpdationDetails()
    }

   suspend fun deleteAllUnsentNotifyStateUploadDetails(){
        localDataBase.unsentApiDao().deleteAllUnsentNotifyStateUploadDetails()
    }
    suspend fun isExistsUpdateLanguageDB(): Boolean{
        return localDataBase.unsentApiDao().isExistsUpdateLanguageDB()
    }
    suspend fun isExistsUnsentNotifyStateUploadDB(): Boolean{
        return localDataBase.unsentApiDao().isExistsUnsentNotifyStateUploadDB()
    }


    suspend fun getUnsentLanguageUpdateDetails(): UnsentLanguageUpdation{
        return localDataBase.unsentApiDao().getUnsentLanguageUpdateDetails()
    }
    suspend fun getUnsentNotifyStateUploadDetails(): UnsentNotifyStateUpload{

        return localDataBase.unsentApiDao().getUnsentNotifyStateUploadDetails()
    }
    suspend fun insertUnsentStateUpdate(unsentStateUpdate: UnsentStateUpdate)
    {
        localDataBase.unsentApiDao().insertUnsentStateUpdate(unsentStateUpdate)
    }

    suspend fun insertUnsentUploadActivity(unsentUploadActivity: UnsentUploadActivity)
    {
        localDataBase.unsentApiDao().insertUnsentUploadActivity(unsentUploadActivity)
    }

    suspend fun insertUnsentAvatarUpdate(unsentUpdateAvatar: UnsentUpdateAvatar){
        localDataBase.unsentApiDao().insertUnsentAvatarUpdate(unsentUpdateAvatar)
    }

    suspend fun insertUnsentProfileUpdate(unsentProfileUpdate: UnsentProfileUpdate){
        localDataBase.unsentApiDao().insertUnsentProfileUpdate(unsentProfileUpdate)
    }



    suspend fun deleteAllUUnsentStateUpdateDetails(){
        localDataBase.unsentApiDao().deleteAllUUnsentStateUpdateDetails()
    }

    suspend fun deleteAllUnsentUploadActivityDetails(){
        localDataBase.unsentApiDao().deleteAllUnsentUploadActivityDetails()
    }

    suspend fun deleteAllUnsentUpdateAvatarDetails(){
        localDataBase.unsentApiDao().deleteAllUnsentUpdateAvatarDetails()
    }

    suspend fun deleteAllUnsentProfileUpdateDetails(){
        localDataBase.unsentApiDao().deleteAllUnsentProfileUpdateDetails()
    }

    suspend fun isExistsUnsentStateUpdateDB(): Boolean{
        return localDataBase.unsentApiDao().isExistsUnsentStateUpdateDB()
    }

    suspend fun isExistsUnsentUploadActivityDB(): Boolean{
        return localDataBase.unsentApiDao().isExistsUnsentUploadActivityDB()
    }
    suspend fun isExistsUnsentUpdateAvatarDB(): Boolean{
        return localDataBase.unsentApiDao().isExistsUnsentUpdateAvatarDB()
    }
    suspend fun isExistsUnsentProfileUpdateDB(): Boolean{
        return localDataBase.unsentApiDao().isExistsUnsentProfileUpdateDB()
    }


    suspend fun getUnsentStateUpdateDetails():List< UnsentStateUpdate>{
        return localDataBase.unsentApiDao().getUnsentStateUpdateDetails()
    }

    suspend fun getUnsentUploadActivityDetails(): List<UnsentUploadActivity> {
        return localDataBase.unsentApiDao().getUnsentUploadActivityDetails()
    }

    suspend fun getUnsentUpdateAvatarDetails(): UnsentUpdateAvatar {
        return localDataBase.unsentApiDao().getUnsentUpdateAvatarDetails()
    }

    suspend fun getUnsentProfileUpdateDetails(): UnsentProfileUpdate {
        return localDataBase.unsentApiDao().getUnsentProfileUpdateDetails()
    }

    // delete Particular item
    suspend fun deleteUnsentStateUpdate(id: Int) {
        localDataBase.unsentApiDao().deleteUnsentStateUpdate(id)
    }

    suspend fun deleteUnsentUploadActivity(id: Int){
        localDataBase.unsentApiDao().deleteUnsentUploadActivity(id)
    }

    suspend fun insertUnsentStartWorkTime(unsentStartWorkTime: UnsentStartWorkTime)
    {
        localDataBase.unsentApiDao().insertUnsentStartWorkTime(unsentStartWorkTime)
    }

    suspend fun insertUnsentStartBreakTime(unsentStartBreakTimeWorkTime: UnsentStartBreakTime)
    {
        localDataBase.unsentApiDao().insertUnsentStartBreakTime(unsentStartBreakTimeWorkTime)
    }



    suspend fun deleteAllUnsentStartWorkTime()
    {
        localDataBase.unsentApiDao().deleteAllUnsentStartWorkTime()
    }

    suspend fun deleteAllUnsentStartBreakTime()
    {
        localDataBase.unsentApiDao().deleteAllUnsentStartBreakTime()
    }


    suspend fun getUnsentStartWorkTimeDetails(): UnsentStartWorkTime
    {
        return localDataBase.unsentApiDao().getUnsentStartWorkTimeDetails()
    }

    suspend fun getUnsentStartBreakTimeDetails(): UnsentStartBreakTime
    {
        return localDataBase.unsentApiDao().getUnsentStartBreakTimeDetails()
    }

}