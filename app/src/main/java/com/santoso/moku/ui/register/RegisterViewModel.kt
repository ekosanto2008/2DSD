package com.santoso.moku.ui.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor() : ViewModel() {

    private val _registerResult = MutableLiveData<Pair<Boolean, String?>>()
    val registerResult: LiveData<Pair<Boolean, String?>> get() = _registerResult

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun registerUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.sendEmailVerification()
                        ?.addOnSuccessListener {
                            _registerResult.value = Pair(true, "Registrasi berhasil! Cek email untuk verifikasi.")
                        }
                        ?.addOnFailureListener { e ->
                            _registerResult.value = Pair(false, "Gagal mengirim email verifikasi: ${e.message}")
                        }
                } else {
                    _registerResult.value = Pair(false, task.exception?.message)
                }
            }
    }
}
