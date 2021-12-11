package com.example.marcadechoferes.splashscreen

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.marcadechoferes.Extra.Language
import com.example.marcadechoferes.Extra.TinyDB
import com.example.marcadechoferes.R
import com.example.marcadechoferes.auth.signin.SignInActivity
import com.example.marcadechoferes.databinding.ActivitySplashScreenBinding
import com.example.marcadechoferes.splashscreen.viewModel.SplashScreenViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class SplashScreen : AppCompatActivity() {
    val viewModel:SplashScreenViewModel by viewModels()
    lateinit var tinyDB: TinyDB
    lateinit var binding: ActivitySplashScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val language= Language()
        language.setLanguage(baseContext)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash_screen)
        initViews()
    }

    fun initViews() {
        viewModel.viewsOfActivity(this)
        tinyDB= TinyDB(this)
        var checker= tinyDB.getString("User")
        println("checker $checker")
        binding.startButton.setOnClickListener {
            if(netCheck()==true){
                if(checker?.length!! >= 3){
                   viewModel.syncdata()
                }else{
                    var intent = Intent(this, SignInActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
            else
            {
                Toast.makeText(this, "Check Your Internet Connection", Toast.LENGTH_SHORT).show()
            }





        }


    }


    fun netCheck(): Boolean {
        var isConnected = false
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
            isConnected = when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.run {
                activeNetworkInfo?.run {
                    isConnected = when (type) {
                        ConnectivityManager.TYPE_WIFI -> true
                        ConnectivityManager.TYPE_MOBILE -> true
                        ConnectivityManager.TYPE_ETHERNET -> true
                        else -> false
                    }
                }
            }
        }


     return isConnected
    }




}