package com.example.marcadechoferes.localDataBase

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.marcadechoferes.localDataBase.unsentApiDao.UnsentApiDao
import com.example.marcadechoferes.network.logoutResponse.MassageResponse
import com.example.marcadechoferes.network.signinResponse.*
import com.example.marcadechoferes.network.unsentApis.*

@Database(
    entities = [Profile::class, LastVarForRoom::class, LastIdVehicle::class, State::class, Vehicle::class, Work::class, UnsentLanguageUpdation::class, UnsentNotifyStateUpload::class,UnsentProfileUpdate::class,UnsentStateUpdate::class,UnsentUpdateAvatar::class,UnsentUploadActivity::class],
    version = 5.toInt()
)
abstract class LocalDataBase : RoomDatabase() {


    abstract fun localDBDao(): LocalDataBaseDao
    abstract fun unsentApiDao(): UnsentApiDao

}