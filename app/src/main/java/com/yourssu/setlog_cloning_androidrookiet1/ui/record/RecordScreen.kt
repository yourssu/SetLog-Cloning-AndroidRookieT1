package com.yourssu.setlog_cloning_androidrookiet1.ui.record

import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.North
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ModeComment
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.yourssu.setlog_cloning_androidrookiet1.ui.room.RoomRecordUi
import com.yourssu.setlog_cloning_androidrookiet1.ui.room.RoomViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs
import kotlin.random.Random

enum class AppScreen {
    MAIN, CAMERA, CHAT
}

data class SetlogMember(
    val name: String,
    val color: Color,
    val isCurrentUser: Boolean,
    val recordedHours: List<Int> = emptyList(),
    val isPlaceholder: Boolean = false
)

@Composable
fun RecordScreen(
    roomId: String = "방1",
    roomName: String = "방1",
    memberCount: Int = 4,
    currentUserName: String = "나",
    roomViewModel: RoomViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf(AppScreen.MAIN) }

    val savedRecords by roomViewModel.myRecords.collectAsStateWithLifecycle()
    var localRecords by remember(roomId) { mutableStateOf(mapOf<Int, RoomRecordUi>()) }
    val myRecords = remember(savedRecords, localRecords) {
        savedRecords + localRecords
    }

    var showMenu by remember { mutableStateOf(false) }
    var showRoomMenu by remember { mutableStateOf(false) }
    var showHistoryBottomSheet by remember { mutableStateOf(false) }
    var selectedHistoryDate by remember { mutableStateOf<LocalDate?>(null) }

    var editCaptionText by remember { mutableStateOf("") }
    var isEditingCaption by remember { mutableStateOf(false) }
    var expandedVideo by remember { mutableStateOf(false) }

    var currentHour by remember { mutableIntStateOf(Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul")).get(Calendar.HOUR_OF_DAY)) }
    var viewedHour by remember { mutableIntStateOf(currentHour) }
    val timeText = String.format(Locale.US, "%02d:00", viewedHour)

    val recordedDates by roomViewModel.recordedDates.collectAsStateWithLifecycle()
    val roomUiState by roomViewModel.uiState.collectAsStateWithLifecycle()
    val roomMembers by roomViewModel.roomMembers.collectAsStateWithLifecycle()

    BackHandler {
        if (currentScreen == AppScreen.MAIN && selectedHistoryDate == null) {
            onBack()
        } else {
            currentScreen = AppScreen.MAIN
            selectedHistoryDate = null
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            val newHour = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul")).get(Calendar.HOUR_OF_DAY)
            if (newHour != currentHour) {
                currentHour = newHour
                viewedHour = newHour
            }
        }
    }

    LaunchedEffect(roomId) {
        roomViewModel.observeRecords(roomId)
    }

    LaunchedEffect(roomUiState.errorMessage) {
        roomUiState.errorMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            roomViewModel.clearError()
        }
    }

    when (currentScreen) {
        AppScreen.CAMERA -> {
            CameraScreen(
                roomName = roomName,
                onClose = { currentScreen = AppScreen.MAIN },
                onVideoRecorded = { videoUri, thumbnail ->
                    localRecords = localRecords.toMutableMap().apply {
                        put(currentHour, RoomRecordUi(thumbnail = thumbnail, caption = "", videoUri = videoUri))
                    }
                    val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"))
                    cal.set(Calendar.HOUR_OF_DAY, currentHour)
                    val sdf = SimpleDateFormat("yyyy-MM-dd-HH", Locale.KOREA).apply {
                        timeZone = TimeZone.getTimeZone("Asia/Seoul")
                    }
                    val dateHourStr = sdf.format(cal.time)
                    roomViewModel.uploadRecord(
                        roomId = roomId,
                        caption = "",
                        dateHour = dateHourStr,
                        thumbnail = thumbnail,
                        videoUri = videoUri,
                        onSuccess = {
                            Toast.makeText(context, "기록이 저장되었습니다.", Toast.LENGTH_SHORT).show()
                        }
                    )
                    viewedHour = currentHour
                    currentScreen = AppScreen.MAIN
                }
            )
        }
        AppScreen.CHAT -> {
            ChatScreen(
                roomName = roomName,
                onClose = { currentScreen = AppScreen.MAIN }
            )
        }
        AppScreen.MAIN -> {
            if (selectedHistoryDate != null) {
                HistoryPagerView(
                    date = selectedHistoryDate!!,
                    onClose = { selectedHistoryDate = null },
                    currentUserName = currentUserName
                )
            } else {
                val actualMembers = roomMembers
                    .take(memberCount)
                    .mapIndexed { index, member ->
                        SetlogMember(
                            name = if (member.isCurrentUser) currentUserName else member.name,
                            color = if (index == 0) Color(0xFFFF33FF) else Color(0xFF5AC8FA),
                            isCurrentUser = member.isCurrentUser,
                            recordedHours = if (member.isCurrentUser) myRecords.keys.toList() else emptyList()
                        )
                    }
                val emptySlots = (memberCount - actualMembers.size).coerceAtLeast(0)
                val memberList = actualMembers + List(emptySlots) { index ->
                    SetlogMember(
                        name = "빈 자리 ${actualMembers.size + index + 1}",
                        color = Color(0xFFD1D1D6),
                        isCurrentUser = false,
                        isPlaceholder = true
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF2F2F7))
                        .pointerInput(currentHour) {
                            detectTapGestures { offset ->
                                val isLeft = offset.x < size.width / 2
                                if (isLeft) {
                                    if (viewedHour > 4) {
                                        viewedHour -= 1
                                    }
                                } else {
                                    if (viewedHour < currentHour) {
                                        viewedHour += 1
                                    }
                                }
                            }
                        }
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize()
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
                                    .align(Alignment.CenterStart)
                                    .clickable { onBack() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Box(modifier = Modifier.align(Alignment.Center)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .background(Color.White, RoundedCornerShape(20.dp))
                                        .border(BorderStroke(1.dp, Color(0xFFE5E5EA)), RoundedCornerShape(20.dp))
                                        .clickable { showRoomMenu = true }
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = roomName,
                                        color = Color.Black,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = Color.DarkGray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                DropdownMenu(
                                    expanded = showRoomMenu,
                                    onDismissRequest = { showRoomMenu = false },
                                    modifier = Modifier.background(Color.White)
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("기록", color = Color.Black) },
                                        onClick = {
                                            showRoomMenu = false
                                            showHistoryBottomSheet = true
                                        }
                                    )
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.align(Alignment.CenterEnd)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(Color.White, CircleShape)
                                        .border(1.dp, Color(0xFFE5E5EA), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .border(2.dp, Color.Black, RoundedCornerShape(5.dp))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.North,
                                            contentDescription = null,
                                            tint = Color.Black,
                                            modifier = Modifier
                                                .size(14.dp)
                                                .align(Alignment.Center)
                                                .offset(y = (-3).dp)
                                                .background(Color.White)
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(Color.White, CircleShape)
                                        .border(1.dp, Color(0xFFE5E5EA), CircleShape)
                                        .clickable { currentScreen = AppScreen.CHAT },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ModeComment,
                                        contentDescription = null,
                                        tint = Color.Black,
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
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            MemberCardGrid(
                                members = memberList,
                                viewedHour = viewedHour,
                                currentHour = currentHour,
                                timeText = timeText,
                                myRecords = myRecords,
                                isEditingCaption = isEditingCaption,
                                editCaptionText = editCaptionText,
                                showMenu = showMenu,
                                onShowMenuChange = { showMenu = it },
                                onCaptionChange = { editCaptionText = it },
                                onCommitCaption = {
                                    val record = myRecords[viewedHour]
                                    if (record != null) {
                                        localRecords = localRecords.toMutableMap().apply {
                                            put(viewedHour, record.copy(caption = editCaptionText))
                                        }
                                    }
                                    isEditingCaption = false
                                },
                                onStartEditing = {
                                    editCaptionText = myRecords[viewedHour]?.caption ?: ""
                                    isEditingCaption = true
                                    showMenu = false
                                },
                                onDelete = {
                                    val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"))
                                    cal.set(Calendar.HOUR_OF_DAY, viewedHour)
                                    val sdf = SimpleDateFormat("yyyy-MM-dd-HH", Locale.KOREA).apply {
                                        timeZone = TimeZone.getTimeZone("Asia/Seoul")
                                    }
                                    val dateHourStr = sdf.format(cal.time)

                                    roomViewModel.deleteRecord(roomId, dateHourStr)
                                    localRecords = localRecords.toMutableMap().apply { remove(viewedHour) }
                                    showMenu = false
                                },
                                onSave = {
                                    val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"))
                                    cal.set(Calendar.HOUR_OF_DAY, viewedHour)
                                    val sdf = SimpleDateFormat("yyyy-MM-dd-HH", Locale.KOREA).apply {
                                        timeZone = TimeZone.getTimeZone("Asia/Seoul")
                                    }
                                    val dateHourStr = sdf.format(cal.time)

                                    roomViewModel.uploadRecord(
                                        roomId = roomId,
                                        caption = myRecords[viewedHour]?.caption ?: "",
                                        dateHour = dateHourStr,
                                        thumbnail = myRecords[viewedHour]?.thumbnail,
                                        videoUri = myRecords[viewedHour]?.videoUri,
                                        onSuccess = {
                                            Toast.makeText(context, "기록이 서버에 저장되었습니다!", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                    showMenu = false
                                },
                                onStartCamera = { currentScreen = AppScreen.CAMERA },
                                onExpand = { expandedVideo = true },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                if (showHistoryBottomSheet) {
                    HistoryCalendarSheet(
                        recordedDates = recordedDates,
                        onDismiss = { showHistoryBottomSheet = false },
                        onDateSelected = { selectedDate ->
                            showHistoryBottomSheet = false
                            selectedHistoryDate = selectedDate
                        }
                    )
                }

                if (expandedVideo) {
                    val record = myRecords[viewedHour]
                    val videoSource = record?.videoUri ?: record?.videoUrl
                        ?.takeIf { it.isNotBlank() }
                        ?.let(Uri::parse)

                    if (videoSource != null) {
                        VideoPlayerOverlay(
                            videoUri = videoSource,
                            caption = record?.caption.orEmpty(),
                            onDismiss = { expandedVideo = false }
                        )
                    } else {
                        LaunchedEffect(record) {
                            Toast.makeText(context, "재생할 영상이 없습니다.", Toast.LENGTH_SHORT).show()
                            expandedVideo = false
                        }
                    }
                }
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun VideoPlayerOverlay(
    videoUri: Uri,
    caption: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val player = remember(videoUri) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUri))
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(player) {
        onDispose {
            player.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC000000))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .aspectRatio(0.6f)
                .clip(RoundedCornerShape(32.dp))
                .background(Color.Black)
                .clickable(enabled = false) {},
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = { viewContext ->
                    PlayerView(viewContext).apply {
                        this.player = player
                        useController = true
                        setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM)
                    }
                },
                update = { playerView ->
                    playerView.player = player
                },
                modifier = Modifier.fillMaxSize()
            )

            if (caption.isNotEmpty()) {
                Text(
                    text = caption,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 24.dp)
                )
            }
        }
    }
}

@Composable
private fun MemberCardGrid(
    members: List<SetlogMember>,
    viewedHour: Int,
    currentHour: Int,
    timeText: String,
    myRecords: Map<Int, RoomRecordUi>,
    isEditingCaption: Boolean,
    editCaptionText: String,
    showMenu: Boolean,
    onShowMenuChange: (Boolean) -> Unit,
    onCaptionChange: (String) -> Unit,
    onCommitCaption: () -> Unit,
    onStartEditing: () -> Unit,
    onDelete: () -> Unit,
    onSave: () -> Unit,
    onStartCamera: () -> Unit,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    val useTwoRows = members.size >= 5

    if (useTwoRows) {
        val rows = members.chunked(2)

        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            rows.forEachIndexed { rowIndex, rowMembers ->
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowMembers.forEachIndexed { columnIndex, member ->
                        val index = rowIndex * 2 + columnIndex
                        MemberRecordCard(
                            member = member,
                            index = index,
                            totalMembers = members.size,
                            viewedHour = viewedHour,
                            currentHour = currentHour,
                            timeText = timeText,
                            myRecords = myRecords,
                            isEditingCaption = isEditingCaption,
                            editCaptionText = editCaptionText,
                            showMenu = showMenu,
                            compact = true,
                            onShowMenuChange = onShowMenuChange,
                            onCaptionChange = onCaptionChange,
                            onCommitCaption = onCommitCaption,
                            onStartEditing = onStartEditing,
                            onDelete = onDelete,
                            onSave = onSave,
                            onStartCamera = onStartCamera,
                            onExpand = onExpand,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }
                    if (rowMembers.size == 1) {
                        Spacer(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }
                }
            }
        }
    } else {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            members.forEachIndexed { index, member ->
                MemberRecordCard(
                    member = member,
                    index = index,
                    totalMembers = members.size,
                    viewedHour = viewedHour,
                    currentHour = currentHour,
                    timeText = timeText,
                    myRecords = myRecords,
                    isEditingCaption = isEditingCaption,
                    editCaptionText = editCaptionText,
                    showMenu = showMenu,
                    compact = false,
                    onShowMenuChange = onShowMenuChange,
                    onCaptionChange = onCaptionChange,
                    onCommitCaption = onCommitCaption,
                    onStartEditing = onStartEditing,
                    onDelete = onDelete,
                    onSave = onSave,
                    onStartCamera = onStartCamera,
                    onExpand = onExpand,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun MemberRecordCard(
    member: SetlogMember,
    index: Int,
    totalMembers: Int,
    viewedHour: Int,
    currentHour: Int,
    timeText: String,
    myRecords: Map<Int, RoomRecordUi>,
    isEditingCaption: Boolean,
    editCaptionText: String,
    showMenu: Boolean,
    compact: Boolean,
    onShowMenuChange: (Boolean) -> Unit,
    onCaptionChange: (String) -> Unit,
    onCommitCaption: () -> Unit,
    onStartEditing: () -> Unit,
    onDelete: () -> Unit,
    onSave: () -> Unit,
    onStartCamera: () -> Unit,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    var cardWidthDp by remember { mutableStateOf(0.dp) }
    var cardHeightDp by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current
    val hasRecordThisHour = when {
        member.isCurrentUser -> myRecords.containsKey(viewedHour)
        member.isPlaceholder -> false
        else -> member.recordedHours.contains(viewedHour)
    }
    val timeFontSize = if (compact) 20.sp else 32.sp
    val nameFontSize = if (compact) 11.sp else 14.sp
    val namePadding = if (compact) 10.dp else 16.dp

    Box(
        modifier = modifier
            .background(Color(0xFFEBEBEB), RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .onGloballyPositioned { coordinates ->
                with(density) {
                    cardWidthDp = coordinates.size.width.toDp()
                    cardHeightDp = coordinates.size.height.toDp()
                }
            }
            .clickable(enabled = member.isCurrentUser && hasRecordThisHour && !isEditingCaption) {
                onExpand()
            }
    ) {
        if (hasRecordThisHour) {
            if (member.isCurrentUser) {
                val thumbnail = myRecords[viewedHour]?.thumbnail
                if (thumbnail != null) {
                    Image(
                        bitmap = thumbnail.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFD1D1D6))
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x33000000))
            )
        } else if (cardWidthDp > 0.dp && cardHeightDp > 0.dp) {
            FloatingBall(
                memberIndex = index,
                totalMembers = totalMembers,
                containerWidth = cardWidthDp,
                containerHeight = cardHeightDp
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = if (member.isCurrentUser && !hasRecordThisHour) (if (compact) (-8).dp else (-16).dp) else 0.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = timeText,
                color = if (hasRecordThisHour) Color.White else Color(0xFFD1D1D6),
                fontSize = timeFontSize,
                fontWeight = FontWeight.Black
            )

            if (member.isCurrentUser && hasRecordThisHour && !isEditingCaption) {
                val caption = myRecords[viewedHour]?.caption
                if (!caption.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(if (compact) 6.dp else 12.dp))
                    Text(
                        text = caption,
                        color = Color.White,
                        fontSize = if (compact) 13.sp else 18.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .padding(namePadding)
                .align(Alignment.TopStart),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(if (compact) 18.dp else 24.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
            Spacer(modifier = Modifier.width(if (compact) 6.dp else 10.dp))
            Text(
                text = member.name,
                color = if (member.isPlaceholder) Color(0xFF8E8E93) else Color.DarkGray,
                fontSize = nameFontSize,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }

        if (member.isCurrentUser) {
            if (hasRecordThisHour && isEditingCaption) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x99000000))
                        .clickable(onClick = onCommitCaption),
                    contentAlignment = Alignment.Center
                ) {
                    BasicTextField(
                        value = editCaptionText,
                        onValueChange = onCaptionChange,
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = if (compact) 14.sp else 18.sp,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        decorationBox = { innerTextField ->
                            if (editCaptionText.isEmpty()) {
                                Text(
                                    text = "캡션을 입력하세요",
                                    color = Color.LightGray,
                                    fontSize = if (compact) 14.sp else 18.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            innerTextField()
                        }
                    )
                }
            }

            if (!hasRecordThisHour && viewedHour == currentHour) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = if (compact) 12.dp else 24.dp)
                        .background(Color.White, RoundedCornerShape(20.dp))
                        .border(BorderStroke(1.dp, Color(0xFFE5E5EA)), RoundedCornerShape(20.dp))
                        .clickable(onClick = onStartCamera)
                        .padding(horizontal = if (compact) 10.dp else 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = if (compact) "촬영" else "눌러서 촬영",
                        color = Color.Black,
                        fontSize = if (compact) 10.sp else 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (hasRecordThisHour) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { onShowMenuChange(true) }
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    if (showMenu) {
                        Popup(
                            alignment = Alignment.BottomEnd,
                            offset = IntOffset(0, -120),
                            onDismissRequest = { onShowMenuChange(false) },
                            properties = PopupProperties(focusable = true)
                        ) {
                            Column(
                                modifier = Modifier
                                    .background(Color.White, RoundedCornerShape(16.dp))
                                    .border(1.dp, Color(0xFFE5E5EA), RoundedCornerShape(16.dp))
                                    .padding(vertical = 8.dp)
                                    .width(150.dp)
                            ) {
                                RecordMenuRow(
                                    icon = { Icon(Icons.Outlined.Delete, contentDescription = null, tint = Color(0xFFFF5555), modifier = Modifier.size(20.dp)) },
                                    text = "삭제",
                                    color = Color(0xFFFF5555),
                                    onClick = onDelete
                                )
                                RecordMenuRow(
                                    icon = { Icon(Icons.Outlined.Edit, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp)) },
                                    text = "캡션 수정",
                                    color = Color.Black,
                                    onClick = onStartEditing
                                )
                                RecordMenuRow(
                                    icon = { Icon(Icons.Outlined.Download, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp)) },
                                    text = "저장",
                                    color = Color.Black,
                                    onClick = onSave
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
private fun RecordMenuRow(
    icon: @Composable () -> Unit,
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Spacer(Modifier.width(12.dp))
        Text(text, color = color, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun HistoryPagerView(
    date: LocalDate,
    onClose: () -> Unit,
    currentUserName: String
) {
    val formatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일")
    val title = date.format(formatter)

    val historyTimes = listOf("15:00", "18:00")
    val pagerState = rememberPagerState(pageCount = { historyTimes.size })

    val memberList = listOf(
        SetlogMember("사용자1", Color(0xFFFF33FF), false, emptyList()),
        SetlogMember("사용자2", Color(0xFF5AC8FA), false, emptyList()),
        SetlogMember(currentUserName, Color(0xFF5AC8FA), true, emptyList())
    )

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
                    .align(Alignment.CenterStart)
                    .clickable { onClose() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(28.dp)
                )
            }

            Text(
                text = title,
                modifier = Modifier.align(Alignment.Center),
                color = Color.Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(historyTimes.size) { iteration ->
                val color = if (pagerState.currentPage == iteration) Color.Black else Color.LightGray
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(8.dp)
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val pageTime = historyTimes[page]

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                memberList.forEachIndexed { index, member ->
                    var cardWidthDp by remember { mutableStateOf(0.dp) }
                    var cardHeightDp by remember { mutableStateOf(0.dp) }
                    val density = LocalDensity.current

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(Color(0xFFEBEBEB), RoundedCornerShape(24.dp))
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

                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = pageTime,
                                color = Color(0xFFD1D1D6),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .align(Alignment.TopStart),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = member.name,
                                color = Color.DarkGray,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
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
    containerWidth: Dp,
    containerHeight: Dp
) {
    val ballSize = 80.dp
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
                    val membersCount = if (totalMembers > 0) totalMembers else 1
                    colorStep = (colorStep + 1) % membersCount
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

    val safeTotalMembers = if (totalMembers > 0) totalMembers else 1
    val baseHue = (360f / safeTotalMembers) * memberIndex
    val stepHue = (360f / safeTotalMembers) * colorStep
    val currentHue = (baseHue + stepHue) % 360f
    val animatedColor = Color.hsv(currentHue, 0.65f, 0.9f)

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Canvas(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = offsetX.dp, y = offsetY.dp)
                .size(ballSize)
        ) {
            drawCircle(color = animatedColor)

            val dynamicEyeRadius = size.width * 0.05f

            drawCircle(
                color = Color.Black,
                radius = dynamicEyeRadius,
                center = Offset(size.width * 0.35f, size.height * 0.55f)
            )
            drawCircle(
                color = Color.Black,
                radius = dynamicEyeRadius,
                center = Offset(size.width * 0.65f, size.height * 0.55f)
            )

            val dynamicStrokeWidth = size.width * 0.04f

            drawArc(
                color = Color.Black,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                style = Stroke(
                    width = dynamicStrokeWidth,
                    cap = StrokeCap.Round
                ),
                topLeft = Offset(size.width * 0.25f, size.height * 0.15f),
                size = Size(size.width * 0.5f, size.height * 0.4f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RecordScreenPreview() {
    RecordScreen()
}
