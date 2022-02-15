package com.logicasur.appchoferes.mainscreen.configuration.viewModel


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
import com.logicasur.appchoferes.Extra.ResendApis
import com.logicasur.appchoferes.Extra.TinyDB
import com.logicasur.appchoferes.R
import com.logicasur.appchoferes.databinding.FragmentConfigurationBinding
import com.logicasur.appchoferes.loadingScreen.LoadingScreen
import com.logicasur.appchoferes.mainscreen.MainActivity
import com.logicasur.appchoferes.mainscreen.repository.MainRepository
import com.logicasur.appchoferes.myApplication.MyApplication
import com.logicasur.appchoferes.network.ApiException
import com.logicasur.appchoferes.network.NoInternetException
import com.logicasur.appchoferes.network.ResponseException
import com.logicasur.appchoferes.network.logoutResponse.MessageResponse
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
    val resendApis: ResendApis
) : ViewModel() {
    var language: Int = 0
    var activityContext: Context? = null
    var dataBinding: FragmentConfigurationBinding? = null
    lateinit var tinyDB: TinyDB
    lateinit var language0: RelativeLayout
    lateinit var language1: RelativeLayout
    lateinit var language2: RelativeLayout

    lateinit var text0: TextView
    lateinit var text1: TextView
    lateinit var text2: TextView

    lateinit var image0: ImageView
    lateinit var image1: ImageView
    lateinit var image2: ImageView
    var TAG2 = ""
    fun viewsForConfigurationFragment(
        context: Context,
        binding: FragmentConfigurationBinding,
        alterDialog: AlertDialog,
        contactPopupView: View
    ) {
        activityContext = context
        dataBinding = binding
        tinyDB = TinyDB(context)
        tagsForToast()

        val notify = tinyDB.getBoolean("notify")
        if (notify == true) {
            dataBinding!!.toggleOFF!!.visibility = View.GONE
            dataBinding!!.toggleON!!.visibility = View.VISIBLE
        } else {
            dataBinding!!.toggleOFF!!.visibility = View.VISIBLE
            dataBinding!!.toggleON!!.visibility = View.GONE
        }
//        dataBinding!!.switchBtn.isChecked=notify
        language0 = contactPopupView.findViewById(R.id.spanish)
        language2 = contactPopupView.findViewById(R.id.Portuguese)
        language1 = contactPopupView.findViewById(R.id.English)

        text0 = contactPopupView.findViewById(R.id.text0)
        text1 = contactPopupView.findViewById(R.id.text1)
        text2 = contactPopupView.findViewById(R.id.text2)


        image0 = contactPopupView.findViewById(R.id.image0)
        image1 = contactPopupView.findViewById(R.id.image1)
        image2 = contactPopupView.findViewById(R.id.image2)


        selectedLanguage()

        language0.setOnClickListener {
            language0Selected()

            var intent = Intent(context, LoadingScreen::class.java)
            ContextCompat.startActivity(context, intent, Bundle.EMPTY)
//            tinyDB.putString("language", "0")
            viewModelScope.launch(Dispatchers.IO) {
//                ServerCheck.serverCheck(null) { selectedLanguageUplaod(0, alterDialog) }

                resendApis.serverCheck.serverCheckMainActivityApi { serverAction ->
                    selectedLanguageUplaod(0, alterDialog) {
                        serverAction()
                    }

                }
            }

//            selectedLanguageUplaod(0, alterDialog)
        }
        language1.setOnClickListener {
            language1Selected()
            var intent = Intent(context, LoadingScreen::class.java)
            ContextCompat.startActivity(context, intent, Bundle.EMPTY)
//            tinyDB.putString("language", "1")
            viewModelScope.launch(Dispatchers.IO) {
//                ServerCheck.serverCheck(null) { selectedLanguageUplaod(1, alterDialog) }
                resendApis.serverCheck.serverCheckMainActivityApi { serverAction ->
                    selectedLanguageUplaod(1, alterDialog) {
                        serverAction()
                    }

                }

            }

//            selectedLanguageUplaod(1, alterDialog)
        }

        language2.setOnClickListener {
            language2Selected()
            var intent = Intent(context, LoadingScreen::class.java)
            ContextCompat.startActivity(context, intent, Bundle.EMPTY)
//            tinyDB.putString("language", "2")
            viewModelScope.launch(Dispatchers.IO) {
//                ServerCheck.serverCheck(null) { selectedLanguageUplaod(2, alterDialog) }

                resendApis.serverCheck.serverCheckMainActivityApi { serverAction ->
                    selectedLanguageUplaod(2, alterDialog) {
                        serverAction()
                    }

                }

            }
//            selectedLanguageUplaod(2, alterDialog)
        }


//        dataBinding?.switchBtn?.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
//            var intent = Intent(context, LoadingScreen::class.java)
//            ContextCompat.startActivity(context, intent, Bundle.EMPTY)
//            if (isChecked) {
//
//                selectedNotifyStateUplaod(true)
//            } else {
//
//                selectedNotifyStateUplaod(false)
//            }
//        })

        dataBinding!!.apply {
            toggleOFF!!.setOnClickListener {
                viewModelScope.launch {
                    withContext(Dispatchers.IO) {
//                        val check = ResendApis.isConnected()
                        withContext(Dispatchers.Main) {
                            if (CheckConnection.netCheck(activityContext!!)) {
                                var intent = Intent(context, LoadingScreen::class.java)
                                ContextCompat.startActivity(context, intent, Bundle.EMPTY)
                                Log.d("ConfigurationViewModel", "true")
//                                ServerCheck.serverCheck(null) { selectedNotifyStateUplaod(true) }

                                resendApis.serverCheck.serverCheckMainActivityApi { serverAction ->

                                    selectedNotifyStateUplaod(true, serverAction)
                                }
//                               selectedNotifyStateUplaod(true)
//                                toggleON!!.visibility = View.VISIBLE
//                                toggleOFF.visibility = View.GONE
                            } else {
                                Toast.makeText(context, TAG2, Toast.LENGTH_SHORT).show()
                            }
                        }

                    }

                }

            }


            toggleON!!.setOnClickListener {
                viewModelScope.launch {
                    withContext(Dispatchers.IO) {
//                        val check = ResendApis.isConnected()
                        withContext(Dispatchers.Main) {
                            if (CheckConnection.netCheck(activityContext!!)) {
                                var intent = Intent(context, LoadingScreen::class.java)
                                ContextCompat.startActivity(context, intent, Bundle.EMPTY)
                                Log.d("ConfigurationViewModel", "false")
//                                ServerCheck.serverCheck(null) {
//                                    selectedNotifyStateUplaod(
//                                        false,
//                                        {}
//                                    )
//                                }

                                resendApis.serverCheck.serverCheckMainActivityApi { serverAction ->

                                    selectedNotifyStateUplaod(false, serverAction)
                                }
//                                selectedNotifyStateUplaod(false)
//                                toggleOFF!!.visibility = View.VISIBLE
//                                toggleON.visibility = View.GONE

                            } else {
                                Toast.makeText(context, TAG2, Toast.LENGTH_SHORT).show()
                            }
                        }

                    }

                }


            }
        }


    }


    fun selectedLanguageUplaod(language: Int, alterDialog: AlertDialog, action: () -> Unit) {


        var Token = tinyDB.getString("Cookie")
        MyApplication.checkForLanguageChange = 200
        viewModelScope.launch {


            withContext(Dispatchers.IO) {
                try {

                    val response = mainRepository.updateLanguage(language, Token!!)

                    println("SuccessResponse $response")

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
                            if (language == 0) {

                                dataBinding!!.languageNameInitails.text = "ESP"
                                (activityContext as MainActivity).restartActivity()
                            } else if (language == 1) {
                                if (dataBinding != null) {
                                    println("English Selected")
                                }
                                var eng = "ENG"
                                dataBinding!!.languageNameInitails.text = "$eng"
                                (activityContext as MainActivity).restartActivity()

                            } else {
                                dataBinding!!.languageNameInitails.text = " (POR)"
                                (activityContext as MainActivity).restartActivity()
                            }
                        }
                    }
                } catch (e: ResponseException) {
                    (MyApplication.loadingContext as LoadingScreen).finish()
                    println("logout Failed $e")
                } catch (e: ApiException) {
                    e.printStackTrace()
                } catch (e: NoInternetException) {

                    println("position 2")
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(activityContext, TAG2, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: SocketException) {
                    LoadingScreen.OnEndLoadingCallbacks?.endLoading()
                    Log.d("connection Exception", "Connect Not Available")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(activityContext, TAG2, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.d("connection Exception", "Connect Not Available")
                }
            }
        }


    }

    fun selectedNotifyStateUplaod(notify: Boolean, serverAction: () -> Unit) {
        var Token = tinyDB.getString("Cookie")

        var notifyResponse: MessageResponse? = null
//        var check = 0
//        val myTimer = Timer()
//        myTimer!!.schedule(object : TimerTask() {
//            override fun run() {
//                if (check == 1 || check == 2) {
//                    viewModelScope.launch(Dispatchers.IO) {
//                        ServerCheck.serverCheck(LoadingScreen.OnEndLoadingCallbacks) {}
//                    }
//                    check++
//                } else if (check > 2) {
//                    Log.d("NETCHECKTEST", "----working")
//                    myTimer.purge()
//                    myTimer.cancel()
////                    if (notifyResponse == null ) {
////
////                    }
//                } else {
//                    check++
//                }
//
//            }
//        }, 0, 20000)


        viewModelScope.launch {

            withContext(Dispatchers.IO) {
                try {

                    notifyResponse = mainRepository.updateNotification(notify, Token!!)


                    println("SuccessResponse $notifyResponse")

                    if (notifyResponse != null) {
                        serverAction()
                        withContext(Dispatchers.Main) {
                            if (notify) {
                                dataBinding!!.toggleON!!.visibility = View.VISIBLE
                                dataBinding!!.toggleOFF.visibility = View.GONE
                            } else {
                                dataBinding!!.toggleOFF!!.visibility = View.VISIBLE
                                dataBinding!!.toggleON.visibility = View.GONE
                            }
                        }

//                        myTimer.cancel()
//                        myTimer.purge()


                        withContext(Dispatchers.Main) {

                            (MyApplication.loadingContext as LoadingScreen).finish()
                            if (notify == true) {
//                                Toast.makeText(
//                                    activityContext,
//                                    "Push Notification ON",
//                                    Toast.LENGTH_SHORT
//                                ).show()
                            } else {
//                                Toast.makeText(
//                                    activityContext,
//                                    "Push Notification OFF",
//                                    Toast.LENGTH_SHORT
//                                ).show()
                            }
                        }


                    }

                } catch (e: ResponseException) {
                    (MyApplication.loadingContext as LoadingScreen).finish()
                    println("logout Failed $e")
                } catch (e: ApiException) {
                    e.printStackTrace()
                } catch (e: NoInternetException) {
                    println("position 2")
                    e.printStackTrace()
                } catch (e: SocketException) {
                    LoadingScreen.OnEndLoadingCallbacks!!.endLoading()
                    Log.d("connection Exception", "Connect Not Available")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(activityContext, TAG2, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.d("connection Exception", "Connect Not Available")
                }
            }
        }


    }

    fun language0Selected() {
        text0.setTextColor(Color.BLACK)
        image0.setBackgroundColor(Color.parseColor(ResendApis.primaryColor))

        text1.setTextColor(Color.parseColor("#C6C6C6"))
        image1.setBackgroundColor(Color.GRAY)

        text2.setTextColor(Color.parseColor("#C6C6C6"))
        image2.setBackgroundColor(Color.GRAY)
    }

    fun language1Selected() {
        text1.setTextColor(Color.BLACK)
        image1.setBackgroundColor(Color.parseColor(ResendApis.primaryColor))

        text0.setTextColor(Color.parseColor("#C6C6C6"))
        image0.setBackgroundColor(Color.GRAY)

        text2.setTextColor(Color.parseColor("#C6C6C6"))
        image2.setBackgroundColor(Color.GRAY)
    }

    fun language2Selected() {
        text2.setTextColor(Color.BLACK)
        image2.setBackgroundColor(Color.parseColor(ResendApis.primaryColor))

        text1.setTextColor(Color.parseColor("#C6C6C6"))
        image1.setBackgroundColor(Color.GRAY)

        text0.setTextColor(Color.parseColor("#C6C6C6"))
        image0.setBackgroundColor(Color.GRAY)
    }

    fun selectedLanguage() {
        var language = tinyDB.getString("language")
        if (language == "0") {
            language0Selected()

        } else if (language == "1") {
            language1Selected()

        } else {
            language2Selected()
        }

    }

    fun tagsForToast() {
        var language = tinyDB.getString("language")
        if (language == "0") {

            TAG2 = "Comprueba tu conexión a Internet"

        } else if (language == "1") {


            TAG2 = "Check Your Internet Connection"
        } else {

            TAG2 = "Verifique a sua conexão com a internet"
        }

    }


}