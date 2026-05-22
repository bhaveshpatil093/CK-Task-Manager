package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Task
import kotlinx.coroutines.delay

enum class RecordingState {
    IDLE,
    RECORDING,
    PAUSED,
    STOPPED
}

@Composable
fun VoiceReportScreen(
    task: Task,
    onDismiss: () -> Unit,
    onSendUpdate: (String, Boolean) -> Unit // parameters: transcription, markAsCompleted
) {
    var recordingState by remember { mutableStateOf(RecordingState.IDLE) }
    var secondsElapsed by remember { mutableStateOf(0) }
    
    // Playback state
    var isPlayingback by remember { mutableStateOf(false) }
    var playbackSeconds by remember { mutableStateOf(0) }
    var playbackSpeed by remember { mutableStateOf("1x") }
    var playbackProgress by remember { mutableStateOf(0.0f) }
    
    // Custom Help and Toast Dialog triggers
    var showHelpDialog by remember { mutableStateOf(false) }
    var showSuccessToast by remember { mutableStateOf(false) }
    var showSuccessToastText by remember { mutableStateOf("") }

    // Dynamic Simulated transcription state
    var transcriptionText by remember { mutableStateOf("") }
    val fullTranscriptionText = remember(task) { getSimulatedTranscription(task.title) }

    // Waveform heights state - 32 bars overall
    var waveAmplitudes by remember { mutableStateOf(FloatArray(32) { 0.05f }) }

    // Active dropdown language selection
    var showLangMenu by remember { mutableStateOf(false) }
    var activeLanguage by remember { mutableStateOf("English") }

    // Timers logic
    // Recording timer
    LaunchedEffect(recordingState) {
        if (recordingState == RecordingState.RECORDING) {
            while (true) {
                delay(1000)
                secondsElapsed++
                
                // Progressively update the live transcription to feel absolutely authentic!
                val fraction = (secondsElapsed.toFloat() / 28f).coerceAtMost(1.0f)
                val words = fullTranscriptionText.split(" ")
                val wordsCountToTake = (words.size * fraction).toInt().coerceAtLeast(1)
                transcriptionText = words.take(wordsCountToTake).joinToString(" ")
                
                if (secondsElapsed >= 28) {
                    // limit simulated recording to 28 seconds to match mock aesthetic exactly
                    recordingState = RecordingState.STOPPED
                    break
                }
            }
        }
    }

    // Playback progression timer
    LaunchedEffect(isPlayingback, recordingState, playbackSpeed) {
        if (isPlayingback && recordingState == RecordingState.STOPPED) {
            val tickRate = when (playbackSpeed) {
                "1.5x" -> 666L
                "2x" -> 500L
                else -> 1000L
            }
            while (playbackSeconds < secondsElapsed) {
                delay(tickRate)
                playbackSeconds++
                playbackProgress = (playbackSeconds.toFloat() / secondsElapsed.toFloat()).coerceIn(0.0f, 1.0f)
            }
            // End playback
            if (playbackSeconds >= secondsElapsed) {
                isPlayingback = false
                playbackSeconds = 0
                playbackProgress = 0f
            }
        }
    }

    // Sound wave amplitude dynamic movement
    LaunchedEffect(recordingState) {
        if (recordingState == RecordingState.RECORDING) {
            while (true) {
                delay(100)
                waveAmplitudes = FloatArray(32) {
                    0.15f + (0.8f * Math.random().toFloat())
                }
            }
        } else if (recordingState == RecordingState.PAUSED) {
            // Freeze wave at low baseline
            waveAmplitudes = FloatArray(32) { 0.12f }
        } else if (isPlayingback && recordingState == RecordingState.STOPPED) {
            // Bouncing wave based on active playback action
            while (isPlayingback) {
                delay(120)
                waveAmplitudes = FloatArray(32) {
                    0.1f + (0.6f * Math.random().toFloat())
                }
            }
        } else {
            // Flatwave
            waveAmplitudes = FloatArray(32) { 0.05f }
        }
    }

    // Interactive Outer pulsing micro animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseSizeMultiplier by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseSize"
    )

    val isDark = MaterialTheme.colorScheme.background != Color(0xFFF8FAFC)
    val bgScreen = if (isDark) Color(0xFF0A0F1D) else Color.White
    val textPrimary = if (isDark) Color.White else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val borderLight = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
    val cardBg = if (isDark) Color(0xFF131B2E) else Color.White

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(bgScreen),
        color = bgScreen
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header (Back button - Screen Title - Help icon)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(cardBg)
                            .border(1.dp, borderLight, RoundedCornerShape(12.dp))
                            .testTag("voice_report_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back",
                            tint = textPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Voice Report",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = textPrimary,
                            letterSpacing = (-0.5).sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Speak to update this task",
                            style = MaterialTheme.typography.bodySmall,
                            color = textSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    IconButton(
                        onClick = { showHelpDialog = true },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(cardBg)
                            .border(1.dp, borderLight, RoundedCornerShape(12.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Help Info",
                            tint = if (isDark) Color(0xFF60A5FA) else Color(0xFF1E40AF),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // target Task Summary details Card Context
                Card(
                     modifier = Modifier
                         .fillMaxWidth()
                         .shadow(if (isDark) 0.dp else 1.dp, RoundedCornerShape(16.dp), ambientColor = Color.LightGray),
                     colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC)),
                     shape = RoundedCornerShape(16.dp),
                     border = BorderStroke(1.dp, borderLight)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left bank / project category icon wrapper
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (isDark) Color(0xFF1E3A8A).copy(alpha = 0.4f) else Color(0xFFEFF6FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🏛️",
                                fontSize = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${task.category} • Assigned Task Coordinator",
                                style = MaterialTheme.typography.bodySmall,
                                color = textSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Right Priority Indicator
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    when (task.priority.uppercase()) {
                                        "HIGH" -> if (isDark) Color(0xFFEF4444).copy(alpha = 0.2f) else Color(0xFFFEE2E2)
                                        "MEDIUM" -> if (isDark) Color(0xFFF59E0B).copy(alpha = 0.2f) else Color(0xFFFEF3C7)
                                        else -> if (isDark) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFFD1FAE5)
                                    }
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = task.priority,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = when (task.priority.uppercase()) {
                                    "HIGH" -> if (isDark) Color(0xFFFCA5A5) else Color(0xFFEF4444)
                                    "MEDIUM" -> if (isDark) Color(0xFFFDE047) else Color(0xFFD97706)
                                    else -> if (isDark) Color(0xFF86EFAC) else Color(0xFF059669)
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Recording Status Label indicator
                Text(
                    text = when (recordingState) {
                        RecordingState.IDLE -> "Press Microphone to Record"
                        RecordingState.RECORDING -> "Recording..."
                        RecordingState.PAUSED -> "Recording Paused"
                        RecordingState.STOPPED -> "Recording Complete"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = when (recordingState) {
                        RecordingState.RECORDING -> Color(0xFF2563EB)
                        RecordingState.PAUSED -> Color(0xFFF59E0B)
                        else -> Color(0xFF475569)
                    },
                    fontWeight = FontWeight.Bold
                )

                // Big timer layout
                Text(
                    text = formatTimer(if (recordingState == RecordingState.STOPPED) secondsElapsed else secondsElapsed),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0F172A),
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Text(
                    text = when (recordingState) {
                        RecordingState.IDLE -> "Tap to start"
                        RecordingState.RECORDING -> "Tap microphone to pause"
                        RecordingState.PAUSED -> "Tap microphone to resume"
                        RecordingState.STOPPED -> "Preview report details below"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Beautiful high-fidelity oscillating sound Waveform canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(84.dp)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    WaveformVisualizer(
                        amplitudes = waveAmplitudes,
                        recordingState = recordingState,
                        playbackProgress = if (recordingState == RecordingState.STOPPED) playbackProgress else 0.0f
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Central Control Panel (Pause | MICROPHONE | Stop)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Control: PAUSE / RESUME button
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(68.dp)
                    ) {
                        val isPauseActive = recordingState == RecordingState.RECORDING || recordingState == RecordingState.PAUSED
                        IconButton(
                            onClick = {
                                if (recordingState == RecordingState.RECORDING) {
                                    recordingState = RecordingState.PAUSED
                                } else if (recordingState == RecordingState.PAUSED) {
                                    recordingState = RecordingState.RECORDING
                                }
                            },
                            enabled = isPauseActive,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(if (isPauseActive) Color.White else Color(0xFFF8FAFC))
                                .border(
                                    width = 1.dp,
                                    color = if (isPauseActive) Color(0xFFE2E8F0) else Color(0xFFF1F5F9),
                                    shape = CircleShape
                                )
                                .testTag("voice_pause_btn")
                        ) {
                            PauseIcon(
                                tint = if (isPauseActive) Color(0xFF2563EB) else Color(0xFFCBD5E1),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (recordingState == RecordingState.PAUSED) "Resume" else "Pause",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isPauseActive) Color(0xFF475569) else Color(0xFF94A3B8),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(28.dp))

                    // Center Control: Primary Master WhatsApp Microphone button (Large)
                    Box(
                        modifier = Modifier.size(114.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Pulsing outer glass circles when recording
                        if (recordingState == RecordingState.RECORDING) {
                            Box(
                                modifier = Modifier
                                    .size((94 * pulseSizeMultiplier).dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF3B82F6).copy(alpha = 0.12f))
                            )
                            Box(
                                modifier = Modifier
                                    .size((114 * pulseSizeMultiplier).dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF3B82F6).copy(alpha = 0.05f))
                            )
                        }

                        // Main core button
                        Box(
                            modifier = Modifier
                                .size(82.dp)
                                .shadow(
                                    elevation = 6.dp,
                                    shape = CircleShape,
                                    ambientColor = Color(0xFF2563EB),
                                    spotColor = Color(0xFF1E40AF)
                                )
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8))
                                    )
                                )
                                .clickable {
                                    when (recordingState) {
                                        RecordingState.IDLE -> {
                                            recordingState = RecordingState.RECORDING
                                            secondsElapsed = 0
                                            transcriptionText = ""
                                        }
                                        RecordingState.RECORDING -> {
                                            recordingState = RecordingState.PAUSED
                                        }
                                        RecordingState.PAUSED -> {
                                            recordingState = RecordingState.RECORDING
                                        }
                                        RecordingState.STOPPED -> {
                                            // Reset to record again
                                            recordingState = RecordingState.RECORDING
                                            secondsElapsed = 0
                                            transcriptionText = ""
                                        }
                                    }
                                }
                                .testTag("voice_microphone_primary_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            MicrophoneIcon(
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(28.dp))

                    // Right Control: STOP recording
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(68.dp)
                    ) {
                        val isStopActive = recordingState == RecordingState.RECORDING || recordingState == RecordingState.PAUSED
                        IconButton(
                            onClick = {
                                if (isStopActive) {
                                    recordingState = RecordingState.STOPPED
                                    // Ensure final complete text is set
                                    transcriptionText = fullTranscriptionText
                                    showSuccessToastText = "Recording locked & saved! 🎙️"
                                    showSuccessToast = true
                                }
                            },
                            enabled = isStopActive,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(if (isStopActive) Color.White else Color(0xFFF8FAFC))
                                .border(
                                    width = 1.dp,
                                    color = if (isStopActive) Color(0xFFE2E8F0) else Color(0xFFF1F5F9),
                                    shape = CircleShape
                                )
                                .testTag("voice_stop_btn")
                        ) {
                            StopIcon(
                                tint = if (isStopActive) Color(0xFFEF4444) else Color(0xFFCBD5E1),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Stop",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isStopActive) Color(0xFF475569) else Color(0xFF94A3B8),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Transcription card (Editable & clean)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(1.dp, RoundedCornerShape(16.dp), ambientColor = Color.LightGray),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, borderLight)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Live Icon",
                                    tint = if (isDark) Color(0xFF60A5FA) else Color(0xFF2563EB),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Live Transcription",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimary
                                )
                            }

                            // Language Selector menu drop down list
                            Box {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(if (isDark) Color(0xFF1E3A8A).copy(alpha = 0.4f) else Color(0xFFEFF6FF))
                                        .border(1.dp, if (isDark) Color(0xFF1E3A8A) else Color(0xFFDBEAFE), RoundedCornerShape(24.dp))
                                        .clickable { showLangMenu = !showLangMenu }
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = activeLanguage,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isDark) Color(0xFF93C5FD) else Color(0xFF1E40AF),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Search, // custom drop representation
                                        contentDescription = "Dropdown indicators",
                                        tint = Color(0xFF1E40AF),
                                        modifier = Modifier.size(12.dp)
                                    )
                                }

                                DropdownMenu(
                                    expanded = showLangMenu,
                                    onDismissRequest = { showLangMenu = false },
                                    modifier = Modifier.background(Color.White)
                                ) {
                                    listOf("English", "Marathi", "Hindi", "Kannada").forEach { lang ->
                                        DropdownMenuItem(
                                            text = { Text(text = lang, color = Color(0xFF0F172A), fontWeight = FontWeight.Bold) },
                                            onClick = {
                                                activeLanguage = lang
                                                showLangMenu = false
                                                showSuccessToastText = "Switched listening focus to $lang!"
                                                showSuccessToast = true
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Transcription output textfield for older users to modify manually
                        OutlinedTextField(
                            value = if (transcriptionText.isNotEmpty()) transcriptionText else "Tap record & start talking to populate real-time transcription notes...",
                            onValueChange = { transcriptionText = it },
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (transcriptionText.isNotEmpty()) Color(0xFF1E293B) else Color(0xFF94A3B8),
                                lineHeight = 21.sp
                            ),
                            placeholder = { Text("Speak clearly to update the system...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("voice_transcription_box_input"),
                            shape = RoundedCornerShape(12.dp),
                            maxLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFF8FAFC),
                                unfocusedContainerColor = Color(0xFFF8FAFC),
                                focusedBorderColor = Color(0xFF2563EB),
                                unfocusedBorderColor = Color(0xFFF1F5F9)
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Auto-detect: $activeLanguage",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8),
                                fontWeight = FontWeight.SemiBold
                            )

                            if (recordingState == RecordingState.STOPPED && transcriptionText.isNotEmpty()) {
                                Row(
                                    modifier = Modifier.clickable {
                                        transcriptionText = getSimulatedTranscription(task.title)
                                        showSuccessToastText = "Reset transcription note!"
                                        showSuccessToast = true
                                    },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Sync",
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Restore simulated text",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF64748B),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Playback container section
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(1.dp, RoundedCornerShape(16.dp), ambientColor = Color.LightGray),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Playback Controls",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        val canPlayback = recordingState == RecordingState.STOPPED
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Play / Pause Circle
                            IconButton(
                                onClick = {
                                    if (canPlayback) {
                                        isPlayingback = !isPlayingback
                                    } else {
                                        showSuccessToastText = "Record and save a message first!"
                                        showSuccessToast = true
                                    }
                                },
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(if (canPlayback) Color(0xFFEFF6FF) else Color(0xFFF8FAFC))
                                    .testTag("voice_playback_toggle_btn")
                            ) {
                                Icon(
                                    imageVector = if (isPlayingback) Icons.Default.Close else Icons.Default.PlayArrow,
                                    contentDescription = "Toggle Playback",
                                    tint = if (canPlayback) Color(0xFF2563EB) else Color(0xFFCBD5E1),
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            // Slider timeline progression progress
                            Slider(
                                value = playbackProgress,
                                onValueChange = {
                                    if (canPlayback) {
                                        playbackProgress = it
                                        playbackSeconds = (it * secondsElapsed).toInt()
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 12.dp)
                                    .testTag("voice_playback_timeline_slider"),
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF2563EB),
                                    activeTrackColor = Color(0xFF3B82F6),
                                    inactiveTrackColor = Color(0xFFE2E8F0)
                                )
                            )

                            // Playback timeline timer
                            Text(
                                text = String.format("%02d:%02d", playbackSeconds / 60, playbackSeconds % 60),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF475569)
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            // Playback speed pill selection
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (canPlayback) Color(0xFFEFF6FF) else Color(0xFFF8FAFC))
                                    .clickable(enabled = canPlayback) {
                                        playbackSpeed = when (playbackSpeed) {
                                            "1x" -> "1.5x"
                                            "1.5x" -> "2x"
                                            else -> "1x"
                                        }
                                        showSuccessToastText = "Playback speed set to $playbackSpeed!"
                                        showSuccessToast = true
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("voice_playback_speed_pill"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = playbackSpeed,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = if (canPlayback) Color(0xFF1E40AF) else Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Send Update (Primary) & Discard (Secondary) Row Callouts
                val isSendEnabled = recordingState == RecordingState.STOPPED && transcriptionText.isNotEmpty()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // DISCARD Button (Outline style)
                    Button(
                        onClick = {
                            recordingState = RecordingState.IDLE
                            secondsElapsed = 0
                            transcriptionText = ""
                            playbackSeconds = 0
                            playbackProgress = 0f
                            isPlayingback = false
                            showSuccessToastText = "Reporting draft discarded!"
                            showSuccessToast = true
                        },
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .weight(0.4f)
                            .height(54.dp)
                            .testTag("voice_discard_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF0F172A)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Discard Icon",
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Discard",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF475569)
                            )
                        }
                    }

                    // SEND UPDATE Button (Deep clean blue accent gradient)
                    Button(
                        onClick = {
                            if (isSendEnabled) {
                                onSendUpdate(transcriptionText, true)
                            } else {
                                showSuccessToastText = "Please record a message before sending!"
                                showSuccessToast = true
                            }
                        },
                        modifier = Modifier
                            .weight(0.6f)
                            .height(54.dp)
                            .shadow(
                                elevation = if (isSendEnabled) 4.dp else 0.dp,
                                shape = RoundedCornerShape(14.dp),
                                ambientColor = Color(0xFF2563EB)
                            )
                            .testTag("voice_send_update_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSendEnabled) Color(0xFF1E40AF) else Color(0xFFEFF6FF),
                            contentColor = if (isSendEnabled) Color.White else Color(0xFF94A3B8)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check, // Paper plane / Send representation
                                contentDescription = "Paper Plane Send Icon",
                                tint = if (isSendEnabled) Color.White else Color(0xFFCBD5E1),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Send Update",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Black,
                                color = if (isSendEnabled) Color.White else Color(0xFF94A3B8)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Secure Shield Indicator Footer
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Shield Guard",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Your voice report is secure and encrypted",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(48.dp))
            }

            // Success Toast notification floating bar overlay
            AnimatedVisibility(
                visible = showSuccessToast,
                enter = slideInVertically(
                    initialOffsetY = { -it },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
                ) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 20.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .shadow(8.dp, RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Success tick",
                            tint = Color(0xFF10B981)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = showSuccessToastText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Auto dismiss toast logic
                LaunchedEffect(showSuccessToast) {
                    if (showSuccessToast) {
                        delay(2500)
                        showSuccessToast = false
                    }
                }
            }
        }
    }

    // Modal Help dialog for older users
    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = {
                Text(
                    text = "How to use Voice Report? ❓",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "1. TAP the large blue microphone button in the center to start recording. Speak clearly into your device's mic.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF475569)
                    )
                    Text(
                        text = "2. WATCH the waveforms dance to confirm your sound is being captured in the workspace.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF475569)
                    )
                    Text(
                        text = "3. TAP STOP when complete to generate. Review the live transcribed text in the card or edit manually with your keyboard.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF475569)
                    )
                    Text(
                        text = "4. TAP Send Update to officially post this voice report to the task's specifications backlog!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF475569)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showHelpDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E40AF))
                ) {
                    Text("Got It!", fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }
}

@Composable
fun WaveformVisualizer(
    amplitudes: FloatArray,
    recordingState: RecordingState,
    playbackProgress: Float
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val barCount = amplitudes.size
        
        val gap = 6.dp.toPx()
        val totalGapsWidth = gap * (barCount - 1)
        val barWidth = (width - totalGapsWidth) / barCount
        
        val activeBarIndex = (playbackProgress * barCount).toInt().coerceAtMost(barCount - 1)

        for (i in 0 until barCount) {
            val amp = amplitudes[i]
            
            // Calculate height of the bar based on amplitude
            val barHeight = (height * amp).coerceIn(4.dp.toPx(), height)
            val x = i * (barWidth + gap)
            val y = (height - barHeight) / 2
            
            // Highlight color based on playback progression of voice note
            val isPassed = i <= activeBarIndex && recordingState == RecordingState.STOPPED
            val color = when {
                recordingState == RecordingState.RECORDING -> Color(0xFF2563EB)
                recordingState == RecordingState.PAUSED -> Color(0xFF93C5FD)
                isPassed -> Color(0xFF1D4ED8) // Playback past highlight
                else -> Color(0xFFDBEAFE) // Idle/unvisited baseline color
            }

            drawRoundRect(
                color = color,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
            )
        }
    }
}

@Composable
fun MicrophoneIcon(
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val w = size.width
        val h = size.height
        
        // Microphone body (capsule)
        val bodyWidth = w * 0.35f
        val bodyHeight = h * 0.5f
        val bodyLeft = (w - bodyWidth) / 2
        val bodyTop = h * 0.15f
        drawRoundRect(
            color = tint,
            topLeft = Offset(bodyLeft, bodyTop),
            size = Size(bodyWidth, bodyHeight),
            cornerRadius = CornerRadius(bodyWidth / 2, bodyWidth / 2)
        )
        
        // Cradle (U-shape line)
        val cradleRadius = w * 0.28f
        val strokeWidth = 2.5.dp.toPx()
        drawArc(
            color = tint,
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset((w - cradleRadius * 2) / 2, bodyTop + bodyHeight / 2 - cradleRadius),
            size = Size(cradleRadius * 2, cradleRadius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        
        // Stand base line
        val standTop = bodyTop + bodyHeight / 2 + cradleRadius
        val standBottom = h * 0.85f
        drawLine(
            color = tint,
            start = Offset(w / 2, standTop),
            end = Offset(w / 2, standBottom),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        
        // Base horizontal line
        val baseWidth = w * 0.4f
        drawLine(
            color = tint,
            start = Offset((w - baseWidth) / 2, standBottom),
            end = Offset((w + baseWidth) / 2, standBottom),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun PauseIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(24.dp)) {
        val w = size.width
        val h = size.height
        val barWidth = w * 0.22f
        val spacing = w * 0.2f
        val top = h * 0.22f
        val bottom = h * 0.78f
        
        // Left bar
        drawRoundRect(
            color = tint,
            topLeft = Offset((w - (barWidth * 2 + spacing)) / 2, top),
            size = Size(barWidth, bottom - top),
            cornerRadius = CornerRadius(barWidth / 4, barWidth / 4)
        )
        // Right bar
        drawRoundRect(
            color = tint,
            topLeft = Offset((w - (barWidth * 2 + spacing)) / 2 + barWidth + spacing, top),
            size = Size(barWidth, bottom - top),
            cornerRadius = CornerRadius(barWidth / 4, barWidth / 4)
        )
    }
}

@Composable
fun StopIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(24.dp)) {
        val w = size.width
        val h = size.height
        val sizeSquare = w * 0.45f
        drawRoundRect(
            color = tint,
            topLeft = Offset((w - sizeSquare) / 2, (h - sizeSquare) / 2),
            size = Size(sizeSquare, sizeSquare),
            cornerRadius = CornerRadius(sizeSquare / 6, sizeSquare / 6)
        )
    }
}

private fun formatTimer(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format("%02d:%02d", m, s)
}

fun getSimulatedTranscription(taskTitle: String): String {
    return when {
        taskTitle.contains("HDFC", ignoreCase = true) || taskTitle.contains("Call", ignoreCase = true) -> {
            "I have called the HDFC branch manager and confirmed the documents. They will process the application by tomorrow."
        }
        taskTitle.contains("Profile", ignoreCase = true) || taskTitle.contains("CKTM", ignoreCase = true) -> {
            "I have updated the application profile. All system tokens are successfully setup, and the primary corporate palette is now live."
        }
        taskTitle.contains("Pendency", ignoreCase = true) || taskTitle.contains("KPI", ignoreCase = true) -> {
            "Met with the operations director to establish the zero pendency benchmarks. Every unresolved issue must be logged and addressed within our 24-hour cycle starting immediately."
        }
        else -> {
            "I have completed a comprehensive review of the active item '$taskTitle'. The task status looks healthy, and the outstanding milestones are ready for deployment."
        }
    }
}
