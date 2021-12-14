package com.example.marcadechoferes.mainscreen

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.marcadechoferes.R
import com.example.marcadechoferes.databinding.ActivityMainBinding
import com.example.marcadechoferes.databinding.FragmentHomeBinding
import com.example.marcadechoferes.mainscreen.home.timerServices.BreakTimerService
import com.example.marcadechoferes.mainscreen.home.timerServices.TimerService
import com.example.marcadechoferes.mainscreen.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.roundToInt
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions
import kotlinx.coroutines.launch

import android.location.LocationManager

import android.content.IntentSender
import com.example.marcadechoferes.Extra.Language
import com.example.marcadechoferes.mainscreen.home.viewmodel.HomeViewModel
import com.example.marcadechoferes.myApplication.MyApplication

import com.google.android.gms.location.*
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationServices

import com.google.android.gms.common.api.*
import java.util.*


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    companion object {
        const val LOCATION_SETTING_REQUEST = 999
    }
    val mainViewModel: MainViewModel by viewModels()
    var context: Context = this
    var timerStarted = false
    lateinit var serviceIntent: Intent
    var time = 0.0
    var dpHeight: Float? = null
    var dpWidth: Float? = null
    lateinit var binding: ActivityMainBinding
    lateinit var serviceIntentB: Intent
    private var timeBreak = 0.0
    var dataBinding: FragmentHomeBinding? = null
    val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val language= Language()
        language.setLanguage(baseContext)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        serviceIntent = Intent(applicationContext, TimerService::class.java)
        registerReceiver(updateTime, IntentFilter(TimerService.TIMER_UPDATED))

        serviceIntentB = Intent(applicationContext, BreakTimerService::class.java)
        registerReceiver(updateTimeBreak, IntentFilter(BreakTimerService.TIMER_UPDATED_B))
        binding.menu.setItemSelected(R.id.home, true)
        getWidth()
        initPermission()
        NavBar()


    }


    fun NavBar() {
        binding.menu.setOnItemSelectedListener {

            when (it) {
                R.id.home -> {
                    mainViewModel.updateValueTo1()

                }
                R.id.User -> {
                    println("clicked")
                    mainViewModel.updateValueTo2()

                }
                R.id.Settings -> {
                    mainViewModel.updateValueTO3()

                }

            }


        }
    }

    fun startTimer() {
        println("work timer start")
        serviceIntent.putExtra(TimerService.TIME_EXTRA, time)
        startService(serviceIntent)
        timerStarted = true
    }

    fun stopTimer() {
        println("work Timer stop")
        stopService(serviceIntent)
        timerStarted = false
    }

    private val updateTime: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            time = intent.getDoubleExtra(TimerService.TIME_EXTRA, 0.0)
            dataBinding?.workTimer?.text = getTimeStringFromDouble(time)
        }
    }

    private fun getTimeStringFromDouble(time: Double): String {

        val resultInt = time.roundToInt()
        lifecycleScope.launch {
            viewModel.workTimerupdater(time.roundToInt(), dataBinding)
        }

        println(" $resultInt")
        val hours = resultInt % 86400 / 3600
        val minutes = resultInt % 86400 % 3600 / 60
        val seconds = resultInt % 86400 % 3600 % 60

        return makeTimeString(hours, minutes, seconds)
    }

    private fun makeTimeString(hour: Int, min: Int, sec: Int): String =
        String.format("%02d:%02d", hour, min)

    fun viewsOfFragment(binding: FragmentHomeBinding) {
        dataBinding = binding
    }


    fun startTimerBreak() {
        println("breakTimer Start")
        serviceIntent.putExtra(BreakTimerService.TIME_EXTRA_B, timeBreak)
        startService(serviceIntentB)
        timerStarted = true
    }

    fun stopTimerBreak() {
        println("break Timer stop")
        stopService(serviceIntentB)
        timerStarted = false
    }

    private val updateTimeBreak: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            timeBreak = intent.getDoubleExtra(BreakTimerService.TIME_EXTRA_B, 0.0)
            dataBinding?.TimerBreak?.text = getTimeStringFromDoubleBreak(timeBreak)
        }
    }

    private fun getTimeStringFromDoubleBreak(time: Double): String {
        val resultIntBreak = time.roundToInt()

        lifecycleScope.launch {
            viewModel.breakTimerupdater(time.roundToInt(), dataBinding,this@MainActivity)
        }
        println("$resultIntBreak")
        val hours = resultIntBreak % 86400 / 3600
        val minutes = resultIntBreak % 86400 % 3600 / 60
        val seconds = resultIntBreak % 86400 % 3600 % 60

        return makeTimeStringBreak(hours, minutes, seconds)
    }

    private fun makeTimeStringBreak(hour: Int, min: Int, sec: Int): String =
        String.format("%02d:%02d", hour, min)

    override fun onBackPressed() {
        Log.d("CDA", "onBackPressed Called")
        val setIntent = Intent(Intent.ACTION_MAIN)
        setIntent.addCategory(Intent.CATEGORY_HOME)
        setIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(setIntent)
    }



//    override fun onBackPressed() {
//
////        super.onBackPressed()
////
////        var intent = Intent(this, MainActivity::class.java)
////        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
////        finish()
//        onUserLeaveHint()
//    }
//
//    override fun onUserLeaveHint() {
//        super.onUserLeaveHint()
//    }

    fun getWidth() {
        val displayMetrics = resources.displayMetrics
        dpHeight = displayMetrics.heightPixels / displayMetrics.density
        dpWidth = displayMetrics.widthPixels / displayMetrics.density
        Log.d("MyHeight", dpHeight.toString() + "")
        Log.d("MyWidth", dpWidth.toString() + "")
    }



    fun initPermission() {

        val permissions =
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        Permissions.check(
            this /*context*/,
            permissions,
            null /*rationale*/,
            null /*options*/,
            object : PermissionHandler() {
                override fun onGranted() {
                    // hideIcon()
                    //startService(locationServiceIntent)
                 CheckGpsStatus()


                }
            })


    }

    fun CheckGpsStatus() {
        var locationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager
        assert(locationManager != null)
        var GpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (GpsStatus == true) {

        } else {
            showEnableLocationSetting()
        }
    }


    fun showEnableLocationSetting() {
      this.let {
            val locationRequest = LocationRequest.create()
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

            val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)

            val task = LocationServices.getSettingsClient(it)
                .checkLocationSettings(builder.build())

            task.addOnSuccessListener { response ->
                val states = response.locationSettingsStates
                if (states.isLocationPresent) {
                    //Do something
                }
            }
            task.addOnFailureListener { e ->
                if (e is ResolvableApiException) {
                    try {
                        // Handle result in onActivityResult()
                        e.startResolutionForResult(it,
                            MainActivity.LOCATION_SETTING_REQUEST)
                    } catch (sendEx: IntentSender.SendIntentException) { }
                }
            }
        }
    }


    fun restartActivity() {
        val intent = intent
        finish()
        startActivity(intent)
    }

    fun navSelection(){
        binding.menu.setItemSelected(R.id.User, true)

    }
}