package com.example.marcadechoferes.auth.forgotPassword.viewModel

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.ViewModel
import com.example.marcadechoferes.auth.otp.OTP_Activity
import com.example.marcadechoferes.databinding.ActivityForgotPasswordBinding
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import com.example.marcadechoferes.Extra.TinyDB
import com.example.marcadechoferes.auth.forgotPassword.ForgotPasswordActivity
import com.example.marcadechoferes.auth.repository.AuthRepository
import com.example.marcadechoferes.loadingScreen.LoadingScreen
import com.example.marcadechoferes.network.ApiException
import com.example.marcadechoferes.network.NoInternetException
import com.example.marcadechoferes.network.ResponseException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(val authRepository: AuthRepository):ViewModel() {

    var activityContext:Context?= null
    lateinit var tinyDB: TinyDB
    fun viewsForForgotPasswordActivity(context: Context,binding: ActivityForgotPasswordBinding){
        activityContext= context
        tinyDB= TinyDB(context)
        binding.SubmitButton.setOnClickListener {

                val emailCheck: String = binding.email.text.toString()
                 val validater= emailCheck.isValidEmail()

                if(validater==true){
                    userforgotPassword(emailCheck)
                    var intent= Intent(activityContext, LoadingScreen::class.java)
                    startActivity(activityContext!!,intent, Bundle.EMPTY)


                }else{
                    Toast.makeText(activityContext, "Invalid Email", Toast.LENGTH_SHORT).show()

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
                        tinyDB.putString("User",name)
                        var intent= Intent(activityContext, OTP_Activity::class.java)
                        startActivity(activityContext!!,intent, Bundle.EMPTY)
                    }

                } catch (e: ResponseException) {
                    (activityContext as ForgotPasswordActivity).finish()
                    println("ErrorResponse")
                    var intent= Intent(activityContext, ForgotPasswordActivity::class.java)
                    startActivity(activityContext!!,intent, Bundle.EMPTY)
                    Toast.makeText(activityContext, "Failed", Toast.LENGTH_SHORT).show()
                }
                catch (e: ApiException) {
                    e.printStackTrace()
                }
                catch (e: NoInternetException) {
                    println("position 2")
                    e.printStackTrace()
                    withContext(Dispatchers.Main){
                        Toast.makeText(activityContext, "Check Your Internet Connection", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }


    }

    fun String.isValidEmail(): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(this).matches()
    }


}