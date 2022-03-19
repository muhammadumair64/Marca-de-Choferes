package com.logicasur.appchoferes.beforeAuth.forgotPasswordScreen

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.logicasur.appchoferes.Extra.BaseClass
import com.logicasur.appchoferes.utils.ResendApis
import com.logicasur.appchoferes.Extra.Language
import com.logicasur.appchoferes.R
import com.logicasur.appchoferes.beforeAuth.forgotPasswordScreen.viewModel.ForgotPasswordViewModel
import com.logicasur.appchoferes.databinding.ActivityForgotPasswordBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ForgotPasswordActivity :BaseClass() {


    lateinit var binding:ActivityForgotPasswordBinding
    val context:Context=this
    val forgotPasswordViewModel:ForgotPasswordViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val language=Language()
        language.setLanguage(baseContext)
        ResendApis.primaryColor="#7A59FC"
        ResendApis.secondaryColor="#653FFB"
        binding = DataBindingUtil.setContentView(this,R.layout.activity_forgot_password)
        initViews()
    }

    fun initViews(){
        forgotPasswordViewModel.viewsForForgotPasswordActivity(context,binding)
        setGrad(ResendApis.primaryColor, ResendApis.secondaryColor,binding.SubmitButton)
        showSoftKeyboard(binding.email)

    }

    fun showSoftKeyboard(view: View) {
        if (view.requestFocus()) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
        }
    }
}