package com.logicasur.appchoferes.data.localDataBase.unsentApiDao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.logicasur.appchoferes.data.network.unsentApis.*
import com.logicasur.appchoferes.data.network.wrongData.wrongDataReport

@Dao
interface UnsentApiDao {

    @Insert
    fun insertUpdateLanguage(unsentLanguageUpdation: UnsentLanguageUpdation)

    @Insert
    fun insertUnsentNotifyStateUpload(unsentNotifyStateUpload: UnsentNotifyStateUpload)

    @Insert
    fun insertUnsentStateUpdate(unsentStateUpdate: UnsentStateUpdate)

    @Insert
    fun insertUnsentStateOrUploadActivity(unsentStatusOrUploadActivity: UnsentStatusOrUploadActivity)

    @Insert
    fun insertUnsentAvatarUpdate(unsentUpdateAvatar: UnsentUpdateAvatar)

    @Insert
    fun insertUnsentProfileUpdate(unsentProfileUpdate: UnsentProfileUpdate)

    @Insert
    fun insertUnsentStartWorkTime(unsentStartWorkTime: UnsentStartWorkTime)

    @Insert
    fun insertUnsentStartBreakTime(unsentStartBreakTimeWorkTime: UnsentStartBreakTime)


    // Delete All
    @Query("DELETE FROM UnsentLanguageUpdation")
    fun deleteAllUnsentLanguageUpdationDetails()

    @Query("DELETE FROM UnsentNotifyStateUpload")
    fun deleteAllUnsentNotifyStateUploadDetails()

    @Query("DELETE FROM UnsentStateUpdate")
    fun deleteAllUUnsentStateUpdateDetails()

    @Query("DELETE FROM UnsentStatusOrUploadActivity")
    fun deleteAllUnsentUploadActivityDetails()

    @Query("DELETE FROM UnsentUpdateAvatar")
    fun deleteAllUnsentUpdateAvatarDetails()

    @Query("DELETE FROM UnsentProfileUpdate")
    fun deleteAllUnsentProfileUpdateDetails()

    @Query("DELETE FROM UnsentStartWorkTime")
    fun deleteAllUnsentStartWorkTime()

    @Query("DELETE FROM UnsentStartBreakTime")
    fun deleteAllUnsentStartBreakTime()


    // Delete Particular item

    @Query("DELETE FROM UnsentStateUpdate WHERE roomDBId = :id")
    fun deleteUnsentStateUpdate(id: Int)


    @Query("DELETE FROM UnsentStatusOrUploadActivity WHERE roomDBId = :id")
    fun deleteUnsentUploadActivity(id: Int)

    @Query("DELETE FROM UnsentStatusOrUploadActivity")
    fun deleteAllUnsendApis()

    // Check Exists

    @Query("SELECT EXISTS(SELECT * FROM UnsentLanguageUpdation)")
    fun isExistsUpdateLanguageDB(): Boolean

    @Query("SELECT EXISTS(SELECT * FROM UnsentNotifyStateUpload)")
    fun isExistsUnsentNotifyStateUploadDB(): Boolean

    @Query("SELECT EXISTS(SELECT * FROM UnsentStateUpdate)")
    fun isExistsUnsentStateUpdateDB(): Boolean

    @Query("SELECT EXISTS(SELECT * FROM UnsentStatusOrUploadActivity)")
    fun isExistsUnsentUploadActivityDB(): Boolean

    @Query("SELECT EXISTS(SELECT * FROM UnsentUpdateAvatar)")
    fun isExistsUnsentUpdateAvatarDB(): Boolean

    @Query("SELECT EXISTS(SELECT * FROM UnsentProfileUpdate)")
    fun isExistsUnsentProfileUpdateDB(): Boolean


    @Query("SELECT * from UnsentLanguageUpdation")
    fun getUnsentLanguageUpdateDetails(): UnsentLanguageUpdation

    @Query("SELECT * from UnsentNotifyStateUpload")
    fun getUnsentNotifyStateUploadDetails(): UnsentNotifyStateUpload

    @Query("SELECT * from UnsentStateUpdate")
    fun getUnsentStateUpdateDetails(): List<UnsentStateUpdate>

    @Query("SELECT * from UnsentStatusOrUploadActivity")
    fun getUnsentUploadActivityDetails(): List<UnsentStatusOrUploadActivity>

    @Query("SELECT * from UnsentUpdateAvatar")
    fun getUnsentUpdateAvatarDetails(): UnsentUpdateAvatar

    @Query("SELECT * from UnsentProfileUpdate")
    fun getUnsentProfileUpdateDetails(): UnsentProfileUpdate


    @Query("SELECT * from UnsentStartWorkTime")
    fun getUnsentStartWorkTimeDetails(): UnsentStartWorkTime

    @Query("SELECT * from UnsentStartBreakTime")
    fun getUnsentStartBreakTimeDetails(): UnsentStartBreakTime



@Insert
fun insertWorngDataReport(lastVar :wrongDataReport)


}