package com.example.marcadechoferes.splashscreen

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.example.marcadechoferes.R
import com.example.marcadechoferes.auth.signin.SignInActivity
import com.example.marcadechoferes.databinding.ActivitySplashScreenBinding

class SplashScreen : AppCompatActivity() {
   lateinit var binding : ActivitySplashScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash_screen)
        initViews()
    }
    fun initViews(){
        binding.startButton.setOnClickListener {
            var intent= Intent(this,SignInActivity::class.java)
            startActivity(intent)
            finish()

        }




    }
}