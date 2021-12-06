package com.example.marcadechoferes.auth.signin.viewModel

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Patterns
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.marcadechoferes.Extra.NavigatorC
import com.example.marcadechoferes.Extra.TinyDB
import com.example.marcadechoferes.R
import com.example.marcadechoferes.auth.forgotPassword.ForgotPasswordActivity
import com.example.marcadechoferes.auth.otp.OTP_Activity
import com.example.marcadechoferes.auth.repository.AuthRepository
import com.example.marcadechoferes.auth.signin.SignInActivity
import com.example.marcadechoferes.databinding.ActivitySignInBinding
import com.example.marcadechoferes.loadingScreen.LoadingScreen
import com.example.marcadechoferes.mainscreen.MainActivity
import com.example.marcadechoferes.network.ResponseException
import com.example.marcadechoferes.network.signinResponse.SigninResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.Reader
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.schedule

@HiltViewModel
class SigninViewModel @Inject constructor(val authRepository: AuthRepository) : ViewModel() {
    var activityContext: Context? = null
    lateinit var tinyDB: TinyDB


    fun viewsOfActivitySignin(context: Context, binding: ActivitySignInBinding) {
        tinyDB= TinyDB(context)

        activityContext = context
        binding.apply {
            showPassBtn.setOnClickListener {
                if (editPassword.transformationMethod.equals(PasswordTransformationMethod.getInstance())) {
                    editPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance())
                    showPassBtn.setImageResource(R.drawable.hide_password)
                } else {
                    editPassword.setTransformationMethod(PasswordTransformationMethod.getInstance())
                    showPassBtn.setImageResource(R.drawable.ic_icon_visibility)
                }


            }

            forgotPassword.setOnClickListener {
                var intent = Intent(context, ForgotPasswordActivity::class.java)
                ContextCompat.startActivity(context, intent, Bundle.EMPTY)

            }

            signInBtn.setOnClickListener {
                val emailCheck: String = email.text.toString()
                val passwordCheck= editPassword.text.toString()
                val validater= emailCheck.isValidEmail()

                if(validater==true && passwordCheck.length>=4){

                    signinAuth(emailCheck,passwordCheck)
                        var intent = Intent(activityContext,LoadingScreen::class.java)
                        ContextCompat.startActivity(activityContext!!, intent, Bundle.EMPTY)



                }else if(validater==false){
                    Toast.makeText(activityContext, "Invalid Email", Toast.LENGTH_SHORT).show()

                }else{
                    Toast.makeText(activityContext, "Invalid password", Toast.LENGTH_SHORT).show()

                }

            }


        }
    }


    fun signinAuth(userName:String,userPassword:String) {
        val iPath: File = Environment.getDataDirectory()
        val iStat = StatFs(iPath.path)
        val iBlockSize = iStat.blockSizeLong
        val iAvailableBlocks = iStat.availableBlocksLong
        val iTotalBlocks = iStat.blockCountLong
        val iAvailableSpace = formatSize(iAvailableBlocks * iBlockSize)
        val iTotalSpace = formatSize(iTotalBlocks * iBlockSize)
        var unUsed =(iTotalBlocks * iBlockSize)-(iAvailableBlocks * iBlockSize)
        val usedSpace= formatSize(unUsed)





        var name = userName
        var password = userPassword
        var idApp: String? = "1234"
        var memUsed: String? = usedSpace
        var diskFree: String? = iAvailableSpace
        var diskTotal: String? =iTotalSpace
        var model: String? = Build.MODEL
        var operatingSystem: String? = "android"
        var osVersion: String? = "10"
        var appVersion: String? = "1.1.0"
        var appBuild: String? = "198"
        var platform: String? = "Android"
        var manufacturer: String? = Build.MANUFACTURER
        var uuid: String? = Settings.Secure.getString(
            activityContext?.getContentResolver(),
            Settings.Secure.ANDROID_ID
        )

        var isVirtual: String? = "false"




        viewModelScope.launch {

            withContext(Dispatchers.IO) {
                try {
                    val response =
                        authRepository.userSignin(
                            name, password, idApp!!,
                            memUsed!!,
                            diskFree!!,
                            diskTotal!!,
                            model!!,
                            operatingSystem!!,
                            osVersion!!,
                            appVersion!!,
                            appBuild!!,
                            platform!!,
                            manufacturer!!,
                            uuid!!,
                            isVirtual!!
                        )

                    println("SuccessResponse $response")

                    authRepository.InsertSigninData(response)
                    if(response!=null) {
                        tinyDB.putString("User",userName)

                        var intent = Intent(activityContext,MainActivity::class.java)
                        ContextCompat.startActivity(activityContext!!, intent, Bundle.EMPTY)
                        (activityContext as SignInActivity).finish()

//                        Timer().schedule(5000) {
//                                  getAvatar()
//                        }


                    }
                } catch (e: ResponseException) {
                    val response = convertErrorBody(e.response)
                    println("ErrorResponse $response")
                }
            }
        }
    }

    fun convertErrorBody(responseString: Reader?): SigninResponse {
        val gson = Gson()
        val type = object : TypeToken<SigninResponse>() {}.type
        val errorResponse: SigninResponse? = gson.fromJson(responseString, type)
        return errorResponse!!
    }

    private fun formatSize(size: Long): String? {
        println("orignal size $size")
        var size = size
        var suffix: String? = null
        if (size >= 1024) {
            suffix = "KB"
            size /= 1024
            if (size >= 1024) {
                suffix = "MB"
                size /= 1024
                if (size >= 1024) {
                    suffix = "GB"
                    size /= 1024


                }
            }
        }
        val resultBuffer = StringBuilder(java.lang.Long.toString(size))
        var commaOffset = resultBuffer.length - 3
        while (commaOffset > 0) {
            resultBuffer.insert(commaOffset, ',')
            commaOffset -= 3
        }
        if (suffix != null) resultBuffer.append(suffix)

        return resultBuffer.toString()


    }

    fun String.isValidEmail(): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(this).matches()
    }


    fun getAvatar(){
        viewModelScope.launch {

            withContext(Dispatchers.IO) {

                try {
                   var user=tinyDB.getString("User")

                    val response = authRepository.getUserAvatar()

                    println("SuccessResponse $response")



                    if(response!=null) {

                        tinyDB.putString("Avatar",response.avatar)



                    }

                } catch (e: ResponseException) {
                    println("ErrorResponse")

                }
            }
        }


    }

}



