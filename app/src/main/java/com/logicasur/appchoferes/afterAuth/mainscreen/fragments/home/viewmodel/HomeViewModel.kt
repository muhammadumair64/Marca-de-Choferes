package com.logicasur.appchoferes.afterAuth.mainscreen.fragments.home.viewmodel

import android.content.Context
import android.view.View
import androidx.lifecycle.ViewModel
import com.logicasur.appchoferes.R

import com.logicasur.appchoferes.afterAuth.mainscreen.MainActivity
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import javax.inject.Inject
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.viewModelScope
import com.logicasur.appchoferes.data.repository.AuthRepository
import com.logicasur.appchoferes.common.loadingScreen.LoadingScreen
import com.logicasur.appchoferes.afterAuth.mainscreen.fragments.home.timerServices.BreakTimerService
import com.logicasur.appchoferes.afterAuth.mainscreen.fragments.home.timerServices.TimerService
import com.logicasur.appchoferes.utils.myApplication.MyApplication
import com.logicasur.appchoferes.data.network.ApiException
import com.logicasur.appchoferes.data.network.GeoPosition
import com.logicasur.appchoferes.data.network.NoInternetException
import com.logicasur.appchoferes.data.network.ResponseException
import com.logicasur.appchoferes.data.network.signinResponse.State
import com.logicasur.appchoferes.data.network.signinResponse.Vehicle
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.logicasur.appchoferes.data.repository.MainRepository
import com.logicasur.appchoferes.data.network.unsentApis.UnsentStartBreakTime
import com.logicasur.appchoferes.data.network.unsentApis.UnsentStartWorkTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.net.SocketException
import java.net.SocketTimeoutException
import java.text.SimpleDateFormat
import kotlin.concurrent.schedule
import com.logicasur.appchoferes.Extra.*
import com.logicasur.appchoferes.databinding.FragmentHomeBinding
import com.logicasur.appchoferes.utils.ResendApis


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

    @SuppressLint("SimpleDateFormat")
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
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
        checkAndSetValuesOnMainScreen()
        setPreviousWork()
        setDate()
        tagsForToast()
        (context as MainActivity).initRepo(authRepository, mainRepository)
        MyBroadastReceivers.authRepository = authRepository
        changeBackgroundColor()
        MyApplication.TotalTime = tinyDB.getInt("defaultWork")
        MyApplication.TotalBreak = tinyDB.getInt("defaultBreak")
        updateSomeUiComponents()

    }


    //--------------------------------------------Actions ----------------------------------------------------------

    /**
     * ************************************* Action Function for Higher order function pass **************************
     */

    private fun secondStateAction() {
        MyApplication.check = 0
        buttonSecondState()
    }

    private fun takeBreakAction() {
        val intent = (activityContext as MainActivity)
        MyApplication.check = 0
        Log.d("HomeViewModel...", "Call btn take from takeBreakAction function.")
        buttonTakeBreak()
//        intent.stopTimer()

        intent.startTimerBreak()

        tinyDB.putString("selectedState", "takeBreak")
        Log.d("HomeViewModel...", "Call hit Api Activity(1)")
        prepareDataForActivityAPI(1, MyApplication.BreakToSend)
    }

    private fun endDayAction() {
        val intent = (activityContext as MainActivity)
        MyApplication.check = 300
        buttonEndDay()
        intent.stopTimer()
        intent.stopTimerBreak()
        val max = MyApplication.TotalBreak * 60
        tinyDB.putInt("MaxBreakBar", max)

        val maxWork = MyApplication.TotalTime * 60
        tinyDB.putInt("MaxBar", maxWork)
        tinyDB.putString("selectedState", "endDay")
        Log.d("HomeViewModel...", "Call hit Api Activity(3)")
        prepareDataForActivityAPI(3, MyApplication.TimeToSend)
    }

    private fun initialStateAction() {
        MyApplication.check = 0
        buttonInitailState()
        tinyDB.putString("selectedState", "initialState")
    }

    //------------------------------------------- Action functionality------------------------------------

    /**
     * ************************************* Change the screen Ui on change activity **************************
     */
    private fun buttonInitailState() {
        (activityContext as MainActivity).time = 0.0
//            intent.startTimer()
//            dataBinding?.spacer?.setVisibility(View.VISIBLE)
//        if(check==1) {
////            dataBinding?.initialState?.setVisibility(View.GONE)
////            dataBinding?.secondState?.setVisibility(View.VISIBLE)
//        }
//            dataBinding?.bar?.progressBarColor= Color.parseColor("#7A59FC")


    }

    private fun buttonEndDay() {
        val language = tinyDB.getString("language")
        val overTime = tinyDB.getBoolean("overTime")
        val overBreakTime = tinyDB.getBoolean("overBreakTime")

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

            StateActive.setVisibility(View.GONE)
            vehicleListBtn.isClickable = true
            spacer?.setVisibility(View.VISIBLE)
            secondState.setVisibility(View.VISIBLE)
        }



        tinyDB.putInt("state", 0)


        dataBinding?.statusListBtn?.visibility = View.GONE

        if (language == "0") {
            dataBinding?.secondState?.text = "Empezar"

            dataBinding?.statusSelected?.text = "Selección estado"
        } else if (language == "1") {

            dataBinding?.statusSelected?.text = "Status Select"
            dataBinding?.secondState?.text = "Start"
        } else {
            dataBinding?.statusSelected?.text = "Seleção de estado"
            dataBinding?.secondState?.text = "Começar"
        }


//        dataBinding?.secondState?.text = getApplication(MyApplication.appContext).resources.getString(R.string.start_Timer)

    }

    private fun buttonTakeBreak() {
        if (overTimeCheck) {
            dataBinding?.bar?.progressBarColor = Color.parseColor("#169DFD")
        } else {
            fadeColor()
        }

        val overBreakTime = tinyDB.getBoolean("overBreakTime")
        if (overBreakTime) {
            dataBinding!!.breakBar.progressBarColor = Color.parseColor("#FF4D4E")
        } else {
            dataBinding!!.breakBar.progressBarColor = Color.parseColor("#FFD6D9")
        }

        dataBinding?.breakBar?.progressBarColor = Color.parseColor("#FFA023") //chnage to orange
        workTimerLargeToSmall()
        breakTimerSmallToLarge()
        dataBinding!!.statusListBtn.visibility = View.GONE
        dataBinding?.StateActive?.visibility = View.GONE
        dataBinding?.vehicleListBtn?.isClickable = true
        dataBinding?.spacer?.visibility = View.VISIBLE


        val language = tinyDB.getString("language")
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

    private fun buttonSecondState() {
        val intent = (activityContext as MainActivity)
        if (dataBinding?.secondState?.text == "End Break" || dataBinding?.secondState?.text == "Fin del descanso" || dataBinding?.secondState?.text == "Fim do intervalo") {
            goToSecondState()

            val overBreakTime = tinyDB.getBoolean("overBreakTime")
            if (overBreakTime) {
                dataBinding!!.breakBar.progressBarColor =
                    Color.parseColor("#FFD6D9") // change to light red
            } else {
                dataBinding!!.breakBar.progressBarColor = Color.parseColor("#FFD297")
            }
            intent.stopTimerBreak()

            dataBinding!!.statusListBtn.visibility = View.VISIBLE

            Log.d("break timer", "${MyApplication.BreakToSend}")

            Log.d("HomeViewModel...", "Call hit Api Activity(2)")
            prepareDataForActivityAPI(2, MyApplication.BreakToSend)
            dataBinding!!.statusListBtn.isClickable = true
        } else {
            dataBinding?.breakBar?.progressBarColor = Color.parseColor("#FFD297")
            val time = 0
            MyApplication.dayEndCheck = 0
            intent.timeBreak = 0.0
            tinyDB.putInt("lasttimebreak", 1)
            tinyDB.putInt("lasttimework", 1)
            maxBreakBarValue = MyApplication.TotalBreak
            maxWorkBarValue = MyApplication.TotalTime
            dataBinding?.breakBar?.progress = 0f
            val overtime = getTimeStringFromDouble(time)
            dataBinding!!.overTime!!.text = overtime
            startDaySetter(intent)
            Timer().schedule(200) {
                intent.startTimer()
                intent.startTimerBreak()
                Log.d("BREAKTIMERTEST", "3")
                intent.stopTimerBreak()
            }
            val myStatus: String = activityContext!!.getResources()
                .getString(com.logicasur.appchoferes.R.string.select_status)
            dataBinding?.statusSelected!!.text = myStatus


            goToActivState()
            Log.d("HomeViewModel...", "Call hit Api Activity(0)")
            prepareDataForActivityAPI(0, MyApplication.TimeToSend)

        }
    }

    private fun goToSecondState() {

        dataBinding?.breakBar?.progressBarColor = Color.parseColor("#FFD6D9")
        if (overTimeCheck) {
            dataBinding?.bar?.progressBarColor = Color.parseColor("#169DFD")
        } else {
            dataBinding?.bar?.progressBarColor = Color.parseColor(ResendApis.primaryColor)
        }

        breakTimerLargeToSmall()
        workTimerSmallToLarge()
        dataBinding?.secondState?.visibility = View.GONE
        dataBinding?.StateActive?.visibility = View.VISIBLE
        dataBinding?.vehicleListBtn?.isClickable = false
        dataBinding?.spacer?.visibility = View.GONE
        tinyDB.putString("selectedState", "goTosecondState")
    }

    private fun goToActivState() {
        Log.d("isMYControlComeHere", "yes")
        if (overTimeCheck) {
            dataBinding?.bar?.progressBarColor = Color.parseColor("#169DFD")
        } else {
            dataBinding?.bar?.progressBarColor = Color.parseColor(ResendApis.primaryColor)
        }
        dataBinding?.secondState?.visibility = View.GONE
        dataBinding?.StateActive?.visibility = View.VISIBLE
        dataBinding?.vehicleListBtn?.isClickable = false
        dataBinding?.spacer?.visibility = View.GONE
        dataBinding!!.statusListBtn.visibility = View.VISIBLE
        tinyDB.putString("selectedState", "goToActiveState")
    }


    //------------------------------------------------------- Getters -----------------------------------------

    /**
     * ************************************* Get Data and update on screen **************************
     */

    private fun getProfile() {

        viewModelScope.launch {
            withContext(Dispatchers.IO) {

                val profile = authRepository.getProfile()
                dataBinding?.apply {

                    val userName = profile.name
                    Name.text = userName.split(" ").toTypedArray()[0]
                    val fatherName = profile.surname
                    surname.text = fatherName.split(" ").toTypedArray()[0]
                }
                println("user personal data $profile")
            }

        }

        val image = tinyDB.getString("Avatar")
        base64ToBitmap(image!!)

    }

    private fun getVehicle() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {

                val vehicles = authRepository.getVehicle()
                println("user personal data ${vehicles}")
                searchedArrayList.clear()
                var position = 1
                val lastId = tinyDB.getInt("lastVehicleid")
                for (item in vehicles) {
                    if (item.id == lastId) {
                        Log.d("VEHICALTESTING", "Vehicle ${item.plateNumber}")
                        tinyDB.putInt("vehicle", position)
                    }
                    position = position.plus(1)
                    searchedArrayList.add("${item.plateNumber} - ${item.description}")
                    vehicleArrayListforUpload.add(item)
                }


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

    private fun getState() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {

                val state = authRepository.getState()
                println("user personal data $state")
                statusArrayList.clear()
                for (item in state) {
                    statusArrayList.add(item.description)
                    statusArrayListforUpload.add(item)
                }
                withContext(Dispatchers.Main) {
                    var status = tinyDB.getInt("state")
                    if (status != 0) {

                        status = status.minus(1)
                        selectState(status)
                    }
                }


            }

        }
    }

    //Get Location
    private fun getLocation(context: Context, forActivity: Boolean) {
        Log.d("LOADING_ISSUE_TESTING", "IN_LOADING SCREEN")
        //change
        println("location call")
        val locationRequest = LocationRequest()
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

                        Log.d("CurrentLocation", " $longitude and $latitude")
                        val geoPosition = GeoPosition(latitude, longitude)

                        if (forActivity) {
                            Log.d("LOADING_ISSUE_TESTING", "IN location after")
                            uploadActivity(selectedActivty, totalTimeForActivty, geoPosition)
                        } else {
                            uploadState(positionForState, geoPosition)
                        }


                    }
                }


            }, Looper.getMainLooper())

        // uploadLocation()
        println("Current Location $longitude and $latitude")
    }

    fun selectVehicle(position: Int) {
        MyApplication.check = 0
        val text = searchedArrayList[position]
        val vehicle = vehicleArrayListforUpload[position]
        tinyDB.putObject("VehicleForBackgroundPush", vehicle)
        println("selected text $text")
        dataBinding!!.apply {
            vehicleListBtn.setBackgroundResource(R.drawable.bg_selectedvehicleback)
            // iconCar.setBackground(ContextCompat.getDrawable(activityContext!!,R.drawable.ic_white_car))
            iconCar.visibility = View.GONE
            iconCarbg.visibility = View.GONE
            iconCarWhite.visibility = View.VISIBLE
            vehicleNameSelected.setTextColor(Color.WHITE)
            vehicleNameSelected.text = text
            Arrow.visibility = View.GONE
            dots.visibility = View.VISIBLE
            statusListBtn.visibility = View.VISIBLE
            initialState.setVisibility(View.GONE)
            secondState.setVisibility(View.VISIBLE)
        }

        autoTimerStart()

//        dataBinding!!.secondState.isClickable=false
//        getState()
    }

    private fun selectVehicleByLocalDB(position: Int) {
        val text = searchedArrayList[position]
        println("selected text $text")

        val vehicle = vehicleArrayListforUpload[position]
        tinyDB.putObject("VehicleForBackgroundPush", vehicle)

        dataBinding!!.apply {
            vehicleListBtn.setBackgroundResource(R.drawable.bg_selectedvehicleback)
//            iconCar.setBackgroundResource(R.drawable.ic_white_car)
            iconCar.visibility = View.GONE
            iconCarbg.visibility = View.GONE
            iconCarWhite.visibility = View.VISIBLE
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
            val text = statusArrayList[position]
            dataBinding!!.statusSelected.text = text
        }

    }


    //-----------------------------------------------------Apis--------------------------------------------
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
                    val Token = tinyDB.getString("Cookie")

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
                                showToast()
                            } catch (e: SocketTimeoutException) {

                                showToast()
                            } catch (e: SocketException) {
                                showToast()
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


    fun getDataForStateApi(position: Int) {
        Log.d("STATETESTING", "HIT")
        positionForState = position
        val status = statusArrayListforUpload[position]
        tinyDB.putObject("STATE_OBJ", status)
        tinyDB.putBoolean("STATEAPI", true)
        (activityContext as MainActivity).initPermission() { getLocation(activityContext!!, false) }
    }


    fun uploadState(position: Int, geoPosition: GeoPosition?) {
        var currentDate = sdf.format(Date())
        currentDate = currentDate.replace(" ", "T")
        currentDate += "Z"


        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val vehiclePosition = tinyDB.getInt("vehicle")
                val vehicle = vehicleArrayListforUpload[vehiclePosition - 1]
                val status = statusArrayListforUpload[position]
                println("status is $status")




                viewModelScope.launch(Dispatchers.IO) {
                    stateUploadByAction(
                        currentDate,
                        MyApplication.TimeToSend,
                        status,
                        geoPosition,
                        vehicle
                    )
                }
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
            /**
             *  changes
             */
            if (mainRepository.isExistsUnsentUploadActivityDB()) {
                (activityContext as MainActivity).updatePendingData(true)
                var position = tinyDB.getInt("state")
                position = position.minus(1)
                selectState(position)
//                action()
                (MyApplication.loadingContext as LoadingScreen).finish()
            } else {
                resendApis.serverCheck.serverCheckMainActivityApi(true) { serverAction ->

                    updateState(
                        currentDate,
                        timeToSend,
                        status,
                        geoPosition,
                        vehicle
                    )
                    { serverAction() }
                }
            }


        }


    }


    @SuppressLint("SimpleDateFormat")
    fun prepareDataForActivityAPI(activity: Int, totalTime: Int?) {
        Log.d("LOADING_ISSUE_TESTING", "IN_ hit activity api")
        Log.d("HomeviewModel", "hitActivityAPI")
        val sdf = SimpleDateFormat("yyyy-MM-dd,HH:mm:ss")
        var currentDate = sdf.format(Date())
        currentDate += "Z"

        insertActivityTimeInDB(currentDate, totalTime, activity)

        totalTimeForActivty = totalTime!!
        selectedActivty = activity
        if (CheckConnection.netCheck(activityContext!!)) {
            Log.d("LOADING_ISSUE_TESTING", "In condition")
            getLocation(activityContext!!, true)
        }


    }


    fun uploadActivity(activity: Int, totalTime: Int?, geoPosition: GeoPosition) {
        Log.d("startUpload", "activity function.")
        MyApplication.checKForActivityLoading = true
        var currentDate = sdf.format(Date())
        when (activity) {
            0 -> tinyDB.putString("WorkDate", currentDate)
            1 -> tinyDB.putString("BreakDate", currentDate)
        }

        currentDate = currentDate.replace(" ", "T")
        tinyDB.putString("ActivityDate", currentDate)
        currentDate += "Z"
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                var vehiclePosition = tinyDB.getInt("vehicle")
                vehiclePosition = vehiclePosition.minus(1)
                Log.d("positionOfVehicle ", "$vehiclePosition")
                val vehicle = vehicleArrayListforUpload[vehiclePosition]


                /**
                 * My Changes
                 */

                    resendApis.serverCheck.serverCheckMainActivityApi(true) { serverAction ->
                        updateActivityForAction(
                            currentDate,
                            totalTime,
                            activity,
                            geoPosition,
                            vehicle
                        ) { serverAction() }

                    }



            }
        }
    }

    private fun updateActivityForAction(
        s: String,
        totalTime: Int?,
        activity: Int,
        geoPosition: GeoPosition,
        vehicle: Vehicle,
        stopInBetweenServerCheck: () -> Unit
    ) {
        Log.d("UpdateActivity", "ForAction")
        viewModelScope.launch {
            (activityContext as MainActivity).updateActivity(
                s,
                totalTime,
                activity,
                geoPosition,
                vehicle,
                authRepository,
                stopInBetweenServerCheck
            )
        }
    }


    private fun insertActivityTimeInDB(currentDate: String, totalTime: Int?, activity: Int) {
        when (activity) {
            0 -> {
                viewModelScope.launch {
                    withContext(Dispatchers.IO) {
                        if (mainRepository.getUnsentStartWorkTimeDetails() != null) {
                            mainRepository.deleteAllUnsentStartWorkTime()
                        }
                        Log.d("DATABASEATESTING", " IN TAKE work")
                        mainRepository.insertUnsentStartWorkTime(
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

                        Log.d("DATABASEATESTING", " IN TAKE BREAK")
                        mainRepository.insertUnsentStartBreakTime(
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
    }

    private fun setPreviousWork() {
        val intent = (activityContext as MainActivity)
        if (MyApplication.check == 200) {
            intent.startTimer()
            intent.startTimerBreak()
            dataBinding?.bar?.progressBarColor = Color.parseColor(ResendApis.primaryColor)
            val workTime = tinyDB.getInt("lasttimework")
            val breakTime = tinyDB.getInt("lasttimebreak")
            dataBinding?.bar?.progress = workTime.toFloat()
            dataBinding?.breakBar?.progress = breakTime.toFloat()
            dataBinding?.breakBar?.progressBarColor = Color.parseColor("#FFD6D9")
            intent.stopTimerBreak()
            intent.stopTimer()
            overtimeBarColor(intent.isMyServiceRunning(BreakTimerService::class.java))

        } else if (MyApplication.check == 300) {
            intent.startTimer()
            intent.startTimerBreak()
            val workTime = tinyDB.getInt("lasttimework")
            val breakTime = tinyDB.getInt("lasttimebreak")
            dataBinding?.bar?.progress = workTime.toFloat()
            dataBinding?.breakBar?.progress = breakTime.toFloat()
            dataBinding?.breakBar?.progressBarColor = Color.parseColor("#FFD6D9")
            intent.stopTimerBreak()
            intent.stopTimer()
            Timer().schedule(200) {
                barColor()
            }

        }

    }


    private fun autoTimerStart() {
        val ref = (activityContext as MainActivity)
        val timerServiceCheck = ref.isMyServiceRunning(TimerService::class.java)
        val breakTimerService = ref.isMyServiceRunning(BreakTimerService::class.java)


        if (dataBinding?.StateActive?.isVisible == true && !timerServiceCheck) {
            ref.startTimer()
        } else if (dataBinding?.secondState!!.isVisible && !breakTimerService) {
            if (dataBinding?.secondState?.text == "End Break" || dataBinding?.secondState?.text == "Fin del descanso" || dataBinding?.secondState?.text == "Fim do intervalo") {
                ref.startTimerBreak()
                ref.startTimer()
                dataBinding?.breakBar?.progressBarColor =
                    Color.parseColor("#FFA023")//change to orange
                fadeColor()
            }

        }



        if (timerServiceCheck && !breakTimerService) {

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


    //---------------------------------------Timer Animaters------------------------------------------

    /**
     * These functions are for animate time large to small and vice versa
     */

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
        val temp: Float? = (activityContext as MainActivity).dpWidth

        if (temp!! >= 650.0) {
            max = 100f
            mini = 30f
        } else {
            max = 42f
            mini = 12f

        }


    }


    //---------------------------------------------time calculation----------------------
    fun workTimerUpdater(time: Int, binding: FragmentHomeBinding?, tinyDB: TinyDB) {
        print("${time.toDouble()}")
        val default = MyApplication.TotalTime * 60
        MyApplication.TimeToSend = time
        maxWorkBarValue = tinyDB.getInt("MaxBar")
        Log.d("DAYENDCHECK", "${MyApplication.dayEndCheck}")


        if (time == default) {
            binding!!.bar.progress = 1F
        } else if (time > default) {
            println("overTime started ")
            tinyDB.putBoolean("overTime", true)
            overTimeCheck = true
            if (binding!!.secondState.isVisible) {
                binding.bar.progressBarColor = Color.parseColor("#7ECAFF")
            } else {
                binding.bar.progressBarColor = Color.parseColor("#169DFD")
            }

            //over time calculation
            val newOvertimer = time - default
            val overtime = getTimeStringFromDouble(newOvertimer)
            println("time is thie $time")
            binding.overTime!!.text = overtime


            val cycle = time / default
            val value = cycle.toInt() * default
            val progress = time - value
            binding.bar.progress = progress.toFloat()


        } else {
            Log.d("BARPROGRESS", "Yes here")
            tinyDB.putInt("BARPROGRESS", 0)
            tinyDB.putBoolean("overTime", false)
            dataBinding?.bar?.progressBarColor = Color.parseColor(ResendApis.primaryColor)
            overTimeCheck = false
            binding!!.bar.progress = time.toFloat()
        }
    }

    fun breakTimerUpdater(
        time: Int,
        binding: FragmentHomeBinding?,
        tinyDB: TinyDB,
        context: Context
    ) {
        print("${time.toDouble()}")
        MyApplication.BreakToSend = time

        val default = MyApplication.TotalBreak * 60


        Log.d("DAYENDCHECK", "${MyApplication.dayEndCheck}")




        if (time >= default) {
            tinyDB.putBoolean("overBreakTime", true)
            if (binding!!.StateActive.isVisible) {
                binding.breakBar.progressBarColor =
                    Color.parseColor("#FFD6D9")//change to light red
            } else {
                binding.breakBar.progressBarColor = Color.parseColor("#FF4D4E")
            }


            val cycle = time / default
            val value = cycle.toInt() * default
            val progress = time - value
            binding.breakBar.progress = progress.toFloat()


        } else {
            tinyDB.putInt("BREAKBARPROGRESS", 0)
            tinyDB.putBoolean("overBreakTime", false)
            //  binding!!.breakBar?.progressBarColor = Color.parseColor("#FFA023") //change to orange
            binding!!.breakBar.progress = time.toFloat()
        }


    }

    //----------------------------------------------UI Section --------------------
    /**
     * These functions can handle ui functionalites
     */

    @SuppressLint("SimpleDateFormat")
    private fun updateSomeUiComponents() {
        val DefaultTOShow = MyApplication.TotalTime * 60
        val time = getTimeStringFromDouble(DefaultTOShow)
        dataBinding!!.maxTimer.text = time
        val sdf = SimpleDateFormat("dd MMM")
        val currentDate = sdf.format(Date())
        dataBinding!!.date.text = currentDate
        dataBinding?.bar?.progressBarColor = Color.parseColor(ResendApis.primaryColor)
        val Choice = tinyDB.getString("selectedState")

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
                    autoTimerStart()
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

    fun initWorkBar() {
        dataBinding!!.bar.apply {
            var default = tinyDB.getInt("defaultWork")
            dataBinding?.bar?.progressBarColor = Color.parseColor(ResendApis.primaryColor)
            default = default * 60
            val maxTime = default
            progressMax = maxTime.toFloat()
            roundBorder = true
            progressDirection = CircularProgressBar.ProgressDirection.TO_RIGHT
        }

    }

    fun initBreakBar() {
        dataBinding!!.breakBar.apply {

//            setProgressWithAnimation(50f, 1000) // =1s
            var default = tinyDB.getInt("defaultBreak")

            default *= 60
            val maxBreakTime = default

            // Set Progress Max
            progressMax = maxBreakTime.toFloat()

            dataBinding?.breakBar?.progressBarColor = Color.parseColor("#FFD6D9")

            backgroundProgressBarColor = Color.TRANSPARENT


            roundBorder = true
            progressDirection = CircularProgressBar.ProgressDirection.TO_RIGHT
        }


    }

    fun clickListnersForActivityButtons() {
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

    fun breakErrorOverWrite() {
        val ref = (activityContext as MainActivity)
        val breakTimerService = ref.isMyServiceRunning(BreakTimerService::class.java)
        if (dataBinding!!.StateActive.isVisible) {
            if (breakTimerService) {
                ref.stopTimerBreak()
                breakTimerLargeToSmall()
                workTimerSmallToLarge()

            }

        }

    }

    fun overtimeBarColor(breakTimerService: Boolean) {
        if (dataBinding!!.secondState.isVisible) {
            val overTime = tinyDB.getBoolean("overTime")
            val overTimeBreak = tinyDB.getBoolean("overBreakTime")
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
                            if (!breakTimerService) {
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

    fun checkBreakBaColor() {
        val ref = (activityContext as MainActivity)
        val timerServiceCheck = ref.isMyServiceRunning(TimerService::class.java)
        val breakTimerService = ref.isMyServiceRunning(BreakTimerService::class.java)

        if (breakTimerService) {
            dataBinding?.breakBar?.progressBarColor =
                Color.parseColor("#FFA023")   //change to orange
        } else {
            dataBinding?.breakBar?.progressBarColor = Color.parseColor("#FFD297")
        }
        if (!timerServiceCheck && !breakTimerService) {
            ref.startTimer()
            ref.startTimerBreak()

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

    /**
     * This function restart timers and Button
     */

    @SuppressLint("SimpleDateFormat")
    private fun startDaySetter(intent: MainActivity) {
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val currentDate = sdf.format(Date())
        var workDate = tinyDB.getString("ActivityDate")
        if (workDate!!.isNotEmpty()) {
            workDate = workDate.split(":").toTypedArray()[0]
            Log.d("workDate", "date is $workDate")
            Log.d("workDatecurrent", "current Date $currentDate")
        }


        tinyDB.putInt("lasttimebreak", 0)
        tinyDB.putInt("lasttimework", 0)
        intent.time = 0.0

    }

    private fun barColor() {
        viewModelScope.launch {
            withContext(Dispatchers.Main) {
                fadeColor()
            }

        }
    }


    @SuppressLint("SimpleDateFormat")
    fun setDate() {
        val sdf = SimpleDateFormat("EEEE")
        val d = Date()
        val dayOfTheWeek = sdf.format(d)
        dataBinding?.day?.text = dayOfTheWeek
    }


    //---------------------------------------------------Utils----------------------------

    /**
     * This function can change the color of buttons by server
     */
    private fun changeBackgroundColor() {
        dataBinding!!.apply {
            cardColor.setCardBackgroundColor(Color.parseColor(ResendApis.primaryColor))
            arrowdownbg.setBackgroundColor(Color.parseColor(ResendApis.primaryColor))
            arrowbg.setBackgroundColor(Color.parseColor(ResendApis.primaryColor))
            iconCarbg.setBackgroundColor(Color.parseColor(ResendApis.primaryColor))
        }

    }

    fun tagsForToast() {
        val language = tinyDB.getString("language")
        if (language == "0") {
            TAG2 = "Comprueba tu conexión a Internet"

        } else if (language == "1") {


            TAG2 = "Check Your Internet Connection"
        } else {

            TAG2 = "Verifique a sua conexão com a internet"
        }

    }

    private fun checkByServer() {
        val check = tinyDB.getInt("selectedStateByServer")
        if (check == 1) {
            val language = tinyDB.getString("language")
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

    fun fadeColor() {
        var color = ResendApis.primaryColor.substringAfter("#")
        color = "#99$color"
        dataBinding?.bar?.progressBarColor = Color.parseColor(color)
        Log.d("FadeColor ", color)
    }

    fun checkAndSetValuesOnMainScreen() {
        val check = tinyDB.getInt("lastVehicleid")
        if (check != 0) {
            dataBinding?.initialState?.setVisibility(View.GONE)
            dataBinding?.secondState?.setVisibility(View.VISIBLE)
        }

        if (dataBinding?.StateActive?.isVisible == true) {
            dataBinding?.vehicleListBtn?.isClickable = false
        }
    }

    fun checkNetConnection() {
        if (CheckConnection.netCheck(activityContext!!)) {
            Log.d("StatusTesting", "IN FUNCTION HOME VIEW MODEL LINE 1311")
            if (!(activityContext as MainActivity).isMyServiceRunning(LoadingScreen::class.java)) {
                ContextCompat.startActivity(
                    activityContext!!,
                    loadingIntent,
                    Bundle.EMPTY
                )
//                viewModelScope.launch(Dispatchers.IO) {
//                    if (!mainRepository.isExistsUnsentUploadActivityDB()) {
//                        withContext(Dispatchers.Main) {
//                            ContextCompat.startActivity(
//                                activityContext!!,
//                                loadingIntent,
//                                Bundle.EMPTY
//                            )
//                        }
//
//                    }
//
//
//                }


            }
        }


    }


    fun openPopup(networkAlertDialog: AlertDialog, PopupView: View, resources: Resources) {
        networkAlertDialog.setView(PopupView)
        try {
            if (!networkAlertDialog.isShowing) {
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

    private suspend fun showToast() {
        withContext(Dispatchers.Main) {
            (MyApplication.loadingContext as LoadingScreen).finish()
            Toast.makeText(
                activityContext,
                (activityContext as MainActivity).TAG2,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Formaters
     */

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

    fun base64ToBitmap(base64: String) {
        val imageBytes = Base64.decode(base64, 0)
        val image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        dataBinding?.profileImage?.setImageBitmap(image)
        println("imageprinted")

    }

}