package com.logicasur.appchoferes.loadingScreen

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.ImageView
import com.logicasur.appchoferes.R
import androidx.activity.viewModels
import com.logicasur.appchoferes.Extra.Language
import com.logicasur.appchoferes.Extra.TinyDB
import com.logicasur.appchoferes.auth.otp.interfaces.onEndLoadingCallbacks
import com.logicasur.appchoferes.mainscreen.MainActivity
import com.logicasur.appchoferes.myApplication.MyApplication
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoadingScreen : AppCompatActivity(),onEndLoadingCallbacks{
    val loadingViewModel: loadingViewModel by viewModels()
    companion object
    {
         var onEndLoadingCallbacks : onEndLoadingCallbacks? = null
    }

    lateinit var tinyDB: TinyDB
    var imageFromServer=""
    lateinit var  imageBackground:ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onEndLoadingCallbacks  = this
        val language= Language()
        language.setLanguage(baseContext)
        setContentView(R.layout.activity_loading_screen)

        tinyDB= TinyDB(this)
        initView()
         imageFromServer= tinyDB.getString("loadingBG").toString()
        if(imageFromServer.isNotEmpty()){
            Base64ToBitmap(imageFromServer)
        }


    }

   fun initView(){

       MyApplication.loadingContext = this
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
        val imageBytes = Base64.decode(base64, 0)
        val image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        imageBackground.setImageBitmap(image)
    }

    override fun endLoading() {
        Log.d("LoadingScreenFinish","Finish")
        finish()
    }


}