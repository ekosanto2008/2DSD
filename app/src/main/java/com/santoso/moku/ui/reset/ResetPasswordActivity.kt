package com.santoso.moku.ui.reset

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.rejowan.cutetoast.CuteToast
import com.santoso.moku.databinding.ActivityResetPasswordBinding

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResetPasswordBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.btnSendReset.setOnClickListener {
            val email = binding.etResetEmail.text.toString().trim()
            if (email.isEmpty()) {
                CuteToast.ct(this, "Email tidak boleh kosong", CuteToast.LENGTH_SHORT, CuteToast.WARN, true).show()
                return@setOnClickListener
            }

            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        CuteToast.ct(this, "Link reset dikirim ke email", CuteToast.LENGTH_SHORT, CuteToast.SUCCESS, true).show()
                        finish()
                    } else {
                        CuteToast.ct(this, "Gagal mengirim link reset", CuteToast.LENGTH_SHORT, CuteToast.ERROR, true).show()
                    }
                }
        }
    }
}
