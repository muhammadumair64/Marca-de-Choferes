package com.logicasur.appchoferes.auth.forgotPassword.viewModel

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.ViewModel
import com.logicasur.appchoferes.auth.otp.OTP_Activity
import com.logicasur.appchoferes.databinding.ActivityForgotPasswordBinding
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import com.logicasur.appchoferes.Extra.CheckConnection
import com.logicasur.appchoferes.Extra.ResendApis
import com.logicasur.appchoferes.Extra.TinyDB
import com.logicasur.appchoferes.Extra.serverCheck.ServerCheck
import com.logicasur.appchoferes.auth.forgotPassword.ForgotPasswordActivity
import com.logicasur.appchoferes.auth.repository.AuthRepository
import com.logicasur.appchoferes.loadingScreen.LoadingScreen
import com.logicasur.appchoferes.myApplication.MyApplication
import com.logicasur.appchoferes.network.ApiException
import com.logicasur.appchoferes.network.NoInternetException
import com.logicasur.appchoferes.network.ResponseException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.SocketException
import java.text.SimpleDateFormat
import java.util.*


@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(val authRepository: AuthRepository,val serverCheck: ServerCheck):ViewModel() {

    var activityContext:Context?= null
    lateinit var tinyDB: TinyDB
    fun viewsForForgotPasswordActivity(context: Context,binding: ActivityForgotPasswordBinding){
        activityContext= context
        tinyDB= TinyDB(context)
        binding.arrowBack.setBackgroundColor(Color.parseColor(ResendApis.primaryColor))
        binding.SubmitButton.setOnClickListener {

                val emailCheck: String = binding.email.text.toString()
                 val validater= emailCheck.isValidEmail()

            if(emailCheck.isEmpty()){
            Toast.makeText(activityContext, "Enter Email", Toast.LENGTH_SHORT).show()
                    }
            else {
                if (validater) {

                    if(CheckConnection.netCheck(context)){
                        viewModelScope.launch(Dispatchers.IO) {
                            MyApplication.authCheck = true
//                            serverCheck.serverCheck {
//                                userforgotPassword(emailCheck)
//                            }
                            userforgotPassword(emailCheck)
                        }
                        var intent = Intent(activityContext,LoadingScreen::class.java)
                        ContextCompat.startActivity(activityContext!!, intent, Bundle.EMPTY)
                    }
                    else{
                        Toast.makeText(activityContext,"Comprueba tu conexión a Internet" , Toast.LENGTH_SHORT).show()
                    }


                } else {
                    Toast.makeText(activityContext, "Invalid Email", Toast.LENGTH_SHORT).show()
                }
            }

        }
        binding.backButton.setOnClickListener {
            (context as Activity).finish()
        }
    }


    fun userforgotPassword(name:String){
        viewModelScope.launch {

            withContext(Dispatchers.IO) {

                try {

                    val response =
                        authRepository.retrofitInterface.forgotPassword(name!!)

                    println("SuccessResponse $response")


                    if(response!=null) {
                        putSomeDataForOTPCheck()
                        tinyDB.putString("UserOTP",name)
                        var intent= Intent(activityContext, OTP_Activity::class.java)
                        startActivity(activityContext!!,intent, Bundle.EMPTY)
                    }

                } catch (e: ResponseException) {
                    withContext(Dispatchers.Main){
                        MyApplication.authCheck = true
                        LoadingScreen.OnEndLoadingCallbacks!!.openServerPopup()
                        Toast.makeText(activityContext, "Comprueba tu conexión a Internet", Toast.LENGTH_SHORT).show()
                    }

                }
                catch (e: ApiException) {
                    e.printStackTrace()
                }
                catch (e: NoInternetException) {
                    println("position 2")
                    e.printStackTrace()
                    withContext(Dispatchers.Main){

                        MyApplication.authCheck = true
                        LoadingScreen.OnEndLoadingCallbacks!!.openServerPopup()

                        Toast.makeText(activityContext, "Comprueba tu conexión a Internet", Toast.LENGTH_SHORT).show()
                    }

                }
                catch(e: SocketException){
                    Log.d("connection Exception","Connect Not Available")
                    withContext(Dispatchers.Main){
                        MyApplication.authCheck = true
                        LoadingScreen.OnEndLoadingCallbacks!!.openServerPopup()
                        Toast.makeText(activityContext, "Comprueba tu conexión a Internet", Toast.LENGTH_SHORT).show()
                    }
                }            }
        }


    }

    fun String.isValidEmail(): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(this).matches()
    }
    fun putSomeDataForOTPCheck(){
        val sdf = SimpleDateFormat("yyyy-MM-dd:hh:mm:ss")
        val currentDate = sdf.format(Date())
        tinyDB.putString("OTPtime","$currentDate")
    }

}