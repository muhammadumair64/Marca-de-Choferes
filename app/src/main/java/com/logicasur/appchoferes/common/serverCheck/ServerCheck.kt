package com.logicasur.appchoferes.common.serverCheck

import android.util.Log
import com.logicasur.appchoferes.Extra.CheckConnection
import com.logicasur.appchoferes.utils.ResendApis
import com.logicasur.appchoferes.Extra.TinyDB
import com.logicasur.appchoferes.data.repository.AuthRepository
import com.logicasur.appchoferes.common.loadingScreen.LoadingScreen
import com.logicasur.appchoferes.afterAuth.mainscreen.MainActivity
import com.logicasur.appchoferes.data.repository.MainRepository
import com.logicasur.appchoferes.utils.myApplication.MyApplication
import com.logicasur.appchoferes.data.network.GeoPosition
import com.logicasur.appchoferes.data.network.NoInternetException
import com.logicasur.appchoferes.data.network.logoutResponse.MessageResponse
import com.logicasur.appchoferes.data.network.signinResponse.State
import com.logicasur.appchoferes.data.network.signinResponse.Vehicle
import com.logicasur.appchoferes.data.network.unsentApis.UnsentStatusOrUploadActivity
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
        val TAG = "ServerCheck"
    }


    suspend fun serverCheck(action: () -> Unit) {

        var checkServerResponse: MessageResponse

        tagsForToast()
        Log.d(TAG, "Server Check function 1st")

        CoroutineScope(Job()).launch(Dispatchers.IO) {
            try {
                val token = tinyDB.getString("Cookie")

                Log.d(TAG, "Server CHECK API Hit")
                checkServerResponse =
                    authRepository.checkServer(token ?: "")

                Log.d(TAG, "$checkServerResponse")
                if (checkServerResponse.checkIfMessageIsOkay()) {
                    Log.d(TAG, "Server is working fine")
                    action()
                }


            } catch (e: SocketTimeoutException) {
                Log.d("Exception", "SocketTimeOut..${e.localizedMessage}")
                handleException()
            } catch (e: SocketException) {
                Log.d("Exception", "Socket..${e.localizedMessage}")
                handleException()
            } catch (e: NoInternetException) {

                Log.d("Exception", "NoInternet..${e.localizedMessage}")
                handleException()
            } catch (e: Exception) {

                Log.d("Exception", "first place Exception..${e.localizedMessage}")
                handleException()
            }


        }


    }

    fun endLoading() {
        if (!MyApplication.checKForSyncLoading) {
            LoadingScreen.OnEndLoadingCallbacks?.endLoading()
        }
    }


   suspend fun handleException(){
       withContext(Dispatchers.Main)
       {
           if(MyApplication.authCheck){
               MyApplication.authCheck = false
               LoadingScreen.OnEndLoadingCallbacks?.openServerPopup()
           }else{
               endLoading()
           }
           if(MyApplication.syncCheck){
               LoadingScreen.OnEndLoadingCallbacks?.openPopup(null,true)
               MyApplication.syncCheck = false
           }

       }
   }



    fun serverCheckMainActivityApi(
        toSaveInDB: Boolean = false, apiCall: (serverAction: () -> Unit) -> Unit
    ) {
        Log.d(TAG, "Server Check Testing function starts")


        tagsForToast()
        Log.d(TAG, "Server Check function 3rd")


        mainActivityCall( apiCall,toSaveInDB)


    }


    private fun mainActivityCall(
        apiCall: (serverAction: () -> Unit) -> Unit,
        toSaveInDB: Boolean
    ) {
        tinyDB.getString("Cookie")?.let { token ->

            CoroutineScope(Job()).launch(Dispatchers.IO) {
                try {
                    authRepository.checkServer(token).apply {

                        Log.d(TAG, "$this")
                        if (this.checkIfMessageIsOkay()) {

                            Log.d(TAG, "ServerTesting is working fine")

                            var checkOnApiCall = 0
                            val inBetweenApiCallTimer = Timer().also { timer ->
                                timer.schedule(object : TimerTask() {
                                    override fun run() {
                                        when (checkOnApiCall) {
                                            in 1..3 -> {
                                                CoroutineScope(Job()).launch {
                                                    try {
                                                        serverCheckDuringStatus() {}
                                                    } catch (e: Exception) {
                                                        Log.d(
                                                            "EXCEPTION_TESTING",
                                                            " ${e.localizedMessage}"
                                                        )
                                                    }

                                                }
                                                checkOnApiCall++
                                                if(checkOnApiCall > 3){
                                                    Log.d("NETCHECKTEST", "----workingTesting")
                                                    timer.purge()
                                                    timer.cancel()
                                                    LoadingScreen.OnEndLoadingCallbacks?.openPopup(null, false)
                                                }
                                            }
                                            else -> {
                                                checkOnApiCall++
                                            }
                                        }

                                    }
                                }, 0, 15000)

                            }

                            apiCall() {
                                // Server is not well
                                Log.d(TAG, "Response is received Cancel the timer.")
                                inBetweenApiCallTimer.cancel()
                                inBetweenApiCallTimer.purge()

                            }
                        }
                    }
                }
                catch (e:SocketTimeoutException) {
                    LoadingScreen.OnEndLoadingCallbacks?.apply {
                        Log.d("NETCHECKTEST", "----In Also")
                        if (toSaveInDB) openPopup(null, true) else {
                            Log.d("NETCHECKTEST", "----In else")
                            withContext(Dispatchers.Main) {
                                Log.d(TAG,"Open Server Popup....serverCheckMainActivityApi")
                                openServerPopup()
                            }

                        }
                    }
                }
                catch (e: Exception) {
                  if(toSaveInDB){
                          if(tinyDB.getBoolean("STATEAPI")){
                              (MyApplication.activityContext as MainActivity).updatePendingData(true)

                          }else{
                              (MyApplication.activityContext as MainActivity).updatePendingData(false)
                          }

                  }
                    Log.d("EXCEPTION_TESTING", " ${e.localizedMessage}")
                }


            }


        }

    }


    suspend fun serverCheckDuringStatus(
        statusApiCall: () -> Unit
    ) {

        tagsForToast()
        Log.d(TAG, "Server Check function 4th")


        tinyDB.getString("Cookie")?.let { token ->
            try {
                authRepository.checkServer(token).apply {
                    Log.d(TAG, "$this")
                    if (this.checkIfMessageIsOkay()) {
                        Log.d(TAG, "Server is working fine")

                        statusApiCall()
                    }
                }

            } catch (e: SocketTimeoutException) {
                LoadingScreen.OnEndLoadingCallbacks?.openPopup(null, false)
                Log.d("Exception", "SocketTimeOut..${e.localizedMessage}")

            } catch (e: SocketException) {
                LoadingScreen.OnEndLoadingCallbacks?.openPopup(null, false)
                Log.d("Exception", "Socket..${e.localizedMessage}")


            } catch (e: NoInternetException) {

                Log.d("Exception", "NoInternet..${e.localizedMessage}")


            } catch (e: Exception) {

                Log.d("Exception", " last Place Exception..${e.localizedMessage}")

            }


        }


    }


    /// ///------------- Utils ---------------------


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


//    private fun mainActivityCall(
//        serverCheckTimer: Timer,
//        apiCall: (serverAction: () -> Unit) -> Unit,
//        toSaveInDB: Boolean
//    ) {
//        tinyDB.getString("Cookie")?.let { token ->
//
//            CoroutineScope(Job()).launch(Dispatchers.IO) {
//                try {
//                    authRepository.checkServer(token).apply {
//
//                        Log.d(TAG, "$this")
//                        if (this.checkIfMessageIsOkay()) {
//                            serverCheckTimer.cancel()
//                            serverCheckTimer.purge()
//                            Log.d(TAG, "ServerTesting is working fine")
//
//                            var checkOnApiCall = 0
//                            val inBetweenApiCallTimer = Timer().also { timer ->
//                                timer.schedule(object : TimerTask() {
//                                    override fun run() {
//                                        if (checkOnApiCall == 1 || checkOnApiCall == 2) {
//
//
//                                            CoroutineScope(Job()).launch {
//                                                try {
//                                                    serverCheckDuringStatus() {}
//                                                } catch (e: Exception) {
//                                                    Log.d(
//                                                        "EXCEPTION_TESTING",
//                                                        " ${e.localizedMessage}"
//                                                    )
//                                                }
//
//                                            }
//
//
//                                            checkOnApiCall++
//                                        } else if (checkOnApiCall > 2) {
//                                            Log.d("NETCHECKTEST", "----workingTesting")
//                                            timer.purge()
//                                            timer.cancel()
//
//                                        } else {
//                                            checkOnApiCall++
//                                        }
//
//                                    }
//                                }, 0, 13000)
//
//                            }
//                            apiCall() {
//                                // Server is not well
//                                Log.d(TAG, "Response is received Cancel the timer.")
//                                inBetweenApiCallTimer.cancel()
//                                inBetweenApiCallTimer.purge()
//
//                            }
//                        }
//                    }
//                } catch (e: Exception) {
//                    serverCheckTimer.cancel()
//                    if(toSaveInDB){
//                        if(tinyDB.getBoolean("STATEAPI")){
//                            (MyApplication.activityContext as MainActivity).updatePendingData(true)
//
//                        }else{
//                            (MyApplication.activityContext as MainActivity).updatePendingData(false)
//
//                        }
//
//                    }
//                    Log.d("EXCEPTION_TESTING", " ${e.localizedMessage}")
//                }
//
//
//            }
//
//
//        }
//
//    }


}