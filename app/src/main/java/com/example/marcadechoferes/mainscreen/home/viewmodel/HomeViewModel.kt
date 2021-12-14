package com.example.marcadechoferes.mainscreen.home.viewmodel

import android.content.Context
import android.view.View
import androidx.lifecycle.ViewModel
import com.example.marcadechoferes.R
import com.example.marcadechoferes.databinding.FragmentHomeBinding
import com.example.marcadechoferes.mainscreen.MainActivity
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import javax.inject.Inject
import android.animation.ValueAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.location.Geocoder
import android.os.Bundle
import android.os.Looper
import android.util.Base64
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.viewModelScope
import com.example.marcadechoferes.Extra.TinyDB
import com.example.marcadechoferes.auth.repository.AuthRepository
import com.example.marcadechoferes.loadingScreen.LoadingScreen
import com.example.marcadechoferes.myApplication.MyApplication
import com.example.marcadechoferes.network.ApiException
import com.example.marcadechoferes.network.GeoPosition
import com.example.marcadechoferes.network.NoInternetException
import com.example.marcadechoferes.network.ResponseException
import com.example.marcadechoferes.network.signinResponse.State
import com.example.marcadechoferes.network.signinResponse.Vehicle
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.internal.Contexts.getApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.SocketTimeoutException
import java.text.SimpleDateFormat
import kotlin.concurrent.schedule


@HiltViewModel
class HomeViewModel @Inject constructor(val authRepository: AuthRepository) : ViewModel() {
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
    var DefaultWork = 0
    var DefaultBreakTime = 0
    var WorkTimeToSend = 0
    var BreakTimeToSend = 0
    var overTime =false

    //activity

    var totalTimeForActivty = 0
    var selectedActivty = 0


    // state

    var positionForState = 0
    fun viewsForHomeFragment(context: Context, binding: FragmentHomeBinding) {
        activityContext = context
        dataBinding = binding
        tinyDB = TinyDB(context)
        getProfile()
        setMaxMini()
        getVehicle()
        getState()
        checkInitial()
        setPreviousWork()
        setDay()
        DefaultWork = tinyDB.getInt("defaultWork")
        println("total Work Given $DefaultWork")
        MyApplication.TotalTime=DefaultWork
        DefaultBreakTime = tinyDB.getInt("defaultBreak")
       MyApplication.TotalBreak=DefaultBreakTime
        var DefaultTOShow = DefaultWork * 60

        binding.maxTimer.text = getTimeStringFromDouble(DefaultTOShow)
        val sdf = SimpleDateFormat("dd MMM")
        val currentDate = sdf.format(Date())
        System.out.println(" C DATE is  " + currentDate)

        binding.date.text = "$currentDate"

        dataBinding?.bar?.progressBarColor = Color.parseColor("#C1B1FF")
        var Choice = tinyDB.getString("selectedState")
        when (Choice) {

            "initialState" -> {
                buttonInitailStaet()
            }
            "goToActiveState" -> {
                goToActivState()

            }
            "goTosecondState" -> {
                goToSecondState()


            }
            "takeBreak" -> {
                buttonTakeBreak()

            }
            "endDay" -> {
                buttonEndDay()

            }


        }
        Timer().schedule(200) {
            forgroundCheck()
        }


    }


    fun Workbar() {
        dataBinding!!.bar.apply {
            var default = tinyDB.getInt("defaultWork")
            dataBinding?.bar?.progressBarColor = Color.parseColor("#7A59FC")
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
        var intent = (activityContext as MainActivity)

        dataBinding?.secondState?.setOnClickListener {
            MyApplication.check=0
            buttonSecondState()
        }
        dataBinding?.TakeBreak?.setOnClickListener {
            MyApplication.check=0
            buttonTakeBreak()
            intent.stopTimer()
            intent.startTimerBreak()
            tinyDB.putString("selectedState", "takeBreak")
            hitActivityAPI(1, BreakTimeToSend)
        }
        dataBinding?.EndDay?.setOnClickListener {
            MyApplication.check=0
            buttonEndDay()
            intent.stopTimer()
            intent.stopTimerBreak()
            tinyDB.putString("selectedState", "endDay")
            hitActivityAPI(3, WorkTimeToSend)
        }
        dataBinding?.initialState?.setOnClickListener {
            MyApplication.check=0
            buttonInitailStaet()
            tinyDB.putString("selectedState", "initialState")
        }


    }

    fun buttonInitailStaet() {
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
        tinyDB.putInt("vehicle", 0)
        tinyDB.putInt("state", 0)
        dataBinding?.bar?.progressBarColor = Color.parseColor("#C1B1FF")
        dataBinding?.breakBar?.progressBarColor = Color.parseColor("#FFD6D9")
//        dataBinding?.breakBar?.progress=0f
//        dataBinding?.bar?.progress=0f
        dataBinding?.StateActive?.setVisibility(View.GONE)
        dataBinding?.vehicleListBtn?.isClickable = true
        dataBinding?.spacer?.setVisibility(View.VISIBLE)
        dataBinding?.secondState?.setVisibility(View.GONE)
        dataBinding?.initialState?.setVisibility(View.VISIBLE)
        dataBinding?.vehicleListBtn?.setBackgroundResource(R.drawable.item_popup_btn_bg)
        dataBinding?.iconCar?.setBackgroundResource(R.drawable.ic_icon_awesome_car_alt)
        dataBinding?.vehicleNameSelected?.setTextColor(Color.parseColor("#000000"))
        dataBinding?.vehicleNameSelected?.text = "Vehículo"
        dataBinding?.Arrow?.setVisibility(View.VISIBLE)
        dataBinding?.dots?.visibility = View.GONE
        (activityContext as MainActivity).time = 0.0
//        dataBinding?.workTimer?.text = "00:00"
//        dataBinding?.TimerBreak?.text = "00:00"
        dataBinding?.statusSelected?.text = "Selección estado"
        dataBinding?.statusListBtn?.visibility = View.GONE
        dataBinding?.vehicleNameSelected?.setTypeface(
            dataBinding?.vehicleNameSelected?.getTypeface(),
            Typeface.NORMAL
        )

    }

    fun buttonTakeBreak() {
        if(overTime==true){
            dataBinding?.bar?.progressBarColor = Color.parseColor("#169DFD")
        }else{
            dataBinding?.bar?.progressBarColor = Color.parseColor("#C1B1FF")
        }


        dataBinding?.breakBar?.progressBarColor = Color.parseColor("#FF4D4E")
        workTimerLargeToSmall()
        breakTimerSmallToLarge()

        dataBinding?.StateActive?.setVisibility(View.GONE)
        dataBinding?.vehicleListBtn?.isClickable = true
        dataBinding?.spacer?.setVisibility(View.VISIBLE)
        dataBinding?.secondState?.text =
            getApplication(MyApplication.appContext).resources.getString(R.string.end_break)
        dataBinding?.secondState?.setVisibility(View.VISIBLE)


    }

    fun buttonSecondState() {
        var intent = (activityContext as MainActivity)
        if (dataBinding?.secondState?.text == "End Break" || dataBinding?.secondState?.text == "Fin del descanso"||dataBinding?.secondState?.text == "Fim do intervalo") {
            goToSecondState()
            intent.stopTimerBreak()
            intent.startTimer()
            hitActivityAPI(2, WorkTimeToSend)
        } else {

            intent.startTimer()
            goToActivState()
            hitActivityAPI(0, WorkTimeToSend)

        }
    }

    fun goToSecondState() {

        dataBinding?.breakBar?.progressBarColor = Color.parseColor("#FFD6D9")
        if(overTime==true){
            dataBinding?.bar?.progressBarColor = Color.parseColor("#169DFD")
        }else{
            dataBinding?.bar?.progressBarColor = Color.parseColor("#7A59FC")
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

        if(overTime==true){
            dataBinding?.bar?.progressBarColor = Color.parseColor("#169DFD")
        }else{
            dataBinding?.bar?.progressBarColor = Color.parseColor("#7A59FC")
        }
        dataBinding?.secondState?.setVisibility(View.GONE)
        dataBinding?.StateActive?.setVisibility(View.VISIBLE)
        dataBinding?.vehicleListBtn?.isClickable = false
        dataBinding?.spacer?.setVisibility(View.GONE)
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
                    surname.text =fatherName!!.split(" ").toTypedArray()[0]
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


    fun workTimerupdater(time: Int, binding: FragmentHomeBinding?) {
        print("${time.toDouble()}")

        var default = MyApplication.TotalTime * 60

        println("default work in timer $DefaultWork")
        WorkTimeToSend = time
        if (time == default) {
            binding!!.bar.progress = 0F
        }else if (time > default){
            println("overTime started ")
            overTime = true
            binding!!.bar.progressBarColor= Color.parseColor("#169DFD")
            var newOvertimer= time-default
            binding!!.bar.progress = newOvertimer.toFloat()
        } else
        {
            dataBinding?.bar?.progressBarColor = Color.parseColor("#7A59FC")
           overTime =false
            binding!!.bar.progress = time.toFloat()
        }
    }

    fun breakTimerupdater(time: Int, binding: FragmentHomeBinding?, mainActivity: MainActivity) {
        print("${time.toDouble()}")

        binding!!.breakBar.progress = time.toFloat()

        var default = MyApplication.TotalBreak * 60
        BreakTimeToSend = time
        if (time == default) {
            mainActivity.stopTimerBreak()
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
                        position
                        tinyDB.putInt("vehicle", position)
                    }
                    position = position.plus(1)
                    searchedArrayList.add("${item.plateNumber} ${item.description}")
                    vehicleArrayListforUpload.add(item)
                }

                withContext(Dispatchers.Main) {
                    var vehicle = tinyDB.getInt("vehicle")
                    if (vehicle != 0) {
                        vehicle = vehicle.minus(1)
                        selectVehicleByLocalDB(vehicle)
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
                        state = state.minus(1)
                        selectState(state)
                    }
                }


            }

        }
    }

    //Get Location
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
                        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                        var location = addresses[0].featureName.toString()
                        println("City Name is $location")
//               Toast.makeText(context, "$location", Toast.LENGTH_SHORT).show()

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
        var text = searchedArrayList[position]
        println("selected text $text")
        dataBinding!!.apply {
            vehicleListBtn.setBackgroundResource(R.drawable.bg_selectedvehicleback)
            iconCar.setBackgroundResource(R.drawable.ic_white_car)
            vehicleNameSelected.setTextColor(Color.WHITE)
            vehicleNameSelected.text = text
            Arrow.visibility = View.GONE
            dots.visibility = View.VISIBLE
            statusListBtn.visibility = View.VISIBLE
            initialState?.setVisibility(View.GONE)
            secondState?.setVisibility(View.VISIBLE)
        }

        forgroundCheck()

    }

    fun selectVehicleByLocalDB(position: Int) {
        var text = searchedArrayList[position]
        println("selected text $text")
        dataBinding!!.apply {
            vehicleListBtn.setBackgroundResource(R.drawable.bg_selectedvehicleback)
            iconCar.setBackgroundResource(R.drawable.ic_white_car)
            vehicleNameSelected.setTextColor(Color.WHITE)
            vehicleNameSelected.text = text
            Arrow.visibility = View.GONE
            dots.visibility = View.VISIBLE
            statusListBtn.visibility = View.VISIBLE
            if (initialState.isVisible) {
                initialState.visibility = View.GONE
            }
            if (dataBinding?.StateActive?.isVisible == false && dataBinding?.initialState?.isVisible == false) {
                dataBinding?.secondState?.visibility= View.VISIBLE
            }

        }

    }

    fun selectState(position: Int) {
        var text = statusArrayList[position]
        dataBinding!!.statusSelected.text = text
    }


    fun updateState(
        datetime: String?,
        totalTime: Int?,
        state: State?,
        geoPosition: GeoPosition?,
        vehicle: Vehicle?
    ) {

        var Token = tinyDB.getString("Cookie")

        var intent = Intent(activityContext, LoadingScreen::class.java)
        ContextCompat.startActivity(activityContext!!, intent, Bundle.EMPTY)
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
                        (MyApplication.loadingContext as LoadingScreen).finish()
                    }

                } catch (e: ResponseException) {
                    (MyApplication.loadingContext as LoadingScreen).finish()
                    println("ErrorResponse")
                } catch (e: ApiException) {
                    e.printStackTrace()
                } catch (e: NoInternetException) {
                    println("position 2")
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            activityContext,
                            "Check Your Internet Connection",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }


    }

    fun hitStateAPI(position: Int) {
        positionForState = position
        getLocationForState(activityContext!!)

    }

    fun uploadState(position: Int, geoPosition: GeoPosition?) {
        val sdf = SimpleDateFormat("yyyy-M-dd:hh:mm:ss")
        val currentDate = sdf.format(Date())
        System.out.println(" C DATE is  " + currentDate)

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                var vehiclePosition = tinyDB.getInt("vehicle")
                var vehicle = vehicleArrayListforUpload[vehiclePosition]
                var status = statusArrayListforUpload[position]
                println("status is $status")
                updateState("$currentDate", WorkTimeToSend, status, geoPosition, vehicle)
            }
        }
    }


    fun updateActivity(
        datetime: String?,
        totalTime: Int?,
        activity: Int?,
        geoPosition: GeoPosition?,
        vehicle: Vehicle?
    ) {

        var Token = tinyDB.getString("Cookie")
        viewModelScope.launch {

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

                    }

                } catch (e: ResponseException) {
                    (MyApplication.loadingContext as LoadingScreen).finish()
                    println("ErrorResponse")
                } catch (e: ApiException) {
                    e.printStackTrace()
                } catch (e: NoInternetException) {
                    println("position 2")
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            activityContext,
                            "Check Your Internet Connection",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                catch (e:SocketTimeoutException){

                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            activityContext,
                            "Check Your Internet Connection",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }


    }

    fun hitActivityAPI(activity: Int, totalTime: Int?) {

        totalTimeForActivty = totalTime!!
        selectedActivty = activity
        getLocation(activityContext!!)

    }


    fun uploadActivity(activity: Int, totalTime: Int?, geoPosition: GeoPosition) {
        val sdf = SimpleDateFormat("yyyy-M-dd:hh:mm:ss")
        val currentDate = sdf.format(Date())
        System.out.println(" C DATE is  " + currentDate)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                var vehiclePosition = tinyDB.getInt("vehicle")
                var vehicle = vehicleArrayListforUpload[vehiclePosition]
                updateActivity("$currentDate", totalTime, activity, geoPosition, vehicle)
            }
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
        val hours = resultIntBreak % 86400 / 3600
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

    fun forgroundCheck(){
        viewModelScope.launch {
            withContext(Dispatchers.Main){

                if (MyApplication.check==200){
                    dataBinding!!.initialState!!.visibility=View.GONE
                    dataBinding!!.StateActive!!.visibility=View.GONE
                    dataBinding!!.secondState.visibility=View.VISIBLE
                }
            }
        }


    }




    fun setPreviousWork(){
        var intent = (activityContext as MainActivity)
        intent.startTimer()
        intent.startTimerBreak()
        var workTime=tinyDB.getInt("lasttimework")
        var breakTime=tinyDB.getInt("lasttimebreak")
        dataBinding?.bar?.progress= workTime.toFloat()
        dataBinding?.breakBar?.progress=breakTime.toFloat()
        dataBinding?.bar?.progressBarColor = Color.parseColor("#C1B1FF")
        dataBinding?.breakBar?.progressBarColor = Color.parseColor("#FFD6D9")
        intent.stopTimerBreak()
        intent.stopTimer()
    }


}