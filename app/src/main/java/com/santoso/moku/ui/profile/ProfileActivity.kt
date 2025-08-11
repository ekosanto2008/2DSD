package com.santoso.moku.ui.profile

import android.app.DatePickerDialog
import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.rejowan.cutetoast.CuteToast
import com.santoso.moku.R
import com.santoso.moku.databinding.ActivityProfileBinding
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar
import androidx.core.net.toUri

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }
    private var selectedPhotoUri: Uri? = null

    private var currentPhotoUri: String? = null

    // Launcher untuk pilih gambar dari galeri
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val savedPath = saveImageToInternalStorage(it)
                selectedPhotoUri = Uri.fromFile(File(savedPath))
                Glide.with(this).load(selectedPhotoUri).circleCrop().into(binding.imgProfile)
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Isi email otomatis dari Firebase Auth
        binding.etEmail.setText(auth.currentUser?.email ?: "")

        // Spinner Jenis Kelamin
        val genderOptions = listOf("Laki-laki", "Perempuan")
        val genderAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, genderOptions)
        binding.spinnerGender.adapter = genderAdapter

        // Klik singkat foto untuk pilih dari galeri
        binding.imgProfile.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.imgProfile.setOnLongClickListener {
            val dialog = Dialog(this)
            dialog.setContentView(R.layout.dialog_fullscreen_image)
            val imgPreview = dialog.findViewById<ImageView>(R.id.imgPreview)

            val fotoUri = selectedPhotoUri?.toString() ?: currentPhotoUri

            if (!fotoUri.isNullOrEmpty()) {
                Glide.with(this).load(fotoUri).into(imgPreview)
            } else {
                imgPreview.setImageResource(android.R.drawable.ic_menu_report_image)
            }

            dialog.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            dialog.show()
            true
        }

        // DatePicker untuk Tanggal Lahir
        binding.etBirthDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    val selectedDate = "$dayOfMonth/${month + 1}/$year"
                    binding.etBirthDate.setText(selectedDate)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }

        // Load data dari Firestore jika ada
        loadUserData()

        // Simpan data ke Firestore
        binding.btnSave.setOnClickListener {
            saveUserData()
        }
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    binding.etFullName.setText(document.getString("nama"))
                    binding.etBirthPlace.setText(document.getString("tempatLahir"))
                    binding.etBirthDate.setText(document.getString("tanggalLahir"))
                    binding.etHeight.setText(document.getString("tinggiBadan"))
                    binding.etWeight.setText(document.getString("beratBadan"))

                    val gender = document.getString("jenisKelamin")
                    val genderIndex = if (gender == "Perempuan") 1 else 0
                    binding.spinnerGender.setSelection(genderIndex)

                    // Load foto profil
                    document.getString("fotoUri")?.let { uriString ->
                        if (uriString.isNotEmpty()) {
                            val uri = uriString.toUri()
                            selectedPhotoUri = uri // ✅ Simpan ke variabel biar long click berfungsi
                            Glide.with(this).load(uri).circleCrop().into(binding.imgProfile)
                        }
                    }
                }
            }
    }


    private fun saveUserData() {
        val nama = binding.etFullName.text.toString()
        val tempatLahir = binding.etBirthPlace.text.toString()
        val tanggalLahir = binding.etBirthDate.text.toString()
        val gender = binding.spinnerGender.selectedItem.toString()
        val tinggi = binding.etHeight.text.toString()
        val berat = binding.etWeight.text.toString()
        val email = binding.etEmail.text.toString()

        if (nama.isEmpty() || tempatLahir.isEmpty() || tanggalLahir.isEmpty()) {
            CuteToast.ct(this, "Lengkapi semua data!", CuteToast.LENGTH_SHORT, CuteToast.WARN, true).show()
            return
        }

        val userId = auth.currentUser?.uid ?: return

        val data = hashMapOf(
            "nama" to nama,
            "tempatLahir" to tempatLahir,
            "tanggalLahir" to tanggalLahir,
            "jenisKelamin" to gender,
            "tinggiBadan" to tinggi,
            "beratBadan" to berat,
            "email" to email,
            "fotoUri" to (selectedPhotoUri?.path ?: "")

        )

        db.collection("users").document(userId)
            .set(data)
            .addOnSuccessListener {
                CuteToast.ct(this, "Data berhasil disimpan!", CuteToast.LENGTH_SHORT, CuteToast.SUCCESS, true).show()
            }
            .addOnFailureListener {
                CuteToast.ct(this, "Gagal menyimpan data!", CuteToast.LENGTH_SHORT, CuteToast.ERROR, true).show()
            }
    }

    private fun saveImageToInternalStorage(uri: Uri): String {
        val fileName = "profile_image.jpg"
        val file = File(filesDir, fileName)
        contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        return file.absolutePath
    }


}
