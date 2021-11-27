package com.example.marcadechoferes.mainscreen.home.viewmodel

import android.content.Context
import android.opengl.Visibility
import android.view.View
import android.view.animation.AnimationUtils
import androidx.core.view.marginStart
import androidx.lifecycle.ViewModel
import com.example.marcadechoferes.R
import com.example.marcadechoferes.databinding.FragmentHomeBinding
import com.example.marcadechoferes.mainscreen.MainActivity
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import javax.inject.Inject
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.graphics.Color
import android.graphics.Typeface
import android.transition.Fade
import android.util.Log

import android.widget.TextView
import androidx.lifecycle.MutableLiveData


@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {
    var activityContext: Context? = null
    var dataBinding: FragmentHomeBinding? = null
    var statusArrayList = ArrayList<String>()
    var searchedArrayList = ArrayList<String>()
       var max:Float? =null
    var mini:Float? =null






    fun viewsForHomeFragment(context: Context, binding: FragmentHomeBinding) {
        activityContext = context
        dataBinding = binding
        setMaxMini()
        dataBinding?.bar?.progressBarColor= Color.parseColor("#C1B1FF")
    }


    fun statusRV() {
        statusArrayList.clear()
        statusArrayList.add("llegada al cliente")
        statusArrayList.add("salida del cliente")
        statusArrayList.add("espera")
        statusArrayList.add("inicio de carga")
        statusArrayList.add("fin de la carga")
        statusArrayList.add("iniciar descarga")
        statusArrayList.add("fin de la descarga")

    }

    fun dammyData() {
        searchedArrayList.clear()
        searchedArrayList.add("V-ad789f6a7f6a789df8a6sdf78a6sf78a")
        searchedArrayList.add("V-td789f6a7f6a789df8a6sdf78a6sf78a")
        searchedArrayList.add("V-yd789f6a7f6a789df8a6sdf78a6sf78a")
        searchedArrayList.add("V-yd789f6a7f6a789df8a6sdf78a6sf78a")
        searchedArrayList.add("V9384d789f6a7f6a789df8a6sdf78a6sf78a")

    }


    fun Workbar() {

        dataBinding!!.bar.apply {
            // Set Progress
            progress = 2f
            // or with animation
            setProgressWithAnimation(25f, 1000) // =1s

            // Set Progress Max
            progressMax = 200f

            // Set ProgressBar Color
            // or with gradient
//            progressBarColorStart = R.color.gradient_start
//            progressBarColorEnd = R.color.gradient_end
//            progressBarColorDirection = CircularProgressBar.GradientDirection.TOP_TO_BOTTOM

            // Set background ProgressBar Color

            // or with gradient
//            backgroundProgressBarColorStart = Color.WHITE
//            backgroundProgressBarColorEnd = Color.RED


            // Set Width
           // in DP

            // Other
            roundBorder = true
            progressDirection = CircularProgressBar.ProgressDirection.TO_RIGHT
        }

    }

    fun Breakbar() {


        dataBinding!!.breakBar.apply {
            // Set Progress
//            progress = 2f
            // or with animation
            setProgressWithAnimation(50f, 1000) // =1s

            // Set Progress Max
            progressMax = 200f

            // Set ProgressBar Color
            // or with gradient

            dataBinding?.breakBar?.progressBarColor= Color.parseColor("#FFD6D9")
//            progressBarColorStart = R.color.redGradiendStart
//            progressBarColorEnd = R.color.redGradientend
//            progressBarColorDirection = CircularProgressBar.GradientDirection.TOP_TO_BOTTOM

            // Set background ProgressBar Color

            backgroundProgressBarColor=Color.TRANSPARENT
            // Set Width // in DP

            // Other
            roundBorder = true
            progressDirection = CircularProgressBar.ProgressDirection.TO_RIGHT
        }


    }

    fun timers() {
        var intent = (activityContext as MainActivity)

        dataBinding?.secondState?.setOnClickListener {

            if (dataBinding?.secondState?.text == "End Break"||dataBinding?.secondState?.text =="Fin del descanso") {
                intent.stopTimerB()
                dataBinding?.breakBar?.progressBarColor= Color.parseColor("#FFD6D9")
                intent.startTimer()
                dataBinding?.bar?.progressBarColor= Color.parseColor("#7A59FC")
                breakTimerLargeToSmall()
                workTimerSmallToLarge()

                dataBinding?.secondState?.setVisibility(View.GONE)
                dataBinding?.StateActive?.setVisibility(View.VISIBLE)
                dataBinding?.vehicleListBtn?.isClickable=false
                dataBinding?.spacer?.setVisibility(View.GONE)

            } else {
                intent.startTimer()
                dataBinding?.bar?.progressBarColor= Color.parseColor("#7A59FC")
                dataBinding?.secondState?.setVisibility(View.GONE)
                dataBinding?.StateActive?.setVisibility(View.VISIBLE)
                dataBinding?.vehicleListBtn?.isClickable=false
                dataBinding?.spacer?.setVisibility(View.GONE)
            }
        }

        dataBinding?.TakeBreak?.setOnClickListener {
            intent.stopTimer()
            dataBinding?.bar?.progressBarColor= Color.parseColor("#C1B1FF")
            intent.startTimerB()
            dataBinding?.breakBar?.progressBarColor= Color.parseColor("#FF4D4E")
            workTimerLargeToSmall()
            breakTimerSmallToLarge()

            dataBinding?.StateActive?.setVisibility(View.GONE)
            dataBinding?.vehicleListBtn?.isClickable=true
            dataBinding?.spacer?.setVisibility(View.VISIBLE)
            dataBinding?.secondState?.text = "Fin del descanso"
            dataBinding?.secondState?.setVisibility(View.VISIBLE)

        }

        dataBinding?.EndDay?.setOnClickListener {
            intent.stopTimer()
            intent.stopTimerB()

            dataBinding?.bar?.progressBarColor= Color.parseColor("#C1B1FF")
            dataBinding?.breakBar?.progressBarColor= Color.parseColor("#FFD6D9")
            dataBinding?.StateActive?.setVisibility(View.GONE)
            dataBinding?.vehicleListBtn?.isClickable=true
            dataBinding?.spacer?.setVisibility(View.VISIBLE)
            dataBinding?.secondState?.setVisibility(View.GONE)
            dataBinding?.initialState?.setVisibility(View.VISIBLE)
            dataBinding?.vehicleListBtn?.setBackgroundResource(R.drawable.item_popup_btn_bg)
            dataBinding?.iconCar?.setBackgroundResource(R.drawable.ic_icon_awesome_car_alt)
            dataBinding?.vehicleNameSelected?.setTextColor(Color.parseColor("#000000"))
            dataBinding?.vehicleNameSelected?.text = "Vehículo"
            dataBinding?.Arrow?.setVisibility(View.VISIBLE)
            dataBinding?.dots?.visibility=View.GONE
            (activityContext as MainActivity).time = 0.0
            dataBinding?.workTimer?.text="00:00"
            dataBinding?.TimerBreak?.text="00:00"
            dataBinding?.statusSelected?.text="Selección estado"
            dataBinding?.statusListBtn?.visibility=View.GONE
            dataBinding?.vehicleNameSelected?.setTypeface(dataBinding?.vehicleNameSelected?.getTypeface(), Typeface.NORMAL)

        }

        dataBinding?.initialState?.setOnClickListener {
            (activityContext as MainActivity).time = 0.0
//            intent.startTimer()
//            dataBinding?.spacer?.setVisibility(View.VISIBLE)
//            dataBinding?.initialState?.setVisibility(View.GONE)
//            dataBinding?.secondState?.setVisibility(View.VISIBLE)
//            dataBinding?.bar?.progressBarColor= Color.parseColor("#7A59FC")


        }


    }





    fun workTimerSmallToLarge(){
        dataBinding?.workTimer?.setTypeface(dataBinding?.workTimer?.getTypeface(), Typeface.BOLD)
        val startSize = mini // Size in pixels
        val endSize = max
        val animationDuration: Long = 500 // Animation duration in ms

        val animator = ValueAnimator.ofFloat(startSize!!, endSize!!)
        animator.duration = animationDuration

        animator.addUpdateListener { valueAnimator ->
            val animatedValue = valueAnimator.animatedValue as Float
            dataBinding?.workTimer?.textSize = animatedValue
        }

        animator.start()
    }
    fun workTimerLargeToSmall(){
        dataBinding?.workTimer?.setTypeface(dataBinding?.workTimer?.getTypeface(), Typeface.NORMAL)
        val startSize = max // Size in pixels
        val endSize = mini
        val animationDuration: Long = 500 // Animation duration in ms

        val animator = ValueAnimator.ofFloat(startSize!!, endSize!!)
        animator.duration = animationDuration

        animator.addUpdateListener { valueAnimator ->
            val animatedValue = valueAnimator.animatedValue as Float
            dataBinding?.workTimer?.textSize = animatedValue
        }

        animator.start()
    }

    fun breakTimerSmallToLarge(){
        dataBinding?.TimerBreak?.setTypeface(dataBinding?.TakeBreak?.getTypeface(), Typeface.BOLD)
        val startSize = mini // Size in pixels
        val endSize = max
        val animationDuration: Long = 500 // Animation duration in ms

        val animator = ValueAnimator.ofFloat(startSize!!, endSize!!)
        animator.duration = animationDuration

        animator.addUpdateListener { valueAnimator ->
            val animatedValue = valueAnimator.animatedValue as Float
           dataBinding?.TimerBreak?.textSize = animatedValue
        }

        animator.start()
    }
    fun breakTimerLargeToSmall(){
        dataBinding?.TimerBreak?.setTypeface(dataBinding?.TakeBreak?.getTypeface(), Typeface.NORMAL)
        val startSize = max// Size in pixels
        val endSize = mini
        val animationDuration: Long = 500 // Animation duration in ms

        val animator = ValueAnimator.ofFloat(startSize!!, endSize!!)
        animator.duration = animationDuration

        animator.addUpdateListener { valueAnimator ->
            val animatedValue = valueAnimator.animatedValue as Float
            dataBinding?.TimerBreak?.textSize = animatedValue
        }

        animator.start()
    }
fun setMaxMini(){
    var temp:Float? = (activityContext as MainActivity).dpWidth

  if(temp!! >= 800.0){
      max=100f
      mini=30f
  }else{
      max=42f
      mini=12f

  }


}





}