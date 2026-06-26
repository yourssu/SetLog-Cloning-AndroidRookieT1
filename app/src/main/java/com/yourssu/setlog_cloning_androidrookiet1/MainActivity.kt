package com.yourssu.setlog_cloning_androidrookiet1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yourssu.setlog_cloning_androidrookiet1.data.auth.GoogleSignInClient
import com.yourssu.setlog_cloning_androidrookiet1.ui.auth.AuthScreen
import com.yourssu.setlog_cloning_androidrookiet1.ui.auth.AuthViewModel
import com.yourssu.setlog_cloning_androidrookiet1.ui.record.RecordScreen
import com.yourssu.setlog_cloning_androidrookiet1.ui.room.RoomScreen
import com.yourssu.setlog_cloning_androidrookiet1.ui.room.RoomViewModel
import com.yourssu.setlog_cloning_androidrookiet1.ui.theme.SetLogCloningAndroidRookieT1Theme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SetLogCloningAndroidRookieT1Theme {
                SetLogApp()
            }
        }
    }
}

@Composable
private fun SetLogApp(authViewModel: AuthViewModel = viewModel()) {
    val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()

    if (authUiState.isAuthenticated) {
        val roomViewModel: RoomViewModel = viewModel(
            key = "room-${authUiState.uid}"
        )
        val roomUiState by roomViewModel.uiState.collectAsStateWithLifecycle()
        RoomScreen(
            uiState = roomUiState,
            userName = authUiState.displayName,
            onCreateRoom = roomViewModel::createRoom,
            onJoinRoom = roomViewModel::joinRoom,
            onLogout = authViewModel::logout
        )
    } else {
        val context = LocalContext.current
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
