package com.example.marcadechoferes.auth.forgotPassword.viewModel

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.ViewModel
import com.example.marcadechoferes.auth.otp.OTP_Activity
import com.example.marcadechoferes.databinding.ActivityForgotPasswordBinding
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.core.content.ContextCompat.getSystemService



@HiltViewModel
class ForgotPasswordViewModel @Inject constructor():ViewModel() {

    var activityContext:Context?= null

    fun viewsForForgotPasswordActivity(context: Context,binding: ActivityForgotPasswordBinding){
        activityContext= context
        binding.SubmitButton.setOnClickListener {
            var intent= Intent(context, OTP_Activity::class.java)
            startActivity(context,intent, Bundle.EMPTY)

        }
        binding.backButton.setOnClickListener {
            (context as Activity).finish()
        }


    }



}