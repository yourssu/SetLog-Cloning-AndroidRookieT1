package com.yourssu.setlog_cloning_androidrookiet1.ui.auth

import androidx.annotation.RawRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.yourssu.setlog_cloning_androidrookiet1.R
import com.yourssu.setlog_cloning_androidrookiet1.ui.theme.SetLogCloningAndroidRookieT1Theme

private val LoginBackground = Color(0xFFF6F6F6)

@Composable
fun AuthScreen(
    uiState: AuthUiState,
    onGoogleSignIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(LoginBackground)
    ) {
        val scaleX = maxWidth / 393.dp
        val scaleY = maxHeight / 852.dp

        FigmaAsset(R.raw.doodle_star, 74, 47, 57, 56, scaleX, scaleY)
        FigmaAsset(R.raw.doodle_star_two, 275, 60, 53, 52, scaleX, scaleY)
        FigmaAsset(R.raw.doodle_envelope, 2, 215, 73, 54, scaleX, scaleY)
        FigmaAsset(R.raw.doodle_moon, 316, 207, 66, 75, scaleX, scaleY)
        FigmaAsset(R.raw.hero_friendly_cloud, 115, 120, 163, 153, scaleX, scaleY)
        FigmaAsset(R.raw.doodle_pizza, 0, 680, 104, 97, scaleX, scaleY)
        FigmaAsset(R.raw.doodle_plant, 264, 661, 121, 134, scaleX, scaleY)

        Text(
            text = "SETLOG",
            modifier = Modifier
                .offset(154.dp * scaleX, 307.dp * scaleY)
                .width(99.dp * scaleX),
            color = Color.Black,
            fontSize = 25.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )
        Text(
            text = "매 시간 새로운 순간을\n친구들과 함께 브이로그로 남겨요.",
            modifier = Modifier
                .offset(54.dp * scaleX, 349.dp * scaleY)
                .width(286.dp * scaleX),
            color = Color(0xFF0D0D0D),
            fontSize = 16.sp,
            lineHeight = 23.sp,
            textAlign = TextAlign.Center
        )

        Box(
            modifier = Modifier
                .offset(42.dp * scaleX, 534.dp * scaleY)
                .width(310.dp * scaleX)
                .height(60.dp * scaleY)
                .clip(RoundedCornerShape(28.dp))
                .background(Color.Black)
                .clickable(enabled = !uiState.isLoading, onClick = onGoogleSignIn),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "G",
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .offset(x = 28.dp * scaleX),
                    color = Color(0xFF4285F5),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Google로 계속하기",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Text(
            text = uiState.errorMessage ?: "로그인에 문제가 있나요?",
            modifier = Modifier
                .offset(42.dp * scaleX, 626.dp * scaleY)
                .width(310.dp * scaleX),
            color = if (uiState.errorMessage == null) {
                Color(0xFF0D0D0D)
            } else {
                MaterialTheme.colorScheme.error
            },
            fontSize = if (uiState.errorMessage == null) 15.sp else 13.sp,
            textAlign = TextAlign.Center,
            textDecoration = if (uiState.errorMessage == null) {
                TextDecoration.Underline
            } else {
                TextDecoration.None
            }
        )
    }
}

@Composable
private fun FigmaAsset(
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

@Preview(
    name = "Figma Login",
    widthDp = 393,
    heightDp = 852,
    showBackground = true,
    backgroundColor = 0xFFF6F6F6
)
@Composable
private fun AuthScreenPreview() {
    SetLogCloningAndroidRookieT1Theme(
        darkTheme = false,
        dynamicColor = false
    ) {
        AuthScreen(
            uiState = AuthUiState(),
            onGoogleSignIn = {}
        )
    }
}

@Preview(
    name = "Figma Login Loading",
    widthDp = 393,
    heightDp = 852,
    showBackground = true,
    backgroundColor = 0xFFF6F6F6
)
@Composable
private fun AuthScreenLoadingPreview() {
    SetLogCloningAndroidRookieT1Theme(
        darkTheme = false,
        dynamicColor = false
    ) {
        AuthScreen(
            uiState = AuthUiState(isLoading = true),
            onGoogleSignIn = {}
        )
    }
}
