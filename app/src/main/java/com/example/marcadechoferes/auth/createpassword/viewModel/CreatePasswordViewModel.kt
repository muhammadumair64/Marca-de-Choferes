package com.example.marcadechoferes.auth.createpassword.viewModel

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.example.marcadechoferes.loadingScreen.LoadingScreen
import com.example.marcadechoferes.R
import com.example.marcadechoferes.auth.createpassword.CreateNewPasswordScreen
import com.example.marcadechoferes.databinding.ActivityCreateNewPasswordScreenBinding
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
@HiltViewModel
class CreatePasswordViewModel @Inject constructor():ViewModel() {

    fun viewsForCreatePassword(context: Context,binding: ActivityCreateNewPasswordScreenBinding){
        binding.backButton.setOnClickListener {

            (context as Activity).finish()
        }
        binding.showPassBtn.setOnClickListener {
            if (binding.editPassword.transformationMethod.equals(PasswordTransformationMethod.getInstance())) {
                binding.editPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance())
                binding.showPassBtn.setImageResource(R.drawable.hide_password)
            } else {
                binding.editPassword.setTransformationMethod(PasswordTransformationMethod.getInstance())
                binding.showPassBtn.setImageResource(R.drawable.ic_icon_visibility)
            }
        }

        binding.showRepeatPassBtn.setOnClickListener {

            if (binding.repeatPassword.transformationMethod.equals(PasswordTransformationMethod.getInstance())) {
                binding.repeatPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance())
                binding.showRepeatPassBtn.setImageResource(R.drawable.hide_password)
            } else {
                binding.repeatPassword.setTransformationMethod(PasswordTransformationMethod.getInstance())
                binding.showRepeatPassBtn.setImageResource(R.drawable.ic_icon_visibility)
            }

        }

    binding.SubmitBtn.setOnClickListener {
        var intent= Intent(context, LoadingScreen::class.java)
        ContextCompat.startActivity(context, intent, Bundle.EMPTY)
        (context as CreateNewPasswordScreen).finish()
        (context as CreateNewPasswordScreen).closeKeyboard()

    }


    }

}