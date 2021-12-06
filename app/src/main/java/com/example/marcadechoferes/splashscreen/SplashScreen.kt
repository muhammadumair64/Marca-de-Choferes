package com.example.marcadechoferes.splashscreen

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import android.util.Log
import androidx.databinding.DataBindingUtil
import com.example.marcadechoferes.BuildConfig
import com.example.marcadechoferes.BuildConfig.VERSION_CODE
import com.example.marcadechoferes.Extra.TinyDB
import com.example.marcadechoferes.R
import com.example.marcadechoferes.auth.signin.SignInActivity
import com.example.marcadechoferes.databinding.ActivitySplashScreenBinding
import com.example.marcadechoferes.mainscreen.MainActivity
import java.io.File
import kotlin.math.log


class SplashScreen : AppCompatActivity() {
    lateinit var tinyDB: TinyDB
    lateinit var binding: ActivitySplashScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash_screen)
        initViews()
    }

    fun initViews() {
        tinyDB= TinyDB(this)
        var checker= tinyDB.getString("User")
        println("checker $checker")
        binding.startButton.setOnClickListener {
            if(checker?.length!! >= 3){
                var intent = Intent(this,MainActivity ::class.java)
                startActivity(intent)
                finish()
            }else{
                var intent = Intent(this, SignInActivity::class.java)
                startActivity(intent)
                finish()
            }



        }


    }




}