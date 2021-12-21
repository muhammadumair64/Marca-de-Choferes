package com.example.marcadechoferes.loadingScreen

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.widget.ImageView
import com.example.marcadechoferes.R
import androidx.activity.viewModels
import com.example.marcadechoferes.Extra.Language
import com.example.marcadechoferes.Extra.TinyDB
import com.example.marcadechoferes.mainscreen.MainActivity
import com.example.marcadechoferes.myApplication.MyApplication
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoadingScreen : AppCompatActivity(){
    val loadingViewModel: loadingViewModel by viewModels()
    lateinit var tinyDB: TinyDB
    var imageFromServer=""
    lateinit var  imageBackground:ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val language= Language()
        language.setLanguage(baseContext)
        setContentView(R.layout.activity_loading_screen)
        tinyDB= TinyDB(this)
       imageFromServer= tinyDB.getString("loadingBG").toString()
        if(imageFromServer!=""){
            Base64ToBitmap(imageFromServer)
        }

        initView()
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




}