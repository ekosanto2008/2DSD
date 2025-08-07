package com.santoso.moku.ui.login

import android.content.Intent
import android.graphics.drawable.Animatable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
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

        if (viewModel.isUserLoggedIn()) {
            goToMain()
        }

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
                        startActivity(Intent(this, DashboardActivity::class.java))
                        finish()
                    } else {
                        CuteToast.ct(this, "Periksa kembali email dan password", CuteToast.LENGTH_SHORT, CuteToast.ERROR, true).show()
                    }
                }
        }


        binding.tvRegisterLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }


        viewModel.loginResult.observe(this) { (success, _) ->
            if (success) {
                CuteToast.ct(this, "Login berhasil", CuteToast.LENGTH_SHORT, CuteToast.SUCCESS, true).show()
                goToMain()
            } else {
                CuteToast.ct(this, "Periksa kembali email dan password anda", CuteToast.LENGTH_SHORT, CuteToast.WARN, true).show()
            }
        }

        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ResetPasswordActivity::class.java))
        }

    }

    private fun setLoading(isLoading: Boolean) {
        binding.btnLogin.isEnabled = !isLoading
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnLogin.text = if (isLoading) "Loading..." else "Login"
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
