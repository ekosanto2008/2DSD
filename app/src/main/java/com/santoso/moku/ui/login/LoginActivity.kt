package com.santoso.moku.ui.login

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.rejowan.cutetoast.CuteToast
import com.santoso.moku.R
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
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        window.statusBarColor = ContextCompat.getColor(this, R.color.system_ui_color)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.system_ui_color)
        setSystemUIColor()

        if (viewModel.isUserLoggedIn()) {
            goToMain()
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty()) {
                CuteToast.ct(this, "Email tidak boleh kosong", CuteToast.LENGTH_SHORT, CuteToast.WARN, true).show()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                CuteToast.ct(this, "Password tidak boleh kosong", CuteToast.LENGTH_SHORT, CuteToast.WARN, true).show()
                return@setOnClickListener
            }

            viewModel.login(email, password)
        }

        binding.tvRegisterLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        viewModel.loginResult.observe(this) { (success, error) ->
            if (success) {
                CuteToast.ct(this, "Login berhasil", CuteToast.LENGTH_SHORT, CuteToast.SUCCESS, true).show()
                goToMain()
            } else {
                CuteToast.ct(this, "Periksa kembali email dan password anda", CuteToast.LENGTH_SHORT, CuteToast.WARN, true).show()
            }
        }
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
