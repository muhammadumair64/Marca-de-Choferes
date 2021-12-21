package com.example.marcadechoferes.auth.createpassword

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.example.marcadechoferes.Extra.BaseClass
import com.example.marcadechoferes.Extra.K
import com.example.marcadechoferes.R
import com.example.marcadechoferes.auth.createpassword.viewModel.CreatePasswordViewModel
import com.example.marcadechoferes.databinding.ActivityCreateNewPasswordScreenBinding
import com.example.marcadechoferes.Extra.Language
import com.example.marcadechoferes.mainscreen.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreateNewPasswordScreen : BaseClass() {
    lateinit var binding: ActivityCreateNewPasswordScreenBinding
    val context: Context = this
    lateinit var imm:InputMethodManager
    val createPasswordViewModel: CreatePasswordViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val language=Language()
        language.setLanguage(baseContext)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_create_new_password_screen)

        initView()
    }

    fun initView() {
        setGrad(K.primaryColor, K.secondrayColor,binding.SubmitBtn)
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


    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    fun moveToNext(){
        var intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finishAffinity()
    }


}