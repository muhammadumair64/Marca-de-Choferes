package com.example.marcadechoferes.auth.otp

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.example.marcadechoferes.Extra.Language
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
        val language=Language()
        language.setLanguage(baseContext)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_otp)
        initViews()
    }

    fun initViews(){
        otpViewModel.viewsForOTPScreen(context,binding)
        showSoftKeyboard(binding.edt1)
    }

    fun showSoftKeyboard(view: View) {
        if (view.requestFocus()) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
    }
}