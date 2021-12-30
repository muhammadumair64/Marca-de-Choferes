package com.example.marcadechoferes.Extra

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler

import android.app.job.JobService
import android.content.ComponentName
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.marcadechoferes.myApplication.MyApplication


class MyJobScheduler : JobService() {
    override fun onStartJob(jobParameters: JobParameters): Boolean {
    Log.d("isMyJobisWorking fine","Yeah its working fine")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            scheduleJobFirebaseToRoomDataUpdate()
        }


        this.stopSelf()
        Toast.makeText(MyApplication.appContext, "Schedule Job is Working", Toast.LENGTH_SHORT).show()
        return true
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onStopJob(jobParameters: JobParameters): Boolean {
        Log.d("OnStop Called Once ", " yes ")

        return true
    }



    @RequiresApi(Build.VERSION_CODES.N)
    private fun scheduleJobFirebaseToRoomDataUpdate() {
        val jobScheduler = applicationContext
            .getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
        val componentName = ComponentName(
            this,
            MyJobScheduler::class.java
        )
        val jobInfo = JobInfo.Builder(123, componentName)
            .setMinimumLatency(10000)
            .setRequiredNetworkType(
                JobInfo.NETWORK_TYPE_NOT_ROAMING
            )
            .setPersisted(true)
        var result= jobScheduler.schedule(jobInfo.build())
        if(result== JobScheduler.RESULT_SUCCESS){
        }

    }
}
