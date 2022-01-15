package com.logicasur.appchoferes.localDataBase

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.logicasur.appchoferes.network.signinResponse.*


@Dao
interface LocalDataBaseDao {
    @Insert
    fun insertProfile(profile: Profile)

    @Insert
    fun insertLastVar(lastVar: LastVarForRoom)

    @Insert
    fun insertLastidVehicle(lastIdVehicle: LastIdVehicle)

    @Insert
    fun insertState(states: List<State>)

    @Insert
    fun insertVehicle(vehicles: List<Vehicle>)

    @Insert
    fun insertWork(work:Work)





    @Query("DELETE  FROM Profile")
     fun deleteProfile()


    @Query("DELETE  FROM LastVarForRoom")
     fun deleteLastVar()

    @Query("DELETE  FROM LastIdVehicle")
     fun deleteLastidVehicle()


    @Query("DELETE  FROM State")
     fun deleteState()

    @Query("DELETE  FROM Vehicle")
     fun deleteVehicle()

    @Query("DELETE  FROM Work")
     fun deleteWork()



    @Query("SELECT * FROM Profile")
    fun getProfile() : Profile

    @Query("SELECT * FROM Vehicle")
    fun getVehicles():List<Vehicle>

    @Query("SELECT * FROM State")
    fun getState():List<State>
}