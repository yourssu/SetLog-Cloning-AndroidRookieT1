package com.yourssu.setlog_cloning_androidrookiet1.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourssu.setlog_cloning_androidrookiet1.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isAuthenticated: Boolean = false,
    val uid: String? = null,
    val displayName: String = "User",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        AuthUiState(
            isAuthenticated = repository.getCurrentUser() != null,
            uid = repository.getCurrentUid(),
            displayName = repository.getCurrentDisplayName()
        )
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeCurrentUser().collect { user ->
                _uiState.update {
                    it.copy(
                        isAuthenticated = user != null,
                        uid = user?.uid,
                        displayName =
                            user?.displayName
                            ?: user?.email?.substringBefore("@")
                            ?: "User",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun login(email: String, password: String) {
        executeAuth { repository.login(email.trim(), password) }
    }

    fun signUp(email: String, password: String, nickname: String) {
        executeAuth { repository.signUp(email.trim(), password, nickname.trim()) }
    }

    fun signInWithGoogle(idToken: String) {
        executeAuth(allowWhileLoading = true) { repository.signInWithGoogle(idToken) }
    }

    fun startGoogleSignIn() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
    }

    fun showError(message: String) {
        _uiState.update { it.copy(isLoading = false, errorMessage = message) }
    }

    fun logout() {
        repository.logout()
        _uiState.value = AuthUiState()
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun executeAuth(
        allowWhileLoading: Boolean = false,
        action: suspend () -> Result<Unit>
    ) {
        if (_uiState.value.isLoading && !allowWhileLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            action().fold(
                onSuccess = {
                    _uiState.value = AuthUiState(
                        isAuthenticated = true,
                        uid = repository.getCurrentUid(),
                        displayName = repository.getCurrentDisplayName()
                    )
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.localizedMessage ?: "요청을 처리하지 못했습니다."
                        )
                    }
                }
            )
        }
    }
}
