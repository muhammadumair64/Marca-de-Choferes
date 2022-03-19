package com.logicasur.appchoferes.beforeAuth.createPasswordScreen

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.logicasur.appchoferes.Extra.BaseClass
import com.logicasur.appchoferes.utils.ResendApis
import com.logicasur.appchoferes.beforeAuth.createPasswordScreen.viewModel.CreatePasswordViewModel
import com.logicasur.appchoferes.Extra.Language
import com.logicasur.appchoferes.R
import com.logicasur.appchoferes.databinding.ActivityCreateNewPasswordScreenBinding
import com.logicasur.appchoferes.afterAuth.mainscreen.MainActivity
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class CreateNewPasswordScreen : BaseClass() {
    lateinit var binding: ActivityCreateNewPasswordScreenBinding
    val context: Context = this



    private val createPasswordViewModel: CreatePasswordViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val language=Language()
        language.setLanguage(baseContext)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_new_password_screen)

        initView()
    }

    private fun initView() {

        if(ResendApis.primaryColor.isEmpty() && ResendApis.secondaryColor.isEmpty()){
            ResendApis.primaryColor = "#7A59FC"
            ResendApis.secondaryColor ="#653FFB"
        }

        setGrad(ResendApis.primaryColor, ResendApis.secondaryColor,binding.SubmitBtn)
        createPasswordViewModel.viewsForCreatePassword(context, binding)
         showSoftKeyboard(binding.editPassword)
    }


    override fun onDestroy() {
        super.onDestroy()
          println("function start")

    }



    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }


    //-------------------------------------------------------utils-----------------------------------
    fun moveToNext(){
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finishAffinity()
    }
    fun closeKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
    private fun showSoftKeyboard(view: View) {
        if (view.requestFocus()) {
            val imm = context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
    }

}