package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.Task
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PremiumAnalyticsScreen(
    allTasksList: List<Task>,
    stats: DashboardStats,
    onViewPerformance: () -> Unit = {}
) {
    var selectedTimeframe by remember { mutableStateOf("This Week") }
    var selectedDepartment by remember { mutableStateOf("All Departments") }
    var showTimeframeMenu by remember { mutableStateOf(false) }
    var showDeptMenu by remember { mutableStateOf(false) }

    // Interactivity: Highlighted element index on line chart hover / tap simulation
    var highlightedDayIndex by remember { mutableStateOf(-1) }

    // Interactive Employee feedback stats toast
    var toastMessage by remember { mutableStateOf("") }
    var showToast by remember { mutableStateOf(false) }

    fun triggerLocalToast(message: String) {
        toastMessage = message
        showToast = true
    }

    LaunchedEffect(showToast) {
        if (showToast) {
            delay(2000)
            showToast = false
        }
    }

    val isDark = MaterialTheme.colorScheme.background != Color(0xFFF8FAFC)
    val bgScreen = if (isDark) Color(0xFF0A0F1D) else Color(0xFFFCFDFD)
    val textPrimary = if (isDark) Color.White else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val borderLight = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
    val cardBg = if (isDark) Color(0xFF131B2E) else Color.White

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgScreen)
            .testTag("premium_analytics_screen")
    ) {
        val solvedPoints = remember(allTasksList) {
            val counts = FloatArray(7) { 0f }
            allTasksList.forEach { task ->
                if (task.isCompleted) {
                    val dayIdx = ((task.createdAt / (1000 * 60 * 60 * 24)) % 7).toInt()
                    counts[dayIdx] += 1f
                }
            }
            counts.toList()
        }
        val pendingPoints = remember(allTasksList) {
            val counts = FloatArray(7) { 0f }
            allTasksList.forEach { task ->
                if (!task.isCompleted) {
                    val dayIdx = ((task.createdAt / (1000 * 60 * 60 * 24)) % 7).toInt()
                    counts[dayIdx] += 1f
                }
            }
            counts.toList()
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 76.dp) // padding to avoid floating bottom tab overlap
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Analytics & Reports",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = textPrimary,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Real-time insights and productivity overview",
                        style = MaterialTheme.typography.bodySmall,
                        color = textSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Small Calendar / Clock indicator
                IconButton(
                    onClick = { triggerLocalToast("Synced core audit database logs.") },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF8FAFC))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Sync reports calendar",
                        tint = Color(0xFF475569),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Top Filter Dropdowns Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Timeframe Select
                Box {
                    Button(
                        onClick = { showTimeframeMenu = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF334155)
                        ),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(selectedTimeframe, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    }
                    DropdownMenu(
                        expanded = showTimeframeMenu,
                        onDismissRequest = { showTimeframeMenu = false },
                        scrollState = rememberScrollState()
                    ) {
                        listOf("This Week", "This Month", "Q2 - Forecast").forEach { tf ->
                            DropdownMenuItem(
                                text = { Text(tf, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                                onClick = {
                                    selectedTimeframe = tf
                                    showTimeframeMenu = false
                                    triggerLocalToast("Timeframe filtered: $tf")
                                }
                            )
                        }
                    }
                }

                // Department Select
                Box {
                    Button(
                        onClick = { showDeptMenu = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF334155)
                        ),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(selectedDepartment, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    }
                    DropdownMenu(
                        expanded = showDeptMenu,
                        onDismissRequest = { showDeptMenu = false },
                        scrollState = rememberScrollState()
                    ) {
                        listOf("All Departments", "Operations", "Finance", "HR", "Admin", "IT").forEach { dept ->
                            DropdownMenuItem(
                                text = { Text(dept, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                                onClick = {
                                    selectedDepartment = dept
                                    showDeptMenu = false
                                    triggerLocalToast("Filtered by department: $dept")
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Export Button
                Button(
                    onClick = { triggerLocalToast("Export compiled analytics report. PDF and CSV saved! 📥") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF2563EB)
                    ),
                    border = BorderStroke(1.dp, Color(0xFFDBEAFE)),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Horizon metrics row panel (Solved, Pending, Rate, Score) -> horizontal scrollable list
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Solved
                MetricCardSummary(
                    title = "Solved",
                    value = "128",
                    subtext = "↑ 18.2% vs last week",
                    tint = Color(0xFF10B981),
                    icon = Icons.Default.CheckCircle,
                    backgroundColor = Color(0xFFEFFBFA)
                )
                // Pending
                MetricCardSummary(
                    title = "Pending",
                    value = "34",
                    subtext = "↓ 8.7% vs last week",
                    tint = Color(0xFFF59E0B),
                    icon = Icons.Default.Refresh,
                    backgroundColor = Color(0xFFFFFBEB)
                )
                // Completion Rate
                MetricCardSummary(
                    title = "Completion Rate",
                    value = "79.1%",
                    subtext = "↑ 12.5% vs last week",
                    tint = Color(0xFF3B82F6),
                    icon = Icons.Default.Star,
                    backgroundColor = Color(0xFFEFF6FF)
                )
                // Productivity Score
                MetricCardSummary(
                    title = "Productivity Score",
                    value = "92.4",
                    subtext = "↑ 9.3% vs last week",
                    tint = Color(0xFF8B5CF6),
                    icon = Icons.Default.Settings,
                    backgroundColor = Color(0xFFF5F3FF)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Solved vs Pending Graph - Stripe & Linear Inspired with real vectors
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .shadow(1.dp, RoundedCornerShape(16.dp), ambientColor = Color.LightGray),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFECEFEF)),
                shape = RoundedCornerShape(16.dp)
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
                        Column {
                            Text(
                                text = "Solved vs Pending",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Analysis of resolved status throughout the week",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF64748B)
                            )
                        }

                        // Colors Legend Indicator
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF10B981)))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Solved", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF475569))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFF59E0B)))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Pending", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF475569))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Custom Line chart via interactive Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    ) {
                        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    highlightedDayIndex = (highlightedDayIndex + 1) % days.size
                                    triggerLocalToast("${days[highlightedDayIndex]} metrics: Solved ${solvedPoints[highlightedDayIndex].toInt()}, Pending ${pendingPoints[highlightedDayIndex].toInt()}")
                                }
                        ) {
                            val width = size.width
                            val height = size.height
                            val maxVal = maxOf(
                                2f,
                                (solvedPoints.maxOrNull() ?: 0f),
                                (pendingPoints.maxOrNull() ?: 0f)
                            ) * 1.2f
                            val stepX = width / (days.size - 1)

                            // 1. Draw horizontal background grid lines & labels
                            val gridLines = 4
                            for (i in 0..gridLines) {
                                val gridY = height - (i * (height / gridLines))
                                drawLine(
                                    color = Color(0xFFF1F5F9),
                                    start = Offset(0f, gridY),
                                    end = Offset(width, gridY),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }

                            // 2. Generate smooth gradient outline for Solved
                            val solvedPath = Path().apply {
                                val startY = height - (solvedPoints[0] / maxVal) * height
                                moveTo(0f, startY)
                                for (i in 1 until solvedPoints.size) {
                                    val nextX = i * stepX
                                    val nextY = height - (solvedPoints[i] / maxVal) * height
                                    lineTo(nextX, nextY)
                                }
                            }

                            // Gradient brush fill under Solved path
                            val solvedFillPath = Path().apply {
                                addPath(solvedPath)
                                lineTo((days.size - 1) * stepX, height)
                                lineTo(0f, height)
                                close()
                            }

                            drawPath(
                                path = solvedFillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color(0xFF10B981).copy(alpha = 0.15f), Color.Transparent),
                                    startY = 0f,
                                    endY = height
                                )
                            )

                            drawPath(
                                path = solvedPath,
                                color = Color(0xFF10B981),
                                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                            )

                            // 3. Generate smooth gradient outline for Pending
                            val pendingPath = Path().apply {
                                val startY = height - (pendingPoints[0] / maxVal) * height
                                moveTo(0f, startY)
                                for (i in 1 until pendingPoints.size) {
                                    val nextX = i * stepX
                                    val nextY = height - (pendingPoints[i] / maxVal) * height
                                    lineTo(nextX, nextY)
                                }
                            }

                            val pendingFillPath = Path().apply {
                                addPath(pendingPath)
                                lineTo((days.size - 1) * stepX, height)
                                lineTo(0f, height)
                                close()
                            }

                            drawPath(
                                path = pendingFillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color(0xFFF59E0B).copy(alpha = 0.1f), Color.Transparent),
                                    startY = 0f,
                                    endY = height
                                )
                            )

                            drawPath(
                                path = pendingPath,
                                color = Color(0xFFF59E0B),
                                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                            )

                            // 4. Markers for points
                            for (i in days.indices) {
                                val ptX = i * stepX
                                val solvedY = height - (solvedPoints[i] / maxVal) * height
                                val pendingY = height - (pendingPoints[i] / maxVal) * height

                                // Draw circle markers
                                drawCircle(
                                    color = Color.White,
                                    radius = 5.dp.toPx(),
                                    center = Offset(ptX, solvedY)
                                )
                                drawCircle(
                                    color = Color(0xFF10B981),
                                    radius = 3.dp.toPx(),
                                    center = Offset(ptX, solvedY)
                                )

                                drawCircle(
                                    color = Color.White,
                                    radius = 5.dp.toPx(),
                                    center = Offset(ptX, pendingY)
                                )
                                drawCircle(
                                    color = Color(0xFFF59E0B),
                                    radius = 3.dp.toPx(),
                                    center = Offset(ptX, pendingY)
                                )

                                // Highlight guide line if active
                                if (i == highlightedDayIndex) {
                                    drawLine(
                                        color = Color(0xFF3B82F6).copy(alpha = 0.6f),
                                        start = Offset(ptX, 0f),
                                        end = Offset(ptX, height),
                                        strokeWidth = 1.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Labels below the line chart
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEachIndexed { index, day ->
                            Text(
                                text = day,
                                fontSize = 10.sp,
                                fontWeight = if (index == highlightedDayIndex) FontWeight.Bold else FontWeight.Medium,
                                color = if (index == highlightedDayIndex) Color(0xFF1E40AF) else Color(0xFF64748B),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.width(32.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Two-column layout: Tasks by Department Circle vs Task Completion Trend Bars
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Column 1: Tasks by Department Donut Chart
                Card(
                    modifier = Modifier
                        .weight(1.1f)
                        .height(240.dp)
                        .shadow(0.5.dp, RoundedCornerShape(16.dp), ambientColor = Color.LightGray),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFECEFEF)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Tasks by Department",
                            style = MaterialTheme.typography.titleSmall,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(95.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.size(84.dp)) {
                                val total = 100f
                                var start = -90f

                                // Finance %28 (0xFF10B981)
                                drawArc(Color(0xFF10B981), start, 360f * 0.28f, false, style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round))
                                start += 360f * 0.28f

                                // Operations %23 (0xFF2563EB)
                                drawArc(Color(0xFF2563EB), start, 360f * 0.23f, false, style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round))
                                start += 360f * 0.23f

                                // HR %18 (0xFF8B5CF6)
                                drawArc(Color(0xFF8B5CF6), start, 360f * 0.18f, false, style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round))
                                start += 360f * 0.18f

                                // Admin %15 (0xFFF59E0B)
                                drawArc(Color(0xFFF59E0B), start, 360f * 0.15f, false, style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round))
                                start += 360f * 0.15f

                                // IT %16 (0xFF06B6D4)
                                drawArc(Color(0xFF06B6D4), start, 360f * 0.16f, false, style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round))
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Total", fontSize = 9.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                                Text("162", fontSize = 16.sp, color = Color(0xFF0F172A), fontWeight = FontWeight.Black)
                            }
                        }

                        // Compact legends
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            DepartmentLegendItem("Finance", "45", "(28%)", Color(0xFF10B981))
                            DepartmentLegendItem("Operations", "38", "(23%)", Color(0xFF2563EB))
                            DepartmentLegendItem("HR", "29", "(18%)", Color(0xFF8B5CF6))
                            DepartmentLegendItem("Admin", "24", "(15%)", Color(0xFFF59E0B))
                            DepartmentLegendItem("IT", "26", "(16%)", Color(0xFF06B6D4))
                        }
                    }
                }

                // Column 2: Task Completion Trend Column/Bar chart
                Card(
                    modifier = Modifier
                        .weight(0.9f)
                        .height(240.dp)
                        .shadow(0.5.dp, RoundedCornerShape(16.dp), ambientColor = Color.LightGray),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFECEFEF)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Completion Trend",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        val completionPercentageStr = remember(allTasksList) {
                            if (allTasksList.isNotEmpty()) {
                                "${(allTasksList.count { it.isCompleted }.toFloat() / allTasksList.size * 100).toInt()}%"
                            } else {
                                "0%"
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = completionPercentageStr,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF0F172A)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFD1FAE5))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(if (allTasksList.isNotEmpty()) "Active" else "Empty", fontSize = 8.sp, color = Color(0xFF065F46), fontWeight = FontWeight.Bold)
                            }
                        }

                        Text("productivity value", fontSize = 8.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)

                        Spacer(modifier = Modifier.height(16.dp))

                        // Drawing Completion trend vertical bar charts
                        val trendBars = remember(allTasksList) {
                            val completedCounts = FloatArray(7) { 0f }
                            val totalCounts = FloatArray(7) { 0f }
                            allTasksList.forEach { task ->
                                val dayIdx = ((task.createdAt / (1000 * 60 * 60 * 24)) % 7).toInt()
                                totalCounts[dayIdx] += 1f
                                if (task.isCompleted) {
                                    completedCounts[dayIdx] += 1f
                                }
                            }
                            List(7) { i ->
                                if (totalCounts[i] > 0f) {
                                    completedCounts[i] / totalCounts[i]
                                } else {
                                    0f
                                }
                            }
                        }
                        val barDays = listOf("M", "T", "W", "T", "F", "S", "S")

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .padding(horizontal = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            trendBars.forEachIndexed { i, barVal ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Vertical Bar column container
                                    Box(
                                        modifier = Modifier
                                            .width(10.dp)
                                            .height(84.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFEFF6FF)),
                                        contentAlignment = Alignment.BottomCenter
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .fillMaxHeight(barVal)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    brush = Brush.verticalGradient(
                                                        colors = listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8))
                                                    )
                                                )
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = barDays[i],
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Performance Heatmap - Activity matrix
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .shadow(1.dp, RoundedCornerShape(16.dp), ambientColor = Color.LightGray),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFECEFEF)),
                shape = RoundedCornerShape(16.dp)
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
                            Text(
                                text = "Performance Heatmap",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Performance guidelines info",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(12.dp)
                            )
                        }

                        // Minimal timeframe info
                        Text(
                            text = "Last 5 Weeks",
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Generating 7 rows (Mon-Sun) vs 5 columns (Week 1-5) heatmap grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Mon-Sun Indicators column
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.width(32.dp)
                        ) {
                            listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                                Text(
                                    text = day,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF64748B),
                                    modifier = Modifier.height(18.dp)
                                )
                            }
                        }

                        // Heatmap grid (7 rows, 5 columns)
                        val heatmapGridValues = remember(allTasksList) {
                            val grid = Array(7) { IntArray(5) { 0 } }
                            if (allTasksList.isNotEmpty()) {
                                val now = System.currentTimeMillis()
                                allTasksList.forEach { task ->
                                    val diffMs = now - task.createdAt
                                    val diffWeeks = (diffMs / (7L * 24 * 60 * 60 * 1000)).toInt()
                                    val w = (4 - diffWeeks).coerceIn(0, 4)
                                    val d = ((task.createdAt / (24L * 60 * 60 * 1000)) % 7).toInt()
                                    grid[d][w] = (grid[d][w] + 1).coerceAtMost(4)
                                }
                            }
                            List(7) { d ->
                                List(5) { w ->
                                    grid[d][w]
                                }
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            // Column representing each week 1 to 5
                            for (w in 0..4) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    for (d in 0..6) {
                                        val intensity = heatmapGridValues[d][w]
                                        // Colors mapping representing minty green gradient shades
                                        val boxColor = when (intensity) {
                                            0 -> Color(0xFFF8FAFC) // Very Light/Gray
                                            1 -> Color(0xFFD1FAE5) // Mint shade 1
                                            2 -> Color(0xFFA7F3D0) // Mint shade 2
                                            3 -> Color(0xFF34D399) // Mint shade 3
                                            else -> Color(0xFF10B981) // High Density Green
                                        }

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(18.dp)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(boxColor)
                                                .clickable {
                                                    triggerLocalToast("Week ${w + 1} Activity level: $intensity/4 checked.")
                                                }
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = "Week ${w + 1}",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        // Right-side color range legend scale
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.width(36.dp)
                        ) {
                            Text("Low", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                            Spacer(modifier = Modifier.height(4.dp))
                            // Simple continuous gradient block box
                            Box(
                                modifier = Modifier
                                    .width(12.dp)
                                    .height(98.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(Color(0xFFF8FAFC), Color(0xFFD1FAE5), Color(0xFF10B981))
                                        )
                                    )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("High", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Employee Scorecards scroll list
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Top Employee Scorecards",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )

                    Text(
                        text = "View All",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF2563EB),
                        modifier = Modifier
                            .clickable { onViewPerformance() }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scroll row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    EmployeeScorecardItem(
                        name = "Chaitra Madam",
                        dept = "Operations",
                        avatar = "👩",
                        percentage = "96.4%",
                        growth = "↑ 14.2%",
                        linePoints = listOf(0.4f, 0.5f, 0.45f, 0.6f, 0.72f, 0.9f),
                        onClick = { onViewPerformance() }
                    )

                    EmployeeScorecardItem(
                        name = "Kiran Madam",
                        dept = "Finance",
                        avatar = "👩‍🏫",
                        percentage = "93.1%",
                        growth = "↑ 11.3%",
                        linePoints = listOf(0.3f, 0.42f, 0.55f, 0.5f, 0.68f, 0.85f),
                        onClick = { onViewPerformance() }
                    )

                    EmployeeScorecardItem(
                        name = "Amruta Madam",
                        dept = "Admin",
                        avatar = "👩‍⚕️",
                        percentage = "90.7%",
                        growth = "↑ 9.8%",
                        linePoints = listOf(0.5f, 0.45f, 0.62f, 0.58f, 0.71f, 0.8f),
                        onClick = { onViewPerformance() }
                    )

                    val adminPct = if (allTasksList.isNotEmpty()) {
                        "${(allTasksList.count { it.isCompleted }.toFloat() / allTasksList.size * 100).toInt()}%"
                    } else {
                        "0%"
                    }
                    val adminGrowth = if (allTasksList.isNotEmpty()) "Active" else "0%"
                    
                    EmployeeScorecardItem(
                        name = "Bhavesh (You)",
                        dept = "IT Admin",
                        avatar = "👨‍💻",
                        percentage = adminPct,
                        growth = adminGrowth,
                        linePoints = if (allTasksList.isNotEmpty()) solvedPoints else listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f),
                        onClick = { onViewPerformance() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }

        // Action Toast alert overlay
        AnimatedVisibility(
            visible = showToast,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 22.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .shadow(6.dp, RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success tick",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = toastMessage,
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun MetricCardSummary(
    title: String,
    value: String,
    subtext: String,
    tint: Color,
    icon: ImageVector,
    backgroundColor: Color
) {
    Card(
        modifier = Modifier
            .width(136.dp)
            .shadow(0.5.dp, RoundedCornerShape(16.dp), ambientColor = Color.LightGray),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFECEFEF)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF0F172A),
                letterSpacing = (-0.5).sp
            )

            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF64748B)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtext,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = if (subtext.contains("↑") && !title.lowercase().contains("pending")) Color(0xFF059669) else if (subtext.contains("↓") && title.lowercase().contains("pending")) Color(0xFF059669) else Color(0xFFD97706)
            )
        }
    }
}

@Composable
fun DepartmentLegendItem(
    name: String,
    count: String,
    pct: String,
    bulletColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(bulletColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = name,
                fontSize = 9.sp,
                color = Color(0xFF475569),
                fontWeight = FontWeight.SemiBold
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = count,
                fontSize = 9.sp,
                color = Color(0xFF0F172A),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = pct,
                fontSize = 8.sp,
                color = Color(0xFF94A3B8),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun EmployeeScorecardItem(
    name: String,
    dept: String,
    avatar: String,
    percentage: String,
    growth: String,
    linePoints: List<Float>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(170.dp)
            .clickable { onClick() }
            .shadow(0.5.dp, RoundedCornerShape(16.dp), ambientColor = Color.LightGray),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF1F5F9)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = avatar, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = name,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = dept,
                        fontSize = 8.sp,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = percentage,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = growth,
                        fontSize = 8.sp,
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.Bold
                    )
                }

                // Smooth Sparkline Graph on Card inside score cards
                Box(
                    modifier = Modifier
                        .width(70.dp)
                        .height(32.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val step = w / (linePoints.size - 1)
                        val path = Path().apply {
                            moveTo(0f, h - linePoints[0] * h)
                            for (j in 1 until linePoints.size) {
                                lineTo(j * step, h - linePoints[j] * h)
                            }
                        }

                        drawPath(
                            path = path,
                            color = Color(0xFF10B981),
                            style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }
            }
        }
    }
}
