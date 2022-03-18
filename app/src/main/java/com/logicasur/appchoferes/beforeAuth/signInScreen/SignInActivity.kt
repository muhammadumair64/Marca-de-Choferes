package com.logicasur.appchoferes.beforeAuth.signInScreen

import android.content.Context
import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.logicasur.appchoferes.Extra.BaseClass
import com.logicasur.appchoferes.Extra.Language
import com.logicasur.appchoferes.R
import com.logicasur.appchoferes.beforeAuth.signInScreen.viewModel.SigninViewModel
import com.logicasur.appchoferes.databinding.ActivitySignInBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignInActivity : BaseClass() {
    lateinit var binding:ActivitySignInBinding
    var context:Context=this
    private val signInViewModel:SigninViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val language= Language()
        language.setLanguage(baseContext)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_sign_in)
        initViews()

    }

    private fun initViews(){

        //setGrad(ResendApis.primaryColor, ResendApis.secondaryColor,binding.signInBtn)
        signInViewModel.viewsOfActivitySign(context,binding)
    }




}