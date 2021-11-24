package com.example.marcadechoferes.mainscreen.profile.viewmodel

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.example.marcadechoferes.auth.createpassword.CreateNewPasswordScreen
import com.example.marcadechoferes.databinding.FragmentProfileBinding
import com.example.marcadechoferes.mainscreen.MainActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor()  :ViewModel()  {


    fun viewsForFragment(context:Context,binding:FragmentProfileBinding){

        binding.edit.setOnClickListener {

            var intent= Intent(context, CreateNewPasswordScreen::class.java)
            ContextCompat.startActivity(context, intent, Bundle.EMPTY)
            (context as MainActivity).finish()

        }


    }
}