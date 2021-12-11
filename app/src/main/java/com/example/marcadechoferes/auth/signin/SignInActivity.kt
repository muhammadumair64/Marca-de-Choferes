package com.example.marcadechoferes.auth.signin

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.example.marcadechoferes.Extra.Language
import com.example.marcadechoferes.R
import com.example.marcadechoferes.auth.signin.viewModel.SigninViewModel
import com.example.marcadechoferes.databinding.ActivitySignInBinding
import com.example.marcadechoferes.mainscreen.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignInActivity : AppCompatActivity() {
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

        signinViewModel.viewsOfActivitySignin(context,binding)

    }
}