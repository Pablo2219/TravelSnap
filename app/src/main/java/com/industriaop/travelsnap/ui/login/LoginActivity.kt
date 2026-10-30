package com.industriaop.travelsnap.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isInvisible
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.industriaop.travelsnap.MainActivity
import com.industriaop.travelsnap.R
import com.industriaop.travelsnap.TravelSnapApp
import com.industriaop.travelsnap.databinding.ActivityLoginBinding
import com.industriaop.travelsnap.ui.common.applySystemBarsPadding
import com.industriaop.travelsnap.ui.common.show
import com.industriaop.travelsnap.util.Constants
import com.industriaop.travelsnap.viewmodel.AuthUiState
import com.industriaop.travelsnap.viewmodel.AuthViewModel
import com.industriaop.travelsnap.viewmodel.ViewModelFactories
import kotlinx.coroutines.launch

/** Welcome / login screen (mockup 1). */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private val viewModel: AuthViewModel by viewModels {
        ViewModelFactories.auth((application as TravelSnapApp).repository)
    }

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

        binding.etEmail.doOnTextChanged { text, _, _, _ ->
            viewModel.onEmailChange(text?.toString().orEmpty())
        }
        binding.etPassword.doOnTextChanged { text, _, _, _ ->
            viewModel.onPasswordChange(text?.toString().orEmpty())
        }
        binding.etPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.onLogin()
                true
            } else {
                false
            }
        }
        binding.btnLogin.setOnClickListener { viewModel.onLogin() }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { render(it) }
            }
        }
    }

    private fun render(state: AuthUiState) {
        binding.tilEmail.error = state.emailError?.let { getString(it.messageRes) }
        binding.tilPassword.error = state.passwordError?.let { getString(it.messageRes) }
        binding.progress.isInvisible = !state.isLoading
        binding.btnLogin.isEnabled = !state.isLoading

        state.message?.let {
            it.show(this)
            viewModel.onMessageShown()
        }

        if (state.isLoggedIn) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
