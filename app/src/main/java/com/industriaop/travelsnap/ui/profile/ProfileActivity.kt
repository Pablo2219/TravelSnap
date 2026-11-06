package com.industriaop.travelsnap.ui.profile

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.industriaop.travelsnap.R
import com.industriaop.travelsnap.TravelSnapApp
import com.industriaop.travelsnap.databinding.ActivityProfileBinding
import com.industriaop.travelsnap.domain.model.AppLanguage
import com.industriaop.travelsnap.ui.common.BottomNav
import com.industriaop.travelsnap.ui.common.applySystemBarsPadding
import com.industriaop.travelsnap.ui.login.LoginActivity

/** Profile and application settings screen. */
class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private val app get() = application as TravelSnapApp
    private val repository get() = app.repository
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
                val language = if (checkedId == R.id.btnSpanish) {
                    AppLanguage.SPANISH
                } else {
                    AppLanguage.ENGLISH
                }
                repository.setLanguage(language)
                app.applySettings()
            }
        }

        binding.switchDark.setOnCheckedChangeListener { _, checked ->
            if (!isRendering) {
                repository.setDarkTheme(checked)
                app.applySettings()
            }
        }

        binding.btnLogout.setOnClickListener {
            repository.logout()
            val intent = Intent(this, LoginActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }
        renderProfile()
    }

    override fun onResume() {
        super.onResume()
        if (::binding.isInitialized) renderProfile()
    }

    private fun renderProfile() {
        isRendering = true
        val user = repository.getCurrentUser()
        val settings = repository.getSettings()

        binding.tvUserName.text = user?.name.orEmpty()
        binding.tvUserEmail.text = user?.email.orEmpty()
        binding.tvAvatar.text = user?.name?.firstOrNull()?.uppercase().orEmpty()
        binding.toggleLanguage.check(
            if (settings.language == AppLanguage.SPANISH) R.id.btnSpanish else R.id.btnEnglish
        )
        binding.switchDark.isChecked = settings.darkTheme
        isRendering = false
    }
}