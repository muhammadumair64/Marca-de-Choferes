package com.logicasur.appchoferes.Extra

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

open class BaseClass:AppCompatActivity() {

    fun setGrad(primaryColor: String, secondaryColor: String, startButton: AppCompatButton): GradientDrawable {
        //set status Bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.setStatusBarColor(Color.parseColor(primaryColor))
        }

        //convert Color #code into Int
        val primaryColorInt = Color.parseColor(primaryColor)
        val secondaryColorInt = Color.parseColor(secondaryColor)

        //set Button gradient
        val gd = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(primaryColorInt, secondaryColorInt)
        )
        gd.cornerRadius = 17f

         startButton.background = gd
        return gd
    }
}