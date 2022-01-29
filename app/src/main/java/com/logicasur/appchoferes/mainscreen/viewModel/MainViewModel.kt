package com.logicasur.appchoferes.mainscreen.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor():ViewModel() {

    val navigationLiveData =MutableLiveData<String>("0")
    val popupLiveData =MutableLiveData<Int>(0)
    fun updateValueTo2(){
        navigationLiveData.value="2"

    }
    fun valueReset(){
        popupLiveData.value=0
    }

 fun updateValueTo1(){
    navigationLiveData.value="1"
    println("My value ${navigationLiveData.value}")
}
    fun updateValueTO3(){
        navigationLiveData.value="3"
    }


    fun updatePopupValue(){
        popupLiveData.value = popupLiveData.value?.plus(1)
    }
}