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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Task
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCountdownScreen(
    task: Task,
    onDismiss: () -> Unit,
    onMarkCompleted: () -> Unit,
    onAddUpdate: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Interactive Demo Simulation Level state:
    // "Safe" (Green), "Warning" (Yellow), "Urgent" (Red), "Overdue" (Overdue Flashing)
    var severityMode by remember { mutableStateOf("Urgent") }

    // Countdown active details State: seconds remaining
    var secondsRemaining by remember {
        mutableStateOf(
            when (severityMode) {
                "Safe" -> 5400L    // 1h 30m
                "Warning" -> 2700L // 45m
                "Urgent" -> 942L   // 15m 42s
                else -> 0L         // Overdue
            }
        )
    }

    // Keep secondsRemaining updated when severityMode changes
    LaunchedEffect(severityMode) {
        secondsRemaining = when (severityMode) {
            "Safe" -> 5400L
            "Warning" -> 2700L
            "Urgent" -> 942L
            else -> -10L // starts countup or flashing state for overdue
        }
    }

    // Live Ticking Clock Ticker
    LaunchedEffect(key1 = Unit) {
        while (true) {
            delay(1000)
            if (severityMode == "Overdue") {
                secondsRemaining-- // continues counting past zero to display overdue amount
            } else {
                if (secondsRemaining > 0) {
                    secondsRemaining--
                } else {
                    severityMode = "Overdue"
                }
            }
        }
    }

    // Overdue Flashing Infinite Alpha animation
    val infiniteTransition = rememberInfiniteTransition(label = "flashing_animation")
    val flashingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    // Layout configuration values depending on dynamic severity modes
    val statusLabel = when (severityMode) {
        "Safe" -> "SAFE"
        "Warning" -> "WARNING"
        "Urgent" -> "URGENT"
        else -> "OVERDUE BREACH"
    }

    val themeColor = when (severityMode) {
        "Safe" -> Color(0xFF10B981) // Green
        "Warning" -> Color(0xFFF59E0B) // Amber/Yellow
        "Urgent" -> Color(0xFFEF4444) // Red
        else -> Color(0xFF991B1B) // Flashing Dark Red
    }

    val urgencyPercentage = when (severityMode) {
        "Safe" -> 25
        "Warning" -> 60
        "Urgent" -> 92
        else -> 100
    }

    val ringColorGradients = when (severityMode) {
        "Safe" -> Color(0xFFD1FAE5)
        "Warning" -> Color(0xFFFEF3C7)
        "Urgent" -> Color(0xFFFEE2E2)
        else -> Color(0xFFFCA5A5)
    }

    // Formatting seconds into standard string output: hrs : mins : secs
    fun formatTimerString(totalSecs: Long): Triple<String, String, String> {
        val absSecs = Math.abs(totalSecs)
        val hrs = absSecs / 3600
        val mins = (absSecs % 3600) / 60
        val secs = absSecs % 60
        return Triple(
            String.format("%02d", hrs),
            String.format("%02d", mins),
            String.format("%02d", secs)
        )
    }

    // Quick text input for sending updates dialogue representation
    var showAddUpdateTextDialog by remember { mutableStateOf(false) }
    var updateTextValue by remember { mutableStateOf("") }

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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Task Countdown Header
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
                            .testTag("countdown_back_btn")
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
                            text = "Task Countdown",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = textPrimary,
                            letterSpacing = (-0.5).sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Real-time monitoring & escalation",
                            style = MaterialTheme.typography.bodySmall,
                            color = textSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Bell counter layout badge
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notification Alerts Feed",
                            tint = themeColor,
                            modifier = Modifier.size(20.dp)
                        )
                        // Badge counter overlay
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444))
                                .align(Alignment.TopEnd)
                                .offset(x = 2.dp, y = (-2).dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "3",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 9.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Interactive Simulator Controller State Selector (For testing and demonstrating custom states)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "DEMO SIMULATION SELECTOR",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF475569),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Safe", "Warning", "Urgent", "Overdue").forEach { mode ->
                                val isSelected = severityMode == mode
                                val selBgColor = when (mode) {
                                    "Safe" -> Color(0xFFD1FAE5)
                                    "Warning" -> Color(0xFFFEF3C7)
                                    "Urgent" -> Color(0xFFFEE2E2)
                                    else -> Color(0xFFFCA5A5)
                                }
                                val textCol = when (mode) {
                                    "Safe" -> Color(0xFF065F46)
                                    "Warning" -> Color(0xFF92400E)
                                    "Urgent" -> Color(0xFF991B1B)
                                    else -> Color(0xFF7F1D1D)
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) selBgColor else Color.White)
                                        .border(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) textCol else Color(0xFFCBD5E1),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { severityMode = mode }
                                        .padding(vertical = 8.dp)
                                        .testTag("countdown_sim_mode_$mode"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = mode,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                                        color = if (isSelected) textCol else Color(0xFF475569)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Timer Visual Dashboard (Includes live count, urgency level ring, dynamic overdue flash layouts)
                val (hrsStr, minsStr, secsStr) = formatTimerString(secondsRemaining)
                val isMinus = secondsRemaining < 0

                // Overdue state flash modifier card
                val layoutBgColor = if (severityMode == "Overdue") {
                    Color(0xFFFEF2F2).copy(alpha = flashingAlpha)
                } else {
                    Color.White
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(24.dp), ambientColor = themeColor),
                    colors = CardDefaults.cardColors(containerColor = layoutBgColor),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, themeColor.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left Section: Timer and urgency tag
                        Column {
                            // Urgency Dynamic Mode chip
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(themeColor.copy(alpha = 0.15f))
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = statusLabel,
                                    color = themeColor,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = if (isMinus) "OVERDUE BREACH" else "TIME REMAINING",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isMinus) Color(0xFFDC2626) else Color(0xFF64748B),
                                letterSpacing = 1.sp
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Large Clock display text
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isMinus) {
                                    Text(
                                        text = "-",
                                        style = MaterialTheme.typography.displayMedium,
                                        fontWeight = FontWeight.Black,
                                        color = themeColor,
                                        fontSize = 32.sp
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                }

                                Text(
                                    text = "$hrsStr:$minsStr:$secsStr",
                                    style = MaterialTheme.typography.displayMedium,
                                    fontWeight = FontWeight.Black,
                                    color = themeColor,
                                    fontSize = 38.sp,
                                    letterSpacing = (-1).sp
                                )
                            }

                            // Horizontal units labels underneath
                            Row(
                                modifier = Modifier.padding(top = 2.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("HRS", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("MINS", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("SECS", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Deadline countdown details indicator text
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Alert deadline logo",
                                    tint = themeColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isMinus) "Breached by $minsStr minutes!" else "Deadline in $minsStr minutes",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = themeColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Right Section: Urgency Level Circular Progress Ring Canvas representation
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Circle Drawing
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                // Draw Background Ring grey circle
                                drawCircle(
                                    color = ringColorGradients,
                                    radius = size.minDimension / 2.1f,
                                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                                )

                                // Draw Sweep urgency value arc outline
                                val sweepAngle = (urgencyPercentage / 100f) * 360f
                                drawArc(
                                    color = themeColor,
                                    startAngle = -90f,
                                    sweepAngle = sweepAngle,
                                    useCenter = false,
                                    style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Urgency dial indicator info",
                                    tint = themeColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$urgencyPercentage%",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = themeColor
                                )
                                Text(
                                    text = "Urgency Level",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF64748B),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 7.sp
                                )
                                Text(
                                    text = when (severityMode) {
                                        "Safe" -> "LIGHT"
                                        "Warning" -> "MODERATE"
                                        "Urgent" -> "CRITICAL"
                                        else -> "BREACHED"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = themeColor,
                                    fontSize = 8.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // High-Fidelity Task Details Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 0.5.dp),
                    border = BorderStroke(1.dp, borderLight),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isDark) Color(0xFF1E3A8A).copy(alpha = 0.4f) else Color(0xFFEFF6FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "🏛️", fontSize = 16.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = task.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${task.category} • Enterprise Operations",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = textSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        HorizontalDivider(color = borderLight)

                        Spacer(modifier = Modifier.height(12.dp))

                        // Grid item parameters
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Deadline clock",
                                        tint = Color(0xFF64748B),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Deadline",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF64748B),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Today, 10:00 AM",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFCE0000)
                                )
                            }

                            Column(modifier = Modifier.weight(1.2f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBox,
                                        contentDescription = "Assigned person badge log",
                                        tint = Color(0xFF64748B),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Assigned To",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF64748B),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFDBEAFE)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("👩", fontSize = 10.sp)
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Chaitra Madam",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B)
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(0.8f)) {
                                Text(
                                    text = "Task ID",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF64748B),
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "#CKTM-2456",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF475569)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Escalation Status flow node diagram map representation
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Escalation Status Flow",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (severityMode == "Safe" || severityMode == "Warning")
                                        Color(0xFFD1FAE5)
                                    else Color(0xFFFEE2E2)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (severityMode == "Safe" || severityMode == "Warning")
                                    "Escalation Idle"
                                else "Escalation Active",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (severityMode == "Safe" || severityMode == "Warning")
                                    Color(0xFF065F46)
                                else Color(0xFF991B1B)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Node Diagram Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Node 1: Assignee (Chaitra Madam)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFD1FAE5))
                                    .border(1.dp, Color(0xFF10B981), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Step compiled completed icon representation",
                                    tint = Color(0xFF065F46),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Assignee", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = Color(0xFF1E293B))
                            Text("Chaitra M.", style = MaterialTheme.typography.labelSmall, color = Color(0xFF64748B), fontSize = 8.sp)
                            Text("09:00 AM", style = MaterialTheme.typography.labelSmall, color = Color(0xFF10B981), fontWeight = FontWeight.Black, fontSize = 8.sp)
                        }

                        // Arrow
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Flow direction arrow direction icon element",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(12.dp)
                        )

                        // Node 2: Reminder 1
                        val r1Color = if (severityMode == "Safe") Color(0xFFCBD5E1) else Color(0xFFFEF3C7)
                        val r1Border = if (severityMode == "Safe") Color(0xFF94A3B8) else Color(0xFFF59E0B)
                        val r1TextCol = if (severityMode == "Safe") Color(0xFF64748B) else Color(0xFFB45309)
                        val r1Completed = severityMode != "Safe"

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(r1Color)
                                    .border(1.dp, r1Border, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (r1Completed) Icons.Default.Check else Icons.Default.Warning,
                                    contentDescription = "Step 2",
                                    tint = r1Border,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Reminder 1", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = Color(0xFF1E293B))
                            Text("09:30 AM", style = MaterialTheme.typography.labelSmall, color = r1Border, fontWeight = FontWeight.Black, fontSize = 8.sp)
                        }

                        // Arrow
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Direction step arrow",
                            tint = if (severityMode == "Safe") Color(0xFFCBD5E1) else Color(0xFFF59E0B),
                            modifier = Modifier.size(12.dp)
                        )

                        // Node 3: Reminder 2
                        val r2Color = when (severityMode) {
                            "Safe", "Warning" -> Color(0xFFCBD5E1)
                            else -> Color(0xFFFEE2E2)
                        }
                        val r2Border = when (severityMode) {
                            "Safe", "Warning" -> Color(0xFF94A3B8)
                            else -> Color(0xFFEF4444)
                        }
                        val r2Completed = severityMode == "Urgent" || severityMode == "Overdue"

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(r2Color)
                                    .border(1.dp, r2Border, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (r2Completed) Icons.Default.Check else Icons.Default.Warning,
                                    contentDescription = "Step 3 indicator element node",
                                    tint = r2Border,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Reminder 2", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = Color(0xFF1E293B))
                            Text("09:40 AM", style = MaterialTheme.typography.labelSmall, color = r2Border, fontWeight = FontWeight.Black, fontSize = 8.sp)
                        }

                        // Arrow
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Direction arrow index route",
                            tint = if (severityMode == "Safe" || severityMode == "Warning") Color(0xFFCBD5E1) else Color(0xFFEF4444),
                            modifier = Modifier.size(12.dp)
                        )

                        // Node 4: Manager Alert
                        val r4Color = if (severityMode == "Overdue") Color(0xFFFEE2E2) else Color(0xFFF8FAFC)
                        val r4Border = if (severityMode == "Overdue") Color(0xFF991B1B) else Color(0xFFCBD5E1)
                        val r4IconCol = if (severityMode == "Overdue") Color(0xFF991B1B) else Color(0xFF94A3B8)

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(r4Color)
                                    .border(1.dp, r4Border, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Manager alert node indicator icon",
                                    tint = r4IconCol,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Manager Alert", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = Color(0xFF1E293B))
                            Text("10:00 AM", style = MaterialTheme.typography.labelSmall, color = r4Border, fontWeight = FontWeight.Black, fontSize = 8.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Escalation warning banner card alert representation
                AnimatedVisibility(
                    visible = severityMode == "Urgent" || severityMode == "Overdue",
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFDF2F2)),
                        border = BorderStroke(1.dp, Color(0xFFFDE8E8)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFDE8E8)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Escalation warning banner icon icon",
                                    tint = Color(0xFFE02424),
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "ESCALATION WARNING",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF9B1C1C)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (severityMode == "Overdue")
                                        "This task has breached its SLA! Senior executives and administrators have been informed."
                                    else "This task is about to breach the deadline. Immediate action is required!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF951414),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Manager escalation alert section
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (severityMode == "Safe") Color(0xFFE2E8F0) else themeColor.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(themeColor.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Siren alarm speaker alert icon",
                                tint = themeColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "MANAGER ESCALATION ALERT",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = if (severityMode == "Safe") Color(0xFF475569) else themeColor
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = when (severityMode) {
                                    "Safe" -> "Monitoring is inactive. Auto-escalation enabled."
                                    "Warning" -> "Approaching critical threshold. Executive monitoring active."
                                    "Urgent" -> "This task will be escalated to the next level in $minsStr minutes if not updated."
                                    else -> "Auto-escalated! Alert dispatch triggered successfully 🚨"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF475569),
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Button(
                            onClick = {
                                severityMode = "Overdue"
                                com.example.MainActivity.triggerCustomToast(context, "Force escalation triggered to Admin Level 🎙️")
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (severityMode == "Safe") Color(0xFF64748B) else Color(0xFFEF4444)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp).testTag("countdown_escalate_now_btn")
                        ) {
                            Text(
                                text = "Escalate",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // List of Reminders & Alerts Sent
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Reminders logo details",
                                tint = Color(0xFF1E3A8A),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Reminders & Alerts History",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }

                        Text(
                            text = "View All",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF2563EB),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable {
                                com.example.MainActivity.triggerCustomToast(context, "All historic dispatch messages loaded.")
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Simulated alerts dispatches
                    val reminderHistory = listOf(
                        Triple("Reminder sent to Chaitra Madam", "09:30 AM • 15m ago", "Sent"),
                        Triple("Executive Warning triggered to Finance", "09:40 AM • 5m ago", "Sent"),
                        Triple("Manager alert will be sent to Kiran Madam", "10:00 AM • Pending", "Pending")
                    )

                    reminderHistory.forEach { alert ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (alert.third == "Sent") Color(0xFFEFF6FF)
                                            else Color(0xFFFEF3C7)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = "Alert historical icon marker representation",
                                        tint = if (alert.third == "Sent") Color(0xFF2563EB) else Color(0xFFD97706),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = alert.first,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B)
                                    )
                                    Text(
                                        text = alert.second,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF64748B),
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (alert.third == "Sent") Color(0xFFD1FAE5)
                                            else Color(0xFFFEF3C7)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = alert.third,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (alert.third == "Sent") Color(0xFF065F46) else Color(0xFFB45309)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Bottom Action buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { showAddUpdateTextDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("countdown_add_update_btn"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, Color(0xFFDBEAFE)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF1E40AF)
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Write a new report commentary log check update icon",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Add Update",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Button(
                        onClick = {
                            onMarkCompleted()
                            com.example.MainActivity.triggerCustomToast(context, "Completed! SLA compliance captured successfully and catalogmed ✅🚀")
                        },
                        modifier = Modifier
                            .weight(1.2f)
                            .height(50.dp)
                            .testTag("countdown_mark_completed_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E40AF)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Completed mark check circle sign represent button",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Mark as Completed",
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                // Compliance Footer disclaimer status
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Shield compliance green mark verify",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Auto-escalation is enabled for this task",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }

    // Modal dialog to input update content description
    if (showAddUpdateTextDialog) {
        Dialog(onDismissRequest = { showAddUpdateTextDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Add Operational Update",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = updateTextValue,
                        onValueChange = { updateTextValue = it },
                        placeholder = {
                            Text(
                                "Type status explanation here to postpone escalation...",
                                fontSize = 13.sp,
                                color = Color(0xFF94A3B8)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("countdown_update_input_field"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1E40AF),
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showAddUpdateTextDialog = false },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Cancel", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF64748B))
                        }

                        Button(
                            onClick = {
                                if (updateTextValue.trim().isNotEmpty()) {
                                    onAddUpdate(updateTextValue)
                                    showAddUpdateTextDialog = false
                                    updateTextValue = ""
                                    com.example.MainActivity.triggerCustomToast(context, "Comment audit log captured! Escalation postponement reviewed.")
                                } else {
                                    com.example.MainActivity.triggerCustomToast(context, "Please enter commentary text first.")
                                }
                            },
                            modifier = Modifier.weight(1.2f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1E40AF)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Submit Status", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
