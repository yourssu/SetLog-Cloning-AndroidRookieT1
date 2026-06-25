package com.yourssu.setlog_cloning_androidrookiet1.ui.theme

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.North
import androidx.compose.material.icons.outlined.ModeComment
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs
import kotlin.random.Random

data class SetlogMember(
    val name: String,
    val color: Color,
    val isCurrentUser: Boolean
)

@Composable
fun RecordScreen() {
    var showCameraScreen by remember { mutableStateOf(false) }
    val calendar = remember { Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul")) }
    val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
    val timeText = String.format(Locale.US, "%02d:00", currentHour)

    if (showCameraScreen) {
        CameraScreen(onClose = { showCameraScreen = false })
    } else {
        val memberList = listOf(
            SetlogMember("사용자1", Color(0xFF8B5CF6), false),
            SetlogMember("사용자2", Color(0xFFD9F99D), false),
            SetlogMember("사용자3", Color(0xFFFBBF24), false),
            SetlogMember("사용자4", Color(0xFFF472B6), false),
            SetlogMember("사용자5", Color(0xFF3B82F6), true)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F0F10))
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFF141414), CircleShape)
                            .border(1.dp, Color(0x22FFFFFF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowLeft,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color.Transparent, RoundedCornerShape(20.dp))
                            .border(BorderStroke(1.dp, Color(0x22FFFFFF)), RoundedCornerShape(20.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "채팅방1",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color(0xFF141414), CircleShape)
                                .border(1.dp, Color(0x22FFFFFF), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .border(2.dp, Color.White, RoundedCornerShape(5.dp))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.North,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(14.dp)
                                        .align(Alignment.Center)
                                        .offset(y = (-3).dp)
                                        .background(Color(0xFF141414))
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color(0xFF141414), CircleShape)
                                .border(1.dp, Color(0x22FFFFFF), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ModeComment,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    memberList.forEachIndexed { index, member ->
                        var cardWidthDp by remember { mutableStateOf(0.dp) }
                        var cardHeightDp by remember { mutableStateOf(0.dp) }
                        val density = LocalDensity.current

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .background(Color(0xFF1C1C1E), RoundedCornerShape(24.dp))
                                .clip(RoundedCornerShape(24.dp))
                                .onGloballyPositioned { coordinates ->
                                    with(density) {
                                        cardWidthDp = coordinates.size.width.toDp()
                                        cardHeightDp = coordinates.size.height.toDp()
                                    }
                                }
                        ) {
                            if (cardWidthDp > 0.dp && cardHeightDp > 0.dp) {
                                FloatingBall(
                                    memberIndex = index,
                                    totalMembers = memberList.size,
                                    containerWidth = cardWidthDp,
                                    containerHeight = cardHeightDp
                                )
                            }

                            Text(
                                text = timeText,
                                color = Color(0xFF2C2C2E),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .offset(y = if (member.isCurrentUser) (-16).dp else 0.dp)
                            )

                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .align(Alignment.TopStart),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(Color.LightGray)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = member.name,
                                    color = Color.Gray,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            if (member.isCurrentUser) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 22.dp)
                                        .background(Color.Transparent, RoundedCornerShape(20.dp))
                                        .border(BorderStroke(1.dp, Color(0x22FFFFFF)), RoundedCornerShape(20.dp))
                                        .clickable { showCameraScreen = true }
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "눌러서 촬영",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(14.dp)
                                        .size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FloatingBall(
    memberIndex: Int,
    totalMembers: Int,
    containerWidth: androidx.compose.ui.unit.Dp,
    containerHeight: androidx.compose.ui.unit.Dp
) {
    val ballSize = 40.dp
    val maxOffsetX = ((containerWidth - ballSize) / 2).value
    val maxOffsetY = ((containerHeight - ballSize) / 2).value

    var offsetX by remember { mutableFloatStateOf(Random.nextDouble(-maxOffsetX.toDouble(), maxOffsetX.toDouble()).toFloat()) }
    var offsetY by remember { mutableFloatStateOf(Random.nextDouble(-maxOffsetY.toDouble(), maxOffsetY.toDouble()).toFloat()) }

    var speedX by remember { mutableFloatStateOf((Random.nextFloat() * 45f + 30f) * if (Random.nextBoolean()) 1f else -1f) }
    var speedY by remember { mutableFloatStateOf((Random.nextFloat() * 45f + 30f) * if (Random.nextBoolean()) 1f else -1f) }

    var colorStep by remember { mutableStateOf(0) }

    LaunchedEffect(containerWidth, containerHeight) {
        var lastTime = withFrameNanos { it }
        var colorTimer = 0f

        while (true) {
            withFrameNanos { time ->
                val deltaSec = (time - lastTime) / 1_000_000_000f
                lastTime = time

                colorTimer += deltaSec
                if (colorTimer >= 0.8f) {
                    colorTimer = 0f
                    colorStep = (colorStep + 1) % totalMembers
                }

                var nextX = offsetX + (speedX * deltaSec)
                var nextY = offsetY + (speedY * deltaSec)

                if (nextX >= maxOffsetX) {
                    nextX = maxOffsetX
                    speedX = -abs(speedX)
                } else if (nextX <= -maxOffsetX) {
                    nextX = -maxOffsetX
                    speedX = abs(speedX)
                }

                if (nextY >= maxOffsetY) {
                    nextY = maxOffsetY
                    speedY = -abs(speedY)
                } else if (nextY <= -maxOffsetY) {
                    nextY = -maxOffsetY
                    speedY = abs(speedY)
                }

                offsetX = nextX
                offsetY = nextY
            }
        }
    }

    val baseHue = (360f / totalMembers) * memberIndex
    val stepHue = (360f / totalMembers) * colorStep
    val currentHue = (baseHue + stepHue) % 360f
    val animatedColor = Color.hsv(currentHue, 0.65f, 0.9f)

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = offsetX.dp, y = offsetY.dp)
                .size(ballSize)
                .clip(CircleShape)
                .background(animatedColor)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RecordScreenPreview() {
    RecordScreen()
}