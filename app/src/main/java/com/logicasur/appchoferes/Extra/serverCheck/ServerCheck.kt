package com.logicasur.appchoferes.Extra.serverCheck

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.logicasur.appchoferes.Extra.ResendApis
import com.logicasur.appchoferes.Extra.TinyDB
import com.logicasur.appchoferes.auth.repository.AuthRepository
import com.logicasur.appchoferes.loadingScreen.LoadingScreen
import com.logicasur.appchoferes.mainscreen.repository.MainRepository
import com.logicasur.appchoferes.myApplication.MyApplication
import com.logicasur.appchoferes.network.GeoPosition
import com.logicasur.appchoferes.network.NoInternetException
import com.logicasur.appchoferes.network.logoutResponse.MessageResponse
import com.logicasur.appchoferes.network.signinResponse.State
import com.logicasur.appchoferes.network.signinResponse.Vehicle
import com.logicasur.appchoferes.network.unsentApis.UnsentStatusOrUploadActivity
import kotlinx.coroutines.*
import java.net.SocketException
import java.net.SocketTimeoutException
import java.util.*


class ServerCheck constructor(
    val authRepository: AuthRepository,
    val mainRepository: MainRepository
) {

    companion object {
        var TAG2 = ""
        var tinyDB = TinyDB(MyApplication.appContext)
        val TAG = "c"
    }


    suspend fun serverCheck(action: () -> Unit) {

        var checkServerResponse: MessageResponse? = null

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
                if (checkServerResponse == MessageResponse("ok")) {
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
            } catch (e: SocketException) {
                withContext(Dispatchers.Main)
                {
                    Toast.makeText(MyApplication.appContext, TAG2, Toast.LENGTH_SHORT)
                        .show()
                    endLoading()
                }

            } catch (e: NoInternetException) {

                Log.d(TAG, " Exception..${e.localizedMessage}")
                endLoading()

            } catch (e: Exception) {

                Log.d(TAG, " Exception..${e.localizedMessage}")
                endLoading()
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
        vehicle: Vehicle?, state: State?, resendApis: ResendApis, action1: () -> Unit
    ) {
        tagsForToast()
        Log.d(TAG, "Server Check function 2nd")


        try {
            tinyDB.getString("Cookie")?.let { token ->

                authRepository.checkServer(token).apply {
                    Log.d(TAG, "$this")
                    if (this.checkIfMessageIsOkay()) {

                        Log.d(TAG, "Server is working fine")
                        action1()
                    }
                }


            }


        } catch (e: Exception) {

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


                runOnMain {
                    LoadingScreen.OnEndLoadingCallbacks?.endLoading()
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

                Log.d("STATE_TESTING", "IN END LOADING")

                runOnMain {
                    LoadingScreen.OnEndLoadingCallbacks?.endLoading()
                }

            }
            resendApis.checkNetAndUpload()

        }


    }


fun serverCheckMainActivityApi(
        toSaveInDB: Boolean = false, apiCall: (serverAction: () -> Unit) -> Unit
    ) {
        Log.d(TAG, "Server Check Testing function starts")

        var isFirst = true
        val serverCheckTimer = Timer().also { timer ->
            timer.schedule(object : TimerTask() {
                override fun run() {
                    if (isFirst) {
                        Log.d("NETCHECKTEST", "----working")
//                        Handler(Looper.getMainLooper()).post {
//                            //code that runs in main
//                        }


                            LoadingScreen.OnEndLoadingCallbacks?.also {
                                if (toSaveInDB) it.openPopup(null) else it.openServerPopup()
                            }



                        timer.purge()
                        timer.cancel()
                    } else {
                        isFirst = false
                    }

                }
            }, 0, 10000)
        }


        tagsForToast()
        Log.d(TAG, "Server Check function")

        exceptionSafeCall {
            mainActivityCall(serverCheckTimer, apiCall)
        }


    }


    private fun mainActivityCall(
        serverCheckTimer: Timer,
        apiCall: (serverAction: () -> Unit) -> Unit
    ) {

        CoroutineScope(Job()).launch(Dispatchers.IO) {
            tinyDB.getString("Cookie")?.let { token ->
                authRepository.checkServer(token).apply {
                    Log.d(TAG, "$this")
                    if (this.checkIfMessageIsOkay()) {
                        serverCheckTimer.cancel()
                        serverCheckTimer.purge()
                        Log.d(TAG, "ServerTesting is working fine")

                        var checkOnApiCall = 0
                        val inBetweenApiCallTimer = Timer().also { timer ->
                            timer.schedule(object : TimerTask() {
                                override fun run() {
                                    if (checkOnApiCall == 1 || checkOnApiCall == 2) {

                                        exceptionSafeCall {
                                            CoroutineScope(Job()).launch {
                                                serverCheckDuringStatus() {}
                                            }
                                        }

                                        checkOnApiCall++
                                    } else if (checkOnApiCall > 2) {
                                        Log.d("NETCHECKTEST", "----workingTesting")
                                        timer.purge()
                                        timer.cancel()

                                    } else {
                                        checkOnApiCall++
                                    }

                                }
                            }, 0, 20000)

                        }
                        apiCall() {
                            // Server is not well
                            Log.d(ServerCheck.TAG, "Response is received Cancel the timer.")
                            inBetweenApiCallTimer.cancel()
                            inBetweenApiCallTimer.purge()

                        }
                    }
                }

            }
        }

    }


    suspend fun serverCheckDuringStatus(
        statusApiCall: () -> Unit
    ) {


        var isFirst = true
        val myTimer = Timer()
        myTimer?.apply {
            this.schedule(object : TimerTask() {
                override fun run() {
                    if (isFirst) {
                        Log.d("NETCHECKTEST", "In popUp condition[p")
                        Log.d("NETCHECKTEST", "----working in required")
                        Log.d("NETCHECKTEST", LoadingScreen.OnEndLoadingCallbacks.toString())

                        LoadingScreen.OnEndLoadingCallbacks?.openPopup(null)
                        myTimer.purge()
                        myTimer.cancel()
                    } else {
                        isFirst = false
                    }

                }
            }, 0, 12000)
        }


        tagsForToast()
        Log.d(TAG, "Server Check function")


        tinyDB.getString("Cookie")?.let { token ->

            authRepository.checkServer(token).apply {
                Log.d(TAG, "$this")
                if (this.checkIfMessageIsOkay()) {
                    myTimer.cancel()
                    myTimer.purge()
                    Log.d(TAG, "Server is working fine")

                    statusApiCall()
                }
            }


        }


    }


    /// ///------------- Utils ---------------------

    private fun exceptionSafeCall(functionCall: () -> Unit) {
        try {
            functionCall()
        } catch (e: SocketTimeoutException) {

            Toast.makeText(MyApplication.appContext, TAG2, Toast.LENGTH_SHORT)
                .show()


        } catch (e: SocketException) {

            Toast.makeText(MyApplication.appContext, TAG2, Toast.LENGTH_SHORT)
                .show()


        } catch (e: NoInternetException) {
            Log.d(TAG, " Exception..${e.localizedMessage}")

        } catch (e: Exception) {

            Log.d(TAG, " Exception..${e.localizedMessage}")

        }
    }


    private fun tagsForToast() {
        var language = tinyDB.getString("language")
        TAG2 = when (language) {
            "0" -> {
                "El servidor está caído"
            }
            "1" -> {
                "Server is down"
            }
            else -> {
                "Servidor caiu"
            }
        }

    }

    private suspend fun runOnMain(function: () -> Unit) {
        withContext(Dispatchers.Main) {
            function()
        }
    }

}