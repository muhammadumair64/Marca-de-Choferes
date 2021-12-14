package com.example.marcadechoferes.localDataBase

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.marcadechoferes.network.signinResponse.*

@Database(entities = [Profile::class,LastVarForRoom::class,LastIdVehicle::class,State::class,Vehicle::class,Work::class], version = 2.toInt())
abstract  class LocalDataBase:RoomDatabase() {


    abstract fun localDBDao(): LocalDataBaseDao

}