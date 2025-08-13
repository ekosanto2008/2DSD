package com.santoso.moku.ui.register

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.rejowan.cutetoast.CuteToast
import com.santoso.moku.R
import com.santoso.moku.databinding.ActivityRegisterBinding
import com.santoso.moku.ui.login.LoginActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: RegisterViewModel by viewModels()
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        window.statusBarColor = ContextCompat.getColor(this, R.color.system_ui_color)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.system_ui_color)
        setSystemUIColor()

        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                CuteToast.ct(this, "Semua field wajib diisi", CuteToast.LENGTH_SHORT, CuteToast.WARN, true).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                CuteToast.ct(this, "Password dan konfirmasi tidak sama", CuteToast.LENGTH_SHORT, CuteToast.WARN, true).show()
                return@setOnClickListener
            }

            setLoading(true)

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    setLoading(false)
                    if (task.isSuccessful) {
                        // Kirim email verifikasi
                        auth.currentUser?.sendEmailVerification()?.addOnCompleteListener { verifyTask ->
                            if (verifyTask.isSuccessful) {
                                CuteToast.ct(this, "Registrasi berhasil! Cek email untuk verifikasi", CuteToast.LENGTH_LONG, CuteToast.SUCCESS, true).show()
                                startActivity(Intent(this, LoginActivity::class.java))
                                finish()
                            } else {
                                CuteToast.ct(this, "Gagal mengirim email verifikasi", CuteToast.LENGTH_SHORT, CuteToast.ERROR, true).show()
                            }
                        }
                    } else {
                        CuteToast.ct(this, "Registrasi gagal: ${task.exception?.message}", CuteToast.LENGTH_SHORT, CuteToast.ERROR, true).show()
                    }
                }
        }

        binding.tvLoginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnRegister.isEnabled = !isLoading
        binding.btnRegister.text = if (isLoading) "Loading..." else "Daftar"
    }

    private fun setSystemUIColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = getColorFromResource(R.color.system_ui_color)
            window.navigationBarColor = getColorFromResource(R.color.system_ui_color)
        }
    }

    private fun getColorFromResource(resId: Int): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            resources.getColor(resId, theme)
        } else {
            @Suppress("DEPRECATION")
            resources.getColor(resId)
        }
    }
}
