package com.logicasur.appchoferes

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.logicasur.appchoferes.Extra.ResendApis
import com.logicasur.appchoferes.utils.TestingViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TestingScreen : AppCompatActivity() {
    lateinit var send: TextView
    val viewModel: TestingViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_testing_screen)
        viewModel.servercheck123()
        initView()


    }

    fun initView() {
        send = findViewById(R.id.send)
        send.setOnClickListener {

            lifecycleScope.launch(Dispatchers.IO) {
//                var check = ResendApis.isConnected()
//                Log.d("Testing_net","$check")

//                ServerCheck.serverCheck(null) {
//                    forToast()
//                }
//
//

            }
        }
    }

    fun forToast() {
        lifecycleScope.launch {
            Toast.makeText(
                this@TestingScreen,
                "APi hit Successfully",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}