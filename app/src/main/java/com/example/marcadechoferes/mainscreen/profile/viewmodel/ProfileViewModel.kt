package com.example.marcadechoferes.mainscreen.profile.viewmodel

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.marcadechoferes.Extra.TinyDB
import com.example.marcadechoferes.auth.createpassword.CreateNewPasswordScreen
import com.example.marcadechoferes.auth.repository.AuthRepository
import com.example.marcadechoferes.auth.signin.SignInActivity
import com.example.marcadechoferes.databinding.FragmentProfileBinding
import com.example.marcadechoferes.loadingScreen.LoadingScreen
import com.example.marcadechoferes.mainscreen.MainActivity
import com.example.marcadechoferes.network.ResponseException
import com.example.marcadechoferes.network.signinResponse.Profile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import android.graphics.Bitmap

import android.app.AlertDialog
import android.widget.Toast
import com.example.marcadechoferes.myApplication.MyApplication
import com.example.marcadechoferes.network.ApiException
import com.example.marcadechoferes.network.NoInternetException
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*


@HiltViewModel
class ProfileViewModel @Inject constructor(val authRepository: AuthRepository) : ViewModel() {
    var activityContext: Context? = null
    lateinit var tinyDB: TinyDB
    lateinit var imageInBitmap:Bitmap
    lateinit var dataBinding: FragmentProfileBinding
    fun viewsForFragment(context: Context, binding: FragmentProfileBinding) {
        activityContext = context
        dataBinding = binding
        tinyDB = TinyDB(context)
        val sdf = SimpleDateFormat("dd MMM")
        val currentDate = sdf.format(Date())
        System.out.println(" C DATE is  "+currentDate)
        binding.date.text="$currentDate"
        getProfile()
        setDay()
        var image=tinyDB.getString("Avatar")
        Base64ToBitmap(image.toString())


        binding.apply {


            edit.setOnClickListener {

                var intent = Intent(context, CreateNewPasswordScreen::class.java)
                ContextCompat.startActivity(context, intent, Bundle.EMPTY)
            }


            Logout.setOnClickListener {
                logoutUser()
                var intent = Intent(context, LoadingScreen::class.java)
                ContextCompat.startActivity(context, intent, Bundle.EMPTY)
            }
        }


    }

    fun logoutUser() {

        viewModelScope.launch {

            withContext(Dispatchers.IO) {
                try {
                    val name = tinyDB.getString("User")
                    val response =
                        authRepository.logoutUser(name!!)

                    println("SuccessResponse $response")

                    if (response != null) {
                        authRepository.clearData()
                        tinyDB.clear()
                        var intent = Intent(activityContext, SignInActivity::class.java)
                        ContextCompat.startActivity(activityContext!!, intent, Bundle.EMPTY)
                        (activityContext as MainActivity).finish()
                    }

                } catch (e: ResponseException) {
                    println("logout Failed $e")
                }
            }
        }


    }

    fun getProfile() {

        viewModelScope.launch {
            withContext(Dispatchers.IO) {

                var profile = authRepository.getProfile()
                dataBinding.apply {
                    Name.text = profile.name
                    surName.text = profile.surname
                    TitleName.text = profile.name
                    FatherName.text = profile.surname
                    Email.text = tinyDB.getString("User")

                }
                println("user personal data $profile")
            }

        }
    }

    fun Base64ToBitmap(base64: String) {
        val imageBytes = Base64.decode(base64, 0)
        val image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        dataBinding.profileImage.setImageBitmap(image)
        println("imageprinted")

    }

    fun bitmapToBase64(bitmapImage:Bitmap){
        imageInBitmap=bitmapImage
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray: ByteArray = byteArrayOutputStream.toByteArray()
        val encoded = Base64.encodeToString(byteArray, Base64.DEFAULT)
        println("encoded $encoded")
            dataBinding.profileImage.setImageBitmap(imageInBitmap)


         updateAvatar(encoded)
        var intent = Intent(activityContext, LoadingScreen::class.java)
        ContextCompat.startActivity(activityContext!!, intent, Bundle.EMPTY)
    }

    fun updateAvatar(avatar:String){
        var Token=tinyDB.getString("Cookie")
        viewModelScope.launch {

            withContext(Dispatchers.IO) {

                try {


                    val response = authRepository.updateAvatar(avatar,Token!!)

                    println("SuccessResponse $response")



                    if(response!=null) {
                        tinyDB.putString("Avatar",avatar)
                        (MyApplication.loadingContext as LoadingScreen).finish()
                    }

                } catch (e: ResponseException) {
                    (MyApplication.loadingContext as LoadingScreen).finish()
                    println("ErrorResponse")

                } catch (e: ApiException) {
                    e.printStackTrace()
                }
                catch (e: NoInternetException) {
                    println("position 2")
                    e.printStackTrace()
                    withContext(Dispatchers.Main){
                        Toast.makeText(activityContext, "Check Your Internet Connection", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }


    }

    fun updateProfile(name:String,surname:String,alertDialog: AlertDialog){
        var Token=tinyDB.getString("Cookie")

        var intent = Intent(activityContext, LoadingScreen::class.java)
        ContextCompat.startActivity(activityContext!!, intent, Bundle.EMPTY)
        viewModelScope.launch {

            withContext(Dispatchers.IO) {

                try {


                    val response = authRepository.updateProfile(name,surname,Token!!)

                    println("SuccessResponse $response")



                    if(response!=null) {
                        dataBinding.TitleName.text=name
                        dataBinding.FatherName.text=surname

                        updateProfileLocal(name,surname)
                        (MyApplication.loadingContext as LoadingScreen).finish()
                        alertDialog.dismiss()
                    }

                } catch (e: ResponseException) {
                    (MyApplication.loadingContext as LoadingScreen).finish()
                    println("ErrorResponse")
                } catch (e: ApiException) {
                    e.printStackTrace()
                }
                catch (e: NoInternetException) {
                    println("position 2")
                    e.printStackTrace()
                    withContext(Dispatchers.Main){
                        Toast.makeText(activityContext, "Check Your Internet Connection", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }


    }

    fun updateProfileLocal(name:String,fatherName:String){

        viewModelScope.launch {
            withContext(Dispatchers.IO) {

                authRepository.clearProfile()
                var language=tinyDB.getString("language")
                var notify=tinyDB.getBoolean("notify")
                var email =tinyDB.getString("User")
                var profileData=Profile(0,"",language?.toInt(),name,notify,fatherName)
                authRepository.insetProfile(profileData)
                var profile=authRepository.getProfile()

                withContext(Dispatchers.Main){
                    dataBinding.apply {
                        Name.text = profile.name
                        surName.text = profile.surname
                        TitleName.text = profile.name
                        FatherName.text = profile.surname
                        Email.text = tinyDB.getString("User")
                    }
                }

                println("user personal data $profile")
            }

        }




    }

    fun setDay(){
        val sdf = SimpleDateFormat("EEEE")
        val d = Date()
        val dayOfTheWeek = sdf.format(d)
        dataBinding?.day?.text="$dayOfTheWeek"
    }


}