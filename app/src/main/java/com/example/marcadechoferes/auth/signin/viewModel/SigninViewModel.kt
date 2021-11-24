package com.example.marcadechoferes.auth.signin.viewModel

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.example.marcadechoferes.R
import com.example.marcadechoferes.auth.forgotPassword.ForgotPasswordActivity
import com.example.marcadechoferes.databinding.ActivitySignInBinding
import com.example.marcadechoferes.mainscreen.MainActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SigninViewModel  @Inject constructor() :ViewModel() {



    fun viewsOfActivitySignin(context: Context,binding: ActivitySignInBinding){
        binding.apply {
            showPassBtn.setOnClickListener {
                if (editPassword.transformationMethod.equals(PasswordTransformationMethod.getInstance())) {
                    editPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance())
                    showPassBtn.setImageResource(R.drawable.hide_password)
                } else {
                  editPassword.setTransformationMethod(PasswordTransformationMethod.getInstance())
                   showPassBtn.setImageResource(R.drawable.ic_icon_visibility)
                }


            }

          forgotPassword.setOnClickListener {
                var intent = Intent(context, ForgotPasswordActivity::class.java)
                ContextCompat.startActivity(context, intent, Bundle.EMPTY)

            }

            signInBtn.setOnClickListener {
                var intent = Intent(context, MainActivity::class.java)
                ContextCompat.startActivity(context, intent, Bundle.EMPTY)

            }


        }
    }


}