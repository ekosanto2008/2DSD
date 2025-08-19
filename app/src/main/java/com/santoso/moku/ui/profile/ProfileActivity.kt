package com.santoso.moku.ui.profile

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.rejowan.cutetoast.CuteToast
import com.santoso.moku.R
import com.santoso.moku.databinding.ActivityProfileBinding
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.Calendar

@AndroidEntryPoint
class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private val viewModel: ProfileViewModel by viewModels()
    private var selectedPhotoUri: Uri? = null

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedPhotoUri = it
                Glide.with(this).load(it).circleCrop().into(binding.imgProfile)
                val savedPath = saveImageToLocal(it)
                viewModel.photoPath.value = savedPath
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupGenderSpinner()
        setupDatePicker()
        setupObservers()

        // Klik foto untuk pilih dari galeri
        binding.imgProfile.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // Tekan lama untuk melihat foto
        binding.imgProfile.setOnLongClickListener {
            viewModel.photoPath.value?.let { path ->
                if (path.isNotEmpty()) showFullImageDialog(path)
            }
            true
        }

        // Tombol simpan
        binding.btnSave.setOnClickListener {
            val nik = binding.etNik.text.toString().trim()

            if (nik.isEmpty()) {
                CuteToast.ct(
                    this,
                    "NIK tidak boleh kosong",
                    CuteToast.LENGTH_SHORT,
                    CuteToast.WARN,
                    true
                ).show()
                return@setOnClickListener
            }

            viewModel.fullName.value = binding.etFullName.text.toString()
            viewModel.nik.value = binding.etNik.text.toString()
            viewModel.birthPlace.value = binding.etBirthPlace.text.toString()
            viewModel.birthDate.value = binding.etBirthDate.text.toString()
            viewModel.height.value = binding.etHeight.text.toString()
            viewModel.weight.value = binding.etWeight.text.toString()
            viewModel.email.value = binding.etEmail.text.toString()
            viewModel.gender.value = binding.spinnerGender.selectedItem.toString()
            viewModel.saveProfile()
        }

        // Load data
        viewModel.loadProfile()
    }

    private fun setupGenderSpinner() {
        val genderOptions = resources.getStringArray(R.array.gender_options).toList()
        val genderAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, genderOptions)
        binding.spinnerGender.adapter = genderAdapter
    }

    private fun setupDatePicker() {
        binding.etBirthDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    binding.etBirthDate.setText("$dayOfMonth/${month + 1}/$year")
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }
    }

    private fun setupObservers() {
        viewModel.loadSuccess.observe(this) { ok ->
            if (ok == true) {
                binding.etFullName.setText(viewModel.fullName.value)
                binding.etNik.setText(viewModel.nik.value)
                binding.etBirthPlace.setText(viewModel.birthPlace.value)
                binding.etBirthDate.setText(viewModel.birthDate.value)
                binding.etHeight.setText(viewModel.height.value)
                binding.etWeight.setText(viewModel.weight.value)
                binding.etEmail.setText(viewModel.email.value)

                if (!viewModel.photoPath.value.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(viewModel.photoPath.value)
                        .placeholder(R.drawable.ic_user)
                        .circleCrop()
                        .into(binding.imgProfile)
                } else {
                    binding.imgProfile.setImageResource(R.drawable.ic_user)
                }


                val genderOptions = resources.getStringArray(R.array.gender_options)
                val index = genderOptions.indexOf(viewModel.gender.value ?: "")
                if (index >= 0) binding.spinnerGender.setSelection(index)
            }
        }

        viewModel.saveSuccess.observe(this) { ok ->
            if (ok == true) {
                CuteToast.ct(
                    this,
                    "Data berhasil disimpan",
                    CuteToast.LENGTH_SHORT,
                    CuteToast.SUCCESS,
                    true
                ).show()
            }
        }

        viewModel.error.observe(this) { err ->
            err?.let {
                CuteToast.ct(this, it, CuteToast.LENGTH_SHORT, CuteToast.ERROR, true).show()
            }
        }
    }

    private fun saveImageToLocal(uri: Uri): String {
        val file = File(filesDir, "profile_image_${System.currentTimeMillis()}.jpg")
        contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                copyStream(inputStream, outputStream)
            }
        }
        return file.absolutePath
    }

    private fun copyStream(input: InputStream, output: FileOutputStream) {
        val buffer = ByteArray(1024)
        var bytesRead: Int
        while (input.read(buffer).also { bytesRead = it } != -1) {
            output.write(buffer, 0, bytesRead)
        }
    }

    private fun showFullImageDialog(path: String) {
        val dialog = android.app.Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setContentView(R.layout.dialog_fullscreen_image)
        val imageView = dialog.findViewById<android.widget.ImageView>(R.id.fullImageView)
        val closeButton = dialog.findViewById<android.widget.ImageView>(R.id.btnClose)
        Glide.with(this).load(path).into(imageView)
        closeButton.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}
