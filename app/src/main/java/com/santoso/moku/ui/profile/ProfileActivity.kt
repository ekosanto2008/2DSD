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
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupGenderSpinner()
        setupListeners()
        observeProfileData()

        binding.etEmail.setText(viewModel.getUserEmail() ?: "")

        viewModel.loadUserData()
    }

    private fun setupGenderSpinner() {
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.gender_options,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerGender.adapter = adapter
    }

    private fun setupListeners() {
        binding.imgProfile.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // Tahan lama foto untuk lihat foto
        binding.imgProfile.setOnLongClickListener {
            val fotoPath = viewModel.getLocalImagePath()
            if (!fotoPath.isNullOrEmpty()) {
                showImageDialog(fotoPath)
            } else {
                CuteToast.ct(this, "Foto tidak tersedia", CuteToast.LENGTH_SHORT, CuteToast.WARN, true).show()
            }
            true
        }


        binding.etBirthDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    binding.etBirthDate.setText("$day/${month + 1}/$year")
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.btnSave.setOnClickListener {
            val nama = binding.etFullName.text.toString()
            val tempatLahir = binding.etBirthPlace.text.toString()
            val tanggalLahir = binding.etBirthDate.text.toString()
            val gender = binding.spinnerGender.selectedItem.toString()
            val tinggi = binding.etHeight.text.toString()
            val berat = binding.etWeight.text.toString()
            val email = binding.etEmail.text.toString()

            if (nama.isEmpty() || tempatLahir.isEmpty() || tanggalLahir.isEmpty()) {
                CuteToast.ct(this, "Lengkapi semua data!", CuteToast.LENGTH_SHORT, CuteToast.WARN, true).show()
                return@setOnClickListener
            }

            viewModel.saveProfile(
                nama, tempatLahir, tanggalLahir, gender, tinggi, berat, email, selectedPhotoUri
            ) { success ->
                if (success) {
                    CuteToast.ct(this, "Data berhasil disimpan!", CuteToast.LENGTH_SHORT, CuteToast.SUCCESS, true).show()
                } else {
                    CuteToast.ct(this, "Gagal menyimpan data!", CuteToast.LENGTH_SHORT, CuteToast.ERROR, true).show()
                }
            }
        }
    }

    private fun observeProfileData() {
        viewModel.profileData.observe(this) { data ->
            data?.let {
                binding.etFullName.setText(it["nama"] as? String ?: "")
                binding.etBirthPlace.setText(it["tempatLahir"] as? String ?: "")
                binding.etBirthDate.setText(it["tanggalLahir"] as? String ?: "")
                binding.etHeight.setText(it["tinggiBadan"] as? String ?: "")
                binding.etWeight.setText(it["beratBadan"] as? String ?: "")

                val gender = it["jenisKelamin"] as? String ?: ""
                val genderIndex = resources.getStringArray(R.array.gender_options).indexOf(gender)
                if (genderIndex >= 0) binding.spinnerGender.setSelection(genderIndex)

                val fotoPath = it["fotoUri"] as? String
                if (!fotoPath.isNullOrEmpty()) {
                    Glide.with(this).load(fotoPath).circleCrop().into(binding.imgProfile)
                } else {
                    viewModel.getLocalImagePath()?.let { path ->
                        Glide.with(this).load(path).circleCrop().into(binding.imgProfile)
                    }
                }
            }
        }
    }

    private fun showImageDialog(imagePath: String) {
        val dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.dialog_fullscreen_image)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val imageView = dialog.findViewById<android.widget.ImageView>(R.id.fullImageView)
        val btnClose = dialog.findViewById<android.widget.ImageButton>(R.id.btnClose)

        Glide.with(this)
            .load(imagePath)
            .into(imageView)

        btnClose.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }


}
