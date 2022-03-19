package com.logicasur.appchoferes.common.loadingScreen

import android.app.AlertDialog
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.ImageView
import com.logicasur.appchoferes.R
import androidx.activity.viewModels
import com.logicasur.appchoferes.Extra.Language
import com.logicasur.appchoferes.Extra.TinyDB
import com.logicasur.appchoferes.afterAuth.mainscreen.MainActivity
import com.logicasur.appchoferes.utils.myApplication.MyApplication
import dagger.hilt.android.AndroidEntryPoint
import android.graphics.Color
import android.view.View

import android.view.WindowManager
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatButton
import com.logicasur.appchoferes.Extra.BaseClass
import com.logicasur.appchoferes.utils.ResendApis
import com.logicasur.appchoferes.beforeAuth.otpScreen.interfaces.OnEndLoadingCallbacks
import com.logicasur.appchoferes.afterAuth.mainscreen.fragments.home.timerServices.UploadRemaingDataService.Companion.activity
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
        ResendApis.primaryColor=tinyDB.getString("primaryColor")!!
        ResendApis.secondaryColor=tinyDB.getString("secondrayColor")!!
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
       MyApplication.loadingContext = this

         imageBackground=findViewById(R.id.loadingBackground)




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

    override fun openPopup(myTimer: Timer?, boolean: Boolean) {
         createPopup(myTimer,boolean)
    }

    override fun openServerPopup() {

            createServerAlertPopup()


    }

    override fun calculateTimeFromLocalDB() {
       println("IN CALCULATION BLOCK")
        loadingViewModel.getPreviousTimeWhenOffline(false)
    }



    override fun onDestroy() {
        try{
            networkAlertDialog?.dismiss()
            serverAlertDialog?.dismiss()


        }catch (e:Exception){
            e.localizedMessage
        }
        super.onDestroy()



        Log.d("LOADING_TESTING","Yes")
    }





//-------------------------------------------------Utils----------------------------------------------
    private fun createServerAlertPopup() {
           Log.d("POPUP_TESTING","IN SERVER POPUP")
           serverDialogBuilder = AlertDialog.Builder(this)
           val PopupView: View = layoutInflater.inflate(R.layout.server_downpopup, null)
           serverAlertDialog = serverDialogBuilder.create()
           go_back_btn=PopupView.findViewById(R.id.go_back)

           Log.d("POPUP_TESTING"," In VIEW MODEL After delay")

           loadingViewModel.openServerPopup(serverAlertDialog!!,PopupView,resources)
         try{
             setGrad(ResendApis.primaryColor, ResendApis.secondaryColor, go_back_btn)
         }catch (e:Exception){
             e.localizedMessage
         }

           go_back_btn.setOnClickListener {
               serverAlertDialog!!.dismiss()
               finish()

           }
       }
    private fun createPopup(myTimer: Timer?, boolean: Boolean) {

        runOnUiThread {
            Log.d("STATUS_TESTING","IN WINDOW")
            networkDialogBuilder = AlertDialog.Builder(this)
            val PopupView: View = layoutInflater.inflate(R.layout.item_networkcheck_popup, null)
            networkAlertDialog= networkDialogBuilder.create()
            proceed_btn=PopupView.findViewById(R.id.proceed_btn)
            cancel_btn=PopupView.findViewById(R.id.cancel_btn)
            loadingViewModel.openPopup(networkAlertDialog!!,PopupView,resources)
            setGrad(ResendApis.primaryColor, ResendApis.secondaryColor, proceed_btn)
            cancel_btn.setOnClickListener {

                networkAlertDialog!!.dismiss()

                if(myTimer != null){
                    finishAffinity()
                }
                finish()

            }
            proceed_btn.setOnClickListener {
                if(myTimer!=null || boolean){
                    if(boolean){
                        myTimer?.cancel()
                    }
                    loadingViewModel.getPreviousTimeWhenOffline(true)
                }
                else{

                    try{
                        Log.d("STATUS_TESTING","Before entring DaTa base")
                        (activity as MainActivity).updatePendingData(true)
                    } catch (e:Exception){
                        Log.d("IN Starting main","ERROR")
                    }
                    finish()

                }

                networkAlertDialog!! .dismiss()
            }
        }




    }
    private fun setBarColor(){

        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)


        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)



        val color = tinyDB.getString("primaryColor")
        if(color?.isNotEmpty() == true){
            window.setStatusBarColor(Color.parseColor(color))
        }

    }

   }







