package com.yourssu.setlog_cloning_androidrookiet1.ui.room

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import java.io.File
import androidx.annotation.RawRes
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import coil.compose.AsyncImage
import com.yourssu.setlog_cloning_androidrookiet1.R
import com.yourssu.setlog_cloning_androidrookiet1.data.model.UserRoom
import com.yourssu.setlog_cloning_androidrookiet1.ui.theme.SetLogCloningAndroidRookieT1Theme

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

private enum class HomeTab {
    Camera,
    Log
}

@Composable
fun RoomScreen(
    uiState: RoomUiState,
    userName: String,
    onCreateRoom: (String, () -> Unit) -> Unit,
    onJoinRoom: (String, () -> Unit) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var dialog by remember { mutableStateOf<RoomDialog?>(null) }
    var showSettings by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(HomeTab.Log) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF6F6F6))
    ) {
        val scaleX = maxWidth / 393.dp
        val scaleY = maxHeight / 852.dp

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(44.dp))
                .background(HomeBlack)
                .border(2.dp, HomePink, RoundedCornerShape(44.dp))
        ) {
            Text(
                text = if (selectedTab == HomeTab.Camera) "6:50" else "6:49",
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
            if (selectedTab == HomeTab.Camera) {
                CameraContent(
                    scaleX = scaleX,
                    scaleY = scaleY,
                    onClose = { selectedTab = HomeTab.Log },
                    onCaptureComplete = { selectedTab = HomeTab.Log }
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
                    onClick = { dialog = RoomDialog.Create }
                )
                HomeActionButton(
                    icon = R.raw.home_notifications,
                    description = "초대 코드로 참가",
                    x = 263,
                    y = 76,
                    scaleX = scaleX,
                    scaleY = scaleY,
                    onClick = { dialog = RoomDialog.Join }
                )
                HomeActionButton(
                    icon = R.raw.home_more,
                    description = "설정",
                    x = 323,
                    y = 76,
                    scaleX = scaleX,
                    scaleY = scaleY,
                    onClick = { showSettings = !showSettings }
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
                                GroupCard(room, scaleX, scaleY)
                                Spacer(modifier = Modifier.height(10.dp * scaleY))
                            }
                        }
                    }
                }
            }

            CameraLogSwitch(
                selectedTab = selectedTab,
                scaleX = scaleX,
                scaleY = scaleY,
                onSelectTab = { tab ->
                    selectedTab = tab
                    if (tab == HomeTab.Camera) showSettings = false
                }
            )

            if (showSettings) {
                SettingsPopover(
                    userName = userName,
                    scaleX = scaleX,
                    scaleY = scaleY,
                    onLogout = {
                        showSettings = false
                        onLogout()
                    }
                )
            }
        }
    }

    dialog?.let { type ->
        RoomInputDialog(
            type = type,
            isSubmitting = uiState.isSubmitting,
            errorMessage = uiState.errorMessage,
            onDismiss = { dialog = null },
            onConfirm = { input ->
                val closeDialog = { dialog = null }
                when (type) {
                    RoomDialog.Create -> onCreateRoom(input, closeDialog)
                    RoomDialog.Join -> onJoinRoom(input, closeDialog)
                }
            }
        )
    }
}

@Composable
private fun SettingsPopover(
    userName: String,
    scaleX: Float,
    scaleY: Float,
    onLogout: () -> Unit
) {
    Box(
        modifier = Modifier
            .offset(91.dp * scaleX, 78.dp * scaleY)
            .width(272.dp * scaleX)
            .height(220.dp * scaleY)
            .clip(RoundedCornerShape(34.dp))
            .background(Color(0xF5291226))
            .border(1.dp, Color(0xFF8C2E82), RoundedCornerShape(34.dp))
    ) {
        Box(
            modifier = Modifier
                .offset(19.dp * scaleX, 16.dp * scaleY)
                .size(28.dp)
                .clip(CircleShape)
                .background(HomePink),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "♫",
                color = Color(0xFF291226),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = userName.ifBlank { "User" },
            modifier = Modifier
                .offset(57.dp * scaleX, 17.dp * scaleY)
                .width(180.dp * scaleX),
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )

        SettingsMenuItem("♙", "프로필 편집", true, 54, scaleX, scaleY)
        SettingsMenuItem("♧", "로그 알림", true, 85, scaleX, scaleY)
        SettingsMenuItem("◉", "계정 관리", true, 116, scaleX, scaleY)
        SettingsMenuItem("⊕", "피드백 보내기", false, 147, scaleX, scaleY)
        SettingsMenuItem("↪", "로그아웃", false, 178, scaleX, scaleY, onClick = onLogout)
    }
}

@Composable
private fun SettingsMenuItem(
    icon: String,
    label: String,
    showChevron: Boolean,
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
                text = "›",
                modifier = Modifier.offset(218.dp * scaleX, (-1).dp * scaleY),
                color = Color.White,
                fontSize = 24.sp
            )
        }
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
private fun GroupCard(room: UserRoom, scaleX: Float, scaleY: Float) {
    Box(
        modifier = Modifier
            .width(369.dp * scaleX)
            .height(80.dp * scaleY)
            .clip(RoundedCornerShape(26.dp))
            .background(HomeCard)
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
    onClose: () -> Unit,
    onCaptureComplete: () -> Unit
) {
    val context = LocalContext.current
    val isInPreview = LocalInspectionMode.current
    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
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
                imageCapture = imageCapture,
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
            text = "18:00",
            modifier = Modifier
                .offset(151.dp * scaleX, 303.dp * scaleY)
                .rotate(90f),
            color = Color.White,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold
        )

        CameraZoomLabel(".5", 116, 553, Color.White.copy(alpha = 0.75f), false, scaleX, scaleY)
        CameraZoomLabel("1", 164, 556, Color(0xFFFFC700), true, scaleX, scaleY)
        CameraZoomLabel("2", 212, 556, Color.White.copy(alpha = 0.8f), false, scaleX, scaleY)
        CameraZoomLabel("3", 260, 556, Color.White.copy(alpha = 0.8f), false, scaleX, scaleY)

        Text(
            text = "⚡",
            modifier = Modifier.offset(97.dp * scaleX, 610.dp * scaleY),
            color = Color.White,
            fontSize = 26.sp
        )

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
                        onCaptureComplete = onCaptureComplete
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

    CameraRoundAction("↻", 37, 772, scaleX, scaleY)
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
        }
    )
}

private fun takeSetLogPhoto(
    context: Context,
    imageCapture: ImageCapture,
    onCaptureComplete: () -> Unit
) {
    val photoFile = File.createTempFile(
        "setlog_${System.currentTimeMillis()}_",
        ".jpg",
        context.cacheDir
    )
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                onCaptureComplete()
            }

            override fun onError(exception: ImageCaptureException) = Unit
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
    imageCapture: ImageCapture,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
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
                )
            }.recoverCatching {
                val fallbackSelector = CameraSelector.DEFAULT_BACK_CAMERA
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    fallbackSelector,
                    preview,
                    imageCapture
                )
            }
        }

        cameraProviderFuture.addListener(listener, executor)

        onDispose {
            runCatching {
                cameraProviderFuture.get().unbindAll()
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
    color: Color,
    selected: Boolean,
    scaleX: Float,
    scaleY: Float
) {
    Text(
        text = text,
        modifier = Modifier
            .offset(x.dp * scaleX, y.dp * scaleY)
            .rotate(-90f),
        color = color,
        fontSize = 13.sp,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
    )
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
            onCreateRoom = { _, _ -> },
            onJoinRoom = { _, _ -> },
            onLogout = {}
        )
    }
}
