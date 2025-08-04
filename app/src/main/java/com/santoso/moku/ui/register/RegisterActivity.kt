package com.santoso.moku.ui.register

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.rejowan.cutetoast.CuteToast
import com.santoso.moku.databinding.ActivityRegisterBinding
import com.santoso.moku.ui.login.LoginActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty()) {
                CuteToast.ct(this, "Email tidak boleh kosong", CuteToast.LENGTH_SHORT, CuteToast.WARN, true).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                CuteToast.ct(this, "Password minimal 6 karakter", CuteToast.LENGTH_SHORT, CuteToast.WARN, true).show()
                return@setOnClickListener
            }

            val passwordRegex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{6,}$")
            if (!password.matches(passwordRegex)) {
                CuteToast.ct(this, "Password harus mengandung huruf besar, huruf kecil, dan angka", CuteToast.LENGTH_SHORT, CuteToast.WARN, true).show()
                return@setOnClickListener
            }


            viewModel.register(email, password)
        }

        binding.tvLoginLink.setOnClickListener {
            finish() // atau startActivity(Intent(this, LoginActivity::class.java)) jika kamu ingin eksplisit
        }

        viewModel.registerResult.observe(this) { (success, _) ->
            if (success) {
                CuteToast.ct(this, "Registrasi berhasil. Silakan login.", CuteToast.LENGTH_SHORT, CuteToast.SUCCESS, true).show()

                FirebaseAuth.getInstance().signOut() // ⬅️ Logout otomatis

                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            } else {
                CuteToast.ct(this, "Registrasi gagal:", CuteToast.LENGTH_SHORT, CuteToast.ERROR, true).show()
            }
        }


    }
}