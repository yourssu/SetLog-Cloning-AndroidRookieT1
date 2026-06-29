package com.yourssu.setlog_cloning_androidrookiet1.ui.record

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview as CameraPreviewUseCase
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.delay
import java.io.File
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

@Composable
fun CameraScreen(roomName: String, onClose: () -> Unit, onVideoRecorded: (Uri?, Bitmap?) -> Unit) {
    val context = LocalContext.current
    val previewView = remember { PreviewView(context) }
    val calendar = remember { Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul")) }
    val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
    val timeText = String.format(Locale.US, "%02d:00", currentHour)

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
    var zoomRatio by remember { mutableFloatStateOf(1f) }
    var isFlashEnabled by remember { mutableStateOf(false) }
    var boundCamera by remember { mutableStateOf<Camera?>(null) }
    var isRecording by remember { mutableStateOf(false) }
    var activeRecording by remember { mutableStateOf<Recording?>(null) }
    var cameraMessage by remember { mutableStateOf<String?>(null) }
    var pendingThumbnail by remember { mutableStateOf<Bitmap?>(null) }
    val videoCapture = remember {
        val recorder = Recorder.Builder()
            .setQualitySelector(
                QualitySelector.from(
                    Quality.SD,
                    FallbackStrategy.lowerQualityOrHigherThan(Quality.SD)
                )
            )
            .build()
        VideoCapture.withOutput(recorder)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            delay(1500)
            pendingThumbnail = previewView.bitmap
            activeRecording?.stop()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(top = 16.dp, start = 8.dp, end = 8.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(Color(0xFF4A4A4A))
                .border(
                    width = if (isRecording) 4.dp else 0.dp,
                    color = if (isRecording) Color(0xFFFF33FF) else Color.Transparent,
                    shape = RoundedCornerShape(32.dp)
                )
        ) {
            if (hasCameraPermission) {
                CameraPreview(
                    lensFacing = lensFacing,
                    zoomRatio = zoomRatio,
                    isFlashEnabled = isFlashEnabled,
                    videoCapture = videoCapture,
                    previewView = previewView,
                    onCameraReady = { camera ->
                        boundCamera = camera
                        cameraMessage = null
                        val supportedZoomRatio = camera.coerceZoomRatio(zoomRatio)
                        if (supportedZoomRatio != zoomRatio) {
                            zoomRatio = supportedZoomRatio
                        }
                        if (!camera.cameraInfo.hasFlashUnit()) {
                            isFlashEnabled = false
                        }
                    },
                    onCameraError = { message ->
                        cameraMessage = message
                    }
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(40.dp)
                    .background(Color(0x4D000000), CircleShape)
                    .clickable { onClose() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = Color.White
                )
            }

            if (!isRecording) {
                Text(
                    text = timeText,
                    color = Color.White,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .rotate(90f)
                )

                Text(
                    text = roomName,
                    color = Color.White,
                    fontSize = 18.sp,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 24.dp)
                        .rotate(90f)
                )

                cameraMessage?.let { message ->
                    Text(
                        text = message,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 32.dp)
                            .rotate(90f)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                if (!isRecording) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Text(
                            text = ".5",
                            color = if (zoomRatio == 0.5f) Color(0xFFFFD700) else Color.White,
                            fontSize = 16.sp,
                            fontWeight = if (zoomRatio == 0.5f) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier
                                .rotate(90f)
                                .clickable { zoomRatio = boundCamera?.coerceZoomRatio(0.5f) ?: 0.5f }
                        )
                        Text(
                            text = "1",
                            color = if (zoomRatio == 1f) Color(0xFFFFD700) else Color.White,
                            fontSize = 16.sp,
                            fontWeight = if (zoomRatio == 1f) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier
                                .rotate(90f)
                                .clickable { zoomRatio = boundCamera?.coerceZoomRatio(1f) ?: 1f }
                        )
                        Text(
                            text = "2",
                            color = if (zoomRatio == 2f) Color(0xFFFFD700) else Color.White,
                            fontSize = 16.sp,
                            fontWeight = if (zoomRatio == 2f) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier
                                .rotate(90f)
                                .clickable { zoomRatio = boundCamera?.coerceZoomRatio(2f) ?: 2f }
                        )
                        Text(
                            text = "3",
                            color = if (zoomRatio == 3f) Color(0xFFFFD700) else Color.White,
                            fontSize = 16.sp,
                            fontWeight = if (zoomRatio == 3f) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier
                                .rotate(90f)
                                .clickable { zoomRatio = boundCamera?.coerceZoomRatio(3f) ?: 3f }
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(Color(0xFF5AC8FA), CircleShape)
                        .border(4.dp, Color(0xFFFFFF00), CircleShape)
                        .clickable(enabled = !isRecording) {
                            activeRecording = startVideoRecording(
                                context = context,
                                videoCapture = videoCapture,
                                onStarted = {
                                    isRecording = true
                                    cameraMessage = null
                                },
                                onFinished = { uri ->
                                    isRecording = false
                                    activeRecording = null
                                    onVideoRecorded(uri, pendingThumbnail ?: previewView.bitmap)
                                    pendingThumbnail = null
                                },
                                onError = { message ->
                                    isRecording = false
                                    activeRecording = null
                                    cameraMessage = message
                                    pendingThumbnail = null
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize().rotate(90f)) {
                        val eyeRadius = 4.dp.toPx()
                        drawCircle(
                            color = Color.Black,
                            radius = eyeRadius,
                            center = Offset(size.width * 0.35f, size.height * 0.45f)
                        )
                        drawCircle(
                            color = Color.Black,
                            radius = eyeRadius,
                            center = Offset(size.width * 0.65f, size.height * 0.45f)
                        )
                        drawArc(
                            color = Color.Black,
                            startAngle = 20f,
                            sweepAngle = 140f,
                            useCenter = false,
                            style = Stroke(
                                width = 3.dp.toPx(),
                                cap = StrokeCap.Round
                            ),
                            topLeft = Offset(size.width * 0.25f, size.height * 0.3f),
                            size = Size(size.width * 0.5f, size.height * 0.5f)
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(Color(0xFF1C1C1E), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Timer,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(Color(0xFF1C1C1E), CircleShape)
                    .clickable(
                        enabled = boundCamera?.cameraInfo?.hasFlashUnit() == true
                    ) {
                        isFlashEnabled = !isFlashEnabled
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FlashOn,
                    contentDescription = null,
                    tint = when {
                        isFlashEnabled -> Color(0xFFFFD700)
                        boundCamera?.cameraInfo?.hasFlashUnit() == true -> Color.White
                        else -> Color.White.copy(alpha = 0.35f)
                    },
                    modifier = Modifier.size(24.dp)
                )
            }
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(Color(0xFF1C1C1E), CircleShape)
                    .clickable {
                        cameraMessage = "카메라 전환 중"
                        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                            CameraSelector.LENS_FACING_FRONT
                        } else {
                            CameraSelector.LENS_FACING_BACK
                        }
                        zoomRatio = 1f
                        isFlashEnabled = false
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FlipCameraAndroid,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun CameraPreview(
    lensFacing: Int,
    zoomRatio: Float,
    isFlashEnabled: Boolean,
    videoCapture: VideoCapture<Recorder>,
    previewView: PreviewView,
    onCameraReady: (Camera) -> Unit,
    onCameraError: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var camera by remember { mutableStateOf<Camera?>(null) }

    DisposableEffect(lifecycleOwner, lensFacing, previewView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val executor = ContextCompat.getMainExecutor(context)
        val listener = Runnable {
            val cameraProvider = cameraProviderFuture.get()
            val preview = CameraPreviewUseCase.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
            runCatching {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, videoCapture).also {
                    camera = it
                    onCameraReady(it)
                    it.cameraControl.setZoomRatio(it.coerceZoomRatio(zoomRatio))
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

    LaunchedEffect(zoomRatio) {
        camera?.cameraControl?.setZoomRatio(camera?.coerceZoomRatio(zoomRatio) ?: zoomRatio)
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
        modifier = Modifier.fillMaxSize()
    )
}

private fun startVideoRecording(
    context: android.content.Context,
    videoCapture: VideoCapture<Recorder>,
    onStarted: () -> Unit,
    onFinished: (Uri?) -> Unit,
    onError: (String) -> Unit
): Recording {
    val videoFile = File.createTempFile("setlog_${System.currentTimeMillis()}_", ".mp4", context.cacheDir)
    val outputOptions = FileOutputOptions.Builder(videoFile).build()
    val recording = videoCapture.output
        .prepareRecording(context, outputOptions)
        .start(ContextCompat.getMainExecutor(context)) { event ->
            when (event) {
                is VideoRecordEvent.Start -> onStarted()
                is VideoRecordEvent.Finalize -> {
                    if (event.hasError()) {
                        onError(event.cause?.localizedMessage ?: "영상 저장에 실패했습니다.")
                    } else {
                        onFinished(event.outputResults.outputUri)
                    }
                }
            }
        }
    return recording
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

@Preview(showBackground = true)
@Composable
fun CameraScreenPreview() {
    CameraScreen(roomName = "기머쮜", onClose = {}, onVideoRecorded = { _, _ -> })
}
