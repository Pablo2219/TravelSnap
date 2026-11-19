package com.industriaop.travelsnap.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.industriaop.travelsnap.MainActivity
import com.industriaop.travelsnap.R
import com.industriaop.travelsnap.ui.common.applySystemBarsPadding
import com.industriaop.travelsnap.util.Constants
import com.industriaop.travelsnap.util.Validators
import com.industriaop.travelsnap.viewmodel.AuthViewModel

/** Login screen for the demo account. */
class LoginActivity : AppCompatActivity() {

    private lateinit var btnLogin: MaterialButton
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var tvDemoHint: TextView
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        findViewById<View>(R.id.root).applySystemBarsPadding()

        btnLogin = findViewById(R.id.btnLogin)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        tilEmail = findViewById(R.id.tilEmail)
        tilPassword = findViewById(R.id.tilPassword)
        tvDemoHint = findViewById(R.id.tvDemoHint)

        tvDemoHint.text = getString(
            R.string.login_demo_hint,
            Constants.DEMO_EMAIL,
            Constants.DEMO_PASSWORD
        )
        etPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                login()
                true
            } else false
        }
        btnLogin.setOnClickListener { login() }
    }

    private fun login() {
        tilEmail.error = null
        tilPassword.error = null

        val email = etEmail.text?.toString().orEmpty()
        val password = etPassword.text?.toString().orEmpty()
        val emailError = Validators.validateEmail(email)
        val passwordError = Validators.validatePassword(password)

        tilEmail.error = emailError?.let { getString(it.messageRes) }
        tilPassword.error = passwordError?.let { getString(it.messageRes) }
        if (emailError != null || passwordError != null) return

        if (viewModel.login(email, password) == null) {
            tilPassword.error = getString(R.string.login_error_invalid)
            return
        }

        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
