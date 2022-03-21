package com.logicasur.appchoferes.beforeAuth.splashscreen

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.logicasur.appchoferes.Extra.*
import com.logicasur.appchoferes.R
import com.logicasur.appchoferes.beforeAuth.otpScreen.OtpActivity
import com.logicasur.appchoferes.beforeAuth.signInScreen.SignInActivity
import com.logicasur.appchoferes.databinding.ActivitySplashScreenBinding
import com.logicasur.appchoferes.common.loadingScreen.LoadingScreen
import com.logicasur.appchoferes.beforeAuth.splashscreen.viewModel.SplashScreenViewModel
import com.logicasur.appchoferes.utils.ResendApis
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
    var background = ""
    var spleshCheck = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("SYNC_TESTING", "In On create")
        lifecycleScope.launch {
            val language = Language()
            language.setLanguage(baseContext)
            viewModel.viewsOfActivity(this@SplashScreen)
            tinyDB = TinyDB(this@SplashScreen)
            startScreen()
        }

    }

    private fun initViews() {
        buttonTextSetter()
        showSplashScreen()
        val checker = tinyDB.getString("User")
        println("checker $checker")
        binding.startButton.setOnClickListener {
            if (checker?.length!! >= 3) {
                if (CheckConnection.netCheck(this)) {
                    moveToLoadingScreen()
                    viewModel.syncData() //change
                } else {

                    moveToLoadingScreen()
                    if (viewModel.myTimer != null) {
                        viewModel.myTimer!!.cancel()
                    }
                    viewModel.checkData()


                }

            } else {
                moveToSignIn()
                finish()
            }

        }

        setButtonColor()


    }


    //----------------------utils----------------------------------
    private fun moveToLoadingScreen() {
        val intent = Intent(this, LoadingScreen::class.java)
        startActivity(intent)
    }

    private fun showSplashScreen() {
        if (spleshCheck) {
            if (CheckConnection.netCheck(this)) {
                viewModel.getSplashScreen()
            } else {
                Toast.makeText(this, "Verifique su conexión", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setButtonColor() {
        val firstColor = tinyDB.getString("primaryColor")
        val secondColor = tinyDB.getString("secondrayColor")
        if (firstColor?.isNotEmpty() == true && secondColor?.isNotEmpty() == true) {
            setGrad(firstColor, secondColor, binding.startButton)
            setBarColor()
        } else {
            setGrad(ResendApis.primaryColor, ResendApis.secondaryColor, binding.startButton)
        }
    }

    fun base64ToBitmap(base64: String) {
        val imageBytes = Base64.decode(base64, 0)
        val image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        binding.background.setImageBitmap(image)
    }

    private fun otpTimeCheck() {
        val time = tinyDB.getString("OTPtime")
        if (time != null) {
            val sdf = SimpleDateFormat("yyyy-MM-dd:hh:mm:ss")
            val currentDate = sdf.format(Date())
            val date = currentDate.split(":").toTypedArray()[0]
            val otpDate = time.split(":").toTypedArray()[0]
            if (date == otpDate) {
                val hour = currentDate.split(":").toTypedArray()[1]
                val otpHour = time.split(":").toTypedArray()[1]
                if (hour == otpHour) {
                    val mints = currentDate.split(":").toTypedArray()[2]
                    val otpMints = time.split(":").toTypedArray()[2]
                    val check = mints.toInt()
                    val checkOtp = otpMints.toInt()
                    println("..... $check .......$checkOtp")
                    val temp = check - checkOtp
                    if (temp <= 2) {
                        val intent = Intent(this, OtpActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        intent.putExtra(ResendApis.splashToOtp, true)
                        startActivity(intent)
                        this.finish()
                    }

                }

            }
        }

    }

    private fun buttonTextSetter() {

        val checker = tinyDB.getString("User")
        println("checker $checker")
        if (checker?.length!! >= 3) {
            when (tinyDB.getString("language")) {
                "0" -> {

                    binding.startButton.text = "Rever"

                }
                "1" -> {


                    binding.startButton.text = "Retry"
                }
                else -> {

                    binding.startButton.text = "Repetir"

                }
            }

        }


    }

    private fun setBarColor() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        val color = tinyDB.getString("primaryColor")
        if (color != null) {
            if (color.isNotEmpty()) {
                window.statusBarColor = Color.parseColor(color)
            }
        }

    }

    private fun startScreen() {
        val splashCheck =tinyDB.getBoolean("NOSPLASH")
        val checker = tinyDB.getString("User")
        println("Current User is : $checker, $splashCheck")
        if (checker != null) {
            if(checker.isNotEmpty()) {
                spleshCheck = false
                moveToLoadingScreen()
                tinyDB.putBoolean("SYNC_CHECK", false)
                viewModel.checkData() //change

            } else if(splashCheck) {
                Log.d("SPLASHCHECK","-----in splash block")
                moveToSignIn()
            }else {
                otpTimeCheck()
            }
        }else if(splashCheck) {
            Log.d("SPLASHCHECK","-----in splash block")
            moveToSignIn()
        }else
        {
            otpTimeCheck()
        }

        val context = this
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

    private fun moveToSignIn() {
        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)

    }


}