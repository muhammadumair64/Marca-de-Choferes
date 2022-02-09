package com.logicasur.appchoferes.auth.createpassword.viewModel

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.logicasur.appchoferes.Extra.K
import com.logicasur.appchoferes.Extra.TinyDB
import com.logicasur.appchoferes.Extra.serverCheck.ServerCheck
import com.logicasur.appchoferes.loadingScreen.LoadingScreen
import com.logicasur.appchoferes.R
import com.logicasur.appchoferes.auth.createpassword.CreateNewPasswordScreen
import com.logicasur.appchoferes.auth.repository.AuthRepository
import com.logicasur.appchoferes.databinding.ActivityCreateNewPasswordScreenBinding
import com.logicasur.appchoferes.myApplication.MyApplication
import com.logicasur.appchoferes.network.ApiException
import com.logicasur.appchoferes.network.NoInternetException
import com.logicasur.appchoferes.network.ResponseException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import java.net.SocketException


@HiltViewModel
class CreatePasswordViewModel @Inject constructor(val authRepository: AuthRepository) :
    ViewModel() {
    var TAG1=""

    var TAG2=""
    var activityContext: Context? = null
    lateinit var tinyDB: TinyDB
    var Token = ""
    fun viewsForCreatePassword(context: Context, binding: ActivityCreateNewPasswordScreenBinding) {
        activityContext = context
        tinyDB = TinyDB(MyApplication.appContext)
        Token = tinyDB.getString("Cookie").toString()
         binding.arrowBack.setBackgroundColor(Color.parseColor(K.primaryColor))
        var language= tinyDB.getString("language")
        if (language=="0"){
            TAG1 ="Contraseña demasiado corta"
            TAG2 = "La contraseña no coincide"

        }else if(language=="1"){

           TAG1= "Too Short Password"
            TAG2 ="Password not Match"
        }
        else{
             TAG1="Senha muito curta"
            TAG2="A senha não coincide"
        }

        binding.backButton.setOnClickListener {

            (context as CreateNewPasswordScreen).finish()
        }

        binding.showPassBtn.setOnClickListener {
            if (binding.editPassword.transformationMethod.equals(PasswordTransformationMethod.getInstance())) {
                binding.editPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance())
                binding.editPassword.setSelection(binding.editPassword.getText().length);
                binding.showPassBtn.setImageResource(R.drawable.hide_password)
            } else {
                binding.editPassword.setTransformationMethod(PasswordTransformationMethod.getInstance())
                binding.editPassword.setSelection(binding.editPassword.getText().length)
                binding.showPassBtn.setImageResource(R.drawable.ic_icon_visibility)
            }
        }

        binding.showRepeatPassBtn.setOnClickListener {

            if (binding.repeatPassword.transformationMethod.equals(PasswordTransformationMethod.getInstance())) {
                binding.repeatPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance())
                binding.repeatPassword.setSelection(binding.repeatPassword.getText().length)
                binding.showRepeatPassBtn.setImageResource(R.drawable.hide_password)
            } else {
                binding.repeatPassword.setTransformationMethod(PasswordTransformationMethod.getInstance())
                binding.repeatPassword.setSelection(binding.repeatPassword.getText().length)
                binding.showRepeatPassBtn.setImageResource(R.drawable.ic_icon_visibility)
            }

        }

        binding.SubmitBtn.setOnClickListener {
            binding.apply {
                var password:String = editPassword.text.toString()
                var repeatPassword:String = repeatPassword.text.toString()
                println("passwords here $password   $repeatPassword")
                if (password.equals(repeatPassword)) {
                    if (editPassword.text.length >= 4) {
                        var passsword = editPassword.text
                        viewModelScope.launch(Dispatchers.IO) {
                            ServerCheck.serverCheck {CreateNewPassword(passsword.toString())}
                        }
//                        CreateNewPassword(passsword.toString())
                        var intent = Intent(context, LoadingScreen::class.java)
                        ContextCompat.startActivity(context, intent, Bundle.EMPTY)
                        (context as CreateNewPasswordScreen).closeKeyboard()


                    } else {
                        Toast.makeText(activityContext, TAG1, Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                         Toast.makeText(activityContext, TAG2, Toast.LENGTH_SHORT).show()

                }


            }


        }


    }


    fun CreateNewPassword(password: String) {
        viewModelScope.launch {

            withContext(Dispatchers.IO) {

                try {

                    val response = authRepository.CreateNewPasswordPassword(password, Token)

                    println("SuccessResponse $response")


                    if (response != null) {
                        Log.d("stack clear ","Stack IS Cleared")
                        withContext(Dispatchers.Main){
                            (activityContext as CreateNewPasswordScreen).moveToNext()
                        }

                    }

                } catch (e: ResponseException) {
                    (activityContext as CreateNewPasswordScreen).finish()
                    println("ErrorResponse")
                    var intent = Intent(activityContext, CreateNewPasswordScreen::class.java)
                    ContextCompat.startActivity(activityContext!!, intent, Bundle.EMPTY)

                } catch (e: ApiException) {
                    e.printStackTrace()
                } catch (e: NoInternetException) {
                    println("position 2")
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            activityContext,
                            "Comprueba tu conexión a Internet",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                catch(e: SocketException){
                    LoadingScreen.onEndLoadingCallbacks?.endLoading()
                    Log.d("connection Exception","Connect Not Available")
                    withContext(Dispatchers.Main){
                        Toast.makeText(activityContext, "Comprueba tu conexión a Internet", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }


    }


}