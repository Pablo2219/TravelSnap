package com.industriaop.travelsnap.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.industriaop.travelsnap.domain.repository.UserRepository
import com.industriaop.travelsnap.util.MessageType
import com.industriaop.travelsnap.util.UiMessage
import com.industriaop.travelsnap.R
import com.industriaop.travelsnap.util.ValidationError
import com.industriaop.travelsnap.util.Validators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val emailError: ValidationError? = null,
    val passwordError: ValidationError? = null,
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val message: UiMessage? = null
)

class AuthViewModel(
    private val repository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) =
        _uiState.update { it.copy(email = value, emailError = null) }

    fun onPasswordChange(value: String) =
        _uiState.update { it.copy(password = value, passwordError = null) }

    fun onLogin() {
        val state = _uiState.value
        val emailError = Validators.validateEmail(state.email)
        val passwordError = Validators.validatePassword(state.password)
        if (emailError != null || passwordError != null) {
            _uiState.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val user = repository.login(state.email, state.password)
            _uiState.update {
                if (user != null) {
                    it.copy(isLoading = false, isLoggedIn = true)
                } else {
                    it.copy(
                        isLoading = false,
                        message = UiMessage(MessageType.ERROR, R.string.login_error_invalid)
                    )
                }
            }
        }
    }

    fun onMessageShown() = _uiState.update { it.copy(message = null) }
}