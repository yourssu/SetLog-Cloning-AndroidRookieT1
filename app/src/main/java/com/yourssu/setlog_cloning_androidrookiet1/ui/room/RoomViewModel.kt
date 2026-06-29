package com.yourssu.setlog_cloning_androidrookiet1.ui.room

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourssu.setlog_cloning_androidrookiet1.data.model.UserRoom
import com.yourssu.setlog_cloning_androidrookiet1.data.repository.AuthRepository
import com.yourssu.setlog_cloning_androidrookiet1.data.repository.RoomRepository
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
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
    val errorMessage: String? = null,
    val profile: RoomProfileUi = RoomProfileUi()
)

data class RoomProfileUi(
    val name: String = "User",
    val colorName: String = "pink",
    val imageUrl: String = ""
)

data class RoomRecordUi(
    val thumbnail: Bitmap?,
    val caption: String,
    val videoUrl: String = "",
    val videoUri: Uri? = null
)

data class RoomMemberUi(
    val uid: String,
    val name: String,
    val isCurrentUser: Boolean
)

class RoomViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val roomRepository: RoomRepository = RoomRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(RoomUiState())
    val uiState: StateFlow<RoomUiState> = _uiState.asStateFlow()
    private var roomsJob: Job? = null
    private var recordsJob: Job? = null
    private var profileJob: Job? = null

    private val _recordedDates = MutableStateFlow<List<String>>(emptyList())
    val recordedDates: StateFlow<List<String>> = _recordedDates.asStateFlow()

    private val _myRecords = MutableStateFlow<Map<Int, RoomRecordUi>>(emptyMap())
    val myRecords: StateFlow<Map<Int, RoomRecordUi>> = _myRecords.asStateFlow()

    private val _roomMembers = MutableStateFlow<List<RoomMemberUi>>(emptyList())
    val roomMembers: StateFlow<List<RoomMemberUi>> = _roomMembers.asStateFlow()

    init {
        observeRooms()
        observeProfile()
    }

    fun observeRecords(roomId: String) {
        val uid = authRepository.getCurrentUid() ?: return
        recordsJob?.cancel()
        _recordedDates.update { emptyList() }
        _myRecords.update { emptyMap() }
        _roomMembers.update { emptyList() }
        recordsJob = viewModelScope.launch {
            launch {
                roomRepository.observeRoomMembers(roomId).collect { members ->
                    _roomMembers.update {
                        members.map { member ->
                            RoomMemberUi(
                                uid = member.uid,
                                name = member.nickname.ifBlank { "사용자" },
                                isCurrentUser = member.uid == uid
                            )
                        }
                    }
                }
            }
            launch {
                roomRepository.observeRoomRecords(roomId).collect { dates ->
                    _recordedDates.update { dates }
                }
            }
            launch {
                roomRepository.observeMyRoomRecords(roomId, uid).collect { records ->
                    val today = todayDatePrefix()
                    val recordsByHour = records
                        .filter { it.date.startsWith(today) }
                        .mapNotNull { record ->
                            val hour = record.date.takeLast(2).toIntOrNull() ?: return@mapNotNull null
                            hour to RoomRecordUi(
                                thumbnail = record.thumbnailBase64.toBitmapOrNull(),
                                caption = record.caption,
                                videoUrl = record.videoUrl
                            )
                        }
                        .toMap()
                    _myRecords.update { recordsByHour }
                }
            }
        }
    }

    fun uploadRecord(
        roomId: String,
        caption: String,
        dateHour: String,
        thumbnail: Bitmap?,
        videoUri: Uri?,
        onSuccess: () -> Unit
    ) {
        submit(onSuccess) { uid ->
            roomRepository.uploadRecord(
                roomId = roomId,
                uid = uid,
                caption = caption,
                dateHour = dateHour,
                thumbnailBase64 = thumbnail.toThumbnailBase64(),
                videoUri = videoUri
            )
        }
    }

    fun createRoom(roomName: String, memberCount: Int = 4, onSuccess: () -> Unit) {
        submit(onSuccess) { uid ->
            require(roomName.isNotBlank()) { "방 이름을 입력해주세요." }
            roomRepository.createRoom(uid, roomName, memberCount)
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

    fun updateProfileName(name: String, onSuccess: () -> Unit = {}) {
        if (_uiState.value.isSubmitting) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            authRepository.updateProfileName(name).fold(
                onSuccess = {
                    _uiState.update { state ->
                        state.copy(
                            isSubmitting = false,
                            profile = state.profile.copy(name = name.trim())
                        )
                    }
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = error.localizedMessage ?: "이름을 저장하지 못했습니다."
                        )
                    }
                }
            )
        }
    }

    fun updateProfileColor(colorName: String) {
        _uiState.update { it.copy(profile = it.profile.copy(colorName = colorName)) }
        viewModelScope.launch {
            authRepository.updateProfileColor(colorName).onFailure { error ->
                _uiState.update {
                    it.copy(errorMessage = error.localizedMessage ?: "프로필 색상을 저장하지 못했습니다.")
                }
            }
        }
    }

    fun updateProfileImage(imageUri: Uri) {
        _uiState.update { it.copy(profile = it.profile.copy(imageUrl = imageUri.toString())) }
        viewModelScope.launch {
            authRepository.updateProfileImage(imageUri).onFailure { error ->
                _uiState.update {
                    it.copy(errorMessage = error.localizedMessage ?: "프로필 이미지를 저장하지 못했습니다.")
                }
            }
        }
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

    private fun observeProfile() {
        profileJob?.cancel()
        profileJob = viewModelScope.launch {
            authRepository.observeCurrentUserProfile()
                .catch { error ->
                    _uiState.update {
                        it.copy(errorMessage = error.localizedMessage ?: "프로필을 불러오지 못했습니다.")
                    }
                }
                .collect { user ->
                    if (user != null) {
                        _uiState.update {
                            it.copy(
                                profile = RoomProfileUi(
                                    name = user.nickname.ifBlank { authRepository.getCurrentDisplayName() },
                                    colorName = user.profileColor.ifBlank { "pink" },
                                    imageUrl = user.profileImageUrl
                                )
                            )
                        }
                    }
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

    private fun Bitmap?.toThumbnailBase64(): String {
        if (this == null) return ""
        val maxWidth = 480
        val target = if (width > maxWidth) {
            val scaledHeight = (height * (maxWidth.toFloat() / width)).toInt().coerceAtLeast(1)
            Bitmap.createScaledBitmap(this, maxWidth, scaledHeight, true)
        } else {
            this
        }
        return ByteArrayOutputStream().use { output ->
            target.compress(Bitmap.CompressFormat.JPEG, 70, output)
            Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
        }
    }

    private fun String.toBitmapOrNull(): Bitmap? {
        if (isBlank()) return null
        return runCatching {
            val bytes = Base64.decode(this, Base64.NO_WRAP)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }.getOrNull()
    }

    private fun todayDatePrefix(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).apply {
            timeZone = TimeZone.getTimeZone("Asia/Seoul")
        }
        return formatter.format(Date())
    }
}
