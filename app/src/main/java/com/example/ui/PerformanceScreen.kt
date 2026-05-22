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
import androidx.compose.ui.draw.rotate
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

data class LeaderboardUser(
    val rank: Int,
    val name: String,
    val team: String,
    val score: Double,
    val growth: String,
    val avatar: String,
    val isYou: Boolean = false
)

data class AchievementBadge(
    val name: String,
    val desc: String,
    val date: String,
    val iconEmoji: String,
    val ringColor: Color,
    val containerColor: Color
)

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PerformanceScreen(
    allTasksList: List<Task> = emptyList(),
    stats: DashboardStats = DashboardStats(0, 0, 0, 100),
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedLeaderboardFilter by remember { mutableStateOf("Today") }
    var showFilterMenu by remember { mutableStateOf(false) }

    // Interactivity: simulated states & alerts
    var showBadgeCelebration by remember { mutableStateOf<AchievementBadge?>(null) }
    var userStreakState by remember { mutableStateOf(7) }

    // Action Toast alert overlay state
    var toastMessage by remember { mutableStateOf("") }
    var showToast by remember { mutableStateOf(false) }

    fun triggerLocalToast(message: String) {
        toastMessage = message
        showToast = true
    }

    LaunchedEffect(showToast) {
        if (showToast) {
            delay(2200)
            showToast = false
        }
    }

    // Dynamic scale lists mimicking UI
    val achievements = remember {
        listOf(
            AchievementBadge(
                name = "On-Time Queen",
                desc = "Completed 20 tasks on time",
                date = "21 May 2024",
                iconEmoji = "👑",
                ringColor = Color(0xFF10B981),
                containerColor = Color(0xFFECFDF5)
            ),
            AchievementBadge(
                name = "Perfect Day",
                desc = "100% completion in a day",
                date = "19 May 2024",
                iconEmoji = "🎯",
                ringColor = Color(0xFF3B82F6),
                containerColor = Color(0xFFEFF6FF)
            ),
            AchievementBadge(
                name = "High Performer",
                desc = "Scored 90+ points",
                date = "19 May 2024",
                iconEmoji = "⭐",
                ringColor = Color(0xFFF59E0B),
                containerColor = Color(0xFFFFFBEB)
            ),
            AchievementBadge(
                name = "Streak Master",
                desc = "7 days performance streak",
                date = "17 May 2024",
                iconEmoji = "🔥",
                ringColor = Color(0xFF8B5CF6),
                containerColor = Color(0xFFF5F3FF)
            )
        )
    }

    val leaderboard = remember(allTasksList, stats) {
        val userScore = if (allTasksList.isNotEmpty()) stats.cleanlinessScore.toDouble() else 0.0
        val tempUsers = listOf(
            LeaderboardUser(0, "Chaitra Madam", "Operations Team", 96.4, "↑ 8.7", "👩"),
            LeaderboardUser(0, "Kiran Madam", "Finance Team", 93.1, "↑ 6.2", "👩‍🏫"),
            LeaderboardUser(0, "Amruta Madam", "Admin Team", 90.7, "↑ 5.1", "👩‍⚕️"),
            LeaderboardUser(0, "Aditya Sir", "IT Team", 88.2, "↑ 4.3", "👨"),
            LeaderboardUser(0, "Neha Madam", "HR Team", 85.6, "↑ 3.8", "👩"),
            LeaderboardUser(0, "Bhavesh Patil (You)", "IT Admin Team", userScore, if (userScore > 0) "↑ " + String.format("%.1f", userScore / 10) else "—", "👨‍💻", isYou = true)
        ).sortedByDescending { it.score }
        
        tempUsers.mapIndexed { idx, player ->
            player.copy(rank = idx + 1)
        }
    }

    // Performance bar glow pulsing animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_glow")
    val barGlowScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    val isDark = MaterialTheme.colorScheme.background != Color(0xFFF8FAFC)
    val bgScreen = if (isDark) Color(0xFF0A0F1D) else Color(0xFFFCFDFD)
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
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                // Header (Back button, Title, Share button)
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
                            .testTag("perf_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back to work",
                            tint = textPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "My Performance",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = textPrimary,
                            letterSpacing = (-0.5).sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Track • Improve • Achieve",
                            style = MaterialTheme.typography.bodySmall,
                            color = textSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = {
                            triggerLocalToast("Performance report shared to Slack/Teams channel!")
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                            .testTag("perf_share_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share corporate profile performance analytics",
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Premium Gradient Cosmic Performance Panel Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(20.dp), ambientColor = Color(0xFF1E3A8A)),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color(0xFF1E3A8A), Color(0xFF0D1B2A))
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                 // Avatar circle with border
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF93C5FD))
                                        .border(2.dp, Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "👨‍💻", fontSize = 32.sp)
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column {
                                    Text(
                                        text = "Bhavesh Patil",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "IT Admin Team",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF93C5FD),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    // Soft badge
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(32.dp))
                                            .background(Color(0xFFFFB703).copy(alpha = 0.2f))
                                            .border(1.dp, Color(0xFFFFB703), RoundedCornerShape(32.dp))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = Color(0xFFFFB703),
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Top Performer",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 8.sp,
                                            color = Color(0xFFFFB703)
                                        )
                                    }
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Workspace Score",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF93C5FD)
                                )
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = stats.cleanlinessScore.toString(),
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White,
                                        letterSpacing = (-1).sp
                                    )
                                    Text(
                                        text = "/100",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF93C5FD).copy(alpha = 0.8f),
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                }
                                Text(
                                    text = "${stats.total} Active task entries tracked",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF10B981)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Progress linear gauge index towards milestone target
                        Box(
                            modifier = Modifier.fillMaxWidth().height(16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            // Track
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.15f))
                            )

                            // Fill
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.964f)
                                    .height(6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(Color(0xFF34D399), Color(0xFF10B981))
                                        )
                                    )
                            )

                            // Interactive Star target dot on the filled progress bar
                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(end = 4.dp)
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFB703))
                                    .clickable {
                                        triggerLocalToast("Milestone target checked! Reach 100 for premium rewards.")
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Milestone reach icon",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("0", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                            Text("50", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                            Text("75", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                            Text("100", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFB703))
                        }
                    }
                }

                    Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val onTimeCount = remember(allTasksList) {
                        allTasksList.count { it.isCompleted && !it.dueDate.equals("Yesterday", ignoreCase = true) }
                    }
                    val delayedCount = remember(allTasksList) {
                        allTasksList.count { !it.isCompleted && (it.dueDate.equals("Yesterday", ignoreCase = true) || it.dueDate.contains("Yesterday", ignoreCase = true)) }
                    }
                    val currentRateVal = if (stats.total > 0) (stats.completed * 100 / stats.total) else 0

                    MetricCardItem(
                        title = "Completed",
                        value = stats.completed.toString(),
                        percentageText = if (stats.completed > 0) "Tasks resolved" else "No completed tasks",
                        percentageColor = Color(0xFF10B981),
                        icon = Icons.Default.CheckCircle,
                        iconTint = Color(0xFF10B981),
                        bgColor = Color(0xFFEFFBFA)
                    )

                    MetricCardItem(
                        title = "On Time",
                        value = onTimeCount.toString(),
                        percentageText = if (stats.completed > 0) String.format("%.1f%%", (onTimeCount.toFloat() / stats.completed * 100)) else "0.0%",
                        percentageColor = Color(0xFF2563EB),
                        icon = Icons.Default.DateRange,
                        iconTint = Color(0xFF2563EB),
                        bgColor = Color(0xFFEFF6FF)
                    )

                    MetricCardItem(
                        title = "Delayed",
                        value = delayedCount.toString(),
                        percentageText = if (stats.total > 0) String.format("%.1f%%", (delayedCount.toFloat() / stats.total * 100)) else "0.0%",
                        percentageColor = Color(0xFFEF4444),
                        icon = Icons.Default.Refresh,
                        iconTint = Color(0xFFF59E0B),
                        bgColor = Color(0xFFFFFBEB)
                    )

                    MetricCardItem(
                        title = "Completion Rate",
                        value = "$currentRateVal%",
                        percentageText = if (currentRateVal > 50) "Excellent progress!" else "Needs action momentum",
                        percentageColor = Color(0xFF10B981),
                        icon = Icons.Default.Star,
                        iconTint = Color(0xFF8B5CF6),
                        bgColor = Color(0xFFF5F3FF)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Achievements List Row Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Achievements",
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
                            .clickable { triggerLocalToast("Showing 8 unlocked achievements list.") }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Horizontal scroll achievements grid cards
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    achievements.forEach { achievement ->
                        Card(
                            modifier = Modifier
                                .width(128.dp)
                                .shadow(0.5.dp, RoundedCornerShape(16.dp), ambientColor = Color.LightGray)
                                .clickable { showBadgeCelebration = achievement },
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFECEFEF)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                // Octagon shaped badge visual representation
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(achievement.containerColor)
                                        .border(2.dp, achievement.ringColor, RoundedCornerShape(14.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = achievement.iconEmoji, fontSize = 24.sp)
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = achievement.name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A),
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Text(
                                    text = achievement.desc,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF64748B),
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.height(24.dp)
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = achievement.date,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Leaderboard List Header Layout
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Leaderboard",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )

                    // Daily filters
                    Box {
                        Button(
                            onClick = { showFilterMenu = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFF334155)
                            ),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(selectedLeaderboardFilter, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(3.dp))
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(14.dp))
                            }
                        }
                        DropdownMenu(
                            expanded = showFilterMenu,
                            onDismissRequest = { showFilterMenu = false },
                            scrollState = rememberScrollState()
                        ) {
                            listOf("Today", "This Week", "This Month", "All-Time Records").forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    onClick = {
                                        selectedLeaderboardFilter = item
                                        showFilterMenu = false
                                        triggerLocalToast("Leaderboard scale rearranged: $item")
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Leaderboard List Column
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    leaderboard.forEach { user ->
                        val isHighlighted = user.name == "Chaitra Madam"
                        val customBackground = if (isHighlighted) {
                            Color(0xFFFFFBEB) // Light gold banner for No 1
                        } else {
                            Color.White
                        }
                        val customBorder = if (isHighlighted) {
                            BorderStroke(1.dp, Color(0xFFFDE68A))
                        } else {
                            BorderStroke(1.dp, Color(0xFFF1F5F9))
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(0.5.dp, RoundedCornerShape(12.dp), ambientColor = Color.LightGray)
                                .clickable {
                                    triggerLocalToast("${user.name} occupies spot #${user.rank} with a rating of ${user.score}")
                                },
                            colors = CardDefaults.cardColors(containerColor = customBackground),
                            border = customBorder,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Rank icon/crown representation index
                                Box(
                                    modifier = Modifier.width(36.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    when (user.rank) {
                                        1 -> Text("👑", fontSize = 20.sp)
                                        2 -> Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFE2E8F0)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("2", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF475569))
                                        }
                                        3 -> Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFFFEDD5)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("3", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFFC2410C))
                                        }
                                        else -> Text(
                                            text = "${user.rank}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Avatar emoji bubble
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFF1F5F9)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = user.avatar, fontSize = 20.sp)
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = user.name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = user.team,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF64748B)
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${user.score}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = user.growth,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF10B981)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Interactive Bottom streak and encouragement footer panel
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(1.dp, RoundedCornerShape(16.dp), ambientColor = Color.LightGray),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left-side Trophy Icon Illustration
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFEFF6FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🏆", fontSize = 32.sp)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Keep it up, Bhavesh! 🎉",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "You're in the top 1% of performers. Maintain your streak and achieve more!",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF64748B),
                                maxLines = 3
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // High fidelity circle gauge showing 7 days streak
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clickable {
                                    userStreakState++
                                    triggerLocalToast("Streak status updated! ${userStreakState} Active Days 🔥")
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                // Background circle
                                drawArc(
                                    color = Color(0xFFF1F5F9),
                                    startAngle = -90f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                                )

                                // Filled orange/coral accent gauge
                                val fillPercentage = (userStreakState.toFloat() / 10f).coerceIn(0f, 1f)
                                drawArc(
                                    color = Color(0xFF10B981),
                                    startAngle = -90f,
                                    sweepAngle = fillPercentage * 360f,
                                    useCenter = false,
                                    style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$userStreakState",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF10B981),
                                    lineHeight = 16.sp
                                )
                                Text(
                                    text = "Days Streak",
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF64748B),
                                    lineHeight = 8.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(56.dp))
            }

            // High Fidelity Flow Toast alerts overlay
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
                            imageVector = Icons.Default.Check,
                            contentDescription = "Gamified trigger alert ticker overlay",
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

    // Badge details modal popup dialog
    showBadgeCelebration?.let { badge ->
        Dialog(onDismissRequest = { showBadgeCelebration = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .shadow(8.dp, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(badge.containerColor)
                            .border(3.dp, badge.ringColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = badge.iconEmoji, fontSize = 40.sp)
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Achievement Unlocked!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF8B5CF6)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = badge.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0F172A)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = badge.desc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Unlocked on ${badge.date}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { showBadgeCelebration = null },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0F172A),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Text("Awesome!", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCardItem(
    title: String,
    value: String,
    percentageText: String,
    percentageColor: Color,
    icon: ImageVector,
    iconTint: Color,
    bgColor: Color
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
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
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
                text = percentageText,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = percentageColor
            )
        }
    }
}
