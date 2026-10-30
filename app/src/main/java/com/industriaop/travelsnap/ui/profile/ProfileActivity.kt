package com.industriaop.travelsnap.ui.profile

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.industriaop.travelsnap.R
import com.industriaop.travelsnap.TravelSnapApp
import com.industriaop.travelsnap.databinding.ActivityProfileBinding
import com.industriaop.travelsnap.domain.model.AppLanguage
import com.industriaop.travelsnap.ui.common.BottomNav
import com.industriaop.travelsnap.ui.common.applySystemBarsPadding
import com.industriaop.travelsnap.ui.login.LoginActivity
import com.industriaop.travelsnap.viewmodel.SettingsUiState
import com.industriaop.travelsnap.viewmodel.SettingsViewModel
import com.industriaop.travelsnap.viewmodel.ViewModelFactories
import kotlinx.coroutines.launch

/**
 * Profile and settings screen (mockup 8): user card, language, dark theme and sign out.
 * The language and theme are applied for the whole app by [TravelSnapApp].
 */
class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    private val viewModel: SettingsViewModel by viewModels {
        ViewModelFactories.settings((application as TravelSnapApp).repository)
    }

    /** True while the screen is copying the ViewModel state into the views (ignore listeners). */
    private var isRendering = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarsPadding()

        BottomNav.setup(this, binding.bottomNav, R.id.nav_profile)

        binding.toggleLanguage.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked && !isRendering) {
                val language = if (checkedId == R.id.btnSpanish) AppLanguage.SPANISH else AppLanguage.ENGLISH
                viewModel.onLanguageSelected(language)
            }
        }
        binding.switchDark.setOnCheckedChangeListener { _, checked ->
            if (!isRendering) viewModel.onDarkThemeChange(checked)
        }
        binding.btnLogout.setOnClickListener {
            viewModel.onLogout()
            val intent = Intent(this, LoginActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { render(it) }
            }
        }
    }

    private fun render(state: SettingsUiState) {
        isRendering = true

        val user = state.user
        binding.tvUserName.text = user?.name.orEmpty()
        binding.tvUserEmail.text = user?.email.orEmpty()
        binding.tvAvatar.text = user?.name?.firstOrNull()?.uppercase().orEmpty()

        binding.toggleLanguage.check(
            if (state.settings.language == AppLanguage.SPANISH) R.id.btnSpanish else R.id.btnEnglish
        )
        binding.switchDark.isChecked = state.settings.darkTheme

        isRendering = false
    }
}
