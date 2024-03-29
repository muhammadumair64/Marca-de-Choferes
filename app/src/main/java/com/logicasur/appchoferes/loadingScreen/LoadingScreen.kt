package com.logicasur.appchoferes.loadingScreen

import android.app.AlertDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.ImageView
import com.logicasur.appchoferes.R
import androidx.activity.viewModels
import com.logicasur.appchoferes.Extra.Language
import com.logicasur.appchoferes.Extra.TinyDB
import com.logicasur.appchoferes.mainscreen.MainActivity
import com.logicasur.appchoferes.myApplication.MyApplication
import dagger.hilt.android.AndroidEntryPoint
import android.graphics.Color
import android.view.View

import android.view.WindowManager
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.lifecycleScope
import com.logicasur.appchoferes.Extra.BaseClass
import com.logicasur.appchoferes.Extra.K
import com.logicasur.appchoferes.auth.otp.interfaces.OnEndLoadingCallbacks
import com.logicasur.appchoferes.mainscreen.home.timerServices.UploadRemaingDataService.Companion.activity
import kotlinx.coroutines.launch
import java.util.*


@AndroidEntryPoint
class LoadingScreen :BaseClass(), OnEndLoadingCallbacks {
    lateinit var proceed_btn  : AppCompatButton
    lateinit var cancel_btn: RelativeLayout
    var networkAlertDialog: AlertDialog? = null
    lateinit var networkDialogBuilder:AlertDialog.Builder

    lateinit var go_back_btn  : AppCompatButton
    var serverAlertDialog: AlertDialog? = null
    lateinit var serverDialogBuilder:AlertDialog.Builder



    val loadingViewModel: loadingViewModel by viewModels()
    companion object
    {
         var OnEndLoadingCallbacks : OnEndLoadingCallbacks? = null

    }

    lateinit var tinyDB: TinyDB
    var imageFromServer=""
    lateinit var  imageBackground:ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        OnEndLoadingCallbacks  = this
        loadingViewModel.activityContext = this
        val language= Language()
        language.setLanguage(baseContext)
        setContentView(R.layout.activity_loading_screen)

        tinyDB= TinyDB(this)
        K.primaryColor=tinyDB.getString("primaryColor")!!
        K.secondrayColor=tinyDB.getString("secondrayColor")!!
        initView()
         imageFromServer= tinyDB.getString("loadingBG").toString()
        if(imageFromServer.isNotEmpty()){
            Base64ToBitmap(imageFromServer)
        }else{
            Log.d("LOADINGSCRTEST","Empty")
        }


    }

   fun initView(){
       setBarColor()
//       createServerAlertPopup()
       MyApplication.loadingContext = this
//       createPopup()
         imageBackground=findViewById(R.id.loadingBackground)


//       extra.liveData.observeForever(Observer {
//           println("i am observing")
//           if(it=="1"){
//               MoveToMain()
//           }
//       })
//
//
////     extra.liveData.observe(this, {
////
////
////})

    }

    fun MoveToMain(){
        var intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {

        println("backPressed")
    }

    fun Base64ToBitmap(base64: String) {
        Log.d("LOADINGSCRTEST","IN BASE64")
        val imageBytes = Base64.decode(base64, 0)
        val image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        imageBackground.setImageBitmap(image)
    }

    override fun endLoading() {
        Log.d("LoadingScreenFinish","Finish")
        finish()
    }

    override fun openPopup(myTimer: Timer?) {
         createPopup(myTimer)
    }

    override fun openServerPopup() {

            createServerAlertPopup()


    }

    fun setBarColor(){
// clear FLAG_TRANSLUCENT_STATUS flag:

// clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window

// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

// finally change the color

// finally change the color

        val color = tinyDB.getString("primaryColor")
        if(color?.isNotEmpty() == true){
            window.setStatusBarColor(Color.parseColor(color))
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("LOADING_TESTING","Yes")
    }


    fun createPopup(myTimer: Timer?) {
        if(networkAlertDialog != null){
            if(networkAlertDialog!!.isShowing){
                networkAlertDialog!!.dismiss()
            }
        }

        networkDialogBuilder = AlertDialog.Builder(this)
        val PopupView: View = layoutInflater.inflate(R.layout.item_networkcheck_popup, null)
        networkAlertDialog= networkDialogBuilder.create()
        proceed_btn=PopupView.findViewById(R.id.proceed_btn)
        cancel_btn=PopupView.findViewById(R.id.cancel_btn)
        loadingViewModel.openPopup(networkAlertDialog!!,PopupView,resources)
        setGrad(K.primaryColor, K.secondrayColor, proceed_btn)
        cancel_btn.setOnClickListener {
            networkAlertDialog!!.dismiss()


        }
        proceed_btn.setOnClickListener {
            if(myTimer!=null){
                myTimer.cancel()
                loadingViewModel.getPreviousTimeWhenOffline()
            }
            else{
                finish()
                try{
                    (activity as MainActivity).updatePendingData(true)
                } catch (e:Exception){
                    Log.d("IN Starting main","ERROR")
                }


            }

            networkAlertDialog!! .dismiss()
        }



    }


    fun createServerAlertPopup() {
        serverDialogBuilder = AlertDialog.Builder(this)
        val PopupView: View = layoutInflater.inflate(R.layout.server_downpopup, null)
        serverAlertDialog= serverDialogBuilder.create()
        go_back_btn=PopupView.findViewById(R.id.go_back)

        loadingViewModel.openServerPopup(serverAlertDialog!!,PopupView,resources)

        setGrad(K.primaryColor, K.secondrayColor, go_back_btn)
         go_back_btn.setOnClickListener {
            serverAlertDialog!!.dismiss()
             finish()
        }



    }
}