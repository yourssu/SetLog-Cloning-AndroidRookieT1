package com.yourssu.setlog_cloning_androidrookiet1.ui.room

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import java.io.File
import androidx.annotation.RawRes
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview as CameraPreviewUseCase
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.yourssu.setlog_cloning_androidrookiet1.R
import com.yourssu.setlog_cloning_androidrookiet1.data.alarm.HourlyAlarmReceiver
import com.yourssu.setlog_cloning_androidrookiet1.data.alarm.LOG_ALARM_MINUTE_OPTIONS
import com.yourssu.setlog_cloning_androidrookiet1.data.alarm.LogAlarmInterval
import com.yourssu.setlog_cloning_androidrookiet1.data.alarm.LogAlarmSettings
import com.yourssu.setlog_cloning_androidrookiet1.data.model.NotificationItem
import com.yourssu.setlog_cloning_androidrookiet1.data.model.UserRoom
import com.yourssu.setlog_cloning_androidrookiet1.data.repository.LogAlarmSettingsRepository
import com.yourssu.setlog_cloning_androidrookiet1.data.repository.NotificationRepository
import com.yourssu.setlog_cloning_androidrookiet1.ui.theme.SetLogCloningAndroidRookieT1Theme
import kotlinx.coroutines.delay
import java.text.DateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.text.SimpleDateFormat
import java.util.TimeZone

private val HomeBlack = Color.Black
private val HomeCard = Color(0xFF111111)
private val HomeButton = Color(0xFF131313)
private val HomeBorder = Color(0xFF2E2E2E)
private val HomeMuted = Color(0xFF8C8A91)
private val HomeTeal = Color(0xFF00D3C8)
private val HomePink = Color(0xFFFF52ED)

private enum class RoomDialog {
    Create,
    Join
}

private enum class RoomFlowScreen {
    Home,
    CreateLog,
    LogCreated
}

private enum class HomeTab {
    Camera,
    Log
}

@Composable
fun RoomScreen(
    uiState: RoomUiState,
    userName: String,
    initialOpenNotifications: Boolean = false,
    onCreateRoom: (String, Int, () -> Unit) -> Unit,
    onJoinRoom: (String, () -> Unit) -> Unit,
    onOpenRoom: (UserRoom) -> Unit,
    onUploadRecord: (String, String, String, Bitmap?, Uri?, () -> Unit) -> Unit,
    onUpdateProfileName: (String, () -> Unit) -> Unit,
    onUpdateProfileColor: (String) -> Unit,
    onUpdateProfileImage: (Uri) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var dialog by remember { mutableStateOf<RoomDialog?>(null) }
    var flowScreen by remember { mutableStateOf(RoomFlowScreen.Home) }
    var createLogName by remember { mutableStateOf("") }
    var createdLogName by remember { mutableStateOf("로그 :)") }
    var selectedMemberCount by remember { mutableIntStateOf(4) }
    var createdRoomId by remember { mutableStateOf<String?>(null) }
    var showCreateMenu by remember { mutableStateOf(false) }
    var showNotifications by remember { mutableStateOf(initialOpenNotifications) }
    val notifications by NotificationRepository.notifications.collectAsStateWithLifecycle()
    var showSettings by remember { mutableStateOf(false) }
    var showLogAlarmSheet by remember { mutableStateOf(false) }
    var showNameEditor by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(HomeTab.Log) }
    var selectedRoomId by remember { mutableStateOf<String?>(null) }
    var cameraMessage by remember { mutableStateOf<String?>(null) }
    val timeLabels = rememberCurrentTimeLabels()
    val context = LocalContext.current
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                onUpdateProfileImage(uri)
            }
        }
    )

    LaunchedEffect(uiState.rooms) {
        val selectedExists = uiState.rooms.any { it.roomId == selectedRoomId }
        if (!selectedExists) {
            selectedRoomId = null
        }
        if (flowScreen == RoomFlowScreen.LogCreated && createdRoomId == null) {
            createdRoomId = uiState.rooms.lastOrNull { it.roomName == createdLogName }?.roomId
                ?: uiState.rooms.lastOrNull()?.roomId
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF6F6F6))
    ) {
        val scaleX = maxWidth / 393.dp
        val scaleY = maxHeight / 852.dp
        val hasTransientPopover = showCreateMenu || showNotifications || showSettings

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(44.dp))
                .background(HomeBlack)
                .border(2.dp, HomePink, RoundedCornerShape(44.dp))
        ) {
            Text(
                text = timeLabels.statusTime,
                modifier = Modifier.offset(43.dp * scaleX, 18.dp * scaleY),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "●     ▮▮▮  ◇  62⚡",
                modifier = Modifier.offset(171.dp * scaleX, 21.dp * scaleY),
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            if (flowScreen == RoomFlowScreen.CreateLog) {
                CreateLogContent(
                    scaleX = scaleX,
                    scaleY = scaleY,
                    statusTime = timeLabels.statusTime,
                    logName = createLogName,
                    selectedMemberCount = selectedMemberCount,
                    isSubmitting = uiState.isSubmitting,
                    errorMessage = uiState.errorMessage,
                    onLogNameChange = { createLogName = it },
                    onMemberCountChange = { selectedMemberCount = it },
                    onClose = { flowScreen = RoomFlowScreen.Home },
                    onConfirm = {
                        val roomName = createLogName.ifBlank { "로그 :)" }
                        createdLogName = roomName
                        onCreateRoom(roomName, selectedMemberCount) {
                            createdRoomId = null
                            flowScreen = RoomFlowScreen.LogCreated
                        }
                    }
                )
            } else if (flowScreen == RoomFlowScreen.LogCreated) {
                val createdRoom = createdRoomId?.let { id ->
                    uiState.rooms.firstOrNull { it.roomId == id }
                } ?: uiState.rooms.lastOrNull { it.roomName == createdLogName }
                LogCreatedContent(
                    scaleX = scaleX,
                    scaleY = scaleY,
                    statusTime = timeLabels.statusTime,
                    roomName = createdRoom?.roomName ?: createdLogName,
                    inviteCode = createdRoom?.inviteCode ?: "------",
                    onInvite = { },
                    onDone = {
                        flowScreen = RoomFlowScreen.Home
                        createdRoom?.let(onOpenRoom)
                    }
                )
            } else if (selectedTab == HomeTab.Camera) {
                CameraContent(
                    scaleX = scaleX,
                    scaleY = scaleY,
                    currentHourText = timeLabels.hourText,
                    message = cameraMessage,
                    onClose = { selectedTab = HomeTab.Log },
                    onCaptureComplete = {
                        val roomId = selectedRoomId ?: uiState.rooms.firstOrNull()?.roomId
                        if (roomId == null) {
                            cameraMessage = "사진이 기기에 저장되었습니다."
                            selectedTab = HomeTab.Log
                        } else {
                            cameraMessage = "기록 저장 중..."
                            onUploadRecord(
                                roomId,
                                "",
                                currentDateHour(),
                                null,
                                null
                            ) {
                                cameraMessage = "현재 시간 기록이 저장되었습니다."
                                selectedTab = HomeTab.Log
                            }
                        }
                    },
                    onCaptureError = { errorMessage ->
                        cameraMessage = errorMessage
                    }
                )
            } else {
                Text(
                    text = "SETLOG",
                    modifier = Modifier.offset(24.dp * scaleX, 78.dp * scaleY),
                    color = HomeTeal,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black
                )

                HomeActionButton(
                    icon = R.raw.home_add,
                    description = "방 만들기",
                    x = 203,
                    y = 76,
                    scaleX = scaleX,
                    scaleY = scaleY,
                    onClick = {
                        showCreateMenu = !showCreateMenu
                        showNotifications = false
                        showSettings = false
                    }
                )
                HomeActionButton(
                    icon = R.raw.home_notifications,
                    description = "알림함",
                    x = 263,
                    y = 76,
                    scaleX = scaleX,
                    scaleY = scaleY,
                    onClick = {
                        showNotifications = !showNotifications
                        showCreateMenu = false
                        showSettings = false
                    }
                )
                HomeActionButton(
                    icon = R.raw.home_more,
                    description = "설정",
                    x = 323,
                    y = 76,
                    scaleX = scaleX,
                    scaleY = scaleY,
                    onClick = {
                        showSettings = !showSettings
                        showCreateMenu = false
                        showNotifications = false
                    }
                )

                HomeAsset(
                    resource = R.raw.home_rotation_hint,
                    x = 24,
                    y = 134,
                    width = 20,
                    height = 20,
                    scaleX = scaleX,
                    scaleY = scaleY
                )
                Text(
                    text = "가로로 돌려 촬영",
                    modifier = Modifier.offset(52.dp * scaleX, 131.dp * scaleY),
                    color = HomeMuted,
                    fontSize = 15.sp
                )

                PersonalSpaceCards(scaleX, scaleY)

                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .offset(180.dp * scaleX, 410.dp * scaleY)
                                .size(32.dp),
                            color = HomeTeal,
                            strokeWidth = 2.dp
                        )
                    }

                    uiState.rooms.isEmpty() -> {
                        Text(
                            text = "참여 중인 그룹이 없습니다.",
                            modifier = Modifier
                                .offset(12.dp * scaleX, 397.dp * scaleY)
                                .width(369.dp * scaleX),
                            color = HomeMuted,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .offset(12.dp * scaleX, 377.dp * scaleY)
                                .width(369.dp * scaleX)
                                .height(350.dp * scaleY)
                        ) {
                            items(
                                items = uiState.rooms,
                                key = { it.roomId }
                            ) { room ->
                                GroupCard(
                                    room = room,
                                    scaleX = scaleX,
                                    scaleY = scaleY,
                                    selected = room.roomId == selectedRoomId,
                                    onClick = {
                                        selectedRoomId = room.roomId
                                        onOpenRoom(room)
                                    }
                                )
                                Spacer(modifier = Modifier.height(10.dp * scaleY))
                            }
                        }
                    }
                }

                if (hasTransientPopover) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                showCreateMenu = false
                                showNotifications = false
                                showSettings = false
                            }
                    )
                }

                if (showCreateMenu) {
                    CreateMenuPopover(
                        scaleX = scaleX,
                        scaleY = scaleY,
                        onCreateLog = {
                            showCreateMenu = false
                            createLogName = ""
                            selectedMemberCount = 4
                            flowScreen = RoomFlowScreen.CreateLog
                        },
                        onJoinLog = {
                            showCreateMenu = false
                            dialog = RoomDialog.Join
                        },
                        onStartZip = {
                            showCreateMenu = false
                        }
                    )
                }

                if (showNotifications) {
                    NotificationsPopover(
                        scaleX = scaleX,
                        scaleY = scaleY,
                        notifications = notifications,
                        onMarkRead = { id ->
                            NotificationRepository.markAsRead(context, id)
                        }
                    )
                }
            }

            if (flowScreen == RoomFlowScreen.Home) {
                CameraLogSwitch(
                    selectedTab = selectedTab,
                    scaleX = scaleX,
                    scaleY = scaleY,
                    onSelectTab = { tab ->
                        selectedTab = tab
                        if (tab == HomeTab.Camera) {
                            showSettings = false
                            showCreateMenu = false
                            showNotifications = false
                        }
                    }
                )
            }

            if (showSettings) {
                SettingsPopover(
                    profile = uiState.profile,
                    fallbackUserName = userName,
                    scaleX = scaleX,
                    scaleY = scaleY,
                    onEditName = { showNameEditor = true },
                    onChangeImage = { photoPickerLauncher.launch("image/*") },
                    onSelectColor = onUpdateProfileColor,
                    onLogAlarmClick = {
                        showSettings = false
                        showLogAlarmSheet = true
                    },
                    onLogout = {
                        showSettings = false
                        onLogout()
                    }
                )
            }

            if (showLogAlarmSheet) {
                LogAlarmBottomSheet(
                    initialSettings = remember { LogAlarmSettingsRepository.get(context) },
                    onDismiss = { showLogAlarmSheet = false },
                    onSave = { settings ->
                        LogAlarmSettingsRepository.save(context, settings)
                        HourlyAlarmReceiver.scheduleNextAlarm(context)
                        showLogAlarmSheet = false
                    }
                )
            }
        }
    }

    if (dialog == RoomDialog.Join) {
        JoinLogOverlay(
            isSubmitting = uiState.isSubmitting,
            errorMessage = uiState.errorMessage,
            onDismiss = { dialog = null },
            onConfirm = { code ->
                onJoinRoom(code) { dialog = null }
            }
        )
    }

    if (showNameEditor) {
        ProfileNameEditor(
            currentName = uiState.profile.name.ifBlank { userName },
            isSubmitting = uiState.isSubmitting,
            onDismiss = { showNameEditor = false },
            onSave = { newName ->
                onUpdateProfileName(newName) {
                    showNameEditor = false
                }
            }
        )
    }
}

@Composable
private fun CreateLogContent(
    scaleX: Float,
    scaleY: Float,
    statusTime: String,
    logName: String,
    selectedMemberCount: Int,
    isSubmitting: Boolean,
    errorMessage: String?,
    onLogNameChange: (String) -> Unit,
    onMemberCountChange: (Int) -> Unit,
    onClose: () -> Unit,
    onConfirm: () -> Unit
) {
    Text(
        text = "$statusTime                                      66⚡",
        modifier = Modifier.offset(41.dp * scaleX, 18.dp * scaleY),
        color = Color.White,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold
    )

    RoundTextAction(
        text = "×",
        x = 24,
        y = 66,
        size = 44,
        background = HomeButton,
        border = HomeBorder,
        textColor = Color.White,
        scaleX = scaleX,
        scaleY = scaleY,
        onClick = onClose
    )
    RoundTextAction(
        text = "✓",
        x = 324,
        y = 66,
        size = 44,
        background = HomePink,
        border = HomePink,
        textColor = Color.Black,
        scaleX = scaleX,
        scaleY = scaleY,
        enabled = !isSubmitting,
        onClick = onConfirm
    )

    Text(
        text = "SETLOG",
        modifier = Modifier.offset(153.dp * scaleX, 76.dp * scaleY),
        color = HomePink,
        fontSize = 30.sp,
        fontWeight = FontWeight.Black
    )
    Text(
        text = "→  로그 만들기",
        modifier = Modifier.offset(38.dp * scaleX, 146.dp * scaleY),
        color = Color.White,
        fontSize = 18.sp
    )

    BasicTextField(
        value = logName,
        onValueChange = { onLogNameChange(it.take(18)) },
        singleLine = true,
        textStyle = androidx.compose.ui.text.TextStyle(
            color = Color.White,
            fontSize = 17.sp
        ),
        modifier = Modifier
            .offset(48.dp * scaleX, 200.dp * scaleY)
            .width(270.dp * scaleX),
        decorationBox = { innerTextField ->
            if (logName.isBlank()) {
                Text(
                    text = "로그 이름  (선택)",
                    color = Color(0xFF9E9CA6),
                    fontSize = 17.sp
                )
            }
            innerTextField()
        }
    )

    Text(
        text = "인원수 선택",
        modifier = Modifier.offset(38.dp * scaleX, 249.dp * scaleY),
        color = Color.White,
        fontSize = 16.sp
    )

    val counts = (2..12).toList()
    counts.forEachIndexed { index, count ->
        val row = index / 6
        val column = index % 6
        MemberCountChip(
            count = count,
            selected = selectedMemberCount == count,
            x = 38 + column * 53,
            y = 297 + row * 49,
            scaleX = scaleX,
            scaleY = scaleY,
            onClick = { onMemberCountChange(count) }
        )
    }

    errorMessage?.let {
        Text(
            text = it,
            modifier = Modifier
                .offset(38.dp * scaleX, 402.dp * scaleY)
                .width(315.dp * scaleX),
            color = HomePink,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun MemberCountChip(
    count: Int,
    selected: Boolean,
    x: Int,
    y: Int,
    scaleX: Float,
    scaleY: Float,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .offset(x.dp * scaleX, y.dp * scaleY)
            .size(38.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) HomePink else Color.Black)
            .border(
                width = 1.5.dp,
                color = if (selected) HomePink else Color(0xFF6B6B70),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = count.toString(),
            color = if (selected) Color.Black else Color.White,
            fontSize = 17.sp
        )
    }
}

@Composable
private fun LogCreatedContent(
    scaleX: Float,
    scaleY: Float,
    statusTime: String,
    roomName: String,
    inviteCode: String,
    onInvite: () -> Unit,
    onDone: () -> Unit
) {
    Text(
        text = "$statusTime                                      66⚡",
        modifier = Modifier.offset(41.dp * scaleX, 18.dp * scaleY),
        color = Color.White,
        fontSize = 16.sp,
        fontWeight = FontWeight.Black
    )
    Text(
        text = "SETLOG",
        modifier = Modifier.offset(153.dp * scaleX, 76.dp * scaleY),
        color = HomePink,
        fontSize = 30.sp,
        fontWeight = FontWeight.Black
    )
    Text(
        text = roomName,
        modifier = Modifier.offset(38.dp * scaleX, 149.dp * scaleY),
        color = Color.White,
        fontSize = 18.sp
    )
    Text(
        text = "#  ${inviteCode.lowercase()}",
        modifier = Modifier.offset(38.dp * scaleX, 208.dp * scaleY),
        color = Color.White,
        fontSize = 18.sp,
        letterSpacing = 2.sp
    )
    Text(
        text = "친구 초대  →",
        modifier = Modifier
            .offset(38.dp * scaleX, 264.dp * scaleY)
            .clickable(onClick = onInvite),
        color = Color.White,
        fontSize = 18.sp,
        textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
    )
    Text(
        text = "완료  →",
        modifier = Modifier
            .offset(38.dp * scaleX, 326.dp * scaleY)
            .clickable(onClick = onDone),
        color = Color.White,
        fontSize = 18.sp,
        textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
    )
}

@Composable
private fun JoinLogOverlay(
    isSubmitting: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var code by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xD11A1A1A))
            .clickable(enabled = !isSubmitting, onClick = onDismiss)
    ) {
        Column(
            modifier = Modifier
                .offset(22.dp, 248.dp)
                .width(344.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(Color.Black.copy(alpha = 0.95f))
                .padding(horizontal = 30.dp, vertical = 15.dp)
        ) {
            Text("로그 참여하기", color = Color.White, fontSize = 20.sp)
            Text("친구한테 로그 코드를 받으세요", color = HomePink, fontSize = 15.sp)
        }

        Box(
            modifier = Modifier
                .offset(22.dp, 338.dp)
                .width(344.dp)
                .height(82.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(Color.White)
                .border(1.5.dp, Color.Black, RoundedCornerShape(30.dp))
                .clickable(enabled = false) {},
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = "#",
                modifier = Modifier.offset(26.dp, 0.dp),
                color = Color.Black,
                fontSize = 28.sp
            )
            BasicTextField(
                value = code,
                onValueChange = { code = it.uppercase().take(6) },
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = Color.Black,
                    fontSize = 24.sp
                ),
                modifier = Modifier
                    .offset(68.dp, 0.dp)
                    .width(200.dp),
                decorationBox = { innerTextField ->
                    if (code.isBlank()) {
                        Text("abc123", color = Color(0xFFE0E0E0), fontSize = 24.sp)
                    }
                    innerTextField()
                }
            )
            Box(
                modifier = Modifier
                    .offset(68.dp, 20.dp)
                    .width(200.dp)
                    .height(2.dp)
                    .background(Color.Black)
            )
            Box(
                modifier = Modifier
                    .offset(282.dp, 0.dp)
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(HomePink)
                    .clickable(enabled = code.isNotBlank() && !isSubmitting) {
                        onConfirm(code)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("→", color = Color.Black, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            }
        }

        errorMessage?.let {
            Text(
                text = it,
                modifier = Modifier
                    .offset(38.dp, 430.dp)
                    .width(320.dp),
                color = HomePink,
                fontSize = 13.sp
            )
        }

        Column(
            modifier = Modifier
                .offset(0.dp, 546.dp)
                .fillMaxWidth()
                .height(306.dp)
                .clip(RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp))
                .background(Color(0xFF171717))
                .padding(start = 18.dp, top = 28.dp)
        ) {
            Text("q  w  e  r  t  y  u  i  o  p", color = Color.White, fontSize = 22.sp, letterSpacing = 3.sp)
            Spacer(Modifier.height(36.dp))
            Text("a  s  d  f  g  h  j  k  l", color = Color.White, fontSize = 22.sp, letterSpacing = 3.sp, modifier = Modifier.padding(start = 13.dp))
            Spacer(Modifier.height(36.dp))
            Text("⇧   z  x  c  v  b  n  m   ⌫", color = Color.White, fontSize = 22.sp, letterSpacing = 3.sp)
            Spacer(Modifier.height(34.dp))
            Text("123     space        ✓", color = Color.White, fontSize = 18.sp, letterSpacing = 2.sp)
        }
    }
}

@Composable
private fun RoundTextAction(
    text: String,
    x: Int,
    y: Int,
    size: Int,
    background: Color,
    border: Color,
    textColor: Color,
    scaleX: Float,
    scaleY: Float,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .offset(x.dp * scaleX, y.dp * scaleY)
            .size(size.dp)
            .clip(CircleShape)
            .background(background)
            .border(1.dp, border, CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = textColor, fontSize = 28.sp)
    }
}

@Composable
private fun CreateMenuPopover(
    scaleX: Float,
    scaleY: Float,
    onCreateLog: () -> Unit,
    onJoinLog: () -> Unit,
    onStartZip: () -> Unit
) {
    Box(
        modifier = Modifier
            .offset(6.dp * scaleX, 76.dp * scaleY)
            .width(250.dp * scaleX)
            .height(166.dp * scaleY)
            .clip(RoundedCornerShape(34.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xF00D383D),
                        Color(0xF52E1229),
                        Color(0xF51A121A)
                    )
                )
            )
            .border(1.dp, Color(0xFF4D3847), RoundedCornerShape(34.dp))
    ) {
        PopoverMenuText(
            text = "로그 만들기",
            y = 20,
            scaleX = scaleX,
            scaleY = scaleY,
            onClick = onCreateLog
        )
        PopoverMenuText(
            text = "로그 참여하기",
            y = 62,
            scaleX = scaleX,
            scaleY = scaleY,
            onClick = onJoinLog
        )
        Box(
            modifier = Modifier
                .offset(27.dp * scaleX, 101.dp * scaleY)
                .width(200.dp * scaleX)
                .height(1.dp)
                .background(Color(0xFF403840))
        )
        PopoverMenuText(
            text = "zip 시작하기",
            y = 120,
            scaleX = scaleX,
            scaleY = scaleY,
            onClick = onStartZip
        )
    }
}

@Composable
private fun PopoverMenuText(
    text: String,
    y: Int,
    scaleX: Float,
    scaleY: Float,
    onClick: () -> Unit
) {
    Text(
        text = text,
        modifier = Modifier
            .offset(27.dp * scaleX, y.dp * scaleY)
            .width(200.dp * scaleX)
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp),
        color = Color.White,
        fontSize = 17.sp
    )
}

@Composable
private fun NotificationsPopover(
    scaleX: Float,
    scaleY: Float,
    notifications: List<NotificationItem>,
    onMarkRead: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .offset(122.dp * scaleX, 76.dp * scaleY)
            .width(250.dp * scaleX)
            .wrapContentHeight()
            .clip(RoundedCornerShape(34.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xF01B1B1B),
                        Color(0xF5141B22),
                        Color(0xF52A1229)
                    )
                )
            )
            .border(1.dp, Color(0xFF4D3847), RoundedCornerShape(34.dp))
    ) {
        Column(modifier = Modifier.padding(horizontal = 27.dp * scaleX)) {
            Text(
                text = "알림함",
                modifier = Modifier.padding(top = 20.dp * scaleY),
                color = Color.White,
                fontSize = 17.sp
            )
            Spacer(modifier = Modifier.height(8.dp * scaleY))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFF403840))
            )
            Spacer(modifier = Modifier.height(8.dp * scaleY))

            if (notifications.isEmpty()) {
                Text(
                    text = "새 알림이 없어요",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp * scaleY),
                    color = HomeMuted,
                    fontSize = 15.sp
                )
            } else {
                notifications.take(5).forEach { item ->
                    NotificationRow(
                        item = item,
                        scaleX = scaleX,
                        scaleY = scaleY,
                        onMarkRead = onMarkRead
                    )
                    Spacer(modifier = Modifier.height(4.dp * scaleY))
                }
            }
            Spacer(modifier = Modifier.height(12.dp * scaleY))
        }
    }
}

@Composable
private fun NotificationRow(
    item: NotificationItem,
    scaleX: Float,
    scaleY: Float,
    onMarkRead: (String) -> Unit
) {
    val timeLabel = remember(item.timestamp) {
        DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(item.timestamp))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (item.isRead) Color.Transparent else Color(0x1A00D3C8)
            )
            .clickable {
                if (!item.isRead) onMarkRead(item.id)
            }
            .padding(vertical = 8.dp * scaleY),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(
                    if (item.isRead) Color.Transparent else Color(0xFF00D3C8)
                )
        )
        Spacer(modifier = Modifier.width(8.dp * scaleX))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.body,
                color = if (item.isRead) HomeMuted else Color.White,
                fontSize = 13.sp,
                maxLines = 2
            )
            Text(
                text = timeLabel,
                color = HomeMuted,
                fontSize = 11.sp
            )
        }
    }
}

private data class CurrentTimeLabels(
    val statusTime: String,
    val hourText: String
)

@Composable
private fun rememberCurrentTimeLabels(): CurrentTimeLabels {
    var labels by remember { mutableStateOf(currentTimeLabels()) }

    LaunchedEffect(Unit) {
        while (true) {
            labels = currentTimeLabels()
            delay(1_000L)
        }
    }

    return labels
}

private fun currentTimeLabels(): CurrentTimeLabels {
    val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"))
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)

    return CurrentTimeLabels(
        statusTime = String.format(Locale.US, "%d:%02d", hour, minute),
        hourText = String.format(Locale.US, "%02d:00", hour)
    )
}

private fun currentDateHour(): String {
    val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"))
    val formatter = SimpleDateFormat("yyyy-MM-dd-HH", Locale.KOREA)
    formatter.timeZone = TimeZone.getTimeZone("Asia/Seoul")
    return formatter.format(calendar.time)
}

@Composable
private fun SettingsPopover(
    profile: RoomProfileUi,
    fallbackUserName: String,
    scaleX: Float,
    scaleY: Float,
    onEditName: () -> Unit,
    onChangeImage: () -> Unit,
    onSelectColor: (String) -> Unit,
    onLogAlarmClick: () -> Unit,
    onLogout: () -> Unit
) {
    var isProfileExpanded by remember { mutableStateOf(true) }
    var isColorExpanded by remember { mutableStateOf(false) }
    val profileName = profile.name.ifBlank { fallbackUserName.ifBlank { "User" } }

    Box(
        modifier = Modifier
            .offset(91.dp * scaleX, 78.dp * scaleY)
            .width(272.dp * scaleX)
            .height((if (isProfileExpanded && isColorExpanded) 505 else if (isProfileExpanded) 250 else 220).dp * scaleY)
            .clip(RoundedCornerShape(34.dp))
            .background(Color(0xF5291226))
            .border(1.dp, Color(0xFF8C2E82), RoundedCornerShape(34.dp))
    ) {
        ProfileAvatar(
            profile = profile,
            modifier = Modifier.offset(19.dp * scaleX, 16.dp * scaleY)
        )
        Text(
            text = profileName,
            modifier = Modifier
                .offset(57.dp * scaleX, 17.dp * scaleY)
                .width(180.dp * scaleX),
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )

        SettingsMenuItem(
            icon = "♙",
            label = "프로필 편집",
            showChevron = true,
            expanded = isProfileExpanded,
            y = 54,
            scaleX = scaleX,
            scaleY = scaleY,
            onClick = { isProfileExpanded = !isProfileExpanded }
        )

        if (isProfileExpanded) {
            Box(
                modifier = Modifier
                    .offset(27.dp * scaleX, 92.dp * scaleY)
                    .width(210.dp * scaleX)
                    .height(1.dp)
                    .background(Color(0xFF403840))
            )
            ProfileEditText(
                text = "이름",
                y = 121,
                scaleX = scaleX,
                scaleY = scaleY,
                onClick = onEditName
            )
            ProfileEditText(
                text = "변경",
                y = 162,
                scaleX = scaleX,
                scaleY = scaleY,
                onClick = onChangeImage
            )
            ProfileEditText(
                text = "프로필 색상",
                y = 203,
                scaleX = scaleX,
                scaleY = scaleY,
                trailing = if (isColorExpanded) "⌄" else "›",
                onClick = { isColorExpanded = !isColorExpanded }
            )

            if (isColorExpanded) {
                ProfileColorSelector(
                    selectedColorName = profile.colorName,
                    scaleX = scaleX,
                    scaleY = scaleY,
                    onSelectColor = onSelectColor
                )
            }
        } else {
            SettingsMenuItem("♧", "로그 알림", true, false, 85, scaleX, scaleY, onClick = onLogAlarmClick)
            SettingsMenuItem("◉", "계정 관리", true, false, 116, scaleX, scaleY)
            SettingsMenuItem("⊕", "피드백 보내기", false, false, 147, scaleX, scaleY)
            SettingsMenuItem("↪", "로그아웃", false, false, 178, scaleX, scaleY, onClick = onLogout)
        }
    }
}

@Composable
private fun SettingsMenuItem(
    icon: String,
    label: String,
    showChevron: Boolean,
    expanded: Boolean,
    y: Int,
    scaleX: Float,
    scaleY: Float,
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .offset(17.dp * scaleX, y.dp * scaleY)
            .width(236.dp * scaleX)
            .height(30.dp * scaleY)
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
    ) {
        Text(
            text = icon,
            modifier = Modifier.offset(0.dp, 3.dp * scaleY),
            color = Color.White,
            fontSize = 18.sp
        )
        Text(
            text = label,
            modifier = Modifier.offset(34.dp * scaleX, 3.dp * scaleY),
            color = Color.White,
            fontSize = 16.sp
        )
        if (showChevron) {
            Text(
                text = if (expanded) "⌄" else "›",
                modifier = Modifier.offset(218.dp * scaleX, (-3).dp * scaleY),
                color = Color.White,
                fontSize = 24.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LogAlarmBottomSheet(
    initialSettings: LogAlarmSettings,
    onDismiss: () -> Unit,
    onSave: (LogAlarmSettings) -> Unit
) {
    var selectedInterval by remember { mutableStateOf(initialSettings.interval) }
    var selectedMinute by remember { mutableStateOf(initialSettings.minute) }
    var intervalExpanded by remember { mutableStateOf(false) }
    var minuteExpanded by remember { mutableStateOf(false) }
    val isOff = selectedInterval == LogAlarmInterval.OFF

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1C1620)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = "로그 알림",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(20.dp))

            Text(text = "주기", color = Color(0xFFB9AEB8), fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            ExposedDropdownMenuBox(
                expanded = intervalExpanded,
                onExpandedChange = { intervalExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedInterval.label,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = intervalExpanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF8C2E82),
                        unfocusedBorderColor = Color(0xFF403840)
                    ),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = intervalExpanded,
                    onDismissRequest = { intervalExpanded = false }
                ) {
                    LogAlarmInterval.entries.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.label) },
                            onClick = {
                                selectedInterval = option
                                intervalExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(text = "분", color = Color(0xFFB9AEB8), fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            ExposedDropdownMenuBox(
                expanded = minuteExpanded && !isOff,
                onExpandedChange = { if (!isOff) minuteExpanded = it }
            ) {
                OutlinedTextField(
                    value = String.format(Locale.US, "%02d", selectedMinute),
                    onValueChange = {},
                    readOnly = true,
                    enabled = !isOff,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = minuteExpanded && !isOff) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        disabledTextColor = Color(0xFF6E646C),
                        focusedBorderColor = Color(0xFF8C2E82),
                        unfocusedBorderColor = Color(0xFF403840),
                        disabledBorderColor = Color(0xFF2A222A)
                    ),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = minuteExpanded && !isOff,
                    onDismissRequest = { minuteExpanded = false }
                ) {
                    LOG_ALARM_MINUTE_OPTIONS.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(String.format(Locale.US, "%02d", option)) },
                            onClick = {
                                selectedMinute = option
                                minuteExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            TextButton(
                onClick = {
                    onSave(LogAlarmSettings(interval = selectedInterval, minute = selectedMinute))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "저장", color = Color(0xFFE85DD0), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ProfileAvatar(
    profile: RoomProfileUi,
    modifier: Modifier = Modifier
) {
    val avatarColor = profileColorOf(profile.colorName)

    Box(
        modifier = modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(avatarColor),
        contentAlignment = Alignment.Center
    ) {
        if (profile.imageUrl.isNotBlank()) {
            AsyncImage(
                model = profile.imageUrl,
                contentDescription = "프로필 이미지",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = "♫",
                color = Color(0xFF291226),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ProfileEditText(
    text: String,
    y: Int,
    scaleX: Float,
    scaleY: Float,
    trailing: String? = null,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .offset(30.dp * scaleX, y.dp * scaleY)
            .width(216.dp * scaleX)
            .height(33.dp * scaleY)
            .clickable(onClick = onClick)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 18.sp
        )
        trailing?.let {
            Text(
                text = it,
                modifier = Modifier.offset(190.dp * scaleX, (-5).dp * scaleY),
                color = Color.White,
                fontSize = 28.sp
            )
        }
    }
}

private data class ProfileColorOption(
    val name: String,
    val label: String,
    val color: Color
)

private val ProfileColors = listOf(
    ProfileColorOption("cyan", "파랑", Color(0xFF18C9E8)),
    ProfileColorOption("orange", "주황", Color(0xFFFF8757)),
    ProfileColorOption("purple", "보라", Color(0xFFAA5AF4)),
    ProfileColorOption("green", "초록", Color(0xFF00E879)),
    ProfileColorOption("blue", "바다", Color(0xFF479CF5)),
    ProfileColorOption("pink", "분홍", HomePink)
)

@Composable
private fun ProfileColorSelector(
    selectedColorName: String,
    scaleX: Float,
    scaleY: Float,
    onSelectColor: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .offset(27.dp * scaleX, 254.dp * scaleY)
            .width(210.dp * scaleX)
            .height(1.dp)
            .background(Color(0xFF403840))
    )
    ProfileColors.forEachIndexed { index, option ->
        val y = 278 + index * 47
        Box(
            modifier = Modifier
                .offset(30.dp * scaleX, y.dp * scaleY)
                .width(216.dp * scaleX)
                .height(38.dp * scaleY)
                .clickable { onSelectColor(option.name) }
        ) {
            if (selectedColorName == option.name) {
                Text(
                    text = "✓",
                    modifier = Modifier.offset(0.dp, 3.dp * scaleY),
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Box(
                modifier = Modifier
                    .offset(38.dp * scaleX, 3.dp * scaleY)
                    .size(25.dp)
                    .clip(CircleShape)
                    .background(option.color)
            )
            Text(
                text = option.label,
                modifier = Modifier.offset(82.dp * scaleX, 0.dp),
                color = Color.White,
                fontSize = 20.sp
            )
        }
    }
}

private fun profileColorOf(colorName: String): Color {
    return ProfileColors.firstOrNull { it.name == colorName }?.color ?: HomePink
}

@Composable
private fun ProfileNameEditor(
    currentName: String,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var name by remember(currentName) { mutableStateOf(currentName) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xA6000000))
            .clickable(enabled = !isSubmitting, onClick = onDismiss),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .offset(y = 216.dp)
                .width(344.dp)
                .height(232.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(Color(0xF0262628))
                .border(1.dp, Color(0xFF57545C), RoundedCornerShape(32.dp))
                .clickable {}
        ) {
            Text(
                text = "이름 편집",
                modifier = Modifier.offset(31.dp, 28.dp),
                color = Color.White,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "이름을 입력하세요",
                modifier = Modifier.offset(31.dp, 67.dp),
                color = Color(0xFFAAA7B0),
                fontSize = 17.sp
            )
            Box(
                modifier = Modifier
                    .offset(31.dp, 110.dp)
                    .width(282.dp)
                    .height(52.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color(0xFF3A3A3D)),
                contentAlignment = Alignment.CenterStart
            ) {
                BasicTextField(
                    value = name,
                    onValueChange = { name = it.take(16) },
                    singleLine = true,
                    enabled = !isSubmitting,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = Color.White,
                        fontSize = 20.sp
                    ),
                    modifier = Modifier
                        .padding(horizontal = 18.dp)
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                )
            }
            ProfileNameButton(
                text = "취소",
                x = 31,
                background = Color(0xFF343437),
                enabled = !isSubmitting,
                onClick = onDismiss
            )
            ProfileNameButton(
                text = if (isSubmitting) "저장 중" else "저장",
                x = 193,
                background = HomePink,
                enabled = name.isNotBlank() && !isSubmitting,
                onClick = { onSave(name.trim()) }
            )
        }
    }
}

@Composable
private fun ProfileNameButton(
    text: String,
    x: Int,
    background: Color,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .offset(x.dp, 178.dp)
            .width(120.dp)
            .height(52.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(background.copy(alpha = if (enabled) 1f else 0.55f))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun PersonalSpaceCards(scaleX: Float, scaleY: Float) {
    Box(
        modifier = Modifier
            .offset(12.dp * scaleX, 176.dp * scaleY)
            .width(369.dp * scaleX)
            .height(96.dp * scaleY)
            .clip(RoundedCornerShape(26.dp))
            .background(HomeCard)
    ) {
        Text(
            text = "vlog",
            modifier = Modifier.offset(20.dp * scaleX, 26.dp * scaleY),
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "나만의 공간, 매일 오전 4시에 하루 시작",
            modifier = Modifier.offset(20.dp * scaleX, 56.dp * scaleY),
            color = HomeMuted,
            fontSize = 14.sp
        )
        HomeAsset(R.raw.home_star, 298, 20, 54, 54, scaleX, scaleY)
    }

    Box(
        modifier = Modifier
            .offset(7.dp * scaleX, 288.dp * scaleY)
            .width(379.dp * scaleX)
            .height(78.dp * scaleY)
            .clip(RoundedCornerShape(38.dp))
            .background(HomeCard)
            .border(1.dp, Color(0xFF212121), RoundedCornerShape(38.dp))
    ) {
        Text(
            text = "Zip ✦",
            modifier = Modifier.offset(32.dp * scaleX, 18.dp * scaleY),
            color = Color.White,
            fontSize = 21.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            text = "사진으로 이야기",
            modifier = Modifier.offset(32.dp * scaleX, 48.dp * scaleY),
            color = HomeMuted,
            fontSize = 14.sp
        )
        HomeAsset(R.raw.home_envelope, 308, 18, 50, 39, scaleX, scaleY)
    }
}

@Composable
private fun GroupCard(
    room: UserRoom,
    scaleX: Float,
    scaleY: Float,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(369.dp * scaleX)
            .height(80.dp * scaleY)
            .clip(RoundedCornerShape(26.dp))
            .background(HomeCard)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) HomeTeal else HomeBorder,
                shape = RoundedCornerShape(26.dp)
            )
            .clickable(onClick = onClick)
    ) {
        Text(
            text = room.roomName,
            modifier = Modifier
                .offset(20.dp * scaleX, 25.dp * scaleY)
                .width(210.dp * scaleX),
            color = Color.White,
            fontSize = 20.sp,
            maxLines = 1
        )
        HomeAsset(R.raw.home_avatar, 255, 28, 24, 24, scaleX, scaleY)
        HomeAsset(R.raw.home_avatar, 285, 28, 24, 24, scaleX, scaleY)
        Box(
            modifier = Modifier
                .offset(318.dp * scaleX, 28.dp * scaleY)
                .width(1.dp)
                .height(24.dp * scaleY)
                .background(HomeBorder)
        )
        HomeAsset(R.raw.home_camera, 333, 28, 28, 24, scaleX, scaleY)
    }
}

@Composable
private fun CameraLogSwitch(
    selectedTab: HomeTab,
    scaleX: Float,
    scaleY: Float,
    onSelectTab: (HomeTab) -> Unit
) {
    val cameraSelected = selectedTab == HomeTab.Camera

    Box(
        modifier = Modifier
            .offset(116.dp * scaleX, 769.dp * scaleY)
            .width(160.dp * scaleX)
            .height(54.dp * scaleY)
            .clip(RoundedCornerShape(27.dp))
            .background(HomeButton)
            .border(1.dp, HomeBorder, RoundedCornerShape(27.dp))
    ) {
        Box(
            modifier = Modifier
                .offset((if (cameraSelected) 2 else 80).dp * scaleX, 2.dp * scaleY)
                .width(78.dp * scaleX)
                .height(50.dp * scaleY)
                .clip(RoundedCornerShape(25.dp))
                .background(Color(0xFF333333))
                .border(1.dp, Color(0xFF4A4A4A), RoundedCornerShape(25.dp))
        )
        Text(
            text = "카메라",
            modifier = Modifier
                .offset(19.dp * scaleX, 15.dp * scaleY)
                .clickable { onSelectTab(HomeTab.Camera) },
            color = if (cameraSelected) Color.White else Color(0xFFA6A3AB),
            fontSize = if (cameraSelected) 17.sp else 16.sp,
            fontWeight = if (cameraSelected) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = "로그",
            modifier = Modifier
                .offset(104.dp * scaleX, 15.dp * scaleY)
                .clickable { onSelectTab(HomeTab.Log) },
            color = if (cameraSelected) Color(0xFFA6A3AB) else Color.White,
            fontSize = if (cameraSelected) 16.sp else 17.sp,
            fontWeight = if (cameraSelected) FontWeight.Normal else FontWeight.Bold
        )
    }
}

@Composable
private fun CameraContent(
    scaleX: Float,
    scaleY: Float,
    currentHourText: String,
    message: String?,
    onClose: () -> Unit,
    onCaptureComplete: () -> Unit,
    onCaptureError: (String) -> Unit
) {
    val context = LocalContext.current
    val isInPreview = LocalInspectionMode.current
    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
    var zoomRatio by remember { mutableFloatStateOf(1f) }
    var isFlashEnabled by remember { mutableStateOf(false) }
    var boundCamera by remember { mutableStateOf<Camera?>(null) }
    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }
    var hasCameraPermission by remember {
        mutableStateOf(
            isInPreview ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    LaunchedEffect(Unit) {
        if (!isInPreview && !hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(
        modifier = Modifier
            .offset(10.dp * scaleX, 72.dp * scaleY)
            .width(373.dp * scaleX)
            .height(664.dp * scaleY)
            .clip(RoundedCornerShape(30.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF212929),
                        Color(0xFF75786B),
                        Color(0xFF291F14)
                    )
                )
            )
            .border(2.dp, Color(0xFF8C4080), RoundedCornerShape(30.dp))
    ) {
        if (isInPreview) {
            CameraPreviewPlaceholder()
        } else if (hasCameraPermission) {
            CameraPreview(
                lensFacing = lensFacing,
                zoomRatio = zoomRatio,
                isFlashEnabled = isFlashEnabled,
                imageCapture = imageCapture,
                onCameraReady = { camera ->
                    boundCamera = camera
                    val supportedZoomRatio = camera.coerceZoomRatio(zoomRatio)
                    if (supportedZoomRatio != zoomRatio) {
                        zoomRatio = supportedZoomRatio
                    }
                    if (!camera.cameraInfo.hasFlashUnit()) {
                        isFlashEnabled = false
                    }
                },
                onCameraError = { errorMessage ->
                    onCaptureError(errorMessage)
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xCC111111)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "카메라 권한이 필요합니다.",
                    modifier = Modifier.clickable {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Box(
            modifier = Modifier
                .offset(316.dp * scaleX, 12.dp * scaleY)
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0xB81F1F1F))
                .clickable(onClick = onClose),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "×",
                color = Color.White,
                fontSize = 34.sp,
                fontWeight = FontWeight.Normal
            )
        }

        Text(
            text = currentHourText,
            modifier = Modifier
                .offset(151.dp * scaleX, 303.dp * scaleY)
                .rotate(90f),
            color = Color.White,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold
        )

        message?.let {
            Text(
                text = it,
                modifier = Modifier
                    .offset(42.dp * scaleX, 364.dp * scaleY)
                    .width(290.dp * scaleX),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        CameraZoomLabel(".5", 116, 553, zoomRatio == 0.5f, scaleX, scaleY) {
            zoomRatio = boundCamera?.coerceZoomRatio(0.5f) ?: 0.5f
        }
        CameraZoomLabel("1", 164, 556, zoomRatio == 1f, scaleX, scaleY) {
            zoomRatio = boundCamera?.coerceZoomRatio(1f) ?: 1f
        }
        CameraZoomLabel("2", 212, 556, zoomRatio == 2f, scaleX, scaleY) {
            zoomRatio = boundCamera?.coerceZoomRatio(2f) ?: 2f
        }
        CameraZoomLabel("3", 260, 556, zoomRatio == 3f, scaleX, scaleY) {
            zoomRatio = boundCamera?.coerceZoomRatio(3f) ?: 3f
        }

        Box(
            modifier = Modifier
                .offset(84.dp * scaleX, 596.dp * scaleY)
                .size(52.dp)
                .clip(CircleShape)
                .background(Color(0x66111111))
                .clickable(
                    enabled = boundCamera?.cameraInfo?.hasFlashUnit() == true
                ) {
                    isFlashEnabled = !isFlashEnabled
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "⚡",
                color = when {
                    isFlashEnabled -> Color(0xFFFFC700)
                    boundCamera?.cameraInfo?.hasFlashUnit() == true -> Color.White
                    else -> Color.White.copy(alpha = 0.35f)
                },
                fontSize = 26.sp
            )
        }

        Box(
            modifier = Modifier
                .offset(149.dp * scaleX, 586.dp * scaleY)
                .size(76.dp)
                .clip(CircleShape)
                .background(Color(0xFF0AC7E3))
                .border(5.dp, Color(0xFF141AE5), CircleShape)
                .clickable {
                    takeSetLogPhoto(
                        context = context,
                        imageCapture = imageCapture,
                        isFlashEnabled = isFlashEnabled,
                        onCaptureComplete = onCaptureComplete,
                        onCaptureError = onCaptureError
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "∩",
                modifier = Modifier.rotate(90f),
                color = Color(0xFF101010),
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }

    CameraRoundAction(
        label = "↻",
        x = 37,
        y = 772,
        scaleX = scaleX,
        scaleY = scaleY,
        onClick = {
            lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                CameraSelector.LENS_FACING_FRONT
            } else {
                CameraSelector.LENS_FACING_BACK
            }
            zoomRatio = 1f
            isFlashEnabled = false
        }
    )
    CameraRoundAction(
        label = "⤢",
        x = 307,
        y = 772,
        scaleX = scaleX,
        scaleY = scaleY,
        onClick = {
            lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                CameraSelector.LENS_FACING_FRONT
            } else {
                CameraSelector.LENS_FACING_BACK
            }
            zoomRatio = 1f
            isFlashEnabled = false
        }
    )
}

private fun takeSetLogPhoto(
    context: Context,
    imageCapture: ImageCapture,
    isFlashEnabled: Boolean,
    onCaptureComplete: () -> Unit,
    onCaptureError: (String) -> Unit
) {
    val photoFile = File.createTempFile(
        "setlog_${System.currentTimeMillis()}_",
        ".jpg",
        context.cacheDir
    )
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
    imageCapture.flashMode = if (isFlashEnabled) {
        ImageCapture.FLASH_MODE_ON
    } else {
        ImageCapture.FLASH_MODE_OFF
    }

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                onCaptureComplete()
            }

            override fun onError(exception: ImageCaptureException) {
                onCaptureError(exception.localizedMessage ?: "촬영에 실패했습니다.")
            }
        }
    )
}

@Composable
private fun CameraPreviewPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF212929),
                        Color(0xFF75786B),
                        Color(0xFF291F14)
                    )
                )
            )
    )
}

@Composable
private fun CameraPreview(
    lensFacing: Int,
    zoomRatio: Float,
    isFlashEnabled: Boolean,
    imageCapture: ImageCapture,
    onCameraReady: (Camera) -> Unit,
    onCameraError: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var camera by remember { mutableStateOf<Camera?>(null) }
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }

    DisposableEffect(lifecycleOwner, lensFacing) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val executor = ContextCompat.getMainExecutor(context)
        val listener = Runnable {
            val cameraProvider = cameraProviderFuture.get()
            val preview = CameraPreviewUseCase.Builder()
                .build()
                .also { it.setSurfaceProvider(previewView.surfaceProvider) }
            val selector = CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()

            runCatching {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    selector,
                    preview,
                    imageCapture
                ).also {
                    camera = it
                    onCameraReady(it)
                    Log.d("SetLogCamera", "Bound ${lensFacing.cameraLabel()} camera")
                }
            }.onFailure { error ->
                val message = "${lensFacing.cameraLabel()} 카메라를 열지 못했습니다."
                Log.e("SetLogCamera", message, error)
                onCameraError(message)
            }
        }

        cameraProviderFuture.addListener(listener, executor)

        onDispose {
            runCatching {
                camera = null
                cameraProviderFuture.get().unbindAll()
            }
        }
    }

    LaunchedEffect(camera, zoomRatio) {
        camera?.setSafeZoomRatio(zoomRatio)
    }

    LaunchedEffect(camera, isFlashEnabled) {
        camera?.let {
            if (it.cameraInfo.hasFlashUnit()) {
                it.cameraControl.enableTorch(isFlashEnabled)
            }
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier
    )
}

@Composable
private fun CameraZoomLabel(
    text: String,
    x: Int,
    y: Int,
    selected: Boolean,
    scaleX: Float,
    scaleY: Float,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .offset((x - 14).dp * scaleX, (y - 14).dp * scaleY)
            .size(40.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            modifier = Modifier.rotate(-90f),
            color = if (selected) Color(0xFFFFC700) else Color.White.copy(alpha = 0.8f),
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

private fun Camera.setSafeZoomRatio(requestedRatio: Float) {
    cameraControl.setZoomRatio(coerceZoomRatio(requestedRatio))
}

private fun Camera.coerceZoomRatio(requestedRatio: Float): Float {
    val zoomState = cameraInfo.zoomState.value
    val minZoom = zoomState?.minZoomRatio ?: 1f
    val maxZoom = zoomState?.maxZoomRatio ?: requestedRatio
    return requestedRatio.coerceIn(minZoom, maxZoom)
}

private fun Int.cameraLabel(): String = when (this) {
    CameraSelector.LENS_FACING_FRONT -> "front"
    CameraSelector.LENS_FACING_BACK -> "back"
    else -> "unknown"
}

@Composable
private fun CameraRoundAction(
    label: String,
    x: Int,
    y: Int,
    scaleX: Float,
    scaleY: Float,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .offset(x.dp * scaleX, y.dp * scaleY)
            .size(48.dp)
            .clip(CircleShape)
            .background(HomeButton)
            .border(1.dp, Color(0xFF333333), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun HomeActionButton(
    @RawRes icon: Int,
    description: String,
    x: Int,
    y: Int,
    scaleX: Float,
    scaleY: Float,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .offset(x.dp * scaleX, y.dp * scaleY)
            .size(42.dp)
            .clip(CircleShape)
            .background(HomeButton)
            .border(1.dp, HomeBorder, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = icon,
            contentDescription = description,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun HomeAsset(
    @RawRes resource: Int,
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    scaleX: Float,
    scaleY: Float
) {
    AsyncImage(
        model = resource,
        contentDescription = null,
        modifier = Modifier
            .offset(x.dp * scaleX, y.dp * scaleY)
            .width(width.dp * scaleX)
            .height(height.dp * scaleY)
    )
}

@Composable
private fun RoomInputDialog(
    type: RoomDialog,
    isSubmitting: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var input by remember(type) { mutableStateOf("") }
    val isCreate = type == RoomDialog.Create

    AlertDialog(
        onDismissRequest = {
            if (!isSubmitting) onDismiss()
        },
        title = { Text(if (isCreate) "새 방 만들기" else "방 참가하기") },
        text = {
            Column {
                OutlinedTextField(
                    value = input,
                    onValueChange = {
                        input = if (isCreate) it else it.uppercase().take(6)
                    },
                    label = { Text(if (isCreate) "방 이름" else "초대 코드") },
                    singleLine = true
                )
                errorMessage?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(input) },
                enabled = input.isNotBlank() && !isSubmitting
            ) {
                Text(if (isSubmitting) "처리 중" else if (isCreate) "만들기" else "참가")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSubmitting) {
                Text("취소")
            }
        }
    )
}

@Preview(
    name = "Figma Log Home",
    widthDp = 393,
    heightDp = 852,
    showBackground = true,
    backgroundColor = 0xFFF6F6F6
)
@Composable
private fun RoomScreenPreview() {
    SetLogCloningAndroidRookieT1Theme(
        darkTheme = true,
        dynamicColor = false
    ) {
        RoomScreen(
            uiState = RoomUiState(
                rooms = listOf(
                    UserRoom(
                        roomId = "room-1",
                        roomName = "웅",
                        inviteCode = "SETLOG"
                    )
                ),
                isLoading = false
            ),
            userName = "User",
            onCreateRoom = { _, _, onSuccess -> onSuccess() },
            onJoinRoom = { _, _ -> },
            onOpenRoom = {},
            onUploadRecord = { _, _, _, _, _, onSuccess -> onSuccess() },
            onUpdateProfileName = { _, onSuccess -> onSuccess() },
            onUpdateProfileColor = {},
            onUpdateProfileImage = {},
            onLogout = {}
        )
    }
}
