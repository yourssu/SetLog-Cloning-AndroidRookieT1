package com.yourssu.setlog_cloning_androidrookiet1

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yourssu.setlog_cloning_androidrookiet1.data.auth.GoogleSignInClient
import com.yourssu.setlog_cloning_androidrookiet1.data.model.UserRoom
import com.yourssu.setlog_cloning_androidrookiet1.data.repository.NotificationRepository
import com.yourssu.setlog_cloning_androidrookiet1.ui.auth.AuthScreen
import com.yourssu.setlog_cloning_androidrookiet1.ui.auth.AuthViewModel
import com.yourssu.setlog_cloning_androidrookiet1.ui.record.RecordScreen
import com.yourssu.setlog_cloning_androidrookiet1.ui.room.RoomScreen
import com.yourssu.setlog_cloning_androidrookiet1.ui.room.RoomViewModel
import com.yourssu.setlog_cloning_androidrookiet1.ui.theme.SetLogCloningAndroidRookieT1Theme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    companion object {
        const val EXTRA_OPEN_NOTIFICATIONS = "open_notifications"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 앱 시작 시 저장된 알림 불러오기
        NotificationRepository.init(this)

        val openNotifications = intent.getBooleanExtra(EXTRA_OPEN_NOTIFICATIONS, false)
        val notificationId = intent.getStringExtra(EXTRA_NOTIFICATION_ID)

        // 알림 탭으로 열렸을 경우 해당 알림을 읽음 처리
        if (openNotifications && notificationId != null) {
            NotificationRepository.markAsRead(this, notificationId)
        }

        setContent {
            SetLogCloningAndroidRookieT1Theme {
                SetLogApp(
                    initialOpenNotifications = openNotifications
                )
            }
        }
    }

    // 앱이 이미 실행 중인 상태에서 알림을 탭한 경우
    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        val notificationId = intent.getStringExtra(EXTRA_NOTIFICATION_ID)
        if (notificationId != null) {
            NotificationRepository.markAsRead(this, notificationId)
        }
    }
}

@Composable
private fun SetLogApp(
    authViewModel: AuthViewModel = viewModel(),
    initialOpenNotifications: Boolean = false
) {
    val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(authUiState.isAuthenticated) {
        if (authUiState.isAuthenticated) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            com.yourssu.setlog_cloning_androidrookiet1.data.alarm.HourlyAlarmReceiver.scheduleNextAlarm(context)
        }
    }

    if (authUiState.isAuthenticated) {
        val roomViewModel: RoomViewModel = viewModel(
            key = "room-${authUiState.uid}"
        )
        val roomUiState by roomViewModel.uiState.collectAsStateWithLifecycle()
        val currentUserName = roomUiState.profile.name.ifBlank { authUiState.displayName }
        var activeRoom by remember(authUiState.uid) { mutableStateOf<UserRoom?>(null) }
        var showRoomList by remember(authUiState.uid) { mutableStateOf(false) }
        var hasAttemptedCreate by remember(authUiState.uid) { mutableStateOf(false) }

        LaunchedEffect(roomUiState.rooms, roomUiState.isLoading) {
            if (roomUiState.isLoading || activeRoom != null) return@LaunchedEffect

            val firstRoom = roomUiState.rooms.firstOrNull()
            if (firstRoom != null) {
                activeRoom = firstRoom
            } else if (!hasAttemptedCreate) {
                hasAttemptedCreate = true
                roomViewModel.createRoom("로그 :)", 4) {}
            }
        }

        when {
            showRoomList -> {
                RoomScreen(
                    uiState = roomUiState,
                    userName = currentUserName,
                    initialOpenNotifications = initialOpenNotifications,
                    onCreateRoom = { roomName, memberCount, onSuccess ->
                        roomViewModel.createRoom(roomName, memberCount, onSuccess)
                    },
                    onJoinRoom = roomViewModel::joinRoom,
                    onOpenRoom = { room ->
                        activeRoom = room
                        showRoomList = false
                    },
                    onUploadRecord = { roomId, caption, dateHour, thumbnail, videoUri, onSuccess ->
                        roomViewModel.uploadRecord(
                            roomId,
                            caption,
                            dateHour,
                            thumbnail,
                            videoUri,
                            onSuccess
                        )
                    },
                    onUpdateProfileName = roomViewModel::updateProfileName,
                    onUpdateProfileColor = roomViewModel::updateProfileColor,
                    onUpdateProfileImage = roomViewModel::updateProfileImage,
                    onLogout = authViewModel::logout
                )
            }

            activeRoom != null -> {
                val room = activeRoom!!
                RecordScreen(
                    roomId = room.roomId,
                    roomName = room.roomName,
                    memberCount = room.memberCount,
                    currentUserName = currentUserName,
                    roomViewModel = roomViewModel,
                    onBack = { showRoomList = true }
                )
            }

            else -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    } else {
        val scope = rememberCoroutineScope()
        val googleSignInClient = remember { GoogleSignInClient() }
        AuthScreen(
            uiState = authUiState,
            onGoogleSignIn = {
                authViewModel.startGoogleSignIn()
                scope.launch {
                    googleSignInClient.getIdToken(context).fold(
                        onSuccess = authViewModel::signInWithGoogle,
                        onFailure = { error ->
                            authViewModel.showError(
                                error.localizedMessage ?: "Google 로그인에 실패했습니다."
                            )
                        }
                    )
                }
            }
        )
    }
}
