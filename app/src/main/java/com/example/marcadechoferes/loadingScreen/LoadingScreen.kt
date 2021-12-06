package com.example.marcadechoferes.loadingScreen

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.marcadechoferes.R
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.marcadechoferes.Extra.NavigatorC
import com.example.marcadechoferes.Extra.TinyDB
import com.example.marcadechoferes.auth.forgotPassword.ForgotPasswordActivity
import com.example.marcadechoferes.auth.signin.SignInActivity
import com.example.marcadechoferes.mainscreen.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import kotlin.concurrent.schedule

@AndroidEntryPoint
class LoadingScreen : AppCompatActivity(){
    val loadingViewModel: loadingViewModel by viewModels()
    lateinit var tinyDB: TinyDB
    var extra = NavigatorC()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading_screen)
        tinyDB= TinyDB(this)
        initView()
    }

   fun initView(){





//       extra.liveData.observeForever(Observer {
//           println("i am observing")
//           if(it=="1"){
//               MoveToMain()
//           }
//       })
//
//
////     extra.liveData.observe(this, {
////
////
////})

    }

    fun MoveToMain(){
        var intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }




}