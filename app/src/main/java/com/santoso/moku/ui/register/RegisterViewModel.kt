package com.santoso.moku.ui.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.santoso.moku.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _registerResult = MutableLiveData<Pair<Boolean, String?>>()
    val registerResult: LiveData<Pair<Boolean, String?>> = _registerResult

    fun register(email: String, password: String) {
        viewModelScope.launch {
            try {
                val result = repository.register(email, password)
                if (result.isSuccess) {
                    _registerResult.postValue(Pair(true, null))
                } else {
                    _registerResult.postValue(Pair(false, result.exceptionOrNull()?.message))
                }
            } catch (e: Exception) {
                _registerResult.postValue(Pair(false, e.message))
            }
        }
    }
}

