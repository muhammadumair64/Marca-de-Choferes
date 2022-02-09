package com.logicasur.appchoferes.Extra.serverCheck

import android.util.Log
import android.widget.Toast
import com.logicasur.appchoferes.Extra.K
import com.logicasur.appchoferes.Extra.TinyDB
import com.logicasur.appchoferes.auth.repository.AuthRepository
import com.logicasur.appchoferes.mainscreen.repository.MainRepository
import com.logicasur.appchoferes.myApplication.MyApplication
import com.logicasur.appchoferes.network.GeoPosition
import com.logicasur.appchoferes.network.logoutResponse.MassageResponse
import com.logicasur.appchoferes.network.signinResponse.State
import com.logicasur.appchoferes.network.signinResponse.Vehicle
import com.logicasur.appchoferes.network.unsentApis.UnsentStateUpdate
import com.logicasur.appchoferes.network.unsentApis.UnsentUploadActivity
import kotlinx.coroutines.*


class ServerCheck {

    companion object {
        var TAG2 = ""
        var tinyDB = TinyDB(MyApplication.appContext)
        val TAG = "CheckServer"
        lateinit var authRepository: AuthRepository
        lateinit var mainRepository: MainRepository


        suspend fun serverCheck(action: () -> Unit) {
            tagsForToast()
            Log.d(TAG, "Server Check function")

            CoroutineScope(Job()).launch(Dispatchers.IO) {
                try {
                    val Token = tinyDB.getString("Cookie")

                    val checkServerResponse =
                        authRepository.checkServer(Token!!)

                    Log.d(TAG, "$checkServerResponse")
                    if (checkServerResponse == MassageResponse("ok")) {
                        Log.d(TAG, "Server is working fine")
                        action()
                    }
//                    else{
//
//                        serverCheck { action() }
//                        Log.d(TAG,"Server is Down.")
//
//                    }

                } catch (e: Exception) {
                    withContext(Dispatchers.Main)
                    {
                        Toast.makeText(MyApplication.appContext, TAG2, Toast.LENGTH_SHORT)
                            .show()
                    }
                    Log.d(TAG, " Exception..${e.localizedMessage}")
                    serverCheck { action() }
                }


            }


        }


        suspend fun serverCheckActivityOrStatus(
            datetime: String?,
            totalTime: Int?,
            activity: Int?,
            geoPosition: GeoPosition?,
            vehicle: Vehicle?, state: State?, action1: () -> Unit,
        ) {
            tagsForToast()
            Log.d(TAG, "Server Check function")

            CoroutineScope(Job()).launch(Dispatchers.IO) {
                try {
                    val Token = tinyDB.getString("Cookie")

                    val checkServerResponse =
                        authRepository.checkServer(Token!!)

                    Log.d(TAG, "$checkServerResponse")
                    if (checkServerResponse == MassageResponse("ok")) {
                        Log.d(TAG, "Server is working fine")
                        action1()
                    }
//                    else{
//
//                        serverCheckActivityOrStatus{ action1() }
//                        Log.d(TAG,"Server is Down.")
//
//                    }

                } catch (e: Exception) {
                    withContext(Dispatchers.Main)
                    {
                        Toast.makeText(MyApplication.appContext, TAG2, Toast.LENGTH_SHORT)
                            .show()
                    }
                    Log.d(TAG, " Exception..${e.localizedMessage}")
                    if (state == null) {
                        mainRepository.insertUnsentUploadActivity(
                            UnsentUploadActivity(
                                0,
                                datetime!!,
                                activity!!,
                                totalTime,
                                vehicle!!.id,
                                vehicle.description,
                                vehicle.plateNumber,
                                geoPosition!!.latitud,
                                geoPosition.longitud
                            )
                        )
                    } else {
                        mainRepository.insertUnsentStateUpdate(
                            UnsentStateUpdate(
                                0,
                                datetime!!,
                                totalTime,
                                state.id,
                                state.description,
                                vehicle!!.id,
                                vehicle.description,
                                vehicle.plateNumber,
                                geoPosition!!.latitud,
                                geoPosition.longitud
                            )
                        )
                    }
                    K.checkNet()

                }


            }


        }


        fun tagsForToast() {
            var language = tinyDB.getString("language")
            when (language) {
                "0" -> {
                    TAG2 = "El servidor está caído"
                }
                "1" -> {
                    TAG2 = "Server is down"
                }
                else -> {
                    TAG2 = "Servidor caiu"
                }
            }

        }


    }

}