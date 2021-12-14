package com.example.marcadechoferes.auth.forgotPassword

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.example.marcadechoferes.Extra.Language
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
        val language=Language()
        language.setLanguage(baseContext)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_forgot_password)
        initViews()
    }

    fun initViews(){
        forgotPasswordViewModel.viewsForForgotPasswordActivity(context,binding)
        showSoftKeyboard(binding.email)

    }

    fun showSoftKeyboard(view: View) {
        if (view.requestFocus()) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
        }
    }
}