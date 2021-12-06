package com.example.marcadechoferes.auth.createpassword.viewModel

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.marcadechoferes.loadingScreen.LoadingScreen
import com.example.marcadechoferes.R
import com.example.marcadechoferes.auth.createpassword.CreateNewPasswordScreen
import com.example.marcadechoferes.auth.forgotPassword.ForgotPasswordActivity
import com.example.marcadechoferes.auth.otp.OTP_Activity
import com.example.marcadechoferes.auth.repository.AuthRepository
import com.example.marcadechoferes.databinding.ActivityCreateNewPasswordScreenBinding
import com.example.marcadechoferes.mainscreen.MainActivity
import com.example.marcadechoferes.network.ResponseException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
@HiltViewModel
class CreatePasswordViewModel @Inject constructor(val authRepository: AuthRepository):ViewModel() {
    var activityContext:Context? = null
    fun viewsForCreatePassword(context: Context,binding: ActivityCreateNewPasswordScreenBinding){
        activityContext=context

        binding.backButton.setOnClickListener {

            (context as Activity).finish()
        }
        binding.showPassBtn.setOnClickListener {
            if (binding.editPassword.transformationMethod.equals(PasswordTransformationMethod.getInstance())) {
                binding.editPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance())
                binding.showPassBtn.setImageResource(R.drawable.hide_password)
            } else {
                binding.editPassword.setTransformationMethod(PasswordTransformationMethod.getInstance())
                binding.showPassBtn.setImageResource(R.drawable.ic_icon_visibility)
            }
        }

        binding.showRepeatPassBtn.setOnClickListener {

            if (binding.repeatPassword.transformationMethod.equals(PasswordTransformationMethod.getInstance())) {
                binding.repeatPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance())
                binding.showRepeatPassBtn.setImageResource(R.drawable.hide_password)
            } else {
                binding.repeatPassword.setTransformationMethod(PasswordTransformationMethod.getInstance())
                binding.showRepeatPassBtn.setImageResource(R.drawable.ic_icon_visibility)
            }

        }

    binding.SubmitBtn.setOnClickListener {
        binding.apply {
            var password = editPassword.text
            var repeatPassword = repeatPassword.text

                if (editPassword.text.length>=4){
                    var passsword = editPassword.text
                    CreateNewPassword(passsword.toString())
                    var intent= Intent(context, LoadingScreen::class.java)
                    ContextCompat.startActivity(context, intent, Bundle.EMPTY)
                    (context as CreateNewPasswordScreen).closeKeyboard()


                }else{
                    Toast.makeText(activityContext, "Too Short Password", Toast.LENGTH_SHORT).show()
                }



        }




    }


    }


    fun CreateNewPassword(password:String){
        viewModelScope.launch {

            withContext(Dispatchers.IO) {

                try {

                    val response =authRepository.CreateNewPasswordPassword(password)

                    println("SuccessResponse $response")


                    if(response!=null) {
                        var intent= Intent(activityContext, MainActivity::class.java)
                        ContextCompat.startActivity(activityContext!!,intent, Bundle.EMPTY)
                    }

                } catch (e: ResponseException) {
                    (activityContext as CreateNewPasswordScreen).finish()
                    println("ErrorResponse")
                    var intent= Intent(activityContext,CreateNewPasswordScreen::class.java)
                    ContextCompat.startActivity(activityContext!!,intent, Bundle.EMPTY)

                }
            }
        }


    }

}