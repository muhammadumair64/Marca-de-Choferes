package com.example.marcadechoferes.auth.createpassword

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.example.marcadechoferes.R
import com.example.marcadechoferes.auth.createpassword.viewModel.CreatePasswordViewModel
import com.example.marcadechoferes.databinding.ActivityCreateNewPasswordScreenBinding
import android.app.Activity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreateNewPasswordScreen : AppCompatActivity() {
    lateinit var binding: ActivityCreateNewPasswordScreenBinding
    val context: Context = this
    lateinit var imm:InputMethodManager
    val createPasswordViewModel: CreatePasswordViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this,R.layout.activity_create_new_password_screen)

        initView()
    }

    fun initView() {
        createPasswordViewModel.viewsForCreatePassword(context, binding)
         showSoftKeyboard(binding.editPassword)
    }
    fun showSoftKeyboard(view: View) {
        if (view.requestFocus()) {
            val imm = context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
    }

    override fun onDestroy() {
        super.onDestroy()

          println("function start")

    }

     fun closeKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }


}