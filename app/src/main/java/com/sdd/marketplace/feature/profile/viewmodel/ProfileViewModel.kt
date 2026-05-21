package com.sdd.marketplace.feature.profile.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.sdd.marketplace.domain.model.Product
import com.sdd.marketplace.domain.model.User
import com.sdd.marketplace.domain.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val user: User? = null,
    val isCurrentUser: Boolean = false,
    val isFollowing: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null,
    val selectedTab: Int = 0
)

sealed class ProfileEvent {
    data class NavigateToChat(val chatId: String) : ProfileEvent()
    object NavigateToLogin : ProfileEvent()
    data class ShowError(val message: String) : ProfileEvent()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val productRepository: ProductRepository,
    private val chatRepository: ChatRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val profileUserId: String? = savedStateHandle["userId"]
    private val currentUserId get() = authRepository.getCurrentUserId()

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ProfileEvent>()
    val events: SharedFlow<ProfileEvent> = _events.asSharedFlow()

    val userId get() = profileUserId ?: currentUserId ?: ""

    val userProducts: Flow<PagingData<Product>> = productRepository
        .getSellerProducts(userId)
        .cachedIn(viewModelScope)

    init { loadProfile() }

    private fun loadProfile() = viewModelScope.launch {
        val targetId = userId
        val isSelf = targetId == currentUserId
        _uiState.update { it.copy(isCurrentUser = isSelf) }

        userRepository.getUserProfile(targetId).collect { user ->
            _uiState.update { it.copy(user = user, isLoading = false) }
        }

        if (!isSelf) {
            userRepository.isFollowing(targetId).collect { isFollowing ->
                _uiState.update { it.copy(isFollowing = isFollowing) }
            }
        }
    }

    fun followUnfollow() = viewModelScope.launch {
        val targetId = userId
        if (_uiState.value.isFollowing) {
            userRepository.unfollowUser(targetId)
        } else {
            userRepository.followUser(targetId)
        }
        _uiState.update { it.copy(isFollowing = !it.isFollowing) }
    }

    fun messageUser() = viewModelScope.launch {
        val targetId = userId
        chatRepository.getOrCreateChat(targetId)
            .onSuccess { chat -> _events.emit(ProfileEvent.NavigateToChat(chat.id)) }
            .onFailure { _events.emit(ProfileEvent.ShowError(it.message ?: "Error")) }
    }

    fun selectTab(index: Int) = _uiState.update { it.copy(selectedTab = index) }

    fun updateAvatar(imagePath: String) = viewModelScope.launch {
        userRepository.updateAvatar(imagePath)
            .onFailure { _uiState.update { s -> s.copy(error = it.message) } }
    }

    fun requestVerification() = viewModelScope.launch {
        userRepository.requestVerification()
    }

    fun signOut() = viewModelScope.launch {
        authRepository.signOut()
        _events.emit(ProfileEvent.NavigateToLogin)
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}
