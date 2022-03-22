package com.logicasur.appchoferes.common.loadingScreen

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
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.logicasur.appchoferes.utils.ResendApis
import com.logicasur.appchoferes.Extra.TimeCalculator
import com.logicasur.appchoferes.Extra.TinyDB
import com.logicasur.appchoferes.R
import com.logicasur.appchoferes.afterAuth.mainscreen.MainActivity
import com.logicasur.appchoferes.data.repository.MainRepository
import com.logicasur.appchoferes.utils.myApplication.MyApplication
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


@HiltViewModel
class loadingViewModel @Inject constructor(
    var mainRepository: MainRepository,
    var tinyDB: TinyDB, var resendApis: ResendApis,
    val timeCalculator: TimeCalculator
) : ViewModel() {
    lateinit var activityContext: Context
    val sdf = SimpleDateFormat("yyyy-MM-dd")
    val currentDate = sdf.format(Date())
//----------------------------------------------------------Popups---------------------------------------
    fun openPopup(
    networkAlertDialog: AlertDialog,
    PopupView: View,
    resources: Resources,
    forServer: Boolean,
    subTextView: TextView,
    topTextView: TextView
) {
        try {

            networkAlertDialog.show()
            val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
            val height = (resources.displayMetrics.heightPixels * 0.60).toInt()
            networkAlertDialog.window?.setLayout(width, height)
            networkAlertDialog.setCancelable(false)
            networkAlertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val window: Window? = networkAlertDialog.window
            val wlp: WindowManager.LayoutParams = window!!.attributes
            wlp.gravity = Gravity.BOTTOM
            window.attributes = wlp



        } catch (e: Exception) {
            Log.d("LoadingViewModel..", "Exception ${e.localizedMessage}")
        }

    }


    fun openServerPopup(
        serverAlertDialog: AlertDialog,
        PopupView: View,
        resources: Resources
    ) {
        serverAlertDialog.setView(PopupView)
        Log.d("POPUP_TESTING", " In VIEW MODEL")
        try {
            if (!serverAlertDialog.isShowing) {
                    serverAlertDialog.show()
            }


            val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
            val height = (resources.displayMetrics.heightPixels * 0.60).toInt()
            serverAlertDialog.window?.setLayout(width, height)
            serverAlertDialog.setCancelable(false)
            serverAlertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val window: Window? = serverAlertDialog.getWindow()
            val wlp: WindowManager.LayoutParams = window!!.getAttributes()
            wlp.gravity = Gravity.BOTTOM
            window.setAttributes(wlp)


        } catch (e: Exception) {
            Log.d("PopupWindowTesting", "In Catch Block ${e.localizedMessage}")
        }

    }

//-----------------------------------------------------------utilts------------------------------------
    fun getPreviousTimeWhenOffline(fromWindow: Boolean) {

        val breakTime = tinyDB.getInt("breaksendtime")
        val activity = tinyDB.getInt("SELECTEDACTIVITY")
        Log.d("BreakComeTesting", "when in loading $activity")

        when (activity) {
            0 -> {
                getWorkTimeWhenOffline(fromWindow)
            }
            1 -> {
                tinyDB.putString("checkTimer", "breakTime")
                viewModelScope.launch {
                    withContext(Dispatchers.IO) {
                        var breakDate = mainRepository.getUnsentStartBreakTimeDetails().time
                        if (breakDate.isNotEmpty()) {
                            breakDate = breakDate.split("Z").toTypedArray()[0]
                            breakDate = breakDate.replace(",", " ")
                            breakDate = breakDate.replace("-", "/")
                            Log.d("workDate Is", "date is $breakDate")
                        }
                        tinyDB.putString("goBackTime", breakDate)
                        tinyDB.putInt("ServerBreakTime", breakTime)
                    }


                    val defaultBreak = tinyDB.getInt("defaultBreak")
                    timeCalculator.timeDifference(
                        tinyDB,
                        activityContext!!,
                        false,
                        defaultBreak,
                        null
                    )

                    getWorkTimeWhenOffline(fromWindow)
                }

            }
            2 -> {
                MyApplication.dayEndCheck = 100
                getWorkTimeWhenOffline(fromWindow)
            }
            3 -> {
                MyApplication.dayEndCheck = 200
                viewModelScope.launch {
                    withContext(Dispatchers.IO) {
                        checkStateByDataBase()
                    }
                    if (fromWindow) {
                        var intent = Intent(activityContext, MainActivity::class.java)
                        ContextCompat.startActivity(activityContext, intent, Bundle.EMPTY)
                    }

                }

            }
        }


    }

    fun getWorkTimeWhenOffline(fromWindow: Boolean) {
        tinyDB.putString("checkTimer", "workTime")

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                checkStateByDataBase()


                var workDate = mainRepository.getUnsentStartWorkTimeDetails().time
                if (workDate!!.isNotEmpty()) {
                    workDate = workDate.split("Z").toTypedArray()[0]
                    workDate = workDate.replace(",", " ")
                    workDate = workDate.replace("-", "/")

                    Log.d("workDate Is", "date is $workDate")
                }
                tinyDB.putString("goBackTime", workDate)
            }
            val defaultTime = tinyDB.getInt("defaultWork")

            timeCalculator.timeDifference(tinyDB, activityContext, false, defaultTime, null)
            Log.d("TimerTESTING", "Here")
            if (fromWindow) {
                val intent = Intent(activityContext, MainActivity::class.java)
                ContextCompat.startActivity(activityContext, intent, Bundle.EMPTY)
            }


        }


    }

    suspend fun checkStateByDataBase() {

        Log.d("Dates is ", currentDate)
        val check = tinyDB.getInt("SELECTEDACTIVITY")



        tinyDB.putInt("selectedStateByServer", check)
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