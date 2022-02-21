package com.logicasur.appchoferes.mainscreen.home.viewmodel

import android.content.Context
import android.view.View
import androidx.lifecycle.ViewModel
import com.logicasur.appchoferes.R
import com.logicasur.appchoferes.databinding.FragmentHomeBinding
import com.logicasur.appchoferes.mainscreen.MainActivity
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import javax.inject.Inject
import android.animation.ValueAnimator
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.viewModelScope
import com.logicasur.appchoferes.Extra.CheckConnection
import com.logicasur.appchoferes.Extra.ResendApis
import com.logicasur.appchoferes.Extra.MyBroadastReceivers
import com.logicasur.appchoferes.Extra.TinyDB
import com.logicasur.appchoferes.auth.repository.AuthRepository
import com.logicasur.appchoferes.loadingScreen.LoadingScreen
import com.logicasur.appchoferes.mainscreen.home.timerServices.BreakTimerService
import com.logicasur.appchoferes.mainscreen.home.timerServices.TimerService
import com.logicasur.appchoferes.myApplication.MyApplication
import com.logicasur.appchoferes.network.ApiException
import com.logicasur.appchoferes.network.GeoPosition
import com.logicasur.appchoferes.network.NoInternetException
import com.logicasur.appchoferes.network.ResponseException
import com.logicasur.appchoferes.network.signinResponse.State
import com.logicasur.appchoferes.network.signinResponse.Vehicle
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.logicasur.appchoferes.Extra.serverCheck.ServerCheck
import com.logicasur.appchoferes.mainscreen.repository.MainRepository
import com.logicasur.appchoferes.network.unsentApis.UnsentStartBreakTime
import com.logicasur.appchoferes.network.unsentApis.UnsentStartWorkTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.net.SocketException
import java.net.SocketTimeoutException
import java.text.SimpleDateFormat
import kotlin.concurrent.schedule


@HiltViewModel
class HomeViewModel @Inject constructor(
    val authRepository: AuthRepository,
    val mainRepository: MainRepository,
    val resendApis: ResendApis
) : ViewModel() {
    var activityContext: Context? = null
    var dataBinding: FragmentHomeBinding? = null
    var statusArrayList = ArrayList<String>()
    var statusArrayListforUpload = ArrayList<State>()
    var searchedArrayList = ArrayList<String>()
    var vehicleArrayListforUpload = ArrayList<Vehicle>()
    var max: Float? = null
    var mini: Float? = null
    lateinit var tinyDB: TinyDB
    var temp = 0
    var progressBar = 0
    var latitude = 0.0
    var longitude = 0.0
    var overTimeCheck = false
    var TAG2 = ""
    lateinit var loadingIntent: Intent

    //activity

    var totalTimeForActivty = 0
    var selectedActivty = 0


    // state

    var positionForState = 0


    var maxBreakBarValue = 0
    var maxWorkBarValue = 0

    fun viewsForHomeFragment(context: Context, binding: FragmentHomeBinding) {
        activityContext = context
        dataBinding = binding
        tinyDB = TinyDB(context)

        loadingIntent = Intent(activityContext, LoadingScreen::class.java)



        getProfile()
        setMaxMini()
        getVehicle()
        getState()
        checkInitial()
        setPreviousWork()
        setDay()
        tagsForToast()
        (context as MainActivity).initRepo(authRepository, mainRepository)
        MyBroadastReceivers.authRepository = authRepository
        binding.cardColor.setCardBackgroundColor(Color.parseColor(ResendApis.primaryColor))
        binding.arrowdownbg.setBackgroundColor(Color.parseColor(ResendApis.primaryColor))
        binding.arrowbg.setBackgroundColor(Color.parseColor(ResendApis.primaryColor))
        binding.iconCarbg.setBackgroundColor(Color.parseColor(ResendApis.primaryColor))

        MyApplication.TotalTime = tinyDB.getInt("defaultWork")

        println("total Work Given ")
        MyApplication.TotalBreak = tinyDB.getInt("defaultBreak")


        var DefaultTOShow = MyApplication.TotalTime * 60
        var time = getTimeStringFromDouble(DefaultTOShow)
        println("time is thie $time")
        binding.maxTimer.text = time
        val sdf = SimpleDateFormat("dd MMM")
        val currentDate = sdf.format(Date())
        System.out.println(" C DATE is  " + currentDate)

        binding.date.text = "$currentDate"
        dataBinding?.bar?.progressBarColor = Color.parseColor(ResendApis.primaryColor)
        var Choice = tinyDB.getString("selectedState")

        when (Choice) {

            "initialState" -> {
                buttonInitailState()
            }
            "goToActiveState" -> {
                goToActivState()

            }
            "goTosecondState" -> {
                viewModelScope.launch {
                    withContext(Dispatchers.Main) {
                        checkByServer()
//                        goToSecondState()

                    }

                }


            }
            "takeBreak" -> {
                Log.d("HomeViewModel...", "Call btn take break")
                buttonTakeBreak()

            }
            "endDay" -> {
                buttonEndDay()

            }


        }
        Timer().schedule(200) {
            viewModelScope.launch {
                withContext(Dispatchers.Main) {
                    forgroundCheck()
                }
            }

        }
        Timer().schedule(300) {

            viewModelScope.launch {
                withContext(Dispatchers.Main) {
                    checkBreakBaColor()
                }
            }


        }


    }


    fun Workbar() {
        dataBinding!!.bar.apply {
            var default = tinyDB.getInt("defaultWork")
            dataBinding?.bar?.progressBarColor = Color.parseColor(ResendApis.primaryColor)
            default = default * 60
            var maxTime = default
            progressMax = maxTime.toFloat()
            roundBorder = true
            progressDirection = CircularProgressBar.ProgressDirection.TO_RIGHT
        }

    }

    fun Breakbar() {
        dataBinding!!.breakBar.apply {

//            setProgressWithAnimation(50f, 1000) // =1s
            var default = tinyDB.getInt("defaultBreak")

            default = default * 60
            var maxBreakTime = default

            // Set Progress Max
            progressMax = maxBreakTime.toFloat()

            dataBinding?.breakBar?.progressBarColor = Color.parseColor("#FFD6D9")

            backgroundProgressBarColor = Color.TRANSPARENT


            roundBorder = true
            progressDirection = CircularProgressBar.ProgressDirection.TO_RIGHT
        }


    }

    fun timers() {


        dataBinding?.secondState?.setOnClickListener {
            Log.d("HomeViewModel...", "Click on Second state")
            tinyDB.putBoolean("STATEAPI", false)
            if (dataBinding?.secondState?.text == "End Break" || dataBinding?.secondState?.text == "Fin del descanso" || dataBinding?.secondState?.text == "Fim do intervalo") {
                tinyDB.putInt("SELECTEDACTIVITY", 2)
            } else {
                tinyDB.putInt("SELECTEDACTIVITY", 0)
            }
            checkNetConnection()
            MainActivity.action = null
            (activityContext as MainActivity).initPermission() { secondStateAction() }

        }
        dataBinding?.TakeBreak?.setOnClickListener {
            Log.d("HomeViewModel...", "Click on take break.")

            tinyDB.putBoolean("STATEAPI", false)

            tinyDB.putInt("SELECTEDACTIVITY", 1)

            checkNetConnection()


            MainActivity.action = null
            (activityContext as MainActivity).initPermission() { takeBreakAction() }


        }

        dataBinding?.EndDay?.setOnClickListener {
            Log.d("HomeViewModel...", "Click on End Day.")

            tinyDB.putBoolean("STATEAPI", false)
            tinyDB.putInt("SELECTEDACTIVITY", 3)

            checkNetConnection()
            MainActivity.action = null
            (activityContext as MainActivity).initPermission() { endDayAction() }


        }
        dataBinding?.initialState?.setOnClickListener {
            tinyDB.putBoolean("STATEAPI", false)

            MainActivity.action = null
            (activityContext as MainActivity).initPermission() { initialStateAction() }

        }


    }


    fun secondStateAction() {
        MyApplication.check = 0
        buttonSecondState()
    }

    fun takeBreakAction() {
        var intent = (activityContext as MainActivity)
        MyApplication.check = 0
        Log.d("HomeViewModel...", "Call btn take from takeBreakAction function.")
        buttonTakeBreak()
//        intent.stopTimer()

        intent.startTimerBreak()

        tinyDB.putString("selectedState", "takeBreak")
        Log.d("HomeViewModel...", "Call hit Api Activity(1)")
        hitActivityAPI(1, MyApplication.BreakToSend)
    }

    fun endDayAction() {
        var intent = (activityContext as MainActivity)
        MyApplication.check = 300
        buttonEndDay()
        intent.stopTimer()
        intent.stopTimerBreak()
        var max = MyApplication.TotalBreak * 60
        tinyDB.putInt("MaxBreakBar", max)

        var maxWork = MyApplication.TotalTime * 60
        tinyDB.putInt("MaxBar", maxWork)
        tinyDB.putString("selectedState", "endDay")
        Log.d("HomeViewModel...", "Call hit Api Activity(3)")
        hitActivityAPI(3, MyApplication.TimeToSend)
    }

    fun initialStateAction() {
        MyApplication.check = 0
        buttonInitailState()
        tinyDB.putString("selectedState", "initialState")
    }


    fun buttonInitailState() {
        (activityContext as MainActivity).time = 0.0
//            intent.startTimer()
//            dataBinding?.spacer?.setVisibility(View.VISIBLE)
//        if(check==1) {
////            dataBinding?.initialState?.setVisibility(View.GONE)
////            dataBinding?.secondState?.setVisibility(View.VISIBLE)
//        }
//            dataBinding?.bar?.progressBarColor= Color.parseColor("#7A59FC")


    }

    fun buttonEndDay() {
        var language = tinyDB.getString("language")
        var overTime = tinyDB.getBoolean("overTime")
        var overBreakTime = tinyDB.getBoolean("overBreakTime")

        dataBinding!!.apply {
            if (overTime) {
                dataBinding!!.bar.progressBarColor = Color.parseColor("#7ECAFF")
            } else {
                Log.d("checkOverTimer", "overtime")
                fadeColor()
            }


            if (overBreakTime) {
                breakBar.progressBarColor = Color.parseColor("#FFD6D9")//chnage light red
            } else {
                breakBar.progressBarColor = Color.parseColor("#FFD297")
            }

//        dataBinding?.breakBar?.progress=0f
//        dataBinding?.bar?.progress=0f
            StateActive.setVisibility(View.GONE)
            vehicleListBtn.isClickable = true
            spacer?.setVisibility(View.VISIBLE)
            secondState.setVisibility(View.VISIBLE)
        }



        tinyDB.putInt("state", 0)
//        dataBinding?.initialState?.setVisibility(View.VISIBLE)
//        dataBinding?.vehicleListBtn?.setBackgroundResource(R.drawable.item_popup_btn_bg)
//        dataBinding?.iconCar?.setBackgroundResource(R.drawable.ic_icon_awesome_car_alt)
//        dataBinding?.vehicleNameSelected?.setTextColor(Color.parseColor("#000000"))

//        dataBinding?.Arrow?.setVisibility(View.VISIBLE)
//        dataBinding?.dots?.visibility = View.GONE
//        (activityContext as MainActivity).time = 0.0
////        dataBinding?.workTimer?.text = "00:00"
//        dataBinding?.TimerBreak?.text = "00:00"

        dataBinding?.statusListBtn?.visibility = View.GONE
//        dataBinding?.vehicleNameSelected?.setTypeface(
//            dataBinding?.vehicleNameSelected?.getTypeface(),
//            Typeface.NORMAL
//        )
        if (language == "0") {
            dataBinding?.secondState?.text = "Empezar"
//            dataBinding?.vehicleNameSelected?.text = "Vehículo"
            dataBinding?.statusSelected?.text = "Selección estado"
        } else if (language == "1") {
//            dataBinding?.vehicleNameSelected?.text = "Vehicle"
            dataBinding?.statusSelected?.text = "Status Select"
            dataBinding?.secondState?.text = "Start"
        } else {
//            dataBinding?.vehicleNameSelected?.text = "Veículo"
            dataBinding?.statusSelected?.text = "Seleção de estado"
            dataBinding?.secondState?.text = "Começar"
        }


//        dataBinding?.secondState?.text = getApplication(MyApplication.appContext).resources.getString(R.string.start_Timer)

    }

    fun buttonTakeBreak() {
        if (overTimeCheck == true) {
            dataBinding?.bar?.progressBarColor = Color.parseColor("#169DFD")
        } else {
            fadeColor()
        }

        var overBreakTime = tinyDB.getBoolean("overBreakTime")
        if (overBreakTime) {
            dataBinding!!.breakBar.progressBarColor = Color.parseColor("#FF4D4E")
        } else {
            dataBinding!!.breakBar.progressBarColor = Color.parseColor("#FFD6D9")
        }

        dataBinding?.breakBar?.progressBarColor = Color.parseColor("#FFA023") //chnage to orange
        workTimerLargeToSmall()
        breakTimerSmallToLarge()
        dataBinding!!.statusListBtn.visibility = View.GONE
        dataBinding?.StateActive?.setVisibility(View.GONE)
        dataBinding?.vehicleListBtn?.isClickable = true
        dataBinding?.spacer?.setVisibility(View.VISIBLE)


        var language = tinyDB.getString("language")
        if (language == "0") {
            dataBinding?.secondState?.text = "Fin del descanso"

        } else if (language == "1") {

            dataBinding?.secondState?.text = "End Break"
        } else {
            dataBinding?.secondState?.text = "Fim do intervalo"
        }
//        dataBinding?.secondState?.text = getApplication(MyApplication.appContext).resources.getString(R.string.end_break)
        dataBinding?.secondState?.setVisibility(View.VISIBLE)


    }

    fun buttonSecondState() {
        var intent = (activityContext as MainActivity)
        if (dataBinding?.secondState?.text == "End Break" || dataBinding?.secondState?.text == "Fin del descanso" || dataBinding?.secondState?.text == "Fim do intervalo") {
            goToSecondState()


            var overBreakTime = tinyDB.getBoolean("overBreakTime")
            if (overBreakTime) {
                dataBinding!!.breakBar.progressBarColor =
                    Color.parseColor("#FFD6D9") // change to light red
            } else {
                dataBinding!!.breakBar.progressBarColor = Color.parseColor("#FFD297")
            }
            intent.stopTimerBreak()

            dataBinding!!.statusListBtn.visibility = View.VISIBLE
//            intent.startTimer()
            Log.d("break timer", "${MyApplication.BreakToSend}")

            Log.d("HomeViewModel...", "Call hit Api Activity(2)")
            hitActivityAPI(2, MyApplication.BreakToSend)
            dataBinding!!.statusListBtn.isClickable = true
        } else {
            dataBinding?.breakBar?.progressBarColor = Color.parseColor("#FFD297")
            var time = 0
            MyApplication.dayEndCheck = 0
            intent.timeBreak = 0.0
            tinyDB.putInt("lasttimebreak", 1)
            tinyDB.putInt("lasttimework", 1)
            maxBreakBarValue = MyApplication.TotalBreak
            maxWorkBarValue = MyApplication.TotalTime
            dataBinding?.breakBar?.progress = 0f
            var overtime = getTimeStringFromDouble(time)
            dataBinding!!.overTime!!.text = overtime
            startDaySetter(intent)
            Timer().schedule(200) {
                intent.startTimer()
                intent.startTimerBreak()
                Log.d("BREAKTIMERTEST", "3")
                intent.stopTimerBreak()
            }

            goToActivState()
            Log.d("HomeViewModel...", "Call hit Api Activity(0)")
            hitActivityAPI(0, MyApplication.TimeToSend)

        }
    }

    fun goToSecondState() {

        dataBinding?.breakBar?.progressBarColor = Color.parseColor("#FFD6D9")
        if (overTimeCheck == true) {
            dataBinding?.bar?.progressBarColor = Color.parseColor("#169DFD")
        } else {
            dataBinding?.bar?.progressBarColor = Color.parseColor(ResendApis.primaryColor)
        }

        breakTimerLargeToSmall()
        workTimerSmallToLarge()
        dataBinding?.secondState?.setVisibility(View.GONE)
        dataBinding?.StateActive?.setVisibility(View.VISIBLE)
        dataBinding?.vehicleListBtn?.isClickable = false
        dataBinding?.spacer?.setVisibility(View.GONE)
        tinyDB.putString("selectedState", "goTosecondState")
    }

    fun goToActivState() {
        Log.d("isMYControlComeHere", "yes")
        if (overTimeCheck == true) {
            dataBinding?.bar?.progressBarColor = Color.parseColor("#169DFD")
        } else {
            dataBinding?.bar?.progressBarColor = Color.parseColor(ResendApis.primaryColor)
        }
        dataBinding?.secondState?.setVisibility(View.GONE)
        dataBinding?.StateActive?.setVisibility(View.VISIBLE)
        dataBinding?.vehicleListBtn?.isClickable = false
        dataBinding?.spacer?.setVisibility(View.GONE)
        dataBinding!!.statusListBtn.visibility = View.VISIBLE
        tinyDB.putString("selectedState", "goToActiveState")
    }

// Timer Animations

    fun workTimerSmallToLarge() {
        dataBinding?.workTimer?.setTypeface(dataBinding?.workTimer?.getTypeface(), Typeface.BOLD)
        val startSize = mini // Size in pixels
        val endSize = max
        val animationDuration: Long = 500 // Animation duration in ms

        val animator = ValueAnimator.ofFloat(startSize!!, endSize!!)
        animator.duration = animationDuration

        animator.addUpdateListener { valueAnimator ->
            val animatedValue = valueAnimator.animatedValue as Float
            dataBinding?.workTimer?.textSize = animatedValue
        }

        animator.start()
    }

    fun workTimerLargeToSmall() {
        dataBinding?.workTimer?.setTypeface(dataBinding?.workTimer?.getTypeface(), Typeface.NORMAL)
        val startSize = max // Size in pixels
        val endSize = mini
        val animationDuration: Long = 500 // Animation duration in ms

        val animator = ValueAnimator.ofFloat(startSize!!, endSize!!)
        animator.duration = animationDuration

        animator.addUpdateListener { valueAnimator ->
            val animatedValue = valueAnimator.animatedValue as Float
            dataBinding?.workTimer?.textSize = animatedValue
        }

        animator.start()
    }

    fun breakTimerSmallToLarge() {
        dataBinding?.TimerBreak?.setTypeface(dataBinding?.TakeBreak?.getTypeface(), Typeface.BOLD)
        val startSize = mini // Size in pixels
        val endSize = max
        val animationDuration: Long = 500 // Animation duration in ms

        val animator = ValueAnimator.ofFloat(startSize!!, endSize!!)
        animator.duration = animationDuration

        animator.addUpdateListener { valueAnimator ->
            val animatedValue = valueAnimator.animatedValue as Float
            dataBinding?.TimerBreak?.textSize = animatedValue
        }

        animator.start()
    }

    fun breakTimerLargeToSmall() {
        dataBinding?.TimerBreak?.setTypeface(dataBinding?.TakeBreak?.getTypeface(), Typeface.NORMAL)
        val startSize = max// Size in pixels
        val endSize = mini
        val animationDuration: Long = 500 // Animation duration in ms

        val animator = ValueAnimator.ofFloat(startSize!!, endSize!!)
        animator.duration = animationDuration

        animator.addUpdateListener { valueAnimator ->
            val animatedValue = valueAnimator.animatedValue as Float
            dataBinding?.TimerBreak?.textSize = animatedValue
        }

        animator.start()
    }

    fun setMaxMini() {
        var temp: Float? = (activityContext as MainActivity).dpWidth

        if (temp!! >= 650.0) {
            max = 100f
            mini = 30f
        } else {
            max = 42f
            mini = 12f

        }


    }


    //getProfile
    fun getProfile() {

        viewModelScope.launch {
            withContext(Dispatchers.IO) {

                var profile = authRepository.getProfile()
                dataBinding?.apply {

                    var userName = profile.name
                    Name.text = userName!!.split(" ").toTypedArray()[0]
                    var fatherName = profile.surname
                    surname.text = fatherName!!.split(" ").toTypedArray()[0]
                }
                println("user personal data $profile")
            }

        }

        var image = tinyDB.getString("Avatar")
        base64ToBitmap(image!!)

    }

    fun base64ToBitmap(base64: String) {
        val imageBytes = Base64.decode(base64, 0)
        val image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        dataBinding?.profileImage?.setImageBitmap(image)
        println("imageprinted")

    }

    fun workTimerupdater(time: Int, binding: FragmentHomeBinding?, tinyDB: TinyDB) {
        print("${time.toDouble()}")
        var default = MyApplication.TotalTime * 60
        MyApplication.TimeToSend = time
        maxWorkBarValue = tinyDB.getInt("MaxBar")
        Log.d("DAYENDCHECK", "${MyApplication.dayEndCheck}")


        if (time == default) {
//            tinyDB.putInt("BARPROGRESS", 0)
            binding!!.bar.progress = 1F
        } else if (time > default) {
            println("overTime started ")
            tinyDB.putBoolean("overTime", true)
            overTimeCheck = true
            if (binding!!.secondState.isVisible) {
                binding!!.bar.progressBarColor = Color.parseColor("#7ECAFF")
            } else {
                binding!!.bar.progressBarColor = Color.parseColor("#169DFD")
            }

            //over time calculation
            var newOvertimer = time - default
            var overtime = getTimeStringFromDouble(newOvertimer)
            println("time is thie $time")
            binding.overTime!!.text = overtime


//            var progress = tinyDB.getInt("BARPROGRESS")
//            if (progress == default) {
//                tinyDB.putInt("BARPROGRESS", 0)
//                binding!!.bar.progress = 1F
//            } else {
//                progress = progress + 1
//                binding!!.bar.progress = progress.toFloat()
//                tinyDB.putInt("BARPROGRESS", progress)
//            }


            var cycle = time / default
            var value = cycle.toInt() * default
            var progress = time - value
            binding!!.bar.progress = progress.toFloat()


        } else {
            Log.d("BARPROGRESS", "Yes here")
            tinyDB.putInt("BARPROGRESS", 0)
            tinyDB.putBoolean("overTime", false)
            dataBinding?.bar?.progressBarColor = Color.parseColor(ResendApis.primaryColor)
            overTimeCheck = false
            binding!!.bar.progress = time.toFloat()
        }
    }


    fun breakTimerupdater(
        time: Int,
        binding: FragmentHomeBinding?,
        tinyDB: TinyDB,
        context: Context
    ) {
        print("${time.toDouble()}")
        MyApplication.BreakToSend = time

        var default = MyApplication.TotalBreak * 60


        Log.d("DAYENDCHECK", "${MyApplication.dayEndCheck}")




        if (time >= default) {
            tinyDB.putBoolean("overBreakTime", true)
            if (binding!!.StateActive.isVisible) {
                binding!!.breakBar.progressBarColor =
                    Color.parseColor("#FFD6D9")//change to light red
            } else {
                binding!!.breakBar.progressBarColor = Color.parseColor("#FF4D4E")
            }


//            var progress = tinyDB.getInt("BREAKBARPROGRESS")
//            if (progress == default) {
//                tinyDB.putInt("BREAKBARPROGRESS", 0)
//                binding!!.breakBar.progress = 1F
//            } else {
//                progress = progress + 1
//                binding!!.breakBar.progress = progress.toFloat()
//                tinyDB.putInt("BREAKBARPROGRESS", progress)
//            }


            var cycle = time / default
            var value = cycle.toInt() * default
            var progress = time - value
            binding!!.breakBar.progress = progress.toFloat()


        } else {
            tinyDB.putInt("BREAKBARPROGRESS", 0)
            tinyDB.putBoolean("overBreakTime", false)
            //  binding!!.breakBar?.progressBarColor = Color.parseColor("#FFA023") //change to orange
            binding!!.breakBar.progress = time.toFloat()
        }


    }


    fun getVehicle() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {

                var vehicles = authRepository.getVehicle()
                println("user personal data ${vehicles}")
                searchedArrayList.clear()
                var position = 1
                var lastId = tinyDB.getInt("lastVehicleid")
                for (item in vehicles) {
                    if (item.id == lastId) {
                        Log.d("VEHICALTESTING", "Vehicle ${item.plateNumber}")
                        position
                        tinyDB.putInt("vehicle", position)
                    }
                    position = position.plus(1)
                    searchedArrayList.add("${item.plateNumber} - ${item.description}")
                    vehicleArrayListforUpload.add(item)
                }


//                if(MyApplication.check!=300){
//                    withContext(Dispatchers.Main) {
//                        var vehicle = tinyDB.getInt("vehicle")
//                        if (vehicle != 0) {
//                            vehicle = vehicle.minus(1)
//                            selectVehicleByLocalDB(vehicle)
//                        }
//                    }
//                }else{
//               fadeColor()
//                }

                withContext(Dispatchers.Main) {
                    var vehicle = tinyDB.getInt("vehicle")
                    if (vehicle != 0) {
                        vehicle = vehicle.minus(1)
                        selectVehicleByLocalDB(vehicle)
                    } else {

                        dataBinding!!.initialState.visibility = View.VISIBLE
                        dataBinding!!.secondState.visibility = View.GONE
                    }
                }


            }

        }
    }


    fun getState() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {

                var state = authRepository.getState()
                println("user personal data ${state}")
                statusArrayList.clear()
                for (item in state) {
                    statusArrayList.add("${item.description}")
                    statusArrayListforUpload.add(item)
                }
                withContext(Dispatchers.Main) {
                    var state = tinyDB.getInt("state")
                    if (state != 0) {
//                        dataBinding!!.secondState.isClickable=true
                        state = state.minus(1)
                        selectState(state)
                    }
                }


            }

        }
    }

    //Get Location
    fun getLocation(context: Context) {
        Log.d("LOADING_ISSUE_TESTING", "IN_LOADING SCREEN")
        //change
        println("location call")
        var locationRequest = LocationRequest()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 3000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY


        if (ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

        }


        LocationServices.getFusedLocationProviderClient(context)
            .requestLocationUpdates(locationRequest, object : LocationCallback() {
                override fun onLocationResult(p0: LocationResult?) {
                    super.onLocationResult(p0)

                    LocationServices.getFusedLocationProviderClient(context)
                        .removeLocationUpdates(this)
                    if (p0 != null && p0.locations.size > 0) {
                        longitude = p0.locations[0].longitude
                        latitude = p0.locations[0].latitude

                        val geocoder = Geocoder(context, Locale.getDefault())
//                        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
//                        var location = addresses[0].featureName.toString()
//                        println("City Name is $location")
//                        Toast.makeText(context, "$location", Toast.LENGTH_SHORT).show()

                        Log.d("CurrentLocation", " $longitude and $latitude")
                        var geoPosition = GeoPosition(latitude, longitude)


                        Log.d("LOADING_ISSUE_TESTING", "IN location after")
                        uploadActivity(selectedActivty, totalTimeForActivty, geoPosition)


                    }
                }


            }, Looper.getMainLooper())

        // uploadLocation()
        println("Current Location $longitude and $latitude")
    }

    fun getLocationForState(context: Context) {


        println("location call")
        var locationRequest = LocationRequest()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 3000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY


        if (ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

        }


        LocationServices.getFusedLocationProviderClient(context)
            .requestLocationUpdates(locationRequest, object : LocationCallback() {
                override fun onLocationResult(p0: LocationResult?) {
                    super.onLocationResult(p0)

                    LocationServices.getFusedLocationProviderClient(context)
                        .removeLocationUpdates(this)
                    if (p0 != null && p0.locations.size > 0) {
                        longitude = p0.locations[0].longitude
                        latitude = p0.locations[0].latitude

                        val geocoder = Geocoder(context, Locale.getDefault())
//                        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
//                        var location = addresses[0].featureName.toString()
//                        println("City Name is $location")
//                      Toast.makeText(context, "$location", Toast.LENGTH_SHORT).show()

                        println("Current Location $longitude and $latitude")
                        var geoPosition = GeoPosition(latitude, longitude)

                        uploadState(positionForState, geoPosition)


                    }
                }


            }, Looper.getMainLooper())

        // uploadLocation()
        println("Current Location $longitude and $latitude")
    }

    fun selectVehicle(position: Int) {
        MyApplication.check = 0
        var text = searchedArrayList[position]
        var vehicle = vehicleArrayListforUpload[position]
        tinyDB.putObject("VehicleForBackgroundPush", vehicle)
        println("selected text $text")
        dataBinding!!.apply {
            vehicleListBtn.setBackgroundResource(R.drawable.bg_selectedvehicleback)
            // iconCar.setBackground(ContextCompat.getDrawable(activityContext!!,R.drawable.ic_white_car))
            iconCar.visibility = View.GONE
            iconCarbg.visibility = View.GONE
            iconCarWhite!!.visibility = View.VISIBLE
            vehicleNameSelected.setTextColor(Color.WHITE)
            vehicleNameSelected.text = text
            Arrow.visibility = View.GONE
            dots.visibility = View.VISIBLE
            statusListBtn.visibility = View.VISIBLE
            initialState?.setVisibility(View.GONE)
            secondState?.setVisibility(View.VISIBLE)
        }

        forgroundCheck()

//        dataBinding!!.secondState.isClickable=false
//        getState()
    }

    fun selectVehicleByLocalDB(position: Int) {
        var text = searchedArrayList[position]
        println("selected text $text")

        var vehicle = vehicleArrayListforUpload[position]
        tinyDB.putObject("VehicleForBackgroundPush", vehicle)

        dataBinding!!.apply {
            vehicleListBtn.setBackgroundResource(R.drawable.bg_selectedvehicleback)
//            iconCar.setBackgroundResource(R.drawable.ic_white_car)
            iconCar.visibility = View.GONE
            iconCarbg.visibility = View.GONE
            iconCarWhite!!.visibility = View.VISIBLE
            vehicleNameSelected.setTextColor(Color.WHITE)
            vehicleNameSelected.text = text
            Arrow.visibility = View.GONE
            dots.visibility = View.VISIBLE
//            statusListBtn.visibility = View.VISIBLE
            if (initialState.isVisible) {
                initialState.visibility = View.GONE
            }
            if (dataBinding?.StateActive?.isVisible == false && dataBinding?.initialState?.isVisible == false) {
                dataBinding?.secondState?.visibility = View.VISIBLE
            }

        }

//          dataBinding!!.secondState.isClickable=false
//           getState()
    }

    fun selectState(position: Int) {
        viewModelScope.launch(Dispatchers.Main) {
            dataBinding!!.secondState.isClickable = true
            var text = statusArrayList[position]
            dataBinding!!.statusSelected.text = text
        }

    }


    fun updateState(
        datetime: String?,
        totalTime: Int?,
        state: State?,
        geoPosition: GeoPosition?,
        vehicle: Vehicle?, action: () -> Unit
    ) {

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if (mainRepository.isExistsUnsentUploadActivityDB()) {
                    (activityContext as MainActivity).updatePendingData(true)
                    var position = tinyDB.getInt("state")
                    position = position.minus(1)
                    selectState(position)
                    action()
                    (MyApplication.loadingContext as LoadingScreen).finish()
                } else {
                    var Token = tinyDB.getString("Cookie")

                    viewModelScope.launch {

                        withContext(Dispatchers.IO) {

                            try {

                                val response = authRepository.updateState(
                                    datetime,
                                    totalTime,
                                    state,
                                    geoPosition,
                                    vehicle,
                                    Token!!
                                )

                                println("SuccessResponse $response")



                                if (response != null) {
                                    var position = tinyDB.getInt("state")
                                    position = position.minus(1)

                                    selectState(position)


                                    action()
                                    (MyApplication.loadingContext as LoadingScreen).finish()
                                }

                            } catch (e: ResponseException) {
                                (MyApplication.loadingContext as LoadingScreen).finish()
                                println("ErrorResponse")
                            } catch (e: ApiException) {
                                (MyApplication.loadingContext as LoadingScreen).finish()
                                e.printStackTrace()
                            } catch (e: NoInternetException) {
                                println("position 2")
                                e.printStackTrace()
                                withContext(Dispatchers.Main) {
                                    (MyApplication.loadingContext as LoadingScreen).finish()
                                    Toast.makeText(
                                        activityContext,
                                        (activityContext as MainActivity).TAG2,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } catch (e: SocketTimeoutException) {

                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        activityContext,
                                        TAG2,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    (MyApplication.loadingContext as LoadingScreen).finish()
                                }
                            } catch (e: SocketException) {
                                LoadingScreen.OnEndLoadingCallbacks?.endLoading()
                                Log.d("connection Exception", "Connect Not Available")
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(activityContext, TAG2, Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                LoadingScreen.OnEndLoadingCallbacks?.endLoading()
                                Log.d("connection Exception", "Connect Not Available")
                            }
                        }
                    }
                }
            }
        }


    }


    fun hitStateAPI(position: Int) {
        Log.d("STATETESTING", "HIT")
        positionForState = position
        var status = statusArrayListforUpload[position]
        tinyDB.putObject("STATE_OBJ", status)
        tinyDB.putBoolean("STATEAPI", true)
        (activityContext as MainActivity).initPermission() { getLocationForState(activityContext!!) }

//        getLocationForState(activityContext!!)

//        if (CheckConnection.netCheck(activityContext!!)) {
//            getLocationForState(activityContext!!)
//        } else {
//            Toast.makeText(activityContext, TAG2, Toast.LENGTH_SHORT).show()
//        }


    }


    fun uploadState(position: Int, geoPosition: GeoPosition?) {
//        var intent = Intent(activityContext, LoadingScreen::class.java)
//        ContextCompat.startActivity(activityContext!!, intent, Bundle.EMPTY)
        val sdf = SimpleDateFormat("yyyy-MM-dd:HH:mm:ss")
        var currentDate = sdf.format(Date())
        currentDate = currentDate + "Z"
        System.out.println(" C DATE is  " + currentDate)

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                var vehiclePosition = tinyDB.getInt("vehicle")
                var vehicle = vehicleArrayListforUpload[vehiclePosition - 1]
                var status = statusArrayListforUpload[position]
                println("status is $status")
                viewModelScope.launch(Dispatchers.IO) {
//                    ServerCheck.serverCheckActivityOrStatus(
//                        "$currentDate",
//                        MyApplication.TimeToSend, null,
//                        geoPosition,
//                        vehicle, status
//                    ) {
//                        stateUploadByAction("$currentDate",
//                            MyApplication.TimeToSend,
//                            status,
//                            geoPosition,
//                            vehicle)
//                    }

                    stateUploadByAction(
                        currentDate,
                        MyApplication.TimeToSend,
                        status,
                        geoPosition,
                        vehicle
                    )
                }
//                updateState("$currentDate", MyApplication.TimeToSend, status, geoPosition, vehicle)
            }
        }
    }

    private fun stateUploadByAction(
        currentDate: String,
        timeToSend: Int,
        status: State,
        geoPosition: GeoPosition?,
        vehicle: Vehicle
    ) {

        viewModelScope.launch(Dispatchers.IO) {
            resendApis.serverCheck.serverCheckMainActivityApi(true) { serverAction ->

                updateState(
                    "$currentDate",
                    timeToSend,
                    status,
                    geoPosition,
                    vehicle
                )
                { serverAction() }
            }
        }


    }


    //    fun updateActivity(
//        datetime: String?,
//        totalTime: Int?,
//        activity: Int?,
//        geoPosition: GeoPosition?,
//        vehicle: Vehicle?
//            ) {
//       if(activity==2){
//    if (totalTime != null) {
//        tinyDB.putInt("lasttimebreak", totalTime)
//       }
//              }
//        var intent = Intent(activityContext, LoadingScreen::class.java)
//        ContextCompat.startActivity(activityContext!!, intent, Bundle.EMPTY)
//
//
//        var Token = tinyDB.getString("Cookie")
//        viewModelScope.launch {
//
//            withContext(Dispatchers.IO) {
//
//                try {
//
//                    val response = authRepository.updateActivity(
//                        datetime,
//                        totalTime,
//                        activity,
//                        geoPosition,
//                        vehicle,
//                        Token!!
//                    )
//                    println("SuccessResponse $response")
//
//
//                    if (response != null) {
//                        withContext(Dispatchers.Main) {
//                            (MyApplication.loadingContext as LoadingScreen).finish()
//                        }
//                    }
//
//                } catch (e: ResponseException) {
//                    withContext(Dispatchers.Main) {
//                        Toast.makeText(
//                            activityContext,
//                            "Failed",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                        (MyApplication.loadingContext as LoadingScreen).finish()
//                    }
//                    println("ErrorResponse")
//                } catch (e: ApiException) {
//                    e.printStackTrace()
//                } catch (e: NoInternetException) {
//                    println("position 2")
//                    e.printStackTrace()
//                    withContext(Dispatchers.Main) {
//                        Toast.makeText(
//                            activityContext,
//                            "Check Your Internet Connection",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                }
//                catch (e:SocketTimeoutException){
//
//                    withContext(Dispatchers.Main) {
//                        Toast.makeText(
//                            activityContext,
//                            "Check Your Internet Connection",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                }
//            }
//        }
//
//
//    }
    fun checkNetConnection() {
        if (CheckConnection.netCheck(activityContext!!)) {
            if (!(activityContext as MainActivity).isMyServiceRunning(LoadingScreen::class.java)) {

                ContextCompat.startActivity(activityContext!!, loadingIntent, Bundle.EMPTY)
            }
        }


    }

    fun hitActivityAPI(activity: Int, totalTime: Int?) {


        Log.d("LOADING_ISSUE_TESTING", "IN_ hit activity api")
        Log.d("HomeviewModel", "hitActivityAPI")


//        tinyDB.putInt("SELECTEDACTIVITY",activity)
//        tinyDB.putInt("TOTALTIMETOSEND",totalTime!!)
        val sdf = SimpleDateFormat("yyyy-MM-dd,HH:mm:ss")
        var currentDate = sdf.format(Date())
        currentDate = currentDate + "Z"
        when (activity) {
            0 -> {
                viewModelScope.launch {
                    withContext(Dispatchers.IO) {
                        if (mainRepository.getUnsentStartWorkTimeDetails() != null) {
                            mainRepository.deleteAllUnsentStartWorkTime()
                        }
                        mainRepository!!.insertUnsentStartWorkTime(
                            UnsentStartWorkTime(
                                0,
                                currentDate
                            )
                        )
                    }
                }


            }
            1 -> {
                tinyDB.putInt("breaksendtime", totalTime!!)
                viewModelScope.launch {
                    withContext(Dispatchers.IO) {
                        if (mainRepository.getUnsentStartBreakTimeDetails() != null) {
                            mainRepository.deleteAllUnsentStartBreakTime()
                        }
                        mainRepository!!.insertUnsentStartBreakTime(
                            UnsentStartBreakTime(
                                0,
                                currentDate
                            )
                        )
                    }
                }


            }
            2 -> {
                tinyDB.putInt("breaksendtime", totalTime!!)
            }
        }


        totalTimeForActivty = totalTime!!
        selectedActivty = activity
        if (CheckConnection.netCheck(activityContext!!)) {
            Log.d("LOADING_ISSUE_TESTING", "In condition")
            getLocation(activityContext!!)
        }


    }


    fun uploadActivity(activity: Int, totalTime: Int?, geoPosition: GeoPosition) {
        Log.d("startUpload", "activity function.")
        MyApplication.checKForActivityLoading = true
//        var intent = Intent(activityContext, LoadingScreen::class.java)
//        ContextCompat.startActivity(activityContext!!, intent, Bundle.EMPTY)

        val sdf = SimpleDateFormat("yyyy-MM-dd:HH:mm:ss")
        var currentDate = sdf.format(Date())
        tinyDB.putString("ActivityDate", currentDate)
        currentDate = currentDate + "Z"
        System.out.println(" startUpload" + currentDate)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                var vehiclePosition = tinyDB.getInt("vehicle")
                vehiclePosition = vehiclePosition.minus(1)
                Log.d("positionOfVehicle ", "$vehiclePosition")
                var vehicle = vehicleArrayListforUpload[vehiclePosition]
                resendApis.serverCheck.serverCheckActivityOrStatus(
                    "$currentDate",
                    totalTime,
                    activity,
                    geoPosition,
                    vehicle, null, resendApis
                ) {
                    updateActivityForAction(
                        "$currentDate",
                        totalTime,
                        activity,
                        geoPosition,
                        vehicle
                    )
                }

            }
        }
    }

    fun updateActivityForAction(
        s: String,
        totalTime: Int?,
        activity: Int,
        geoPosition: GeoPosition,
        vehicle: Vehicle
    ) {
        Log.d("UpdateActivity", "ForAction")
        viewModelScope.launch {
            (activityContext as MainActivity).updateActivity(
                s,
                totalTime,
                activity,
                geoPosition,
                vehicle,
                authRepository
            )
        }
    }


    fun checkInitial() {
        var check = tinyDB.getInt("lastVehicleid")
        if (check != 0) {
            dataBinding?.initialState?.setVisibility(View.GONE)
            dataBinding?.secondState?.setVisibility(View.VISIBLE)
        }

        if (dataBinding?.StateActive?.isVisible == true) {
            dataBinding?.vehicleListBtn?.isClickable = false
        }
    }

    private fun getTimeStringFromDouble(time: Int): String {
        val resultIntBreak = time

        println("$resultIntBreak")
        val hours = resultIntBreak / 3600
        val minutes = resultIntBreak % 86400 % 3600 / 60
        val seconds = resultIntBreak % 86400 % 3600 % 60

        return makeTimeString(hours, minutes, seconds)
    }

    private fun makeTimeString(hour: Int, min: Int, sec: Int): String =
        String.format("(%02d:%02d)", hour, min)


    fun setDay() {
        val sdf = SimpleDateFormat("EEEE")
        val d = Date()
        val dayOfTheWeek = sdf.format(d)
        dataBinding?.day?.text = "$dayOfTheWeek"
    }

    fun forgroundCheck() {
        TimerStart()
//        viewModelScope.launch {
//            withContext(Dispatchers.Main){
//
//                if (MyApplication.check==200){
//                    dataBinding!!.initialState!!.visibility=View.GONE
//                    dataBinding!!.StateActive!!.visibility=View.GONE
//                    dataBinding!!.secondState.visibility=View.VISIBLE
//                }
//            }
//        }


    }

    fun setPreviousWork() {
        var intent = (activityContext as MainActivity)
        if (MyApplication.check == 200) {
            intent.startTimer()
            intent.startTimerBreak()
            dataBinding?.bar?.progressBarColor = Color.parseColor(ResendApis.primaryColor)
            var workTime = tinyDB.getInt("lasttimework")
            var breakTime = tinyDB.getInt("lasttimebreak")
            dataBinding?.bar?.progress = workTime.toFloat()
            dataBinding?.breakBar?.progress = breakTime.toFloat()
            dataBinding?.breakBar?.progressBarColor = Color.parseColor("#FFD6D9")
            intent.stopTimerBreak()
            intent.stopTimer()
            overtimeBarColor(intent.isMyServiceRunning(BreakTimerService::class.java))

        } else if (MyApplication.check == 300) {
            intent.startTimer()
            intent.startTimerBreak()
            var workTime = tinyDB.getInt("lasttimework")
            var breakTime = tinyDB.getInt("lasttimebreak")
            dataBinding?.bar?.progress = workTime.toFloat()
            dataBinding?.breakBar?.progress = breakTime.toFloat()
            dataBinding?.breakBar?.progressBarColor = Color.parseColor("#FFD6D9")
            intent.stopTimerBreak()
            intent.stopTimer()
//           buttonEndDay()
            Timer().schedule(200) {
                barColor()
            }

        }

    }

    fun checkGPS(context: Context): Boolean {
        var locationManager =
            context.getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
        assert(locationManager != null)
        var GpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        return GpsStatus
    }

    fun barColor() {
        viewModelScope.launch {
            withContext(Dispatchers.Main) {
                fadeColor()
            }

        }
    }

    fun TimerStart() {
        var ref = (activityContext as MainActivity)
        var timerServiceCheck = ref.isMyServiceRunning(TimerService::class.java)
        var breakTimerService = ref.isMyServiceRunning(BreakTimerService::class.java)


        if (dataBinding?.StateActive?.isVisible == true && timerServiceCheck == false) {
            ref.startTimer()
        } else if (dataBinding?.secondState!!.isVisible == true && breakTimerService == false) {
            if (dataBinding?.secondState?.text == "End Break" || dataBinding?.secondState?.text == "Fin del descanso" || dataBinding?.secondState?.text == "Fim do intervalo") {
                ref.startTimerBreak()
                ref.startTimer()
                dataBinding?.breakBar?.progressBarColor =
                    Color.parseColor("#FFA023")//change to orange
                fadeColor()
            }

        }



        if (timerServiceCheck == true && breakTimerService == false) {

            goToActivState()
            dataBinding?.breakBar?.progressBarColor = Color.parseColor("#FFD6D9")
            dataBinding!!.statusListBtn.visibility = View.VISIBLE
        } else if (breakTimerService) {
            dataBinding!!.statusListBtn.visibility = View.GONE
            dataBinding?.breakBar?.progressBarColor = Color.parseColor("#FFA023")//change to orange
            fadeColor()
        } else {
            if (dataBinding!!.secondState.isVisible) {
                if (dataBinding?.secondState?.text != "End Break" && dataBinding?.secondState?.text != "Fin del descanso" && dataBinding?.secondState?.text != "Fim do intervalo") {
                    dataBinding!!.statusListBtn.visibility = View.GONE
                }
            }

        }


    }

    fun fadeColor() {
        var color = ResendApis.primaryColor.substringAfter("#")
        color = "#99$color"
        dataBinding?.bar?.progressBarColor = Color.parseColor(color)
        Log.d("FadeColor ", "$color")
    }

    fun startDaySetter(intent: MainActivity) {
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val currentDate = sdf.format(Date())
        var workDate = tinyDB.getString("ActivityDate")
        if (workDate!!.isNotEmpty()) {
            workDate = workDate!!.split(":").toTypedArray()[0]
            Log.d("workDate", "date is $workDate")
            Log.d("workDatecurrent", "current Date $currentDate")
        }

//        if(workDate!= currentDate){
//           tinyDB.putInt("lasttimebreak",0)
//           tinyDB.putInt("lasttimework",0)
//           intent.time=0.0
//       }

        tinyDB.putInt("lasttimebreak", 0)
        tinyDB.putInt("lasttimework", 0)
        intent.time = 0.0

    }

    private fun checkByServer() {
        var check = tinyDB.getInt("selectedStateByServer")
        if (check == 1) {
            var language = tinyDB.getString("language")
            if (language == "0") {
                dataBinding?.secondState?.text = "Fin del descanso"

            } else if (language == "1") {

                dataBinding?.secondState?.text = "End Break"
            } else {
                dataBinding?.secondState?.text = "Fim do intervalo"
            }
            Log.d("HomeViewModel...", "Call take break from checkserver function.")
            buttonTakeBreak()
        }
    }

    fun tagsForToast() {
        var language = tinyDB.getString("language")
        if (language == "0") {
            TAG2 = "Comprueba tu conexión a Internet"

        } else if (language == "1") {


            TAG2 = "Check Your Internet Connection"
        } else {

            TAG2 = "Verifique a sua conexão com a internet"
        }

    }

    fun checkBreakBaColor() {
        var ref = (activityContext as MainActivity)
        var timerServiceCheck = ref.isMyServiceRunning(TimerService::class.java)
        var breakTimerService = ref.isMyServiceRunning(BreakTimerService::class.java)

        if (breakTimerService) {
//            fadeColor()
            dataBinding?.breakBar?.progressBarColor =
                Color.parseColor("#FFA023")   //change to orange
        } else {
            dataBinding?.breakBar?.progressBarColor = Color.parseColor("#FFD297")
        }


        if (timerServiceCheck == false && breakTimerService == false) {
            ref.startTimer()
            ref.startTimerBreak()
//            dataBinding!!.bar.progress = (MyApplication.TotalTime / 2).toFloat()
//            dataBinding!!.breakBar.progress = (MyApplication.TotalBreak / 2).toFloat()

            ref.stopTimer()
            ref.stopTimerBreak()

        }



        if (timerServiceCheck == true && breakTimerService == false) {
            dataBinding!!.statusListBtn.visibility = View.VISIBLE
            Log.d("BREAKTIMERTEST", "1")
            ref.startTimerBreak()
            dataBinding?.bar?.progressBarColor = Color.parseColor(ResendApis.primaryColor)
            Timer().schedule(100) {

                ref.stopTimerBreak()
                Log.d("BREAKTIMERTEST", "2")
            }

        }


        overtimeBarColor(breakTimerService)


    }


    fun overtimeBarColor(breakTimerService: Boolean) {
        if (dataBinding!!.secondState.isVisible) {
            var overTime = tinyDB.getBoolean("overTime")
            var overTimeBreak = tinyDB.getBoolean("overBreakTime")
            if (overTime) {
                dataBinding!!.bar.progressBarColor = Color.parseColor("#7ECAFF")
            } else {
                fadeColor()
            }

            Timer().schedule(100) {
                viewModelScope.launch {
                    withContext(Dispatchers.Main) {
                        if (overTimeBreak) {
                            dataBinding!!.breakBar.progressBarColor = Color.parseColor("#FFD6D9") //
                        } else {
                            if (breakTimerService == false) {
                                dataBinding!!.breakBar.progressBarColor =
                                    Color.parseColor("#FFD297")
                            }

                        }
                    }
                }

            }


        }
        breakErrorOverWrite()
    }

    fun openPopup(networkAlertDialog: AlertDialog, PopupView: View, resources: Resources) {
        networkAlertDialog.setView(PopupView)
        try {
            if(!networkAlertDialog.isShowing)
            {
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
            }


        } catch (e: Exception) {
            LoadingScreen.OnEndLoadingCallbacks?.endLoading()
            Log.d("OpenPop", "Exception ${e.localizedMessage}")
        }



    }

    fun breakErrorOverWrite() {
        var ref = (activityContext as MainActivity)
        var breakTimerService = ref.isMyServiceRunning(BreakTimerService::class.java)
        if (dataBinding!!.StateActive.isVisible) {
            if (breakTimerService == true) {
                ref.stopTimerBreak()
                breakTimerLargeToSmall()
                workTimerSmallToLarge()

            }

        }

    }


}