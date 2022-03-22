package com.logicasur.appchoferes.afterAuth.mainscreen.fragments.configuration.viewModel


import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.logicasur.appchoferes.Extra.CheckConnection
import com.logicasur.appchoferes.utils.ResendApis
import com.logicasur.appchoferes.Extra.TinyDB
import com.logicasur.appchoferes.R
import com.logicasur.appchoferes.databinding.FragmentConfigurationBinding
import com.logicasur.appchoferes.common.loadingScreen.LoadingScreen
import com.logicasur.appchoferes.afterAuth.mainscreen.MainActivity
import com.logicasur.appchoferes.data.repository.MainRepository
import com.logicasur.appchoferes.utils.myApplication.MyApplication
import com.logicasur.appchoferes.data.network.ApiException
import com.logicasur.appchoferes.data.network.NoInternetException
import com.logicasur.appchoferes.data.network.ResponseException
import com.logicasur.appchoferes.data.network.logoutResponse.MessageResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.net.SocketException
import javax.inject.Inject


@HiltViewModel
class ConfigurationViewModel @Inject constructor(
    val mainRepository: MainRepository,
    val resendApis: ResendApis,
    val tinyDB: TinyDB
) : ViewModel() {
    var language: Int = 0
    var activityContext: Context? = null
    var dataBinding: FragmentConfigurationBinding? = null

    var TAG = "ConfigurationViewModel"
    @SuppressLint("StaticFieldLeak")
    private lateinit var language0: RelativeLayout
    private lateinit var language1: RelativeLayout
    private lateinit var language2: RelativeLayout

    private lateinit var text0: TextView
    private lateinit var text1: TextView
    private lateinit var text2: TextView

    private lateinit var image0: ImageView
    private lateinit var image1: ImageView
    private lateinit var image2: ImageView
    var TAG2 = ""


    fun viewsForConfigurationFragment(
        context: Context,
        binding: FragmentConfigurationBinding,
        alterDialog: AlertDialog,
        contactPopupView: View
    ) {
        activityContext = context
        dataBinding = binding

        tagsForToast()

        val notify = tinyDB.getBoolean("notify")
        if (notify) {
            dataBinding!!.toggleOFF.visibility = View.GONE
            dataBinding!!.toggleON.visibility = View.VISIBLE
        } else {
            dataBinding!!.toggleOFF.visibility = View.VISIBLE
            dataBinding!!.toggleON.visibility = View.GONE
        }

        language0 = contactPopupView.findViewById(R.id.spanish)
        language2 = contactPopupView.findViewById(R.id.Portuguese)
        language1 = contactPopupView.findViewById(R.id.English)

        text0 = contactPopupView.findViewById(R.id.text0)
        text1 = contactPopupView.findViewById(R.id.text1)
        text2 = contactPopupView.findViewById(R.id.text2)


        image0 = contactPopupView.findViewById(R.id.image0)
        image1 = contactPopupView.findViewById(R.id.image1)
        image2 = contactPopupView.findViewById(R.id.image2)


        selectLanguage()

        language0.setOnClickListener {
            updatingSelectedLanguage(text0, image0)
            updatingUnSelectedLanguage(text1, image1)
            updatingUnSelectedLanguage(text2, image2)
         finishDialog(alterDialog)

            viewModelScope.launch(Dispatchers.IO) {
                resendApis.serverCheck.serverCheckMainActivityApi { serverAction ->
                    selectedLanguageUpload(0, alterDialog) {
                        serverAction()
                    }

                }
            }

        }
        language1.setOnClickListener {
            updatingSelectedLanguage(text1, image1)
            updatingUnSelectedLanguage(text0, image0)
            updatingUnSelectedLanguage(text2, image2)
            finishDialog(alterDialog)
            viewModelScope.launch(Dispatchers.IO) {
                resendApis.serverCheck.serverCheckMainActivityApi { serverAction ->
                    selectedLanguageUpload(1, alterDialog) {
                        serverAction()
                    }

                }

            }

        }

        language2.setOnClickListener {
            updatingSelectedLanguage(text2, image2)
            updatingUnSelectedLanguage(text1, image1)
            updatingUnSelectedLanguage(text0, image0)
            finishDialog(alterDialog)

            viewModelScope.launch(Dispatchers.IO) {
                resendApis.serverCheck.serverCheckMainActivityApi { serverAction ->
                    selectedLanguageUpload(2, alterDialog) {
                        serverAction()
                    }

                }

            }

        }



        dataBinding!!.apply {
            toggleOFF.setOnClickListener {
                viewModelScope.launch {
                    withContext(Dispatchers.IO) {
                        withContext(Dispatchers.Main) {

                                moveToLoadingScreen()
                                Log.d("ConfigurationViewModel", "true")

                                resendApis.serverCheck.serverCheckMainActivityApi { serverAction ->

                                    selectedNotifyStateUpload(true, serverAction)
                                }
//
//                            } else {
//                                Toast.makeText(context, TAG2, Toast.LENGTH_SHORT).show()
//                            }
                        }

                    }

                }

            }


            toggleON.setOnClickListener {
                viewModelScope.launch(Dispatchers.IO) {

                    withContext(Dispatchers.Main) {
                            moveToLoadingScreen()
                            Log.d(TAG, "false")


                            resendApis.serverCheck.serverCheckMainActivityApi { serverAction ->

                                selectedNotifyStateUpload(false, serverAction)
                            }


//                        } else {
//                            Toast.makeText(context, TAG2, Toast.LENGTH_SHORT).show()
//                        }
                    }


                }


            }
        }


    }


    private fun moveToLoadingScreen() {
        LoadingScreen.OnEndLoadingCallbacks?.endLoading("From configration line 210")
        val intent = Intent(activityContext, LoadingScreen::class.java)
        ContextCompat.startActivity(activityContext!!, intent, Bundle.EMPTY)
    }


     private fun finishDialog(alterDialog: AlertDialog) {
    moveToLoadingScreen()
    try{
        alterDialog.dismiss()
    }catch (e:Exception){
        e.localizedMessage
        Log.d("LANGUAGE_WINDOW","IN CATCH BLOCK")
    }
}

    private fun selectedLanguageUpload(
        language: Int,
        alterDialog: AlertDialog,
        action: () -> Unit
    ) {


        val token = tinyDB.getString("Cookie")
        MyApplication.checkForLanguageChange = 200
        viewModelScope.launch(Dispatchers.IO) {

            try {

                val response = mainRepository.updateLanguage(language, token!!)

                Log.d(TAG, "SuccessResponse $response")

                if (response != null) {
                    action()
                    tinyDB.putBoolean("reload", true)
                    when (language) {
                        0 -> {
                            tinyDB.putString("language", "0")
                        }
                        1 -> {
                            tinyDB.putString("language", "1")
                        }
                        2 -> {
                            tinyDB.putString("language", "2")
                        }


                    }
                    withContext(Dispatchers.Main) {
                        (MyApplication.loadingContext as LoadingScreen).finish()
                    }

                    alterDialog.dismiss()
                    withContext(Dispatchers.Main) {
                        when (language) {
                            0 -> {

                                dataBinding!!.languageNameInitails.text = "ESP"
                                (activityContext as MainActivity).restartActivity()
                            }
                            1 -> {
                                if (dataBinding != null) {
                                    println("English Selected")
                                }
                                val eng = "ENG"
                                dataBinding!!.languageNameInitails.text = eng
                                (activityContext as MainActivity).restartActivity()

                            }
                            else -> {
                                dataBinding!!.languageNameInitails.text = " (POR)"
                                (activityContext as MainActivity).restartActivity()
                            }
                        }
                    }
                }
            } catch (e: ResponseException) {

                showServerPopup(true, "")
                Log.d(TAG, "logout Failed $e")
            } catch (e: ApiException) {
                showServerPopup(true, "")
                e.printStackTrace()
            } catch (e: NoInternetException) {

                Log.d(TAG, "position 2")
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    showServerPopup(false, "")
                }
            } catch (e: SocketException) {
                LoadingScreen.OnEndLoadingCallbacks?.endLoading("from configration line 289")
                Log.d(TAG, "Connection Not Available")
                withContext(Dispatchers.Main) {
                    showServerPopup(true, "")
                }
            } catch (e: Exception) {
                showServerPopup(true, "")
                Log.d(TAG, "Connection Not Available")
            }

        }


    }

    private fun selectedNotifyStateUpload(notify: Boolean, serverAction: () -> Unit) {
        val token = tinyDB.getString("Cookie")

        var notifyResponse: MessageResponse? = null
        viewModelScope.launch(Dispatchers.IO) {
            try {

                notifyResponse = mainRepository.updateNotification(notify, token!!)

                Log.d(TAG, "SuccessResponse $notifyResponse")

                if (notifyResponse != null) {
                    serverAction()
                    withContext(Dispatchers.Main) {
                        if (notify) {
                            dataBinding!!.toggleON.visibility = View.VISIBLE
                            dataBinding!!.toggleOFF.visibility = View.GONE
                        } else {
                            dataBinding!!.toggleOFF.visibility = View.VISIBLE
                            dataBinding!!.toggleON.visibility = View.GONE
                        }
                    }



                    withContext(Dispatchers.Main) {

                        (MyApplication.loadingContext as LoadingScreen).finish()
                    }


                }

            } catch (e: ResponseException) {
                showServerPopup(true, "")

                Log.d(TAG, "logout Failed $e")
            } catch (e: ApiException) {
                showServerPopup(false, "")
                e.printStackTrace()
            } catch (e: NoInternetException) {
                showServerPopup(false, "")
                Log.d(TAG, "position 2")
                e.printStackTrace()
            } catch (e: SocketException) {
                LoadingScreen.OnEndLoadingCallbacks!!.endLoading("from configration line 345")
                Log.d(TAG, "Connection Not Available")
                withContext(Dispatchers.Main) {
                    showServerPopup(true, "")
                }
            } catch (e: Exception) {
                showServerPopup(true, "")
                Log.d(TAG, "Connection Not Available")
            }

        }


    }

    private suspend fun showServerPopup(forServer: Boolean, message: String) {
        withContext(Dispatchers.Main) {
            MyApplication.authCheck = true
            LoadingScreen.OnEndLoadingCallbacks!!.openServerPopup(forServer, message)

        }

    }


    fun selectLanguage() {


        when (tinyDB.getString("language")) {
            "0" -> {

                updatingSelectedLanguage(text0, image0)
                updatingUnSelectedLanguage(text1, image1)
                updatingUnSelectedLanguage(text2, image2)
            }
            "1" -> {
                updatingSelectedLanguage(text1, image1)
                updatingUnSelectedLanguage(text0, image0)
                updatingUnSelectedLanguage(text2, image2)
            }
            else -> {
                updatingSelectedLanguage(text2, image2)
                updatingUnSelectedLanguage(text1, image1)
                updatingUnSelectedLanguage(text0, image0)
            }
        }

    }

    private fun updatingUnSelectedLanguage(textView: TextView, imageView: ImageView) {
        textView.setTextColor(Color.parseColor("#C6C6C6"))
        imageView.setBackgroundColor(Color.GRAY)
    }

    private fun updatingSelectedLanguage(textView: TextView, imageView: ImageView) {
        textView.setTextColor(Color.BLACK)
        imageView.setBackgroundColor(Color.parseColor(ResendApis.primaryColor))
    }


    private fun tagsForToast() {
        when (tinyDB.getString("language")) {
            "0" -> {

                TAG2 = "Comprueba tu conexión a Internet"

            }
            "1" -> {


                TAG2 = "Check Your Internet Connection"
            }
            else -> {

                TAG2 = "Verifique a sua conexão com a internet"
            }
        }

    }

    fun customColor() {
        dataBinding?.apply {
            upperLayoutBack.setBackgroundColor(Color.parseColor(ResendApis.primaryColor))
            arrowBack1.setBackgroundColor(Color.parseColor(ResendApis.primaryColor))
            arrowBack2.setBackgroundColor(Color.parseColor(ResendApis.primaryColor))
            togglebg.setBackgroundColor(Color.parseColor(ResendApis.primaryColor))
        }

    }

}