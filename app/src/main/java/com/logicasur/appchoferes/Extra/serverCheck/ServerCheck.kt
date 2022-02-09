package com.logicasur.appchoferes.Extra.serverCheck

import android.util.Log
import com.logicasur.appchoferes.Extra.TinyDB
import com.logicasur.appchoferes.auth.repository.AuthRepository
import com.logicasur.appchoferes.myApplication.MyApplication
import com.logicasur.appchoferes.network.logoutResponse.MassageResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class ServerCheck {

    companion object {
        var tinyDB =TinyDB(MyApplication.appContext)
        val TAG="CheckServer"
        lateinit var authRepository: AuthRepository


       suspend fun serverCheck(action:() -> Unit){
           Log.d(TAG,"Server Check function")

            CoroutineScope(Job()).launch(Dispatchers.IO) {
                try {
                    val Token = tinyDB.getString("Cookie")

                 val checkServerResponse =
                        authRepository.checkServer(Token!!)

                    Log.d(TAG,"$checkServerResponse")
                    if(checkServerResponse==MassageResponse("ok"))
                    {
                        Log.d(TAG,"Server is working fine")
                       action()
                    }
                    else{
                        Log.d(TAG,"Server is Down.")
                    }

                } catch (e: Exception) {
                Log.d(TAG," Exception..${e.localizedMessage}")
                }


            }


        }

    }
}