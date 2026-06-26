package com.yourssu.setlog_cloning_androidrookiet1.ui.room

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourssu.setlog_cloning_androidrookiet1.data.model.UserRoom
import com.yourssu.setlog_cloning_androidrookiet1.data.repository.AuthRepository
import com.yourssu.setlog_cloning_androidrookiet1.data.repository.RoomRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RoomUiState(
    val rooms: List<UserRoom> = emptyList(),
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null
)

class RoomViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val roomRepository: RoomRepository = RoomRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(RoomUiState())
    val uiState: StateFlow<RoomUiState> = _uiState.asStateFlow()
    private var roomsJob: Job? = null

    private val _recordedDates = MutableStateFlow<List<String>>(emptyList())
    val recordedDates: StateFlow<List<String>> = _recordedDates.asStateFlow()

    init {
        observeRooms()
    }

    fun observeRecords(roomId: String) {
        viewModelScope.launch {
            roomRepository.observeRoomRecords(roomId).collect { dates ->
                _recordedDates.update { dates }
            }
        }
    }

    fun uploadRecord(roomId: String, caption: String, dateHour: String, onSuccess: () -> Unit) {
        submit(onSuccess) { uid ->
            roomRepository.uploadRecord(roomId, uid, caption, dateHour)
        }
    }

    fun createRoom(roomName: String, onSuccess: () -> Unit) {
        submit(onSuccess) { uid ->
            require(roomName.isNotBlank()) { "방 이름을 입력해주세요." }
            roomRepository.createRoom(uid, roomName)
        }
    }

    fun joinRoom(inviteCode: String, onSuccess: () -> Unit) {
        submit(onSuccess) { uid ->
            require(inviteCode.isNotBlank()) { "초대 코드를 입력해주세요." }
            roomRepository.joinRoom(uid, inviteCode)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun observeRooms() {
        val uid = authRepository.getCurrentUid() ?: run {
            _uiState.update { it.copy(isLoading = false) }
            return
        }
        roomsJob?.cancel()
        roomsJob = viewModelScope.launch {
            roomRepository.observeUserRooms(uid)
                .catch { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.localizedMessage ?: "방 목록을 불러오지 못했습니다."
                        )
                    }
                }
                .collect { rooms ->
                    _uiState.update { it.copy(rooms = rooms, isLoading = false) }
                }
        }
    }

    private fun submit(
        onSuccess: () -> Unit,
        action: suspend (String) -> Result<Unit>
    ) {
        if (_uiState.value.isSubmitting) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            val uid = authRepository.getCurrentUid()
            val result = if (uid == null) {
                Result.failure(IllegalStateException("로그인이 필요합니다."))
            } else {
                runCatching { action(uid) }.getOrElse { Result.failure(it) }
            }
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isSubmitting = false) }
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = error.localizedMessage ?: "요청을 처리하지 못했습니다."
                        )
                    }
                }
            )
        }
    }
}