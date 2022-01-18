package com.logicasur.appchoferes.splashscreen

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.logicasur.appchoferes.Extra.BaseClass
import com.logicasur.appchoferes.Extra.K
import com.logicasur.appchoferes.Extra.Language
import com.logicasur.appchoferes.Extra.TinyDB
import com.logicasur.appchoferes.R
import com.logicasur.appchoferes.auth.otp.OTP_Activity
import com.logicasur.appchoferes.auth.signin.SignInActivity
import com.logicasur.appchoferes.databinding.ActivitySplashScreenBinding
import com.logicasur.appchoferes.loadingScreen.LoadingScreen
import com.logicasur.appchoferes.splashscreen.viewModel.SplashScreenViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.schedule


@AndroidEntryPoint
class SplashScreen : BaseClass() {
    val viewModel: SplashScreenViewModel by viewModels()
    lateinit var tinyDB: TinyDB
    lateinit var binding: ActivitySplashScreenBinding
    var background =""
    var spleshCheck = true
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

//        background= tinyDB.getString("SplashBG").toString()
//        if(background.isNotEmpty() && !background.contains(".png")){
//            base64ToBitmap(background)
//        }

        println("Current User is : $checker")
        if(checker != null){
            if (checker.isNotEmpty()) {
                spleshCheck=false
                var intent = Intent(this, LoadingScreen::class.java)
                startActivity(intent)
                 viewModel.syncdata()

            }else{
                otpTimeCheck()
            }
        }
        else{
            otpTimeCheck()
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
         buttonTextSetter()
        if(spleshCheck){
            if (netCheck()){
                viewModel.getSplashScreen()
            }
        else{
                Toast.makeText(this, "Verifique su conexión", Toast.LENGTH_SHORT).show()
            }
        }
        var checker = tinyDB.getString("User")
        println("checker $checker")
        binding.startButton.setOnClickListener {

            if (checker?.length!! >= 3) {
                if(netCheck()){
                    var intent = Intent(this, LoadingScreen::class.java)
                    startActivity(intent)
                    viewModel.syncdata()
                }
                else{
                    Toast.makeText(this, "Verifique su conexión", Toast.LENGTH_SHORT).show()
                }

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

    fun otpTimeCheck()
    {
        var time =tinyDB.getString("OTPtime")
        if(time != null){
            val sdf = SimpleDateFormat("yyyy-MM-dd:hh:mm:ss")
            val currentDate = sdf.format(Date())
            var date = currentDate.split(":").toTypedArray()[0]
            var otpDate = time!!.split(":").toTypedArray()[0]
            if(date.equals(otpDate)){
                var hour = currentDate.split(":").toTypedArray()[1]
                var otphour = time.split(":").toTypedArray()[1]
                if(hour.equals(otphour)){
                    var mints = currentDate.split(":").toTypedArray()[2]
                    var otpMints= time.split(":").toTypedArray()[2]
                    var check= mints.toInt()
                    var checkOtp = otpMints.toInt()
                    println("..... $check .......$checkOtp")
                    var temp = check-checkOtp
                    if(temp<=2){
                        var intent = Intent(this, OTP_Activity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        intent.putExtra(K.splashToOtp,true)
                        startActivity(intent)
                        this.finish()
                    }

                }

            }
        }

    }



    fun buttonTextSetter(){

        var checker = tinyDB.getString("User")
        println("checker $checker")
        if (checker?.length!! >= 3) {
            var language= tinyDB.getString("language")
                  if (language=="0"){

                      binding.startButton.text="Rever"

            }else if(language=="1"){


                      binding.startButton.text="Retry"
            }
            else{

                      binding.startButton.text="Repetir"

            }

            }









    }



}