package com.example.marcadechoferes.loadingScreen

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.example.marcadechoferes.R
import com.example.marcadechoferes.mainscreen.MainActivity

class LoadingScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading_screen)

        initView()
    }

   fun initView(){
       Handler().postDelayed({

           var intent= Intent(this, MainActivity::class.java)
           startActivity(intent)
           finish()

       }, 5000)

    }
}