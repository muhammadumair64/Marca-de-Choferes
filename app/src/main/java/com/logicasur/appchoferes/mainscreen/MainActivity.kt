package com.logicasur.appchoferes.mainscreen

import android.Manifest
import android.app.ActivityManager
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import com.logicasur.appchoferes.R
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.logicasur.appchoferes.mainscreen.home.timerServices.BreakTimerService
import com.logicasur.appchoferes.mainscreen.home.timerServices.TimerService
import com.logicasur.appchoferes.mainscreen.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.roundToInt
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions
import kotlinx.coroutines.launch

import android.location.LocationManager

import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.logicasur.appchoferes.Extra.*
import com.logicasur.appchoferes.auth.repository.AuthRepository
import com.logicasur.appchoferes.loadingScreen.LoadingScreen
import com.logicasur.appchoferes.mainscreen.home.timerServices.UploadRemaingDataService
import com.logicasur.appchoferes.mainscreen.home.viewmodel.HomeViewModel
import com.logicasur.appchoferes.myApplication.MyApplication
import com.logicasur.appchoferes.network.ApiException
import com.logicasur.appchoferes.network.GeoPosition
import com.logicasur.appchoferes.network.NoInternetException
import com.logicasur.appchoferes.network.ResponseException
import com.logicasur.appchoferes.network.signinResponse.Vehicle


import com.google.android.gms.common.api.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.SocketTimeoutException
import java.util.*
import com.logicasur.appchoferes.Extra.OnHomePressedListener

import com.logicasur.appchoferes.Extra.HomeWatcher
import com.google.android.gms.location.*
import android.os.PowerManager
import android.provider.Settings
import com.logicasur.appchoferes.databinding.ActivityMainBinding
import com.logicasur.appchoferes.databinding.FragmentHomeBinding
import com.logicasur.appchoferes.mainscreen.repository.MainRepository
import com.logicasur.appchoferes.network.signinResponse.State
import com.logicasur.appchoferes.network.unsentApis.UnsentStatusOrUploadActivity
import java.text.SimpleDateFormat

import java.lang.Exception
import java.net.SocketException
import kotlin.collections.ArrayList


@AndroidEntryPoint
class MainActivity : BaseClass() {

    companion object {
        const val LOCATION_SETTING_REQUEST = 999
        var action: (() -> Unit)? = null
    }

    var arrayList: ArrayList<UpdateActivityDataClass> = ArrayList()
    var TAG1 = ""
    var TAG2 = ""
    val receiver = MyBroadastReceivers()
    val mainViewModel: MainViewModel by viewModels()
    var context: Context = this
    var timerStarted = false
    lateinit var serviceIntent: Intent
    var time = 0.0
    var dpHeight: Float? = null
    var dpWidth: Float? = null
    lateinit var binding: ActivityMainBinding
    lateinit var serviceIntentB: Intent
    var timeBreak = 0.0
    var dataBinding: FragmentHomeBinding? = null
    val viewModel: HomeViewModel by viewModels()
    lateinit var tinyDB: TinyDB
    var WorkTime = 0
    var BreakTime = 0
    var authRepository: AuthRepository? = null
    var mainRepository: MainRepository? = null
    var latitude = 0.0
    var longitude = 0.0
    var breakBarProgress = 0
    lateinit var mgr: PowerManager
    lateinit var wakeLock: PowerManager.WakeLock


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val language = Language()
        language.setLanguage(baseContext)
        MyApplication.activityContext = this
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        Log.d("AVATARTESTING", "IN ON CREATE")

        MyApplication.checKForSyncLoading=false

        tinyDB = TinyDB(this)

        K.primaryColor = tinyDB.getString("primaryColor")!!
        K.secondrayColor = tinyDB.getString("secondrayColor")!!

//        K.timeDifference(tinyDB)
        tagsForToast()
        serviceIntent = Intent(applicationContext, TimerService::class.java)
        registerReceiver(updateTime, IntentFilter(TimerService.TIMER_UPDATED))
        serviceIntentB = Intent(applicationContext, BreakTimerService::class.java)
        registerReceiver(updateTimeBreak, IntentFilter(BreakTimerService.TIMER_UPDATED_B))
        binding.menu.setItemSelected(R.id.home, true)

        initView()
        setTimer()
        getWidth()
        initPermission() { nullFunction() }

        // invalidateOptionsMenu()
//        batteryOptimizing()
        NavBar()

        homepress()
    }


    fun initView() {
        mgr = context.getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWake:Lock")
        MyApplication.backPressCheck = 200
        MyApplication.checkForResume = 0
//        var intent=Intent(this,WatcherService::class.java)
//        startService(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.navigationbar_menu, menu)
        layoutInflater.setFactory(object : LayoutInflater.Factory {
            override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
                Log.d("NAV_BAR", "Called")
                (menu!!.getItem(R.id.home) as com.ismaeldivita.chipnavigation.model.MenuItem).backgroundColor =
                    Color.parseColor("#000000")
                return null
            }
        })
        return super.onCreateOptionsMenu(menu)
    }


    fun NavBar() {
        Log.d("AVATARTESTING", "IN NAV BAR ${K.primaryColor}")

        binding.menu.setMenuResource(R.menu.navigationbar_menu, Color.parseColor(K.primaryColor))
        binding.menu.setItemSelected(R.id.home, true)
        binding.menu.setOnItemSelectedListener {

            when (it) {
                R.id.home -> {
                    mainViewModel.updateValueTo1()
//                    binding.menu.setMenuResource(R.menu.navigationbar_menu)


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
        if (!isMyServiceRunning(TimerService::class.java)) {
            println("work timer start")
            serviceIntent.putExtra(TimerService.TIME_EXTRA, time)
            startService(serviceIntent)
            timerStarted = true
            wakeLock.acquire(10 * 60 * 1000L /*10 minutes*/)

        }

    }

    fun stopTimer() {
        println("work Timer stop")
        tinyDB.putInt("lasttimework", time.toInt())
        stopService(serviceIntent)
        timerStarted = false

//        wakeLock.release()
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
            viewModel.workTimerupdater(time.roundToInt(), dataBinding, tinyDB)
        }
        Log.d("Timer ", "$resultInt")
        println(" $resultInt")
        WorkTime = resultInt
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
        if (!isMyServiceRunning(BreakTimerService::class.java)) {
            println("breakTimer Start")
            serviceIntent.putExtra(BreakTimerService.TIME_EXTRA_B, timeBreak)
            startService(serviceIntentB)
            timerStarted = true
        }

    }

    fun stopTimerBreak() {
        println("break Timer stop")
        tinyDB.putInt("lasttimebreak", timeBreak.toInt())
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
        val test = time
        Log.d("checkBreakTimer", test.toString())
        val resultIntBreak = time.roundToInt()
        lifecycleScope.launch {
            viewModel.breakTimerupdater(time.roundToInt(), dataBinding, tinyDB, this@MainActivity)
        }
        println("$resultIntBreak")
        BreakTime = resultIntBreak
        val hours = resultIntBreak % 86400 / 3600
        val minutes = resultIntBreak % 86400 % 3600 / 60
        val seconds = resultIntBreak % 86400 % 3600 % 60

        return makeTimeStringBreak(hours, minutes, seconds)
    }

    private fun makeTimeStringBreak(hour: Int, min: Int, sec: Int): String =
        String.format("%02d:%02d", hour, min)


//    override fun onUserLeaveHint() {
////        Toast.makeText(applicationContext, "Home Button is Pressed", Toast.LENGTH_SHORT).show()
//        finish()
//        super.onUserLeaveHint()
//    }


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


//    fun initPermission(action:()->Unit) {
//
//        val permissions =
//            arrayOf(
//                Manifest.permission.ACCESS_FINE_LOCATION,
//                Manifest.permission.ACCESS_COARSE_LOCATION
//            )
//        Permissions.check(
//            this /*context*/,
//            permissions,
//            null /*rationale*/,
//            null /*options*/,
//            object : PermissionHandler() {
//                override fun onGranted() {
//                    // hideIcon()
//                    //startService(locationServiceIntent)
//                    requestBackgroundPermission()
//                 CheckGpsStatus(action)
//
//
//                }
//            })
//
//
//    }

    fun CheckGpsStatus(action: () -> Unit) {
        var locationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager
        assert(locationManager != null)
        var GpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (GpsStatus) {
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    if (K.isConnected()) {
                        withContext(Dispatchers.Main) {
                            action()
                            getLocation(context)
                        }

                    } else {
                        withContext(Dispatchers.Main) {
                            mainViewModel.updatePopupValue()
                            var check = tinyDB.getBoolean("STATEAPI")
                            if (check != true) {
                                MainActivity.action = action
                            }
                        }
                    }
                }
            }


        } else {
            showEnableLocationSetting(action)
        }
    }


    fun showEnableLocationSetting(action: () -> Unit) {

        this.let {
            val locationRequest = LocationRequest.create()
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

            val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)

            val task = LocationServices.getSettingsClient(it)
                .checkLocationSettings(builder.build())

            if (K.isConnected()) {
                Log.d("PENDINGAPITESTING", "IN GPS POPUP SETTING")
                MainActivity.action = action
            } else {
                Toast.makeText(this, "NET IS NOT AVAILIABLE", Toast.LENGTH_SHORT).show()
            }

            task.addOnCompleteListener {
            }

            task.addOnSuccessListener { response ->
                Log.d("isSuccess GPS PRO", "nvnf ${checkGPS()}")
                val states = response.locationSettingsStates

            }
            task.addOnFailureListener { e ->
                if (e is ResolvableApiException) {
                    try {
                        // Handle result in onActivityResult()
                        e.startResolutionForResult(
                            it,
                            MainActivity.LOCATION_SETTING_REQUEST
                        )
                    } catch (sendEx: IntentSender.SendIntentException) {
                    }
                }
            }
        }
    }


    fun restartActivity() {
        val intent = intent
        finish()
        startActivity(intent)
    }

    fun setTimer() {
        var workTime = tinyDB.getInt("lasttimework")
        println("Timer is running $workTime")
        time = workTime.toDouble()
        var breakTime = tinyDB.getInt("lasttimebreak")
        timeBreak = breakTime.toDouble()
    }

    fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }


    fun nullFunction() {

    }


    fun checkGPS(): Boolean {
        var locationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager
        assert(locationManager != null)
        var GpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        return GpsStatus
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 999 && resultCode == RESULT_OK) {
            Log.d("isSuccess GPS PRO", "nvnf ${checkGPS()}")
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    if (K.isConnected()) {
                        withContext(Dispatchers.Main) {
                            action?.invoke()
                            getLocation(context)
                        }

                    } else {
                        withContext(Dispatchers.Main) {
                            mainViewModel.updatePopupValue()
                            var check = tinyDB.getBoolean("STATEAPI")
                            if (check != true) {
                                MainActivity.action = action
                            }
                        }
                    }
                }
            }


        }
    }

    suspend fun updateActivity(
        datetime: String?,
        totalTime: Int?,
        activity: Int?,
        geoPosition: GeoPosition?,
        vehicle: Vehicle?,
        authRepository: AuthRepository

    ) {
        Log.d("updateActivity", "Function")

        if (mainRepository!!.isExistsUnsentUploadActivityDB()) {
            Log.d("ACTIVITY_BY_DATABASE", "IN INSERT MODE")
            if (activity == 2) {
                if (totalTime != null) {
                    tinyDB.putInt("lasttimebreak", totalTime)
                }
            }
            this.authRepository = authRepository
            tinyDB.putInt("ActivityCheck", activity!!)
            var obj =
                UpdateActivityDataClass(datetime, totalTime, activity, geoPosition, vehicle, null)
            Log.d("PENDINGDATATESTING", "DATA IS IN MAIN____ $obj")
            tinyDB.putObject("upadteActivity", obj)
            tinyDB.putObject("GeoPosition", geoPosition)
            updatePendingData(false)
            LoadingScreen.OnEndLoadingCallbacks!!.endLoading()
//            MyApplication.checKForActivityLoading = false

        } else {
            Log.d("END_DAY_TESTING", "StartLoading")
//            var intent = Intent(this, LoadingScreen::class.java)
//            lifecycleScope.launch {
//                withContext(Dispatchers.Main) {
//                    if (activity == 3) {
//                        startActivity(intent)
//                    }
//                }
//
//            }

            if (activity == 2) {

                if (totalTime != null) {
                    tinyDB.putInt("lasttimebreak", totalTime)
                }
            }
            this.authRepository = authRepository
            tinyDB.putInt("ActivityCheck", activity!!)
            var obj =
                UpdateActivityDataClass(datetime, totalTime, activity, geoPosition, vehicle, null)
            Log.d("PENDINGDATATESTING", "DATA IS IN MAIN____ $obj")
            tinyDB.putObject("upadteActivity", obj)
            tinyDB.putObject("GeoPosition", geoPosition)


            var Token = tinyDB.getString("Cookie")
            lifecycleScope.launch {

                withContext(Dispatchers.IO) {

                    try {

                        val response = authRepository.updateActivity(
                            datetime,
                            totalTime,
                            activity,
                            geoPosition,
                            vehicle,
                            Token!!
                        )
                        println("SuccessResponse $response")


                        if (response != null) {
                            withContext(Dispatchers.Main) {
                                (MyApplication.loadingContext as LoadingScreen).finish()
                            }
                        }

                    } catch (e: ResponseException) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                TAG1,
                                Toast.LENGTH_SHORT
                            ).show()
                            updatePendingData(false)
                            (MyApplication.loadingContext as LoadingScreen).finish()
                        }
                        println("ErrorResponse")
                    } catch (e: ApiException) {
                        updatePendingData(false)
                        (MyApplication.loadingContext as LoadingScreen).finish()
                        e.printStackTrace()
                    } catch (e: NoInternetException) {
                        updatePendingData(false)
                        println("position 2")
                        e.printStackTrace()
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                TAG2,
                                Toast.LENGTH_SHORT
                            ).show()

                            (MyApplication.loadingContext as LoadingScreen).finish()
                        }
                    } catch (e: SocketTimeoutException) {
                        updatePendingData(false)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                TAG2,
                                Toast.LENGTH_SHORT
                            ).show()
                            (MyApplication.loadingContext as LoadingScreen).finish()
                        }
                    } catch (e: SocketException) {
                        updatePendingData(false)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                TAG2,
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        (MyApplication.loadingContext as LoadingScreen).finish()
                        Log.d("connection Exception", "Connect Not Available")
                    } catch (e: Exception) {
                        (MyApplication.loadingContext as LoadingScreen).finish()
                        Log.d("connection Exception", "Connect Not Available")
                    }
                }
            }

        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDestroy() {

//        var languageCheck = MyApplication.checkForLanguageChange
////        languageCheck=tinyDB.getInt("languageCheck")
////        Log.d("checkLanguageValue", languageCheck.toString())
//
//
//
//        tinyDB.putInt("lasttimebreak", BreakTime)
//        if(languageCheck != 200){
//            stopService(Intent(this,TimerService::class.java))
//            stopService(Intent(this,BreakTimerService::class.java))
//            var check =tinyDB.getInt("ActivityCheck")
//
//            if(isMyServiceRunning(BreakTimerService::class.java)){
//                check = 1
//            }
//            var TimeForUplaod=0
//            when(check){
//                0->{
//                    TimeForUplaod=WorkTime
//
//                    context.startForegroundService(UploadRemaingDataService.getStartIntent(TimeForUplaod,0,authRepository!!,this))
//                }
//                1->{
//                    TimeForUplaod=BreakTime
//
//                    context.startForegroundService(UploadRemaingDataService.getStartIntent(TimeForUplaod,1,authRepository!!,this))
//                }
//                2->{
//                    TimeForUplaod=WorkTime
//
//                    context.startForegroundService(UploadRemaingDataService.getStartIntent(TimeForUplaod,0,authRepository!!,this))
//                }
//                3->{
//
//                }
//
//            }
//        }
//        else{
//            MyApplication.checkForLanguageChange = 0
//        }
        super.onDestroy()
    }

    fun initRepo(authRepository: AuthRepository, mainRepository: MainRepository) {
        this.mainRepository = mainRepository
        this.authRepository = authRepository
    }


    fun homepress() {
        val mHomeWatcher = HomeWatcher(this)
        mHomeWatcher.setOnHomePressedListener(object : OnHomePressedListener {
            override fun onHomePressed() {
//                MyApplication.checkForResume=200
                performSomeActionOnBackPress()
                // do something here...
//                Toast.makeText(this@MainActivity, "Home is pressed", Toast.LENGTH_SHORT).show()
//                finish()
            }

            override fun onHomeLongPressed() {
                //MyApplication.checkForResume=200
                performSomeActionOnBackPress()

//                finish()
            }
        })
        mHomeWatcher.startWatch()
    }

    override fun onBackPressed() {
        // MyApplication.checkForResume=200
        performSomeActionOnBackPress()


        Log.d("CDA", "onBackPressed Called")
        val setIntent = Intent(Intent.ACTION_MAIN)
        setIntent.addCategory(Intent.CATEGORY_HOME)
        setIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(setIntent)
//        finish()
    }


    fun getLocation(context: Context) {
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

                    try {
                        LocationServices.getFusedLocationProviderClient(context)
                            .removeLocationUpdates(this)
                        if (p0 != null && p0.locations.size > 0) {
                            longitude = p0.locations[0].longitude
                            latitude = p0.locations[0].latitude

                            val geocoder = Geocoder(context, Locale.getDefault())
                            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                            var location = addresses[0].featureName.toString()
                            println("City Name is $location")
//                        Toast.makeText(context, "$location", Toast.LENGTH_SHORT).show()

                            println("Current Location $longitude and $latitude")
                            var geoPosition = GeoPosition(latitude, longitude)
                            tinyDB.putObject("GeoPosition", geoPosition)


                        }
                    } catch (e: Exception) {
                        Log.d("GRCP", e.localizedMessage)
                    }


                }


            }, Looper.getMainLooper())

        // uploadLocation()
        println("Current Location $longitude and $latitude")
    }

    fun batteryOptimizing() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent()
            val packageName = packageName
            val pm = getSystemService(POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
        }
    }


    fun performSomeActionOnBackPress() {
        stopService(Intent(this, TimerService::class.java))
        stopService(Intent(this, BreakTimerService::class.java))
        var check = tinyDB.getBoolean("PENDINGCHECK")
        if (check) {
            K.myTimer!!.cancel()
        }
        finishAffinity()
        finish()

//        if( MyApplication.backPressCheck==200){
//
//            var timerService = isMyServiceRunning(TimerService::class.java)
//            var breakService = isMyServiceRunning(BreakTimerService::class.java)
//            if(timerService && breakService == false){
//                try {
//
////                    this.registerReceiver(receiver,IntentFilter(Intent.ACTION_TIME_TICK))
//                    MyApplication.checkForResume = 200
//                    tinyDB.putString("checkTimer","workTime")
//                    MyBroadastReceivers.time = WorkTime
//                    performTask()
//                }catch (e  : Exception){
//                    MyApplication.checkForResume = 100
//                    Log.d("MyBroadCast","Error ${e.localizedMessage}")
//                }
//
//
//            }else if(breakService){
//                try{
////                    this.registerReceiver(receiver,IntentFilter(Intent.ACTION_TIME_TICK))
//                    MyApplication.checkForResume = 200
//                    tinyDB.putString("checkTimer","breakTime")
//                    MyBroadastReceivers.time = BreakTime
//                    MyBroadastReceivers.activiy=1
//                    performTask()
//                }catch (e: Exception){
//                    MyApplication.checkForResume = 100
//                    Log.d("MyBroadCast","Error ${e.localizedMessage}")
//                }
//
//            }
//
//
//
//        }

    }


    fun performTask() {
        MyBroadastReceivers.receivers = receiver
        tinyDB.putInt("lasttimework", WorkTime)
        tinyDB.putInt("lasttimebreak", BreakTime)
        stopService(Intent(this, TimerService::class.java))
        stopService(Intent(this, BreakTimerService::class.java))
        val sdf = SimpleDateFormat("HH:mm:ss")
        val currentDate = sdf.format(Date())
        tinyDB.putString("goBackTime", currentDate)
        MyApplication.backPressCheck = 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            hitWhenleaveScreen()
        }
    }


    override fun onResume() {
        Log.d("check_ON_RESUME", "${MyApplication.checkForResume}")
        binding.menu.setMenuResource(R.menu.navigationbar_menu, Color.parseColor(K.primaryColor))
        if (MyApplication.checkForResume == 200) {

            try {
//                unregisterReceiver(receiver)
                K.timeDifference(tinyDB, this, true, MyApplication.TotalBreak)
                MyApplication.checkForResume = 0
            } catch (e: Exception) {
                MyApplication.checkForResume = 0
                Log.d("MyBroadCast", "Error ${e.localizedMessage}")
            }


        }
        super.onResume()

        var onScreenCheck = tinyDB.getBoolean("SCREENOFF")
        if (onScreenCheck) {
            var intent =
                Intent(this, com.logicasur.appchoferes.splashscreen.SplashScreen::class.java)
            startActivity(intent)
            tinyDB.putBoolean("SCREENOFF", false)
            finish()
        }
    }


//    @RequiresApi(Build.VERSION_CODES.N)
//    private fun scheduleJobFirebaseToRoomDataUpdate() {
//        val jobScheduler = applicationContext
//            .getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
//        val componentName = ComponentName(
//            this,
//            MyJobScheduler::class.java
//        )
//        val jobInfo = JobInfo.Builder(123, componentName)
//            .setMinimumLatency(10000)
//            .setRequiredNetworkType(
//                 JobInfo.NETWORK_TYPE_NOT_ROAMING
//            )
//            .setPersisted(true)
//        var result= jobScheduler.schedule(jobInfo.build())
//        if(result==JobScheduler.RESULT_SUCCESS){
//            Toast.makeText(this, "Job Start", Toast.LENGTH_SHORT).show()
//        }
//
//    }


    fun initPermission(action: () -> Unit) {

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
                @RequiresApi(Build.VERSION_CODES.Q)
                override fun onGranted() {
                    // hideIcon()
                    //startService(locationServiceIntent)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        requestBackgroundPermission()
                    }

                    CheckGpsStatus(action)


                }
            })


    }


    @RequiresApi(Build.VERSION_CODES.Q)
    fun requestBackgroundPermission() {
        var check = true
        val backPermList = arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        Permissions.check(
            this /*context*/,
            backPermList,
            null /*rationale*/,
            null /*options*/,
            object : PermissionHandler() {
                override fun onGranted() {
                    // hideIcon()
                    //startService(locationServiceIntent)
                    check = false
                }
            })


        if (check) {
            AlertDialog.Builder(this)
                .setTitle("Background location permission")
                .setMessage("Allow location permission to get location updates in background")
                .setPositiveButton("Allow") { _, _ ->

                    requestPermissions(
                        backPermList,
                        LOCATION_SETTING_REQUEST
                    )
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }

    }


    fun tagsForToast() {
        var language = tinyDB.getString("language")
        if (language == "0") {
            TAG1 = "Fallida"
            TAG2 = "Comprueba tu conexión a Internet"

        } else if (language == "1") {

            TAG1 = "Failed"
            TAG2 = "Check Your Internet Connection"
        } else {
            TAG1 = "Fracassada"
            TAG2 = "Verifique a sua conexão com a internet"
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun hitWhenleaveScreen() {
        var check = tinyDB.getInt("ActivityCheck")

        if (isMyServiceRunning(BreakTimerService::class.java)) {
            check = 1
        }
        var TimeForUplaod = 0
        when (check) {
            0 -> {
                TimeForUplaod = WorkTime

                context.startForegroundService(
                    UploadRemaingDataService.getStartIntent(
                        TimeForUplaod,
                        0,
                        authRepository!!,
                        this
                    )
                )
            }
            1 -> {
                TimeForUplaod = BreakTime

                context.startForegroundService(
                    UploadRemaingDataService.getStartIntent(
                        TimeForUplaod,
                        1,
                        authRepository!!,
                        this
                    )
                )
            }
            2 -> {
                TimeForUplaod = WorkTime

                context.startForegroundService(
                    UploadRemaingDataService.getStartIntent(
                        TimeForUplaod,
                        0,
                        authRepository!!,
                        this
                    )
                )
            }
            3 -> {

            }
        }
    }


    fun checkScreen() {
        Log.d("SCREENOFFCHECK", "TRUE")
        val isScreenOn: Boolean = mgr.isInteractive()
        if (isScreenOn == false) {
            tinyDB.putBoolean("SCREENOFF", true)
        }
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        tinyDB.putInt("lasttimebreak", timeBreak.toInt())
        tinyDB.putInt("lasttimework", time.toInt())
        savedInstanceState.putBoolean("MyBoolean", true);

    }

    fun getActivityAPIData(): UpdateActivityDataClass {
        var vehicle = tinyDB.getObject("VehicleForBackgroundPush", Vehicle::class.java)
        var geoPosition = tinyDB.getObject("GeoPosition", GeoPosition::class.java)
        val sdf = SimpleDateFormat("yyyy-MM-dd:HH:mm:ss")
        var currentDate = sdf.format(Date())
        currentDate = currentDate + "Z"
        var activity = tinyDB.getInt("SELECTEDACTIVITY")
        var time = 0
        when (activity) {
            0 -> {
                time = 0
            }
            1 -> {
                time = BreakTime
            }
            2 -> {
                time = BreakTime
            }
            3 -> {
                time = WorkTime
            }

        }


        var obj = UpdateActivityDataClass(currentDate, time, activity, geoPosition, vehicle, null)
        return obj
    }

    fun getAPIDataForState(): UpdateActivityDataClass {
        var vehicle = tinyDB.getObject("VehicleForBackgroundPush", Vehicle::class.java)
        var geoPosition = tinyDB.getObject("GeoPosition", GeoPosition::class.java)
        val sdf = SimpleDateFormat("yyyy-MM-dd:HH:mm:ss")
        var currentDate = sdf.format(Date())
        currentDate = currentDate + "Z"
        var activity = tinyDB.getInt("SELECTEDACTIVITY")
        var time = 0
        when (activity) {
            0 -> {
                time = WorkTime
            }
            1 -> {
                time = BreakTime
            }
            2 -> {
                time = BreakTime
            }
            3 -> {
                time = WorkTime
            }

        }
        var state = tinyDB.getObject("STATE_OBJ", State::class.java)
        Log.d("STATE_TESTING", "----> $state")
        var obj = UpdateActivityDataClass(currentDate, time, activity, geoPosition, vehicle, state)
        return obj
    }

    fun updatePendingData(checkState: Boolean) {
        tinyDB.putBoolean("NETCHECK", false)

//        if(checkState){
////            var obj = getAPIDataForState()
////            arrayList= tinyDB.getListObject("PENDINGDATALIST",UpdateActivityDataClass::class.java) as ArrayList<UpdateActivityDataClass>
////            arrayList.add(obj)
//        }else{
//            var obj = getActivityAPIData()
//            arrayList= tinyDB.getListObject("PENDINGDATALIST",UpdateActivityDataClass::class.java) as ArrayList<UpdateActivityDataClass>
//            arrayList.add(obj)
//        }
//        tinyDB.putListObject("PENDINGDATALIST",arrayList as ArrayList<Object>)


        if (checkState) {
            Log.d("STATE_TESTING", "NOT good")
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    val apiData = getAPIDataForState()
                    val objState = UnsentStatusOrUploadActivity(
                        0,
                        apiData.datetime!!,
                        apiData.state!!.id,
                        apiData.state!!.description,
                        null,
                        apiData.totalTime,
                        apiData.vehicle!!.id,
                        apiData.vehicle!!.description,
                        apiData.vehicle!!.plateNumber,
                        apiData.geoPosition!!.latitud,
                        apiData.geoPosition!!.longitud
                    )
                    mainRepository!!.insertUnsentStateOrUploadActivity(objState)
                }
            }
        } else {
            Log.d("STATE_TESTING", "OK")
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    var apiData = getActivityAPIData()
                    var objActivity = UnsentStatusOrUploadActivity(
                        0,
                        apiData.datetime!!,
                        null,
                        null,
                        apiData.activity!!,
                        apiData.totalTime,
                        apiData.vehicle!!.id,
                        apiData.vehicle!!.description,
                        apiData.vehicle!!.plateNumber,
                        apiData.geoPosition!!.latitud,
                        apiData.geoPosition!!.longitud
                    )
                    mainRepository!!.insertUnsentStateOrUploadActivity(objActivity)
                }
            }

        }

        var check = tinyDB.getBoolean("PENDINGCHECK")
        if (check == false) {
            K.checkNet()
        }

        if (checkState == false) {
            action?.invoke()
        }


    }


}