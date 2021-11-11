package com.example.marcadechoferes.auth.otp

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.example.marcadechoferes.R
import com.example.marcadechoferes.auth.otp.viewModel.OTPViewModel
import com.example.marcadechoferes.databinding.ActivityOtpBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OTP_Activity : AppCompatActivity() {
    val otpViewModel:OTPViewModel by viewModels()
    val context:Context=this
    lateinit var binding:ActivityOtpBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_otp)
        initViews()
    }

    fun initViews(){
        otpViewModel.viewsForOTPScreen(context,binding)

    }
}