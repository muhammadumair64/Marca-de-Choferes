package com.example.marcadechoferes.mainscreen

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.view.KeyEvent
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
import android.os.Looper

import android.widget.Toast











@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    val mainViewModel: MainViewModel by viewModels()
    var context: Context = this
    var timerStarted = false
    lateinit var serviceIntent: Intent
    var time = 0.0

    lateinit var binding: ActivityMainBinding
    lateinit var serviceIntentB: Intent
    private var timeB = 0.0
    var dataBinding: FragmentHomeBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        serviceIntent = Intent(applicationContext, TimerService::class.java)
        registerReceiver(updateTime, IntentFilter(TimerService.TIMER_UPDATED))

        serviceIntentB = Intent(applicationContext, BreakTimerService::class.java)
        registerReceiver(updateTimeB, IntentFilter(BreakTimerService.TIMER_UPDATED_B))
        binding.menu.setItemSelected(R.id.home, true)
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
        println("i am here 11")
        serviceIntent.putExtra(TimerService.TIME_EXTRA, time)
        startService(serviceIntent)
        timerStarted = true
    }

    fun stopTimer() {
        println("i am here 000")
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
        println("hello i am here $resultInt")
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


    fun startTimerB() {
        println("i am here 11")
        serviceIntent.putExtra(BreakTimerService.TIME_EXTRA_B, timeB)
        startService(serviceIntentB)
        timerStarted = true
    }

    fun stopTimerB() {
        println("i am here 000")
        stopService(serviceIntentB)
        timerStarted = false
    }

    private val updateTimeB: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            timeB = intent.getDoubleExtra(BreakTimerService.TIME_EXTRA_B, 0.0)
            dataBinding?.TimerBreak?.text = getTimeStringFromDoubleB(timeB)
        }
    }

    private fun getTimeStringFromDoubleB(time: Double): String {
        val resultIntB = time.roundToInt()
        println("hello i am here umair$resultIntB")
        val hours = resultIntB % 86400 / 3600
        val minutes = resultIntB % 86400 % 3600 / 60
        val seconds = resultIntB % 86400 % 3600 % 60

        return makeTimeStringB(hours, minutes, seconds)
    }

    private fun makeTimeStringB(hour: Int, min: Int, sec: Int): String =
        String.format("%02d:%02d", hour, min)

    override fun onBackPressed() {
        super.onBackPressed()
    }


}