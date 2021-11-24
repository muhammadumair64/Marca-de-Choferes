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
import android.transition.Fade

import android.widget.TextView
import androidx.lifecycle.MutableLiveData


@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {
    var activityContext: Context? = null
    var dataBinding: FragmentHomeBinding? = null
    var statusArrayList = ArrayList<String>()
    var searchedArrayList = ArrayList<String>()





    fun viewsForHomeFragment(context: Context, binding: FragmentHomeBinding) {
        activityContext = context
        dataBinding = binding
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
                dataBinding?.spacer?.setVisibility(View.GONE)

            } else {
                intent.startTimer()
                dataBinding?.bar?.progressBarColor= Color.parseColor("#7A59FC")
                dataBinding?.secondState?.setVisibility(View.GONE)
                dataBinding?.StateActive?.setVisibility(View.VISIBLE)
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
            dataBinding?.spacer?.setVisibility(View.VISIBLE)
            dataBinding?.secondState?.setVisibility(View.VISIBLE)
            dataBinding?.initialState?.setVisibility(View.GONE)

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
        val startSize = 15f // Size in pixels
        val endSize = 52f
        val animationDuration: Long = 500 // Animation duration in ms

        val animator = ValueAnimator.ofFloat(startSize, endSize)
        animator.duration = animationDuration

        animator.addUpdateListener { valueAnimator ->
            val animatedValue = valueAnimator.animatedValue as Float
            dataBinding?.workTimer?.textSize = animatedValue
        }

        animator.start()
    }
    fun workTimerLargeToSmall(){
        val startSize = 52f // Size in pixels
        val endSize = 15f
        val animationDuration: Long = 500 // Animation duration in ms

        val animator = ValueAnimator.ofFloat(startSize, endSize)
        animator.duration = animationDuration

        animator.addUpdateListener { valueAnimator ->
            val animatedValue = valueAnimator.animatedValue as Float
            dataBinding?.workTimer?.textSize = animatedValue
        }

        animator.start()
    }

    fun breakTimerSmallToLarge(){
        val startSize = 15f // Size in pixels
        val endSize = 52f
        val animationDuration: Long = 500 // Animation duration in ms

        val animator = ValueAnimator.ofFloat(startSize, endSize)
        animator.duration = animationDuration

        animator.addUpdateListener { valueAnimator ->
            val animatedValue = valueAnimator.animatedValue as Float
           dataBinding?.TimerBreak?.textSize = animatedValue
        }

        animator.start()
    }
    fun breakTimerLargeToSmall(){
        val startSize = 52f // Size in pixels
        val endSize = 15f
        val animationDuration: Long = 500 // Animation duration in ms

        val animator = ValueAnimator.ofFloat(startSize, endSize)
        animator.duration = animationDuration

        animator.addUpdateListener { valueAnimator ->
            val animatedValue = valueAnimator.animatedValue as Float
            dataBinding?.TimerBreak?.textSize = animatedValue
        }

        animator.start()
    }



}