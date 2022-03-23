package com.logicasur.appchoferes.common.loadingScreen

import android.app.AlertDialog
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatButton
import com.logicasur.appchoferes.Extra.BaseClass
import com.logicasur.appchoferes.Extra.CheckConnection
import com.logicasur.appchoferes.Extra.Language
import com.logicasur.appchoferes.Extra.TinyDB
import com.logicasur.appchoferes.R
import com.logicasur.appchoferes.afterAuth.mainscreen.MainActivity
import com.logicasur.appchoferes.beforeAuth.otpScreen.interfaces.OnEndLoadingCallbacks
import com.logicasur.appchoferes.common.loadingScreen.interfaces.dialogActionCallBacks
import com.logicasur.appchoferes.utils.ResendApis
import com.logicasur.appchoferes.utils.myApplication.MyApplication
import dagger.hilt.android.AndroidEntryPoint
import java.util.*


@AndroidEntryPoint
class LoadingScreen : BaseClass(), OnEndLoadingCallbacks {
    lateinit var proceed_btn: AppCompatButton
    lateinit var cancel_btn: RelativeLayout
    var networkAlertDialog: AlertDialog? = null
    lateinit var networkDialogBuilder: AlertDialog.Builder
    lateinit var topTextView :TextView
    lateinit var topServerTextView :TextView
    lateinit var subTextView :TextView
    lateinit var go_back_btn: AppCompatButton
    var serverAlertDialog: AlertDialog? = null
    lateinit var serverDialogBuilder: AlertDialog.Builder
      var isStarted :Boolean = false

    val loadingViewModel: loadingViewModel by viewModels()

    companion object {
          var OnEndLoadingCallbacks: OnEndLoadingCallbacks? = null
          var  dialogActionCallBacks :dialogActionCallBacks?= null
    }

    lateinit var tinyDB: TinyDB
    var imageFromServer = ""
    lateinit var imageBackground: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        OnEndLoadingCallbacks = this
        loadingViewModel.activityContext = this
        val language = Language()
        language.setLanguage(baseContext)
        setContentView(R.layout.activity_loading_screen)

        tinyDB = TinyDB(this)
        ResendApis.primaryColor = tinyDB.getString("primaryColor")!!
        ResendApis.secondaryColor = tinyDB.getString("secondrayColor")!!
        initView()
        imageFromServer = tinyDB.getString("loadingBG").toString()
        if (imageFromServer.isNotEmpty()) {
            Base64ToBitmap(imageFromServer)
        } else {
            Log.d("LOADINGSCRTEST", "Empty")
        }


    }

    fun initView() {

        isStarted= true
        setBarColor()
        MyApplication.loadingContext = this

        imageBackground = findViewById(R.id.loadingBackground)
//        hideKeyboard()

    }


    override fun onBackPressed() {

        println("backPressed")
    }

    fun Base64ToBitmap(base64: String) {
        Log.d("LOADINGSCRTEST", "IN BASE64")
        val imageBytes = Base64.decode(base64, 0)
        val image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        imageBackground.setImageBitmap(image)
    }

    override fun endLoading(messsage: String) {
        Log.d("LOADING_TESTING","THIS WINDOW END BY $messsage")
        finish()
    }


    override fun openPopup(myTimer: Timer?, b: Boolean, forServer: Boolean) {
        Log.d("POPUP_TESTING", "IN SERVER POPUP override function")
        createPopup(myTimer, b,forServer)
    }

    override fun openServerPopup(b: Boolean, s: String) {

        createServerAlertPopup(b,s)


    }

    override fun calculateTimeFromLocalDB() {
        println("IN CALCULATION BLOCK")
        loadingViewModel.getPreviousTimeWhenOffline(false)
    }


    override fun onDestroy() {
        try {
            networkAlertDialog?.dismiss()
            serverAlertDialog?.dismiss()


        } catch (e: Exception) {
            e.localizedMessage
        }
        super.onDestroy()




    }


    //-------------------------------------------------Utils----------------------------------------------
    private fun createServerAlertPopup(forServer: Boolean, message: String) {
        runOnUiThread {
            Log.d("POPUP_TESTING", "IN SERVER POPUP")
            serverDialogBuilder = AlertDialog.Builder(this)
            val PopupView: View = layoutInflater.inflate(R.layout.server_downpopup, null)
            serverAlertDialog = serverDialogBuilder.create()
            go_back_btn = PopupView.findViewById(R.id.go_back)
            topServerTextView = PopupView.findViewById(R.id.topTextServer)

            Log.d("POPUP_TESTING", " In VIEW MODEL After delay $message")
            if(message.isNotEmpty()){
                Log.d("POPUP_TESTING", " In condition $message")
                topServerTextView.text = message
            }else if(!forServer || !CheckConnection.netCheck(this)) {
                topServerTextView.text = resources.getString(R.string.toptext)
            }
            loadingViewModel.openServerPopup(serverAlertDialog!!, PopupView, resources)
            try {
                setGrad(ResendApis.primaryColor, ResendApis.secondaryColor, go_back_btn)


            } catch (e: Exception) {
                e.localizedMessage
            }

            go_back_btn.setOnClickListener {
                serverAlertDialog!!.dismiss()
                finish()

            }
        }
    }

    private fun createPopup(myTimer: Timer?, boolean: Boolean, forServer: Boolean) {
        Log.d("STATUS_TESTING", "IN WINDOW")
        runOnUiThread {
            Log.d("STATUS_TESTING", "IN WINDOW")
            if (networkAlertDialog == null) {
                Log.d("STATUS_TESTING", "IN WINDOW EX")
                networkDialogBuilder = AlertDialog.Builder(this)
                val PopupView: View = layoutInflater.inflate(R.layout.item_networkcheck_popup, null)
                proceed_btn = PopupView.findViewById(R.id.proceed_btn)
                cancel_btn = PopupView.findViewById(R.id.cancel_btn)
                networkAlertDialog = networkDialogBuilder.create()
                topTextView = PopupView.findViewById(R.id.topText)
                subTextView = PopupView.findViewById(R.id.subText)
                networkAlertDialog?.setView(PopupView)

                loadingViewModel.openPopup(networkAlertDialog!!, PopupView, resources,forServer,subTextView,topTextView)
                setGrad(ResendApis.primaryColor, ResendApis.secondaryColor, proceed_btn)
                if(forServer && CheckConnection.netCheck(this)){
                    Log.d("POPUP_TESTING","IN TEXT CHANGE BLOCK")
                    subTextView.text = resources.getString(R.string.proceed_offline)
                    topTextView.text = resources.getString(R.string.go_back_top_text)
                }


                cancel_btn.setOnClickListener {
                    networkAlertDialog?.dismiss()
                    networkAlertDialog =  null

                    if (myTimer != null || boolean) {
                        finishAffinity()
                    }
                    dialogActionCallBacks?.clickOnCancel()
                    finish()

                }
                proceed_btn.setOnClickListener {
                    if (myTimer != null || boolean) {
                        if (boolean) {
                            myTimer?.cancel()
                        }
                        loadingViewModel.getPreviousTimeWhenOffline(true)
                    } else {

                        try {
                            dialogActionCallBacks?.clickOnProceed()
                            Log.d("STATUS_TESTING", "Before entring DaTa base")

                            val stateCheck = tinyDB.getBoolean("STATEAPI")
                            if (stateCheck) {
                                Log.d("APIDATATESTING", "IN IF BLOCK")
                                (MyApplication.activityContext as MainActivity).updatePendingData(true)
                            } else {
                                Log.d("APIDATATESTING", "IN Else")
                                (MyApplication.activityContext as MainActivity).updatePendingData(false)
                            }



                        } catch (e: Exception) {
                            Log.d("IN Starting main", "ERROR")
                        }
                        finish()

                    }

                    networkAlertDialog?.dismiss()
                    networkAlertDialog = null
                }
            }
        }


    }

    private fun setBarColor() {

        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)


        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)


        val color = tinyDB.getString("primaryColor")
        if (color?.isNotEmpty() == true) {
            window.setStatusBarColor(Color.parseColor(color))
        }

    }

    fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
    }

}







