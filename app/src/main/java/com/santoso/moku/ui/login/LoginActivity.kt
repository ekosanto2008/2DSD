package com.santoso.moku.ui.login

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.rejowan.cutetoast.CuteToast
import com.santoso.moku.databinding.ActivityLoginBinding
import com.santoso.moku.ui.dashboard.DashboardActivity
import com.santoso.moku.ui.register.RegisterActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        hideSystemUI()

        if (viewModel.isUserLoggedIn()) {
            goToMain()
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty()) {
                CuteToast.ct(this, "Email tidak boleh kosong", CuteToast.LENGTH_SHORT, CuteToast.WARN, true).show();
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                CuteToast.ct(this, "Password tidak boleh kosong", CuteToast.LENGTH_SHORT, CuteToast.WARN, true).show();
                return@setOnClickListener
            }

            viewModel.login(email, password)
        }

        binding.tvRegisterLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        viewModel.loginResult.observe(this) { (success, error) ->
            if (success) {
                CuteToast.ct(this, "Login Berhasil", CuteToast.LENGTH_SHORT, CuteToast.SUCCESS, true).show();
                goToMain()
            } else {
                CuteToast.ct(this, "Login Gagal", CuteToast.LENGTH_SHORT, CuteToast.ERROR, true).show();
            }
        }
    }

    private fun goToMain() {
        startActivity(Intent(this, DashboardActivity::class.java))
        finish()
    }

    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let {
                it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    )
        }
    }

}
