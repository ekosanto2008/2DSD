package com.santoso.moku.ui.login

import android.content.Intent
import android.graphics.drawable.Animatable
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.rejowan.cutetoast.CuteToast
import com.santoso.moku.R
import com.santoso.moku.databinding.ActivityLoginBinding
import com.santoso.moku.ui.dashboard.DashboardActivity
import com.santoso.moku.ui.register.RegisterActivity
import com.santoso.moku.ui.reset.ResetPasswordActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        window.statusBarColor = ContextCompat.getColor(this, R.color.system_ui_color)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.system_ui_color)
        setSystemUIColor()

        // Jika sudah login & verified -> langsung ke dashboard, kalau belum verified -> signOut
        auth.currentUser?.let { current ->
            current.reload().addOnCompleteListener {
                if (current.isEmailVerified) {
                    goToMain()
                } else {
                    auth.signOut()
                    showVerifyUi(false) // pastikan UI verify disembunyikan saat awal
                }
            }
        } ?: run { showVerifyUi(false) }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                CuteToast.ct(this, "Email dan password wajib diisi", CuteToast.LENGTH_SHORT, CuteToast.WARN, true).show()
                return@setOnClickListener
            }

            setLoading(true)

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    setLoading(false)
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        user?.reload()?.addOnCompleteListener {
                            if (user?.isEmailVerified == true) {
                                CuteToast.ct(this, "Login berhasil", CuteToast.LENGTH_SHORT, CuteToast.SUCCESS, true).show()
                                goToMain()
                            } else {
                                // Kirim ulang verifikasi saat ini juga (opsional), lalu tampilkan UI resend
                                user?.sendEmailVerification()
                                CuteToast.ct(this, "Email belum diverifikasi. Cek inbox kamu.", CuteToast.LENGTH_LONG, CuteToast.WARN, true).show()
                                showVerifyUi(true)
                                auth.signOut() // cegah akses sebelum verifikasi
                            }
                        }
                    } else {
                        CuteToast.ct(this, "Periksa kembali email dan password", CuteToast.LENGTH_SHORT, CuteToast.ERROR, true).show()
                        showVerifyUi(false)
                    }
                }
        }

        // Resend verification (login cepat, kirim, lalu signOut lagi)
        binding.btnResendVerification.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            if (email.isEmpty() || password.isEmpty()) {
                CuteToast.ct(this, "Isi email & password untuk kirim ulang verifikasi", CuteToast.LENGTH_SHORT, CuteToast.WARN, true).show()
                return@setOnClickListener
            }
            setLoading(true)
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { t ->
                if (t.isSuccessful) {
                    auth.currentUser?.sendEmailVerification()?.addOnCompleteListener { v ->
                        setLoading(false)
                        if (v.isSuccessful) {
                            CuteToast.ct(this, "Link verifikasi dikirim ulang. Cek email.", CuteToast.LENGTH_SHORT, CuteToast.SUCCESS, true).show()
                        } else {
                            CuteToast.ct(this, "Gagal kirim ulang: ${v.exception?.message}", CuteToast.LENGTH_SHORT, CuteToast.ERROR, true).show()
                        }
                        auth.signOut()
                    }
                } else {
                    setLoading(false)
                    CuteToast.ct(this, "Email/password salah untuk kirim ulang", CuteToast.LENGTH_SHORT, CuteToast.ERROR, true).show()
                }
            }
        }

        binding.tvRegisterLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ResetPasswordActivity::class.java))
        }

        // Observer lama kamu tetap dipertahankan
        viewModel.loginResult.observe(this) { (success, _) ->
            if (success) {
                CuteToast.ct(this, "Login berhasil", CuteToast.LENGTH_SHORT, CuteToast.SUCCESS, true).show()
                goToMain()
            } else {
                CuteToast.ct(this, "Periksa kembali email dan password anda", CuteToast.LENGTH_SHORT, CuteToast.WARN, true).show()
            }
        }
    }

    private fun showVerifyUi(show: Boolean) {
        // Jangan sembunyikan btnLogin; cukup tampilkan tombol resend + info di bawahnya
        binding.btnResendVerification.visibility = if (show) View.VISIBLE else View.GONE
        binding.tvVerifyInfo.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun setLoading(isLoading: Boolean) {
        binding.btnLogin.isEnabled = !isLoading
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnLogin.text = if (isLoading) "Loading..." else "Login"
        binding.btnResendVerification.isEnabled = !isLoading
    }

    private fun goToMain() {
        startActivity(Intent(this, DashboardActivity::class.java))
        finish()
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
