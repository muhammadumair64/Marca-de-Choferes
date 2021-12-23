package com.example.marcadechoferes.auth.signin

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.example.marcadechoferes.Extra.BaseClass
import com.example.marcadechoferes.Extra.K
import com.example.marcadechoferes.Extra.Language
import com.example.marcadechoferes.R
import com.example.marcadechoferes.auth.signin.viewModel.SigninViewModel
import com.example.marcadechoferes.databinding.ActivitySignInBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignInActivity : BaseClass() {
    lateinit var binding:ActivitySignInBinding
    var context:Context=this
    val signinViewModel:SigninViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val language= Language()
        language.setLanguage(baseContext)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_sign_in)
        initViews()

    }

    fun initViews(){
        setGrad(K.primaryColor, K.secondrayColor,binding.signInBtn)
        signinViewModel.viewsOfActivitySignin(context,binding)
    }




}