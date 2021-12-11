package com.example.marcadechoferes.Extra

import android.content.Context
import android.content.res.Configuration
import com.example.marcadechoferes.myApplication.MyApplication
import java.util.*

class Language {
    lateinit var tinyDB: TinyDB
    var langInitial = "es"
    fun setLanguage(context: Context){
        tinyDB= TinyDB(MyApplication.appContext)
      var language= tinyDB.getString("language")
        if (language=="0"){
            langInitial = "es"

        }else if(language=="1"){

            langInitial = "en"
        }
        else{
            langInitial = "pt"
        }
        val languageToLoad =langInitial // your language
        val locale = Locale(languageToLoad)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
       context.resources.updateConfiguration(
            config,
           context.resources.displayMetrics
        )

    }
}