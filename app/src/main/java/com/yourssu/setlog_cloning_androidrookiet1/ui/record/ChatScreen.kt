package com.yourssu.setlog_cloning_androidrookiet1.ui.record

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ChatScreen(roomName: String, onClose: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F7))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color.White, CircleShape)
                    .border(1.dp, Color(0xFFE5E5EA), CircleShape)
                    .clickable { onClose() }
                    .align(Alignment.CenterStart),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(28.dp)
                )
            }

            Box(
                modifier = Modifier
                    .background(Color.White, RoundedCornerShape(20.dp))
                    .border(1.dp, Color(0xFFE5E5EA), RoundedCornerShape(20.dp))
                    .padding(horizontal = 24.dp, vertical = 10.dp)
                    .align(Alignment.Center),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = roomName,
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.White, CircleShape)
                    .border(1.dp, Color(0xFFE5E5EA), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(28.dp)) {
                    drawCircle(color = Color(0xFFFF66FF))

                    val eyeRadius = 1.5.dp.toPx()
                    drawCircle(
                        color = Color.Black,
                        radius = eyeRadius,
                        center = Offset(size.width * 0.35f, size.height * 0.65f)
                    )
                    drawCircle(
                        color = Color.Black,
                        radius = eyeRadius,
                        center = Offset(size.width * 0.65f, size.height * 0.65f)
                    )

                    drawArc(
                        color = Color.Black,
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        style = Stroke(
                            width = 2.dp.toPx(),
                            cap = StrokeCap.Round
                        ),
                        topLeft = Offset(size.width * 0.25f, size.height * 0.25f),
                        size = Size(size.width * 0.5f, size.height * 0.4f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Row(
                modifier = Modifier
                    .weight(1f)
                    .background(Color.White, RoundedCornerShape(24.dp))
                    .border(1.dp, Color(0xFFE5E5EA), RoundedCornerShape(24.dp))
                    .padding(start = 20.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "메시지",
                    color = Color.Gray,
                    fontSize = 16.sp
                )

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFFF2F2F7), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
    ChatScreen(roomName = "방", onClose = {})
}
