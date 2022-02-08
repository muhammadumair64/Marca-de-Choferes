package com.logicasur.appchoferes.localDataBase.unsentApiDao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.logicasur.appchoferes.network.unsentApis.*

@Dao
interface UnsentApiDao {

    @Insert
    fun insertUpdateLanguage(unsentLanguageUpdation: UnsentLanguageUpdation)

    @Insert
    fun insertUnsentNotifyStateUpload(unsentNotifyStateUpload: UnsentNotifyStateUpload)

    @Insert
    fun insertUnsentStateUpdate(unsentStateUpdate: UnsentStateUpdate)

    @Insert
    fun insertUnsentUploadActivity(unsentUploadActivity: UnsentUploadActivity)

    @Insert
    fun insertUnsentAvatarUpdate(unsentUpdateAvatar: UnsentUpdateAvatar)

    @Insert
    fun insertUnsentProfileUpdate(unsentProfileUpdate: UnsentProfileUpdate)


    // Delete All
    @Query("DELETE FROM UnsentLanguageUpdation")
    fun deleteAllUnsentLanguageUpdationDetails()

    @Query("DELETE FROM UnsentNotifyStateUpload")
    fun deleteAllUnsentNotifyStateUploadDetails()

    @Query("DELETE FROM UnsentStateUpdate")
    fun deleteAllUUnsentStateUpdateDetails()

    @Query("DELETE FROM UnsentUploadActivity")
    fun deleteAllUnsentUploadActivityDetails()

    @Query("DELETE FROM UnsentUpdateAvatar")
    fun deleteAllUnsentUpdateAvatarDetails()

    @Query("DELETE FROM UnsentProfileUpdate")
    fun deleteAllUnsentProfileUpdateDetails()


    // Delete Particular item

    @Query("DELETE FROM UnsentStateUpdate WHERE roomDBId = :id")
    fun deleteUnsentStateUpdate(id: Int)


    @Query("DELETE FROM UnsentUploadActivity WHERE roomDBId = :id")
    fun deleteUnsentUploadActivity(id: Int)



    // Check Exists

    @Query("SELECT EXISTS(SELECT * FROM UnsentLanguageUpdation)")
    fun isExistsUpdateLanguageDB(): Boolean

    @Query("SELECT EXISTS(SELECT * FROM UnsentNotifyStateUpload)")
    fun isExistsUnsentNotifyStateUploadDB(): Boolean

    @Query("SELECT EXISTS(SELECT * FROM UnsentStateUpdate)")
    fun isExistsUnsentStateUpdateDB(): Boolean

    @Query("SELECT EXISTS(SELECT * FROM UnsentUploadActivity)")
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

    @Query("SELECT * from UnsentUploadActivity")
    fun getUnsentUploadActivityDetails(): List<UnsentUploadActivity>

    @Query("SELECT * from UnsentUpdateAvatar")
    fun getUnsentUpdateAvatarDetails(): UnsentUpdateAvatar

    @Query("SELECT * from UnsentProfileUpdate")
    fun getUnsentProfileUpdateDetails(): UnsentProfileUpdate
}