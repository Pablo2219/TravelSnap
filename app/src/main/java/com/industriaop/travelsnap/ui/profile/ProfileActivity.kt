package com.industriaop.travelsnap.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.materialswitch.MaterialSwitch
import com.industriaop.travelsnap.domain.model.AppLanguage
import com.industriaop.travelsnap.R
import com.industriaop.travelsnap.ui.common.applySystemBarsPadding
import com.industriaop.travelsnap.ui.common.BottomNav
import com.industriaop.travelsnap.ui.login.LoginActivity
import com.industriaop.travelsnap.viewmodel.SettingsViewModel

/** Profile and application settings screen. */
class ProfileActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var btnLogout: MaterialButton
    private lateinit var switchDark: MaterialSwitch
    private lateinit var toggleLanguage: MaterialButtonToggleGroup
    private lateinit var tvAvatar: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var tvUserName: TextView
    private val viewModel: SettingsViewModel by viewModels()
    private var isRendering = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        findViewById<View>(R.id.root).applySystemBarsPadding()

        bottomNav = findViewById(R.id.bottomNav)
        btnLogout = findViewById(R.id.btnLogout)
        switchDark = findViewById(R.id.switchDark)
        toggleLanguage = findViewById(R.id.toggleLanguage)
        tvAvatar = findViewById(R.id.tvAvatar)
        tvUserEmail = findViewById(R.id.tvUserEmail)
        tvUserName = findViewById(R.id.tvUserName)
        BottomNav.setup(this, bottomNav, R.id.nav_profile)

        toggleLanguage.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked && !isRendering) {
                val language = if (checkedId == R.id.btnSpanish) {
                    AppLanguage.SPANISH
                } else {
                    AppLanguage.ENGLISH
                }
                viewModel.setLanguage(language)

            }
        }

        switchDark.setOnCheckedChangeListener { _, checked ->
            if (!isRendering) {
                viewModel.setDarkTheme(checked)

            }
        }

        btnLogout.setOnClickListener {
            viewModel.logout()
            val intent = Intent(this, LoginActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }
        renderProfile()
    }

    override fun onResume() {
        super.onResume()
        if (::bottomNav.isInitialized) renderProfile()
    }

    private fun renderProfile() {
        isRendering = true
        val user = viewModel.getCurrentUser()
        val settings = viewModel.getSettings()

        tvUserName.text = user?.name.orEmpty()
        tvUserEmail.text = user?.email.orEmpty()
        tvAvatar.text = user?.name?.firstOrNull()?.uppercase().orEmpty()
        toggleLanguage.check(
            if (settings.language == AppLanguage.SPANISH) R.id.btnSpanish else R.id.btnEnglish
        )
        switchDark.isChecked = settings.darkTheme
        isRendering = false
    }
}
