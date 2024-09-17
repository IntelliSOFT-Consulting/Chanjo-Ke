package com.intellisoft.chanjoke.detail.ui.main.registration

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdminLiveData : ViewModel() {

    private val _adminData = MutableLiveData<String>().apply {
        value = "" // Initial value is an empty mutable list
    }
    val adminData: LiveData<String> = _adminData

    fun updatePatientDetails(boolean: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _adminData.postValue(boolean)
            }
        }
    }
}