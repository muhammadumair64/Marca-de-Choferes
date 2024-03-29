package com.logicasur.appchoferes.Extra.serverCheck

import android.util.Log
import android.widget.Toast
import com.logicasur.appchoferes.Extra.K
import com.logicasur.appchoferes.Extra.TinyDB
import com.logicasur.appchoferes.auth.otp.interfaces.OnEndLoadingCallbacks
import com.logicasur.appchoferes.auth.repository.AuthRepository
import com.logicasur.appchoferes.loadingScreen.LoadingScreen
import com.logicasur.appchoferes.mainscreen.repository.MainRepository
import com.logicasur.appchoferes.myApplication.MyApplication
import com.logicasur.appchoferes.network.GeoPosition
import com.logicasur.appchoferes.network.NoInternetException
import com.logicasur.appchoferes.network.logoutResponse.MassageResponse
import com.logicasur.appchoferes.network.signinResponse.State
import com.logicasur.appchoferes.network.signinResponse.Vehicle
import com.logicasur.appchoferes.network.unsentApis.UnsentStatusOrUploadActivity
import kotlinx.coroutines.*
import java.net.SocketException
import java.net.SocketTimeoutException
import java.util.*


class ServerCheck {

    companion object {
        var TAG2 = ""
        var tinyDB = TinyDB(MyApplication.appContext)
        val TAG = "c"
        lateinit var authRepository: AuthRepository
        lateinit var mainRepository: MainRepository


        suspend fun serverCheck(onEndLoadingCallbacks: OnEndLoadingCallbacks?, action: () -> Unit) {

            var checkServerResponse: MassageResponse? = null

            var check = 0
            val myTimer = Timer()
            myTimer!!.schedule(object : TimerTask() {
                override fun run() {
                    if (check == 1) {
                        Log.d("NETCHECKTEST", "In popUp condition[p")
                        Log.d("NETCHECKTEST", "----working in required")
                        Log.d("NETCHECKTEST", LoadingScreen.OnEndLoadingCallbacks.toString())
                        CoroutineScope(Job()).launch(Dispatchers.Main) {
                            if (!MyApplication.checKForSyncLoading) {
                                LoadingScreen.OnEndLoadingCallbacks!!.openServerPopup()
                            }


                        }

//                        if (onEndLoadingCallbacks != null) {
//
//
//                        }
                        myTimer.purge()
                        myTimer.cancel()
                    } else {
                        check++
                    }

                }
            }, 0, 12000)

            tagsForToast()
            Log.d(TAG, "Server Check function")

            CoroutineScope(Job()).launch(Dispatchers.IO) {
                try {
                    val Token = tinyDB.getString("Cookie")

                    Log.d(TAG, "Server CHECK API Hit")
                    checkServerResponse =
                        authRepository.checkServer(Token!!)

                    Log.d(TAG, "$checkServerResponse")
                    if (checkServerResponse == MassageResponse("ok")) {
                        myTimer.cancel()
                        myTimer.purge()
                        Log.d(TAG, "Server is working fine")

                        action()
                    }


                } catch (e: SocketTimeoutException) {
                    withContext(Dispatchers.Main)
                    {
                        Toast.makeText(MyApplication.appContext, TAG2, Toast.LENGTH_SHORT)
                            .show()
                        endLoading()
                    }
//                    serverCheck { action() }
                } catch (e: SocketException) {
                    withContext(Dispatchers.Main)
                    {
                        Toast.makeText(MyApplication.appContext, TAG2, Toast.LENGTH_SHORT)
                            .show()
                        endLoading()
                    }
//                    serverCheck { action() }
                } catch (e: NoInternetException) {
//                    LoadingScreen.OnEndLoadingCallbacks!!.openPopup(null)
                    Log.d(TAG, " Exception..${e.localizedMessage}")
                    endLoading()
                //                    serverCheck { action() }
                } catch (e: Exception) {

                    Log.d(TAG, " Exception..${e.localizedMessage}")
                    endLoading()
//                    LoadingScreen.OnEndLoadingCallbacks!!.openPopup(null)
//                    serverCheck { action() }
                }


            }


        }

        fun endLoading() {
            if (!MyApplication.checKForSyncLoading) {
                LoadingScreen.OnEndLoadingCallbacks!!.endLoading()
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
            Log.d(TAG, "Server Check function 2nd")




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
//                    withContext(Dispatchers.Main)
//                    {
//                        Toast.makeText(MyApplication.appContext, TAG2, Toast.LENGTH_SHORT)
//                            .show()
//                    }
                    Log.d(TAG, " Exception..${e.localizedMessage}")

                    if (state == null) {
                        mainRepository.insertUnsentStateOrUploadActivity(
                            UnsentStatusOrUploadActivity(
                                0,
                                datetime!!,
                                null,
                                null,
                                activity!!,
                                totalTime,
                                vehicle!!.id,
                                vehicle.description,
                                vehicle.plateNumber,
                                geoPosition!!.latitud,
                                geoPosition.longitud
                            )
                        )
                        CoroutineScope(Job()).launch {
                            Log.d("STATE_TESTING", "IN END LOADING")
                            LoadingScreen.OnEndLoadingCallbacks!!.endLoading()
                        }


                    } else {
                        mainRepository.insertUnsentStateOrUploadActivity(
                            UnsentStatusOrUploadActivity(
                                0,
                                datetime!!,
                                state.id,
                                state.description, null, totalTime,
                                vehicle!!.id,
                                vehicle.description,
                                vehicle.plateNumber,
                                geoPosition!!.latitud,
                                geoPosition.longitud
                            )
                        )
                        CoroutineScope(Job()).launch {
                            Log.d("STATE_TESTING", "IN END LOADING")
                            LoadingScreen.OnEndLoadingCallbacks!!.endLoading()
                        }
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

        suspend fun serverCheckTesting(
            onEndLoadingCallbacks: OnEndLoadingCallbacks?,
            apiCall: (serverAction: () -> Unit) -> Unit
        ) {
            Log.d(TAG, "Server Check Testing function starts")
            var checkServerResponse: MassageResponse? = null

            var check = 0
            val serverCheckTimer = Timer()
            serverCheckTimer.schedule(object : TimerTask() {
                override fun run() {
                    if (check == 1) {
                        Log.d("NETCHECKTEST", "----working")

                        CoroutineScope(Job()).launch(Dispatchers.Main) {
                            LoadingScreen.OnEndLoadingCallbacks!!.openServerPopup()
                        }

//                        if (checkServerResponse == null && onEndLoadingCallbacks != null) {
//                            onEndLoadingCallbacks.openServerPopup()
//                        }
                        serverCheckTimer.purge()
                        serverCheckTimer.cancel()
                    } else {
                        check++
                    }

                }
            }, 0, 10000)

            tagsForToast()
            Log.d(TAG, "Server Check function")

            CoroutineScope(Job()).launch(Dispatchers.IO) {
                try {
                    val Token = tinyDB.getString("Cookie")

                    checkServerResponse =
                        authRepository.checkServer(Token!!)

                    Log.d(TAG, "$checkServerResponse")
                    if (checkServerResponse == MassageResponse("ok")) {
                        serverCheckTimer.cancel()
                        serverCheckTimer.purge()
                        Log.d(TAG, "ServerTesting is working fine")

                        var checkOnApiCall = 0
                        val myTimer = Timer()
                        myTimer.schedule(object : TimerTask() {
                            override fun run() {
                                if (checkOnApiCall == 1 || checkOnApiCall == 2) {
                                    CoroutineScope(Job()).launch {
                                        serverCheck(LoadingScreen.OnEndLoadingCallbacks) {}
                                    }

                                    checkOnApiCall++
                                } else if (checkOnApiCall > 2) {
                                    Log.d("NETCHECKTEST", "----workingTesting")
                                    myTimer.purge()
                                    myTimer.cancel()
//                    if (notifyResponse == null ) {
//
//                    }
                                } else {
                                    checkOnApiCall++
                                }

                            }
                        }, 0, 20000)

                        apiCall() {
                            // Server is not well

                            Log.d(TAG, "Response is received Cancel the timer.")
                            myTimer.cancel()
                            myTimer.purge()


                        }
                    }


                } catch (e: SocketTimeoutException) {
                    withContext(Dispatchers.Main)
                    {
                        Toast.makeText(MyApplication.appContext, TAG2, Toast.LENGTH_SHORT)
                            .show()
                    }
//                    serverCheck { action() }
                } catch (e: SocketException) {
                    withContext(Dispatchers.Main)
                    {
                        Toast.makeText(MyApplication.appContext, TAG2, Toast.LENGTH_SHORT)
                            .show()
                    }
//                    serverCheck { action() }
                } catch (e: NoInternetException) {
                    Log.d(TAG, " Exception..${e.localizedMessage}")
//                    serverCheck { action() }
                } catch (e: Exception) {

                    Log.d(TAG, " Exception..${e.localizedMessage}")
//                    serverCheck { action() }
                }


            }


        }

        suspend fun serverCheckMainActivityApi(
            onEndLoadingCallbacks: OnEndLoadingCallbacks?,
            apiCall: (serverAction: () -> Unit) -> Unit
        ) {
            Log.d(TAG, "Server Check Testing function starts")
            var checkServerResponse: MassageResponse? = null

            var check = 0
            val serverCheckTimer = Timer()
            serverCheckTimer.schedule(object : TimerTask() {
                override fun run() {
                    if (check == 1) {
                        Log.d("NETCHECKTEST", "----working")

                        CoroutineScope(Job()).launch(Dispatchers.Main) {
                            LoadingScreen.OnEndLoadingCallbacks!!.openPopup(null)
                        }

//                        if (checkServerResponse == null && onEndLoadingCallbacks != null) {
//                            onEndLoadingCallbacks.openServerPopup()
//                        }
                        serverCheckTimer.purge()
                        serverCheckTimer.cancel()
                    } else {
                        check++
                    }

                }
            }, 0, 10000)

            tagsForToast()
            Log.d(TAG, "Server Check function")

            CoroutineScope(Job()).launch(Dispatchers.IO) {
                try {
                    val Token = tinyDB.getString("Cookie")

                    checkServerResponse =
                        authRepository.checkServer(Token!!)

                    Log.d(TAG, "$checkServerResponse")
                    if (checkServerResponse == MassageResponse("ok")) {
                        serverCheckTimer.cancel()
                        serverCheckTimer.purge()
                        Log.d(TAG, "ServerTesting is working fine")

                        var checkOnApiCall = 0
                        val myTimer = Timer()
                        myTimer.schedule(object : TimerTask() {
                            override fun run() {
                                if (checkOnApiCall == 1 || checkOnApiCall == 2) {
                                    CoroutineScope(Job()).launch {
                                        serverCheckDuringStatus(LoadingScreen.OnEndLoadingCallbacks) {}
                                    }

                                    checkOnApiCall++
                                } else if (checkOnApiCall > 2) {
                                    Log.d("NETCHECKTEST", "----workingTesting")
                                    myTimer.purge()
                                    myTimer.cancel()
//                    if (notifyResponse == null ) {
//
//                    }
                                } else {
                                    checkOnApiCall++
                                }

                            }
                        }, 0, 20000)

                        apiCall() {
                            // Server is not well

                            Log.d(TAG, "Response is received Cancel the timer.")
                            myTimer.cancel()
                            myTimer.purge()


                        }
                    }


                } catch (e: SocketTimeoutException) {
                    withContext(Dispatchers.Main)
                    {
                        Toast.makeText(MyApplication.appContext, TAG2, Toast.LENGTH_SHORT)
                            .show()
                    }
//                    serverCheck { action() }
                } catch (e: SocketException) {
                    withContext(Dispatchers.Main)
                    {
                        Toast.makeText(MyApplication.appContext, TAG2, Toast.LENGTH_SHORT)
                            .show()
                    }
//                    serverCheck { action() }
                } catch (e: NoInternetException) {
                    Log.d(TAG, " Exception..${e.localizedMessage}")
//                    serverCheck { action() }
                } catch (e: Exception) {

                    Log.d(TAG, " Exception..${e.localizedMessage}")
//                    serverCheck { action() }
                }


            }


        }

        suspend fun serverCheckDuringStatus(
            onEndLoadingCallbacks: OnEndLoadingCallbacks?,
            action: () -> Unit
        ) {

            var checkServerResponse: MassageResponse? = null

            var check = 0
            val myTimer = Timer()
            myTimer!!.schedule(object : TimerTask() {
                override fun run() {
                    if (check == 1) {
                        Log.d("NETCHECKTEST", "In popUp condition[p")
                        Log.d("NETCHECKTEST", "----working in required")
                        Log.d("NETCHECKTEST", LoadingScreen.OnEndLoadingCallbacks.toString())
                        CoroutineScope(Job()).launch(Dispatchers.Main) {
                            LoadingScreen.OnEndLoadingCallbacks!!.openPopup(null)
                        }

//                        if (onEndLoadingCallbacks != null) {
//
//
//                        }
                        myTimer.purge()
                        myTimer.cancel()
                    } else {
                        check++
                    }

                }
            }, 0, 12000)

            tagsForToast()
            Log.d(TAG, "Server Check function")

            CoroutineScope(Job()).launch(Dispatchers.IO) {
                try {
                    val Token = tinyDB.getString("Cookie")

                    checkServerResponse =
                        authRepository.checkServer(Token!!)

                    Log.d(TAG, "$checkServerResponse")
                    if (checkServerResponse == MassageResponse("ok")) {
                        myTimer.cancel()
                        myTimer.purge()
                        Log.d(TAG, "Server is working fine")

                        action()
                    }


                } catch (e: SocketTimeoutException) {
                    withContext(Dispatchers.Main)
                    {
                        Toast.makeText(MyApplication.appContext, TAG2, Toast.LENGTH_SHORT)
                            .show()
                    }
//                    serverCheck { action() }
                } catch (e: SocketException) {
                    withContext(Dispatchers.Main)
                    {
                        Toast.makeText(MyApplication.appContext, TAG2, Toast.LENGTH_SHORT)
                            .show()
                    }
//                    serverCheck { action() }
                } catch (e: NoInternetException) {
                    Log.d(TAG, " Exception..${e.localizedMessage}")
//                    serverCheck { action() }
                } catch (e: Exception) {

                    Log.d(TAG, " Exception..${e.localizedMessage}")
//                    serverCheck { action() }
                }


            }


        }

    }


}