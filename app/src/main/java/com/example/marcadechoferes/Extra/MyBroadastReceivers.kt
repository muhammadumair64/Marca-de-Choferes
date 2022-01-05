package com.example.marcadechoferes.Extra

import android.app.ActivityManager
import android.widget.Toast

import android.content.Intent

import android.content.BroadcastReceiver
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.example.marcadechoferes.auth.repository.AuthRepository
import com.example.marcadechoferes.mainscreen.home.timerServices.UploadRemaingDataService
import com.example.marcadechoferes.myApplication.MyApplication
import com.example.marcadechoferes.network.ApiException
import com.example.marcadechoferes.network.GeoPosition
import com.example.marcadechoferes.network.NoInternetException
import com.example.marcadechoferes.network.ResponseException
import com.example.marcadechoferes.network.signinResponse.Vehicle
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.*
import java.net.SocketTimeoutException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.log


class MyBroadastReceivers : BroadcastReceiver() {
    var tinyDB: TinyDB = TinyDB(MyApplication.appContext)
    var test = 0
    var latitude = 0.0
    var longitude = 0.0

    companion object {
        lateinit var receivers: MyBroadastReceivers
        var activiy = 0
        var time = 0
        lateinit var authRepository: AuthRepository
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(arg0: Context?, arg1: Intent?) {
        Log.d("MyTickerReceiver", "$test")
        if (test == 1 || test == 0) {

            time += 60
            Log.d("MyTickerReceiver", "Received")
            test = 1
            isAppRunning(arg0!!, "com.example.marcadechoferes")
            CoroutineScope(Job()).launch(Dispatchers.IO) {


//                getLocation(arg0!!)
//                delay(1000)
//                updateActivity(authRepository, arg0!!)

             arg0!!.startForegroundService(
                   UploadRemaingDataService.getStartIntent(
                       time,
                       activiy,
                       authRepository!!,
                       arg0
                   )
               )







            }


        }
        test = test.plus(1)

    }


    fun isAppRunning(context: Context, packageName: String) {
        var check = true
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val procInfos = activityManager.runningAppProcesses
        if (procInfos != null) {
            for (processInfo in procInfos) {
                Log.d("pacckageName", "${processInfo.processName}")
                if (processInfo.processName == packageName) {
                    check = false
                }
            }
        }
        if (check) {
            context.unregisterReceiver(receivers)
            Log.d("mylogic is working", "==MAtch!!")
        }else{

        }

    }


    suspend fun updateActivity(
        authRepository: AuthRepository, context: Context
    ) {
        CoroutineScope(Job()).launch(Dispatchers.IO) {


            var vehicle = tinyDB.getObject("VehicleForBackgroundPush", Vehicle::class.java)
            var geoPosition = tinyDB.getObject("GeoPosition", GeoPosition::class.java)
            val sdf = SimpleDateFormat("yyyy-MM-dd:hh:mm:ss")
            val currentDate = sdf.format(Date())


            try {
                val Token = tinyDB.getString("Cookie")
                val response = authRepository.updateActivity(
                    currentDate,
                    time,
                    activiy,
                    geoPosition,
                    vehicle,
                    Token!!
                )
                println("SuccessResponse $response")


                if (response != null) {

                }

            } catch (e: ResponseException) {

                println("ErrorResponse")
            } catch (e: ApiException) {

                e.printStackTrace()
            } catch (e: NoInternetException) {

                println("position 2")
                e.printStackTrace()
            } catch (e: SocketTimeoutException) {

            }

        }


    }


    suspend fun getLocation(context: Context) {
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
                        tinyDB.putObject("GeoPosition", geoPosition)

                    }
                }


            }, Looper.getMainLooper())

        // uploadLocation()
        println("Current Location $longitude and $latitude")


    }
}