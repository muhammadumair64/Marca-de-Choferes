package com.example.marcadechoferes.auth.createpassword

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.example.marcadechoferes.R
import com.example.marcadechoferes.auth.createpassword.viewModel.CreatePasswordViewModel
import com.example.marcadechoferes.databinding.ActivityCreateNewPasswordScreenBinding

class CreateNewPasswordScreen : AppCompatActivity() {
    lateinit var binding: ActivityCreateNewPasswordScreenBinding
    val context: Context = this
    val createPasswordViewModel: CreatePasswordViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this,R.layout.activity_create_new_password_screen)

        initView()
    }

    fun initView() {
        createPasswordViewModel.viewsForCreatePassword(context, binding)

    }


}