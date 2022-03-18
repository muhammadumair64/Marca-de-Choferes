package com.logicasur.appchoferes.data.localDataBase

import androidx.room.Database
import androidx.room.RoomDatabase
import com.logicasur.appchoferes.data.localDataBase.unsentApiDao.UnsentApiDao
import com.logicasur.appchoferes.data.network.signinResponse.*
import com.logicasur.appchoferes.data.network.unsentApis.*
import com.logicasur.appchoferes.data.network.wrongData.wrongDataReport

@Database(entities = [Profile::class,LastVarForRoom::class,LastIdVehicle::class,State::class,Vehicle::class,Work::class,UnsentLanguageUpdation::class,UnsentNotifyStateUpload::class,UnsentProfileUpdate::class,UnsentStateUpdate::class, UnsentUpdateAvatar::class, UnsentStatusOrUploadActivity::class,UnsentStartBreakTime::class,UnsentStartWorkTime::class,wrongDataReport::class], version = 10.toInt())
abstract  class LocalDataBase:RoomDatabase() {


    abstract fun localDBDao(): LocalDataBaseDao
    abstract  fun unsentApiDao():UnsentApiDao

}