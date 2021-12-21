package com.example.marcadechoferes.splashscreen

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Base64
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.example.marcadechoferes.Extra.BaseClass
import com.example.marcadechoferes.Extra.K
import com.example.marcadechoferes.Extra.Language
import com.example.marcadechoferes.Extra.TinyDB
import com.example.marcadechoferes.R
import com.example.marcadechoferes.auth.signin.SignInActivity
import com.example.marcadechoferes.databinding.ActivitySplashScreenBinding
import com.example.marcadechoferes.loadingScreen.LoadingScreen
import com.example.marcadechoferes.splashscreen.viewModel.SplashScreenViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.concurrent.schedule


@AndroidEntryPoint
class SplashScreen : BaseClass() {
    val viewModel: SplashScreenViewModel by viewModels()
    lateinit var tinyDB: TinyDB
    lateinit var binding: ActivitySplashScreenBinding
    var background =""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        var temp = isMyServiceRunning(UploadRemaingDataService::class.java)
//        Log.d("check service ",temp.toString())
//        var intent=Intent(this,UploadRemaingDataService::class.java)
//        stopService(intent)
//        temp = isMyServiceRunning(UploadRemaingDataService::class.java)
//        Log.d("check service ",temp.toString())

        val language = Language()
        language.setLanguage(baseContext)
        viewModel.viewsOfActivity(this)
        tinyDB = TinyDB(this)
        var checker = tinyDB.getString("User")
        background= tinyDB.getString("SplashBG").toString()
        if(background.isNotEmpty() && !background.contains(".png")){
            base64ToBitmap(background)
        }
        println("Current User is : $checker")
        if (checker?.length!! >= 3) {
            var intent = Intent(this, LoadingScreen::class.java)
            viewModel.syncdata()
            startActivity(intent)
        }
        var context = this
        Timer().schedule(200) {
            lifecycleScope.launch {
                withContext(Dispatchers.Main) {
                    binding =
                        DataBindingUtil.setContentView(context, R.layout.activity_splash_screen)
                    initViews()
                }

            }

        }


    }

    fun initViews() {
        var checker = tinyDB.getString("User")
        println("checker $checker")
        binding.startButton.setOnClickListener {

            if (checker?.length!! >= 3) {
                viewModel.syncdata()
            } else {
                var intent = Intent(this, SignInActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        setGrad(K.primaryColor, K.secondrayColor,binding.startButton)


    }


    fun netCheck(): Boolean {
        var isConnected = false
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val activeNetwork =
                connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
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
    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    fun base64ToBitmap(base64: String) {
        val imageBytes = Base64.decode(base64, 0)
        val image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
         binding.background.setImageBitmap(image)
    }





}