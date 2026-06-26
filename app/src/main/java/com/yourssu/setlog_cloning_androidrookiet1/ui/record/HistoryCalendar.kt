package com.yourssu.setlog_cloning_androidrookiet1.ui.record

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryCalendarSheet(
    recordedDates: List<String>,
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White
    ) {
        HistoryCalendar(
            recordedDates = recordedDates,
            onDateSelected = onDateSelected
        )
    }
}

@Composable
fun HistoryCalendar(
    recordedDates: List<String>,
    onDateSelected: (LocalDate) -> Unit
) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val today = LocalDate.now()
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${currentMonth.year}년 ${currentMonth.monthValue}월",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Row {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowLeft,
                    contentDescription = null,
                    tint = Color(0xFF5AC8FA),
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .clickable { currentMonth = currentMonth.minusMonths(1) }
                )
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color.LightGray,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .clickable { currentMonth = currentMonth.plusMonths(1) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        val daysOfWeek = listOf("일", "월", "화", "수", "목", "금", "토")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            daysOfWeek.forEach { day ->
                Text(text = day, color = Color.Gray, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val firstDayOfWeek = currentMonth.atDay(1).dayOfWeek.value % 7
        val daysInMonth = currentMonth.lengthOfMonth()
        val totalCells = ((firstDayOfWeek + daysInMonth + 6) / 7) * 7

        Column(modifier = Modifier.fillMaxWidth()) {
            for (i in 0 until totalCells step 7) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    for (j in 0..6) {
                        val dayIndex = i + j
                        val dayNumber = dayIndex - firstDayOfWeek + 1
                        val isCurrentMonth = dayNumber in 1..daysInMonth

                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCurrentMonth) {
                                val currentDate = currentMonth.atDay(dayNumber)
                                val isToday = currentDate == today
                                val isFuture = currentDate.isAfter(today)
                                val dateString = currentDate.format(dateFormatter)
                                val hasRecord = recordedDates.contains(dateString)
                                val isClickable = !isFuture && hasRecord

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .clip(CircleShape)
                                        .clickable(enabled = isClickable) {
                                            if (isClickable) {
                                                onDateSelected(currentDate)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (hasRecord) {
                                        // 기록이 있는 날은 선명한 스마일 아이콘을 보여줍니다.
                                        SmallSmileIcon(alpha = 1.0f)
                                    } else {
                                        Text(
                                            text = dayNumber.toString(),
                                            // 미래의 날짜이거나 오늘/과거 중 기록이 없는 날짜는 색상을 선명한 블랙보다 연하게(Color(0xFFD1D1D6)) 처리합니다.
                                            color = if (isFuture) {
                                                Color(0xFFE5E5EA)
                                            } else if (isToday) {
                                                Color(0xFF5AC8FA)
                                            } else {
                                                Color(0xFFD1D1D6)
                                            },
                                            fontSize = 16.sp,
                                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun SmallSmileIcon(alpha: Float = 1.0f) {
    Canvas(modifier = Modifier.size(24.dp)) {
        drawCircle(color = Color(0xFFFF33FF).copy(alpha = alpha))

        val eyeRadius = size.width * 0.08f

        drawCircle(
            color = Color.Black.copy(alpha = alpha),
            radius = eyeRadius,
            center = Offset(size.width * 0.35f, size.height * 0.4f)
        )
        drawCircle(
            color = Color.Black.copy(alpha = alpha),
            radius = eyeRadius,
            center = Offset(size.width * 0.65f, size.height * 0.4f)
        )

        drawArc(
            color = Color.Black.copy(alpha = alpha),
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = false,
            style = Stroke(width = size.width * 0.08f, cap = StrokeCap.Round),
            topLeft = Offset(size.width * 0.25f, size.height * 0.45f),
            size = Size(size.width * 0.5f, size.height * 0.35f)
        )
    }
}