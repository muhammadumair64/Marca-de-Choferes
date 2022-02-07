package com.logicasur.appchoferes.localDataBase

import androidx.room.Database
import androidx.room.RoomDatabase
import com.logicasur.appchoferes.localDataBase.unsentApiDao.UnsentApiDao
import com.logicasur.appchoferes.network.signinResponse.*
import com.logicasur.appchoferes.network.unsentApis.*

@Database(entities = [Profile::class,LastVarForRoom::class,LastIdVehicle::class,State::class,Vehicle::class,Work::class,UnsentLanguageUpdation::class,UnsentNotifyStateUpload::class,UnsentProfileUpdate::class,UnsentStateUpdate::class, UnsentUpdateAvatar::class, UnsentUploadActivity::class], version = 6.toInt())
abstract  class LocalDataBase:RoomDatabase() {


    abstract fun localDBDao(): LocalDataBaseDao
    abstract  fun unsentApiDao():UnsentApiDao

}