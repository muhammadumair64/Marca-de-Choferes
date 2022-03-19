package com.logicasur.appchoferes.beforeAuth.createPasswordScreen.viewModel

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.logicasur.appchoferes.utils.ResendApis
import com.logicasur.appchoferes.Extra.TinyDB
import com.logicasur.appchoferes.common.loadingScreen.LoadingScreen
import com.logicasur.appchoferes.R
import com.logicasur.appchoferes.beforeAuth.createPasswordScreen.CreateNewPasswordScreen
import com.logicasur.appchoferes.data.repository.AuthRepository
import com.logicasur.appchoferes.databinding.ActivityCreateNewPasswordScreenBinding
import com.logicasur.appchoferes.utils.myApplication.MyApplication
import com.logicasur.appchoferes.data.network.ApiException
import com.logicasur.appchoferes.data.network.NoInternetException
import com.logicasur.appchoferes.data.network.ResponseException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception
import javax.inject.Inject
import java.net.SocketException


@HiltViewModel
class CreatePasswordViewModel @Inject constructor(
    val authRepository: AuthRepository,
    val resendApis: ResendApis
) :
    ViewModel() {
    var TAG1 = ""

    var TAG2 = ""
    var activityContext: Context? = null
    lateinit var tinyDB: TinyDB
    var Token = ""
    fun viewsForCreatePassword(context: Context, binding: ActivityCreateNewPasswordScreenBinding) {
        activityContext = context
        tinyDB = TinyDB(MyApplication.appContext)
        Token = tinyDB.getString("Cookie").toString()
        binding.arrowBack.setBackgroundColor(Color.parseColor(ResendApis.primaryColor))


        tagForToast()
        binding.backButton.setOnClickListener {

            (context as CreateNewPasswordScreen).finish()
        }

        binding.showPassBtn.setOnClickListener {
            hideOrUnhidePassword(binding.showPassBtn, binding.editPassword)
        }

        binding.showRepeatPassBtn.setOnClickListener {
            hideOrUnhidePassword(binding.showRepeatPassBtn, binding.repeatPassword)
        }

        binding.SubmitBtn.setOnClickListener {
            binding.apply {
                checkPasswordAndHitApi(
                    editPassword.text.toString(),
                    repeatPassword.text.toString(),
                    binding
                )

            }


        }


    }


    fun CreateNewPassword(password: String, action: () -> Unit) {
        viewModelScope.launch {

            withContext(Dispatchers.IO) {

                try {

                    val response = authRepository.CreateNewPasswordPassword(password, Token)

                    println("SuccessResponse $response")


                    if (response != null) {
                        action()
                        Log.d("stack clear ", "Stack IS Cleared")
                        withContext(Dispatchers.Main) {
                            (activityContext as CreateNewPasswordScreen).moveToNext()
                        }

                    }

                } catch (e: ResponseException) {
                    (activityContext as CreateNewPasswordScreen).finish()
                    println("ErrorResponse")
                    val intent = Intent(activityContext, CreateNewPasswordScreen::class.java)
                    ContextCompat.startActivity(activityContext!!, intent, Bundle.EMPTY)

                } catch (e: ApiException) {
                    e.printStackTrace()
                } catch (e: NoInternetException) {
                    println("position 2")
                    e.printStackTrace()
                    showToast()
                } catch (e: SocketException) {
                    LoadingScreen.OnEndLoadingCallbacks?.endLoading()
                    showToast()
                } catch (e: Exception) {
                    Log.d("connection Exception", "Connect Not Available")
                }
            }
        }


    }


    //----------------------------------------utils----------------------------------------------------
    private fun tagForToast() {
        val language = tinyDB.getString("language")
        if (language == "0") {
            TAG1 = "Contraseña demasiado corta"
            TAG2 = "La contraseña no coincide"

        } else if (language == "1") {

            TAG1 = "Too Short Password"
            TAG2 = "Password not Match"
        } else {
            TAG1 = "Senha muito curta"
            TAG2 = "A senha não coincide"
        }
    }

    private suspend fun showToast() {
        withContext(Dispatchers.Main) {
            Toast.makeText(activityContext, "Comprueba tu conexión a Internet", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun checkPasswordAndHitApi(
        password: String,
        repeatPassword: String,
        binding: ActivityCreateNewPasswordScreenBinding
    ) {

        if (password == repeatPassword) {
            if (binding.editPassword.text.length >= 4) {
                val passsword = binding.editPassword.text
                viewModelScope.launch(Dispatchers.IO) {
                    resendApis.serverCheck.serverCheckMainActivityApi { serverAction ->
                        CreateNewPassword(passsword.toString()) { serverAction() }
                    }
                }

                moveToLoadingScreen()


            } else {
                Toast.makeText(activityContext, TAG1, Toast.LENGTH_SHORT)
                    .show()
            }
        } else {
            Toast.makeText(activityContext, TAG2, Toast.LENGTH_SHORT).show()

        }
    }

    private fun moveToLoadingScreen() {
        val intent = Intent(activityContext, LoadingScreen::class.java)
        ContextCompat.startActivity(activityContext!!, intent, Bundle.EMPTY)
        (activityContext as CreateNewPasswordScreen).closeKeyboard()
    }

    private fun hideOrUnhidePassword(iconImage: ImageView, editText: EditText) {
        hideBehaviourOnET(editText)
        if (editText.transformationMethod.equals(PasswordTransformationMethod.getInstance())) {
            iconImage.setImageResource(R.drawable.hide_password)
        } else {
            iconImage.setImageResource(R.drawable.ic_icon_visibility)
        }
    }

    private fun hideBehaviourOnET(editText: EditText) {
        editText.transformationMethod = HideReturnsTransformationMethod.getInstance()
        editText.setSelection(editText.text.length)
    }
}