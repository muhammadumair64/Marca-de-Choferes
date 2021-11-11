package com.example.marcadechoferes.auth.forgotPassword.viewModel

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.ViewModel
import com.example.marcadechoferes.auth.otp.OTP_Activity
import com.example.marcadechoferes.databinding.ActivityForgotPasswordBinding
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
@HiltViewModel
class ForgotPasswordViewModel @Inject constructor():ViewModel() {

    fun viewsForForgotPasswordActivity(context: Context,binding: ActivityForgotPasswordBinding){
        binding.SubmitButton.setOnClickListener {
            var intent= Intent(context, OTP_Activity::class.java)
            startActivity(context,intent, Bundle.EMPTY)

        }
        binding.backButton.setOnClickListener {
            (context as Activity).finish()
        }


    }
}