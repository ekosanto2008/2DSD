package com.santoso.moku.ui.profile

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.santoso.moku.data.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repo: ProfileRepository
) : ViewModel() {

    val nik = MutableLiveData<String>()
    val fullName = MutableLiveData<String>()
    val birthPlace = MutableLiveData<String>()
    val birthDate = MutableLiveData<String>()
    val gender = MutableLiveData<String>()
    val height = MutableLiveData<String>()
    val weight = MutableLiveData<String>()
    val email = MutableLiveData<String>()
    val photoPath = MutableLiveData<String?>()

    val loading = MutableLiveData<Boolean>()
    val error = MutableLiveData<String?>()
    val loadSuccess = MutableLiveData<Boolean>() // untuk load data
    val saveSuccess = MutableLiveData<Boolean>() // untuk save data

    fun loadProfile() {
        viewModelScope.launch {
            loading.value = true
            error.value = null
            try {
                val data = repo.loadProfile()
                fullName.value   = (data["nama"] as? String).orEmpty()
                nik.value = (data["nik"] as? String).orEmpty()
                birthPlace.value = (data["tempatLahir"] as? String).orEmpty()
                birthDate.value  = (data["tanggalLahir"] as? String).orEmpty()
                gender.value     = (data["jenisKelamin"] as? String).orEmpty()
                height.value     = (data["tinggiBadan"] as? String).orEmpty()
                weight.value     = (data["beratBadan"] as? String).orEmpty()
                email.value      = repo.currentEmail().ifEmpty { (data["email"] as? String).orEmpty() }
                photoPath.value  = (data["fotoUri"] as? String).orEmpty().ifEmpty { null }

                loadSuccess.value = true
            } catch (e: Exception) {
                error.value = e.message
                loadSuccess.value = false
            } finally {
                loading.value = false
            }
        }
    }

    fun saveProfile() {
        viewModelScope.launch {
            loading.value = true
            error.value = null
            try {
                val data = mapOf(
                    "nama"         to (fullName.value ?: ""),
                    "nik" to (nik.value ?: ""),
                    "tempatLahir"  to (birthPlace.value ?: ""),
                    "tanggalLahir" to (birthDate.value ?: ""),
                    "jenisKelamin" to (gender.value ?: ""),
                    "tinggiBadan"  to (height.value ?: ""),
                    "beratBadan"   to (weight.value ?: ""),
                    "email"        to (email.value ?: ""),
                    "fotoUri"      to (photoPath.value ?: "")
                )
                repo.saveProfile(data)
                saveSuccess.value = true
            } catch (e: Exception) {
                error.value = e.message
                saveSuccess.value = false
            } finally {
                loading.value = false
            }
        }
    }
}
