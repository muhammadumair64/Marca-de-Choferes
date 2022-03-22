package com.logicasur.appchoferes.common.serverCheck

import android.util.Log
import com.logicasur.appchoferes.Extra.TinyDB
import com.logicasur.appchoferes.data.repository.AuthRepository
import com.logicasur.appchoferes.common.loadingScreen.LoadingScreen
import com.logicasur.appchoferes.data.repository.MainRepository
import com.logicasur.appchoferes.utils.myApplication.MyApplication
import com.logicasur.appchoferes.data.network.NoInternetException
import com.logicasur.appchoferes.data.network.logoutResponse.MessageResponse
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
        Log.d("POPUP_ISSUE_TESTING", "--------2 ${MyApplication.syncCheck}")
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
                checkLoadingCallBack(true)
            } catch (e: SocketException) {
                Log.d("Exception", "Socket..${e.localizedMessage}")
                checkLoadingCallBack(true)
            } catch (e: NoInternetException) {

                Log.d("Exception", "NoInternet..${e.localizedMessage}")
                checkLoadingCallBack(false)
            } catch (e: Exception) {
                 checkLoadingCallBack(true)
                Log.d("Exception", "first place Exception..${e.localizedMessage}")

            }


        }


    }

    fun endLoading() {
//        if (!MyApplication.checKForSyncLoading) {
//            LoadingScreen.OnEndLoadingCallbacks?.endLoading()
//        }
    }


    private  fun handleException(netCheck: Boolean) {
        CoroutineScope(Job()).launch {
            withContext(Dispatchers.Main)
            {
                Log.d("POPUP_ISSUE_TESTING", "IN START OF FUNCTION  ${MyApplication.syncCheck}")
                if (MyApplication.syncCheck) {
                    Log.d("POPUP_ISSUE_TESTING", "IN SYNC BLOCK")
                    LoadingScreen.OnEndLoadingCallbacks?.openPopup(null, true, netCheck)
                    MyApplication.syncCheck = false
                }
                if (MyApplication.authCheck) {
                    MyApplication.authCheck = false
                    LoadingScreen.OnEndLoadingCallbacks?.openServerPopup(netCheck, "")
                } else {
                    endLoading()
                }


            }
        }
    }


    fun serverCheckMainActivityApi(
        toSaveInDB: Boolean = false, apiCall: (serverAction: () -> Unit) -> Unit
    ) {
        Log.d(TAG, "Server Check Testing function starts")


        tagsForToast()
        Log.d(TAG, "Server Check function 3rd")


        mainActivityCall(apiCall, toSaveInDB)


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
                                                        serverCheckDuringStatus(toSaveInDB) {}
                                                    } catch (e: Exception) {
                                                        Log.d(
                                                            "EXCEPTION_TESTING",
                                                            " ${e.localizedMessage}"
                                                        )
                                                    }

                                                }
                                                checkOnApiCall++
                                                if (checkOnApiCall > 3) {
                                                    Log.d("NETCHECKTEST", "----workingTesting")
                                                    timer.purge()
                                                    timer.cancel()
                                                    LoadingScreen.OnEndLoadingCallbacks?.openPopup(
                                                        null,
                                                        false,
                                                        true
                                                    )
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
                }catch (e:NoInternetException){
                    CoroutineScope(Job()).launch {
                        checkLoadingCallBackForMainAPis(false,  toSaveInDB)
                        Log.d("NETCHECKTEST", "----In no Internt Exception")
                    }


                } catch (e: SocketTimeoutException) {

                    checkLoadingCallBackForMainAPis(true,  toSaveInDB)



                } catch (e: Exception) {
                    checkLoadingCallBackForMainAPis(true, toSaveInDB)
                    Log.d("EXCEPTION_TESTING", " ${e.localizedMessage}")
                }


            }


        }

    }


    suspend fun serverCheckDuringStatus(
        toSaveInDB: Boolean,statusApiCall: () -> Unit
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
                checkLoadingCallBackForMainAPis(true, toSaveInDB)
                Log.d("Exception", "Socket..${e.localizedMessage}")

            } catch (e: SocketException) {
                checkLoadingCallBackForMainAPis(true, toSaveInDB)
                Log.d("Exception", "Socket..${e.localizedMessage}")


            } catch (e: NoInternetException) {
                checkLoadingCallBackForMainAPis(false, toSaveInDB)
                Log.d("Exception", "NoInternet..${e.localizedMessage}")

            } catch (e: Exception) {

                checkLoadingCallBackForMainAPis(true, toSaveInDB)
                Log.d("Exception", " last Place Exception..${e.localizedMessage}")

            }


        }


    }


    /// ///------------- Utils ---------------------


    private fun tagsForToast() {
        val language = tinyDB.getString("language")
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

private fun handleExceptionForMainApis(toSaveInDB: Boolean,forServer:Boolean){
    if (toSaveInDB) {
        LoadingScreen.OnEndLoadingCallbacks?.openPopup(null, false, forServer)
    }
    else {
        Log.d("NETCHECKTEST", "----In else")

            Log.d(TAG, "Open Server Popup....serverCheckMainActivityApi")
            LoadingScreen.OnEndLoadingCallbacks?.openServerPopup(forServer, "")
    }
}


    private fun checkLoadingCallBack(forServer: Boolean) {
        Log.d("CallBackTesting","In Call back function")
        if(LoadingScreen.OnEndLoadingCallbacks == null){
            val callBackTimer = Timer()
            callBackTimer.schedule(object : TimerTask() {
                override fun run() {
                    Log.d("CallBackTesting","In Timer")
                       if(LoadingScreen.OnEndLoadingCallbacks != null){
                                Log.d("CallBackTesting","IN for server check")
                               callBackTimer.cancel()
                               callBackTimer.purge()
                               handleException(forServer)


                       }
                }
            }, 0, 1000)
        }

    }


    private fun checkLoadingCallBackForMainAPis(forServer: Boolean, toSaveInDB: Boolean) {
        Log.d("CallBackTesting","In Call back function for main apis")
            val callBackTimer = Timer()
            callBackTimer.schedule(object : TimerTask() {
                override fun run() {
                        Log.d("CallBackTesting","In Timer")
                    val loadingCheck= (MyApplication.loadingContext as  LoadingScreen).window.decorView.rootView.isShown
                    Log.d("CallBackTesting","In loading check $loadingCheck")
                    if(LoadingScreen.OnEndLoadingCallbacks != null && loadingCheck ){
                            Log.d("CallBackTesting","Call back not null for main apis")
                            CoroutineScope(Job()).launch {

                                handleExceptionForMainApis(toSaveInDB,forServer)
                            }
                            callBackTimer.cancel()
                            callBackTimer.purge()
                        }


                }
            }, 0, 1000)
        }

    }


