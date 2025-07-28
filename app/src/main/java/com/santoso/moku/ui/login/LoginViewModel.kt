package com.santoso.moku.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.santoso.moku.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _loginResult = MutableLiveData<Pair<Boolean, String?>>()
    val loginResult: LiveData<Pair<Boolean, String?>> = _loginResult

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                val result = repository.login(email, password)
                if (result.isSuccess) {
                    _loginResult.postValue(Pair(true, null))
                } else {
                    _loginResult.postValue(Pair(false, result.exceptionOrNull()?.message))
                }
            } catch (e: Exception) {
                _loginResult.postValue(Pair(false, e.message))
            }
        }
    }

    fun isUserLoggedIn(): Boolean = repository.isUserLoggedIn()
}
