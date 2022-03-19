package com.logicasur.appchoferes.beforeAuth.forgotPasswordScreen.viewModel

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
import com.logicasur.appchoferes.beforeAuth.otpScreen.OtpActivity
import com.logicasur.appchoferes.databinding.ActivityForgotPasswordBinding
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import com.logicasur.appchoferes.Extra.CheckConnection
import com.logicasur.appchoferes.Extra.TinyDB
import com.logicasur.appchoferes.common.serverCheck.ServerCheck
import com.logicasur.appchoferes.data.repository.AuthRepository
import com.logicasur.appchoferes.common.loadingScreen.LoadingScreen
import com.logicasur.appchoferes.utils.myApplication.MyApplication
import com.logicasur.appchoferes.data.network.ApiException
import com.logicasur.appchoferes.data.network.NoInternetException
import com.logicasur.appchoferes.data.network.ResponseException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.net.SocketException
import java.text.SimpleDateFormat
import java.util.*


@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    val authRepository: AuthRepository,
    val serverCheck: ServerCheck
) : ViewModel() {

    var activityContext: Context? = null
    lateinit var tinyDB: TinyDB
    fun viewsForForgotPasswordActivity(context: Context, binding: ActivityForgotPasswordBinding) {
        activityContext = context
        tinyDB = TinyDB(context)
        binding.arrowBack.setBackgroundColor(Color.parseColor("#7A59FC"))
        binding.SubmitButton.setOnClickListener {

            val email: String = binding.email.text.toString().trim()


            if (email.isEmpty()) {
                Toast.makeText(activityContext, "Enter Email", Toast.LENGTH_SHORT).show()
            } else {
                val emailCheck = validateEmailAndSendApi(email)
                if (emailCheck) {
                    viewModelScope.launch(Dispatchers.IO) {
                        MyApplication.authCheck = true
                        userforgotPassword(email)
                    }
                    showLoadingScreen()
                }
            }

        }

        binding.backButton.setOnClickListener {
            (context as Activity).finish()
        }
    }


    fun userforgotPassword(name: String) {
        viewModelScope.launch {

            withContext(Dispatchers.IO) {

                try {
                    Log.d("EmailTesting", "EmailWithOutSpaces--- ${name.trim()}")
                    val response =
                        authRepository.retrofitInterface.forgotPassword(name.trim())

                    println("SuccessResponse $response")


                    if (response != null) {
                        putSomeDataForOTPCheck()
                        tinyDB.putString("UserOTP", name)
                        val intent = Intent(activityContext, OtpActivity::class.java)
                        startActivity(activityContext!!, intent, Bundle.EMPTY)
                    }

                } catch (e: ResponseException) {
                    withContext(Dispatchers.Main) {
                        MyApplication.authCheck = true
                        LoadingScreen.OnEndLoadingCallbacks!!.openServerPopup()

                    }

                } catch (e: ApiException) {
                    withContext(Dispatchers.Main) {
                        MyApplication.authCheck = true
                        LoadingScreen.OnEndLoadingCallbacks!!.openServerPopup()
                    }
                    e.printStackTrace()
                } catch (e: NoInternetException) {
                    println("position 2")
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {

                        MyApplication.authCheck = true
                        LoadingScreen.OnEndLoadingCallbacks!!.openServerPopup()

                    }

                } catch (e: SocketException) {
                    Log.d("connection Exception", "Connect Not Available")
                    withContext(Dispatchers.Main) {
                        MyApplication.authCheck = true
                        LoadingScreen.OnEndLoadingCallbacks!!.openServerPopup()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        MyApplication.authCheck = true
                        LoadingScreen.OnEndLoadingCallbacks!!.openServerPopup()
                    }
                }


            }
        }


    }


    //---------------------------------------utils--------------------------


    private fun showLoadingScreen() {
        val intent = Intent(activityContext, LoadingScreen::class.java)
        activityContext?.let { ContextCompat.startActivity(it, intent, Bundle.EMPTY) }
    }

    fun String.isValidEmail(): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(this).matches()
    }

    fun putSomeDataForOTPCheck() {
        val sdf = SimpleDateFormat("yyyy-MM-dd:hh:mm:ss")
        val currentDate = sdf.format(Date())
        tinyDB.putString("OTPtime", currentDate)
    }

    private fun validateEmailAndSendApi(emailCheck: String): Boolean {
        val validater = emailCheck.isValidEmail()
        if (validater) {
            if (CheckConnection.netCheck(activityContext!!)) {
                return true
            } else {
                Toast.makeText(
                    activityContext,
                    "Comprueba tu conexión a Internet",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(activityContext, "Invalid Email", Toast.LENGTH_SHORT).show()
        }
        return false
    }

}