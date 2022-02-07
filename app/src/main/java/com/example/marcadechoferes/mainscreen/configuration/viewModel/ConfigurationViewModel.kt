package com.example.marcadechoferes.mainscreen.configuration.viewModel


import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.marcadechoferes.Extra.TinyDB
import com.example.marcadechoferes.R
import com.example.marcadechoferes.databinding.FragmentConfigurationBinding
import com.example.marcadechoferes.loadingScreen.LoadingScreen
import com.example.marcadechoferes.mainscreen.MainActivity
import com.example.marcadechoferes.mainscreen.repository.MainRepository
import com.example.marcadechoferes.myApplication.MyApplication
import com.example.marcadechoferes.network.ApiException
import com.example.marcadechoferes.network.NoInternetException
import com.example.marcadechoferes.network.ResponseException
import com.example.marcadechoferes.network.unsentApis.UnsentLanguageUpdation
import com.example.marcadechoferes.network.unsentApis.UnsentNotifyStateUpload
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class ConfigurationViewModel @Inject constructor(val mainRepository: MainRepository) : ViewModel() {
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

    fun viewsForConfigurationFragment(
        context: Context,
        binding: FragmentConfigurationBinding,
        alterDialog: AlertDialog,
        contactPopupView: View
    ) {
        activityContext = context
        dataBinding = binding
        tinyDB = TinyDB(context)
       val notify=tinyDB.getBoolean("notify")
        dataBinding!!.switchBtn.isChecked=notify
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
            tinyDB.putString("language", "0")
            selectedLanguageUplaod(0, alterDialog)
        }
        language1.setOnClickListener {
            language1Selected()
            var intent = Intent(context, LoadingScreen::class.java)
            ContextCompat.startActivity(context, intent, Bundle.EMPTY)
            tinyDB.putString("language", "1")
            selectedLanguageUplaod(1, alterDialog)        }

        language2.setOnClickListener {
            language2Selected()
            var intent = Intent(context, LoadingScreen::class.java)
            ContextCompat.startActivity(context, intent, Bundle.EMPTY)
            tinyDB.putString("language", "2")
            selectedLanguageUplaod(2, alterDialog)
        }


        dataBinding?.switchBtn?.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            var intent = Intent(context, LoadingScreen::class.java)
            ContextCompat.startActivity(context, intent, Bundle.EMPTY)
            if (isChecked) {

                selectedNotifyStateUplaod(true)
            } else {

                selectedNotifyStateUplaod(false)
            }
        })


    }


    fun selectedLanguageUplaod(language: Int, alterDialog: AlertDialog) {


        val token = tinyDB.getString("Cookie")
      MyApplication.checkForLanguageChange=200
        viewModelScope.launch {


            withContext(Dispatchers.IO) {
                try {

                    val response = mainRepository.updateLanguage(language, token!!)

                    println("SuccessResponse $response")

                    if (response != null) {
                        tinyDB.putBoolean("reload", true)
                        withContext(Dispatchers.Main) {
                            (MyApplication.loadingContext as LoadingScreen).finish()
                        }

                        alterDialog.dismiss()
                        withContext(Dispatchers.Main) {
                            if (language == 0) {

                                dataBinding!!.languageNameInitails.text = "ESP"
                                (activityContext as MainActivity).restartActivity()
                            } else if (language == 1) {
                                if(dataBinding!=null){
                                    println("English Selected")                                }
                                var eng ="ENG"
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
                }
                catch (e: ApiException) {

                    e.printStackTrace()

                }
                catch (e: NoInternetException) {
                    println("position 2")
                    e.printStackTrace()
                    mainRepository.insertUnsentLanguageUpdate(UnsentLanguageUpdation( language))
                    withContext(Dispatchers.Main){
                        Toast.makeText(activityContext, "Check Your Internet Connection", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }


    }

    fun selectedNotifyStateUplaod(notify: Boolean) {
        var Token = tinyDB.getString("Cookie")
        viewModelScope.launch {

            withContext(Dispatchers.IO) {
                try {

                    val response = mainRepository.updateNotification(notify, Token!!)

                    println("SuccessResponse $response")

                    if (response != null) {
                        withContext(Dispatchers.Main) {

                            (MyApplication.loadingContext as LoadingScreen).finish()
                            if (notify == true) {
                                Toast.makeText(
                                    activityContext,
                                    "Push Notification ON",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    activityContext,
                                    "Push Notification OFF",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }


                    }

                } catch (e: ResponseException) {
                    (MyApplication.loadingContext as LoadingScreen).finish()
                    println("logout Failed $e")
                }
                catch (e: ApiException) {

                    e.printStackTrace()
                }
                catch (e: NoInternetException) {
                    mainRepository.insertUnsentNotifyStateUpload(UnsentNotifyStateUpload(notify))
                    println("position 2")
                    e.printStackTrace()
                }
            }
        }


    }

    fun language0Selected() {
        text0.setTextColor(Color.BLACK)
        image0.setBackgroundResource(R.drawable.ic_blue_check)

        text1.setTextColor(Color.parseColor("#C6C6C6"))
        image1.setBackgroundResource(R.drawable.ic_check_circle)

        text2.setTextColor(Color.parseColor("#C6C6C6"))
        image2.setBackgroundResource(R.drawable.ic_check_circle)
    }

    fun language1Selected() {
        text1.setTextColor(Color.BLACK)
        image1.setBackgroundResource(R.drawable.ic_blue_check)

        text0.setTextColor(Color.parseColor("#C6C6C6"))
        image0.setBackgroundResource(R.drawable.ic_check_circle)

        text2.setTextColor(Color.parseColor("#C6C6C6"))
        image2.setBackgroundResource(R.drawable.ic_check_circle)
    }

    fun language2Selected() {
        text2.setTextColor(Color.BLACK)
        image2.setBackgroundResource(R.drawable.ic_blue_check)

        text1.setTextColor(Color.parseColor("#C6C6C6"))
        image1.setBackgroundResource(R.drawable.ic_check_circle)

        text0.setTextColor(Color.parseColor("#C6C6C6"))
        image0.setBackgroundResource(R.drawable.ic_check_circle)
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

}