package com.logicasur.appchoferes.localDataBase

import androidx.room.Database
import androidx.room.RoomDatabase
import com.logicasur.appchoferes.network.signinResponse.*

@Database(entities = [Profile::class,LastVarForRoom::class,LastIdVehicle::class,State::class,Vehicle::class,Work::class], version = 5.toInt())
abstract  class LocalDataBase:RoomDatabase() {


    abstract fun localDBDao(): LocalDataBaseDao

}