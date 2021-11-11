package com.example.marcadechoferes.auth.otp.viewModel

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.ViewModel
import com.example.marcadechoferes.auth.createpassword.CreateNewPasswordScreen
import com.example.marcadechoferes.databinding.ActivityOtpBinding
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
@HiltViewModel
class OTPViewModel @Inject constructor():ViewModel() {

fun viewsForOTPScreen(context: Context,binding: ActivityOtpBinding){
    binding.edt1.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {

        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            if (s.toString().trim().isEmpty()) {
                binding.edt2.requestFocus()
            }
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        }
    })
    binding.edt2.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
        }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            if (s.toString().trim().isEmpty()) {
                binding.edt3.requestFocus()
            }
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        }
    })
    binding.edt3.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            if (s.toString().trim().isEmpty()) {
                binding.edt4.requestFocus()
            }
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        }
    })

    //otp text back press
    binding.edt2.setOnKeyListener(object : View.OnKeyListener {
        override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
            //You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_
            if (event!!.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_DEL
                || binding.edt2.text==null) {
                //this is for backspace
                binding.edt1.requestFocus()

            }
            return false
        }
    })
    binding.edt3.setOnKeyListener(object : View.OnKeyListener {
        override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
            //You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_
            if (event!!.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_DEL
                || binding.edt3.text==null) {
                //this is for backspace
                binding.edt2.requestFocus()

            }
            return false
        }
    })
    binding.edt4.setOnKeyListener(object : View.OnKeyListener {
        override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
            //You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_
            if (event!!.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_DEL
                || binding.edt4.text==null) {
                //this is for backspace
                binding.edt3.requestFocus()

            }
            return false
        }
    })

    //Submit Button
    binding.SubmitButton.setOnClickListener {
        var intent= Intent(context, CreateNewPasswordScreen::class.java)
        startActivity(context,intent, Bundle.EMPTY)
    }

    //Back button
    binding.backButton.setOnClickListener {
        (context as Activity).finish()
    }

}


}