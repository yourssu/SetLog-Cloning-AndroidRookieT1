package com.yourssu.setlog_cloning_androidrookiet1.ui.record

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

@Composable
fun CameraScreen(roomName: String, onClose: () -> Unit, onVideoRecorded: (Bitmap?) -> Unit) {
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
    var isRecording by remember { mutableStateOf(false) }

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
            isRecording = false
            val thumbnail = previewView.bitmap
            onVideoRecorded(thumbnail)
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
                    previewView = previewView
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
                                .clickable { zoomRatio = 0.5f }
                        )
                        Text(
                            text = "1",
                            color = if (zoomRatio == 1f) Color(0xFFFFD700) else Color.White,
                            fontSize = 16.sp,
                            fontWeight = if (zoomRatio == 1f) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier
                                .rotate(90f)
                                .clickable { zoomRatio = 1f }
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(Color(0xFF5AC8FA), CircleShape)
                        .border(4.dp, Color(0xFFFFFF00), CircleShape)
                        .clickable(enabled = !isRecording) { isRecording = true },
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
                    .background(Color(0xFF1C1C1E), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FlashOn,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(Color(0xFF1C1C1E), CircleShape)
                    .clickable {
                        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                            CameraSelector.LENS_FACING_FRONT
                        } else {
                            CameraSelector.LENS_FACING_BACK
                        }
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
fun CameraPreview(lensFacing: Int, zoomRatio: Float, previewView: PreviewView) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var camera by remember { mutableStateOf<Camera?>(null) }

    LaunchedEffect(lensFacing) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = androidx.camera.core.Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)

                val minZoom = camera?.cameraInfo?.zoomState?.value?.minZoomRatio ?: 1f
                val safeZoom = if (zoomRatio < minZoom) minZoom else zoomRatio
                camera?.cameraControl?.setZoomRatio(safeZoom)
            } catch (e: Exception) {
            }
        }, ContextCompat.getMainExecutor(context))
    }

    LaunchedEffect(zoomRatio) {
        val minZoom = camera?.cameraInfo?.zoomState?.value?.minZoomRatio ?: 1f
        val safeZoom = if (zoomRatio < minZoom) minZoom else zoomRatio
        camera?.cameraControl?.setZoomRatio(safeZoom)
    }

    AndroidView(
        factory = { previewView },
        modifier = Modifier.fillMaxSize()
    )
}

@Preview(showBackground = true)
@Composable
fun CameraScreenPreview() {
    CameraScreen(roomName = "기머쮜", onClose = {}, onVideoRecorded = {})
}