package com.example.marcadechoferes.auth.forgotPassword

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.example.marcadechoferes.R
import com.example.marcadechoferes.auth.forgotPassword.viewModel.ForgotPasswordViewModel
import com.example.marcadechoferes.databinding.ActivityForgotPasswordBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ForgotPasswordActivity : AppCompatActivity() {
    lateinit var binding:ActivityForgotPasswordBinding
    val context:Context=this
    val forgotPasswordViewModel:ForgotPasswordViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_forgot_password)
        initViews()
    }

    fun initViews(){
        forgotPasswordViewModel.viewsForForgotPasswordActivity(context,binding)

    }
}