package com.example.marcadechoferes.auth.createpassword.viewModel

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.marcadechoferes.Extra.TinyDB
import com.example.marcadechoferes.loadingScreen.LoadingScreen
import com.example.marcadechoferes.R
import com.example.marcadechoferes.auth.createpassword.CreateNewPasswordScreen
import com.example.marcadechoferes.auth.repository.AuthRepository
import com.example.marcadechoferes.databinding.ActivityCreateNewPasswordScreenBinding
import com.example.marcadechoferes.mainscreen.MainActivity
import com.example.marcadechoferes.myApplication.MyApplication
import com.example.marcadechoferes.network.ApiException
import com.example.marcadechoferes.network.NoInternetException
import com.example.marcadechoferes.network.ResponseException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import androidx.core.content.ContextCompat.startActivity




@HiltViewModel
class CreatePasswordViewModel @Inject constructor(val authRepository: AuthRepository) :
    ViewModel() {
    var activityContext: Context? = null
    lateinit var tinyDB: TinyDB
    var Token = ""
    fun viewsForCreatePassword(context: Context, binding: ActivityCreateNewPasswordScreenBinding) {
        activityContext = context
        tinyDB = TinyDB(MyApplication.appContext)
        Token = tinyDB.getString("Cookie").toString()

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
                var password = editPassword.text
                var repeatPassword = repeatPassword.text
                println("passwords here $password   $repeatPassword")
                if (password.trim() == repeatPassword.trim()) {
                    if (editPassword.text.length >= 4) {
                        var passsword = editPassword.text
                        CreateNewPassword(passsword.toString())
                        var intent = Intent(context, LoadingScreen::class.java)
                        ContextCompat.startActivity(context, intent, Bundle.EMPTY)
                        (context as CreateNewPasswordScreen).closeKeyboard()


                    } else {
                        Toast.makeText(activityContext, "Too Short Password", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Toast.makeText(activityContext, "Password not match", Toast.LENGTH_SHORT).show()

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
                            "Check Your Internet Connection",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }


    }


}