package com.logicasur.appchoferes.utils

import androidx.lifecycle.ViewModel
import com.logicasur.appchoferes.Extra.TinyDB
import com.logicasur.appchoferes.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
@HiltViewModel
class TestingViewModel@Inject constructor(val authRepository: AuthRepository,val tinyDB: TinyDB): ViewModel() {
    fun servercheck123()
    {
        tinyDB.putString("Cookie","choferes=s%3AX2CFlw9SiZO4JZUGp_RNxbhVeo3Ee5No.E1wheEI%2FfEY1KouEF1bVY4VZtJLY1QmO3D%2FnJDeVSa4")
//        ServerCheck.authRepository=authRepository
    }
}