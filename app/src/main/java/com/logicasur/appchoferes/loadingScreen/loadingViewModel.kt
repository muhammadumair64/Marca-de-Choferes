package com.logicasur.appchoferes.loadingScreen

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.logicasur.appchoferes.Extra.ResendApis
import com.logicasur.appchoferes.Extra.TimeCalculator
import com.logicasur.appchoferes.Extra.TinyDB
import com.logicasur.appchoferes.mainscreen.MainActivity
import com.logicasur.appchoferes.mainscreen.repository.MainRepository
import com.logicasur.appchoferes.myApplication.MyApplication
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import android.app.Activity


@HiltViewModel
class loadingViewModel @Inject constructor(
    var mainRepository: MainRepository,
    var tinyDB: TinyDB, var resendApis: ResendApis,
    val timeCalculator: TimeCalculator
) : ViewModel() {
    lateinit var activityContext: Context
    val sdf = SimpleDateFormat("yyyy-MM-dd")
    val currentDate = sdf.format(Date())

    fun openPopup(networkAlertDialog: AlertDialog, PopupView: View, resources: Resources) {
        networkAlertDialog.setView(PopupView)
        try {
            networkAlertDialog.show()
        } catch (e: Exception) {
            Log.d("LoadingViewModel..", "Exception ${e.localizedMessage}")
        }
        val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.60).toInt()
        networkAlertDialog.getWindow()?.setLayout(width, height);
        networkAlertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val window: Window? = networkAlertDialog.getWindow()
        val wlp: WindowManager.LayoutParams = window!!.getAttributes()
        wlp.gravity = Gravity.BOTTOM
        window.setAttributes(wlp)

    }


    fun openServerPopup(
        serverAlertDialog: AlertDialog,
        PopupView: View,
        resources: Resources
    ) {
        serverAlertDialog.setView(PopupView)
        Log.d("POPUP_TESTING", " In VIEW MODEL")

        serverAlertDialog.show()
//        catch (e:Exception){
//            Log.d("PopupWindowTesting","In Catch Block")
//        }
        val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.60).toInt()
        serverAlertDialog.getWindow()?.setLayout(width, height);
        serverAlertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val window: Window? = serverAlertDialog.getWindow()
        val wlp: WindowManager.LayoutParams = window!!.getAttributes()
        wlp.gravity = Gravity.BOTTOM
        window.setAttributes(wlp)
    }


    fun getPreviousTimeWhenOffline() {

        var breakTime = tinyDB.getInt("breaksendtime")
//        tinyDB.putInt("lasttimework", response.lastVar!!.lastWorkedHoursTotal!!)
        var activity = tinyDB.getInt("SELECTEDACTIVITY")
        when (activity) {
            0 -> {
                getWorkTimeWhenOffline()
            }
            1 -> {
                tinyDB.putString("checkTimer", "breakTime")
                viewModelScope.launch {
                    withContext(Dispatchers.IO) {
                        var breakDate = mainRepository.getUnsentStartBreakTimeDetails().time
                        if (breakDate!!.isNotEmpty()) {
                            breakDate = breakDate!!.split("Z").toTypedArray()[0]
                            breakDate = breakDate!!.replace(",", " ")
                            breakDate = breakDate!!.replace("-", "/")
                            Log.d("workDate Is", "date is $breakDate")
                        }
                        tinyDB.putString("goBackTime", breakDate)
                        tinyDB.putInt("ServerBreakTime", breakTime)
                    }


                    var defaultBreak = tinyDB.getInt("defaultBreak")
                    timeCalculator.timeDifference(tinyDB, activityContext!!, false, defaultBreak)

                    getWorkTimeWhenOffline()
                }

            }
            2 -> {
                MyApplication.dayEndCheck = 100
                getWorkTimeWhenOffline()
            }
            3 -> {
                MyApplication.dayEndCheck = 200
                viewModelScope.launch {
                    withContext(Dispatchers.IO) {
                        checkStateByDataBase()
                    }
                    var intent = Intent(activityContext, MainActivity::class.java)
                    ContextCompat.startActivity(activityContext!!, intent, Bundle.EMPTY)
                }

            }
        }


    }

    fun getWorkTimeWhenOffline() {
        tinyDB.putString("checkTimer", "workTime")

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                checkStateByDataBase()
                var date = mainRepository.getUnsentStartWorkTimeDetails()
                var workDate = date.time
                if (workDate!!.isNotEmpty()) {
                    workDate = workDate!!.split("Z").toTypedArray()[0]
                    workDate = workDate!!.replace(",", " ")
                    workDate = workDate!!.replace("-", "/")

                    Log.d("workDate Is", "date is $workDate")
                }
                tinyDB.putString("goBackTime", workDate)
            }
            var defaultTime = tinyDB.getInt("defaultWork")

            timeCalculator.timeDifference(tinyDB, activityContext!!, false, defaultTime)
            Log.d("TimerTESTING", "Here")
            var intent = Intent(activityContext, MainActivity::class.java)
            ContextCompat.startActivity(activityContext!!, intent, Bundle.EMPTY)

        }


    }

    suspend fun checkStateByDataBase() {

        var workDate = mainRepository.getUnsentStartWorkTimeDetails().time
        if (workDate != null) {
            workDate = workDate!!.split(",").toTypedArray()[0]
            Log.d("workDate Is", "date is $workDate")
        }
        Log.d("Dates is ", "$currentDate")
        var check = tinyDB.getInt("SELECTEDACTIVITY")
        if (workDate != currentDate) {
            check = 3
        }


        tinyDB.putInt("selectedStateByServer", check!!)
        Log.d("checkByServer", "check $check")
        when (check) {
            0 -> {
                tinyDB.putString("selectedState", "goToActiveState")

            }
            1 -> {
                tinyDB.putString("selectedState", "goTosecondState")
            }
            2 -> {
                tinyDB.putString("selectedState", "goToActiveState")

            }
            3 -> {
                tinyDB.putString("selectedState", "endDay")
            }
        }


    }
}