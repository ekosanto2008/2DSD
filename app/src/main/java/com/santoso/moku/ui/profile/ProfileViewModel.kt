package com.santoso.moku.ui.profile

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.santoso.moku.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepository
) : ViewModel() {

    private val _profileData = MutableLiveData<Map<String, Any>?>()
    val profileData: LiveData<Map<String, Any>?> get() = _profileData

    fun loadUserData() {
        repository.getProfileData { data ->
            _profileData.postValue(data)
        }
    }

    fun getUserEmail(): String? = repository.getUserEmail()

    fun saveProfile(
        nama: String,
        tempatLahir: String,
        tanggalLahir: String,
        jenisKelamin: String,
        tinggiBadan: String,
        beratBadan: String,
        email: String,
        fotoUri: Uri?,
        callback: (Boolean) -> Unit
    ) {
        val fotoPath = fotoUri?.let { repository.saveImageToLocal(it) } ?: repository.getLocalImagePath().orEmpty()

        val data = mapOf(
            "nama" to nama,
            "tempatLahir" to tempatLahir,
            "tanggalLahir" to tanggalLahir,
            "jenisKelamin" to jenisKelamin,
            "tinggiBadan" to tinggiBadan,
            "beratBadan" to beratBadan,
            "email" to email,
            "fotoUri" to fotoPath
        )

        repository.saveProfileData(data, callback)
    }

    fun getLocalImagePath(): String? = repository.getLocalImagePath()
}
