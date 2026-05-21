package com.sdd.marketplace.feature.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sdd.marketplace.domain.model.User
import com.sdd.marketplace.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val error: String? = null,
    val isAuthenticated: Boolean = false,
    val otpSent: Boolean = false,
    val phone: String = ""
)

sealed class AuthEvent {
    data class ShowError(val message: String) : AuthEvent()
    object NavigateToHome : AuthEvent()
    object NavigateToOtp : AuthEvent()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AuthEvent>()
    val events: SharedFlow<AuthEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            authRepository.isAuthenticated.collect { isAuth ->
                _uiState.update { it.copy(isAuthenticated = isAuth) }
            }
        }
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _uiState.update { it.copy(user = user) }
            }
        }
    }

    fun signInWithEmail(email: String, password: String) = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, error = null) }
        authRepository.signInWithEmail(email, password)
            .onSuccess { _events.emit(AuthEvent.NavigateToHome) }
            .onFailure { _uiState.update { s -> s.copy(error = it.message) } }
        _uiState.update { it.copy(isLoading = false) }
    }

    fun signInWithPhone(phone: String) = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, error = null, phone = phone) }
        authRepository.signInWithPhone(phone)
            .onSuccess { _events.emit(AuthEvent.NavigateToOtp) }
            .onFailure { _uiState.update { s -> s.copy(error = it.message) } }
        _uiState.update { it.copy(isLoading = false) }
    }

    fun verifyOtp(otp: String) = viewModelScope.launch {
        val phone = _uiState.value.phone
        _uiState.update { it.copy(isLoading = true, error = null) }
        authRepository.verifyOtp(phone, otp)
            .onSuccess { _events.emit(AuthEvent.NavigateToHome) }
            .onFailure { _uiState.update { s -> s.copy(error = it.message) } }
        _uiState.update { it.copy(isLoading = false) }
    }

    fun signUp(fullName: String, email: String, phone: String, password: String) = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, error = null) }
        authRepository.signUpWithEmail(fullName, email, phone, password)
            .onSuccess { _events.emit(AuthEvent.NavigateToHome) }
            .onFailure { _uiState.update { s -> s.copy(error = it.message) } }
        _uiState.update { it.copy(isLoading = false) }
    }

    fun continueAsGuest() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, error = null) }
        authRepository.signInAnonymously()
            .onSuccess { _events.emit(AuthEvent.NavigateToHome) }
            .onFailure { _events.emit(AuthEvent.NavigateToHome) }
        _uiState.update { it.copy(isLoading = false) }
    }

    fun sendPasswordReset(email: String) = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, error = null) }
        authRepository.sendPasswordResetEmail(email)
            .onSuccess { _uiState.update { s -> s.copy(otpSent = true) } }
            .onFailure { _uiState.update { s -> s.copy(error = it.message) } }
        _uiState.update { it.copy(isLoading = false) }
    }

    fun signOut() = viewModelScope.launch {
        authRepository.signOut()
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}
