package com.example.marcadechoferes.Extra

import android.widget.Toast

import android.content.Intent

import android.content.BroadcastReceiver
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Looper
import android.util.Log
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


class MyBroadastReceivers : BroadcastReceiver() {
    var tinyDB:TinyDB = TinyDB(MyApplication.appContext)
    var test = 1
    var latitude=0.0
    var longitude=0.0
    var job =Job()
    companion object{
        var activiy =0
         var time = 0
        lateinit var authRepository: AuthRepository
    }
    override fun onReceive(arg0: Context?, arg1: Intent?) {
        test = test.plus(1)
        if(test==3){
   CoroutineScope(job).launch {
    withContext(Dispatchers.IO){
        if(CheckConnection.netCheck(arg0!!)){
            getLocation(arg0!!)
        }else{

        }

                    time += 120
                    Log.d("MyTickerReceiver","Received")
                    test = 1
    }


     }


//



        }

    }

    fun updateActivity(
        authRepository: AuthRepository, context: Context
    ) {
        val childJob =Job(job)
        CoroutineScope(childJob).launch {

            withContext(Dispatchers.IO) {

                var vehicle = tinyDB.getObject("VehicleForBackgroundPush", Vehicle::class.java)
                var geoPosition= tinyDB.getObject("GeoPosition", GeoPosition::class.java)
                val sdf = SimpleDateFormat("yyyy-M-dd:hh:mm:ss")
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
                        tinyDB.putObject("GeoPosition",geoPosition)
                        updateActivity(authRepository,context)

                    }
                }


            }, Looper.getMainLooper())

        // uploadLocation()
        println("Current Location $longitude and $latitude")
    }
}