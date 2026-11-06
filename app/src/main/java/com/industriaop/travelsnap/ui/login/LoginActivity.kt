package com.industriaop.travelsnap.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isInvisible
import com.industriaop.travelsnap.MainActivity
import com.industriaop.travelsnap.R
import com.industriaop.travelsnap.TravelSnapApp
import com.industriaop.travelsnap.databinding.ActivityLoginBinding
import com.industriaop.travelsnap.ui.common.applySystemBarsPadding
import com.industriaop.travelsnap.util.Constants
import com.industriaop.travelsnap.util.Validators

/** Login screen using direct repository calls. */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val repository get() = (application as TravelSnapApp).repository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarsPadding()

        binding.tvDemoHint.text = getString(
            R.string.login_demo_hint,
            Constants.DEMO_EMAIL,
            Constants.DEMO_PASSWORD
        )
        binding.etPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                login()
                true
            } else false
        }
        binding.btnLogin.setOnClickListener { login() }
    }

    private fun login() {
        binding.tilEmail.error = null
        binding.tilPassword.error = null

        val email = binding.etEmail.text?.toString().orEmpty()
        val password = binding.etPassword.text?.toString().orEmpty()
        val emailError = Validators.validateEmail(email)
        val passwordError = Validators.validatePassword(password)

        binding.tilEmail.error = emailError?.let { getString(it.messageRes) }
        binding.tilPassword.error = passwordError?.let { getString(it.messageRes) }
        if (emailError != null || passwordError != null) return

        binding.progress.isInvisible = false
        binding.btnLogin.isEnabled = false
        val user = repository.login(email, password)
        binding.progress.isInvisible = true
        binding.btnLogin.isEnabled = true

        if (user == null) {
            binding.tilPassword.error = getString(R.string.login_error_invalid)
            return
        }

        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}