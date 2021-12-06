package com.example.marcadechoferes.localDataBase

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.marcadechoferes.network.signinResponse.*


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

}