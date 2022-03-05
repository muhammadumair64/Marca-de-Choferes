package com.logicasur.appchoferes.auth.otp

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.logicasur.appchoferes.Extra.BaseClass
import com.logicasur.appchoferes.Extra.ResendApis
import com.logicasur.appchoferes.Extra.Language
import com.logicasur.appchoferes.R
import com.logicasur.appchoferes.auth.otp.viewModel.OTPViewModel
import com.logicasur.appchoferes.databinding.ActivityOtpBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OTP_Activity : BaseClass(){
    val otpViewModel:OTPViewModel by viewModels()
    val context:Context=this
    lateinit var binding:ActivityOtpBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val language=Language()
        language.setLanguage(baseContext)
        ResendApis.primaryColor="#7A59FC"
        ResendApis.secondaryColor="#653FFB"
        binding = DataBindingUtil.setContentView(this,R.layout.activity_otp)
        initViews()
    }



    fun initViews(){
        otpViewModel.fromSplash = intent.getBooleanExtra(ResendApis.splashToOtp,false)
        setGrad("#7A59FC","#653FFB",binding.SubmitButton)
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