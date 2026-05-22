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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
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

enum class WorkflowStageStatus {
    COMPLETED,
    IN_PROGRESS,
    PENDING
}

data class WorkflowStage(
    val id: String,
    val name: String,
    val displayName: String,
    val status: WorkflowStageStatus,
    val avatarEmoji: String,
    val description: String,
    val dateString: String = "Pending",
    val commentCount: Int = 0,
    val isYou: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkflowTrackingScreen(
    task: Task,
    onDismiss: () -> Unit,
    onTransferComplete: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Status / Stages list state, enabling interactive "Transfer Now" progression simulation
    var currentStageIndex by remember { mutableStateOf(2) } // Starts at "Kiran Madam" (In Progress)
    
    // Transfer Animations State
    var isTransferring by remember { mutableStateOf(false) }
    var transferAnimationProgress by remember { mutableStateOf(0f) }

    // State for comments dialog review modal representation
    var showCommentsDialogForStage by remember { mutableStateOf<WorkflowStage?>(null) }
    var newCommentTextState by remember { mutableStateOf("") }

    // Toast configuration state
    var toastMessage by remember { mutableStateOf("") }
    var showToast by remember { mutableStateOf(false) }

    fun triggerToast(msg: String) {
        toastMessage = msg
        showToast = true
    }

    // Trigger timer to clear toast
    LaunchedEffect(showToast) {
        if (showToast) {
            delay(2500)
            showToast = false
        }
    }

    // Define the dynamic workflow stages list built iteratively from status indices
    val stages = remember(currentStageIndex) {
        listOf(
            WorkflowStage(
                id = "admin",
                name = "Admin",
                displayName = "Admin (You)",
                status = if (currentStageIndex > 0) WorkflowStageStatus.COMPLETED else WorkflowStageStatus.IN_PROGRESS,
                avatarEmoji = "👨‍💻",
                description = "Task created and assigned",
                dateString = "21 May, 09:10 AM",
                commentCount = 2,
                isYou = true
            ),
            WorkflowStage(
                id = "chaitra",
                name = "Chaitra",
                displayName = "Chaitra Madam",
                status = when {
                    currentStageIndex > 1 -> WorkflowStageStatus.COMPLETED
                    currentStageIndex == 1 -> WorkflowStageStatus.IN_PROGRESS
                    else -> WorkflowStageStatus.PENDING
                },
                avatarEmoji = "👩",
                description = "Updated task details and added notes",
                dateString = "21 May, 09:20 AM",
                commentCount = 3
            ),
            WorkflowStage(
                id = "kiran",
                name = "Kiran Madam",
                displayName = "Kiran Madam",
                status = when {
                    currentStageIndex > 2 -> WorkflowStageStatus.COMPLETED
                    currentStageIndex == 2 -> WorkflowStageStatus.IN_PROGRESS
                    else -> WorkflowStageStatus.PENDING
                },
                avatarEmoji = "👩‍🏫",
                description = if (currentStageIndex > 2) "Verified structures & escalated logs" else "Working on the task. Making progress...",
                dateString = if (currentStageIndex > 2) "21 May, 09:35 AM" else "Actively monitoring",
                commentCount = 1
            ),
            WorkflowStage(
                id = "amruta",
                name = "Amruta Madam",
                displayName = "Amruta Madam",
                status = when {
                    currentStageIndex > 3 -> WorkflowStageStatus.COMPLETED
                    currentStageIndex == 3 -> WorkflowStageStatus.IN_PROGRESS
                    else -> WorkflowStageStatus.PENDING
                },
                avatarEmoji = "👩‍⚕️",
                description = if (currentStageIndex > 3) "Approved financial requirements" else if (currentStageIndex == 3) "Reviewing resource allocation requirements..." else "Waiting for update from previous stage",
                dateString = if (currentStageIndex > 3) "Today, 10:15 AM" else if (currentStageIndex == 3) "Just started" else "Pending",
                commentCount = 0
            ),
            WorkflowStage(
                id = "approval",
                name = "Final Approval",
                displayName = "Final Approval",
                status = when {
                    currentStageIndex > 4 -> WorkflowStageStatus.COMPLETED
                    currentStageIndex == 4 -> WorkflowStageStatus.IN_PROGRESS
                    else -> WorkflowStageStatus.PENDING
                },
                avatarEmoji = "🛡️",
                description = if (currentStageIndex > 4) "All approvals verified! Complete ✅" else "Final review and approval from Admin",
                dateString = if (currentStageIndex > 4) "Just Now" else "Pending",
                commentCount = 0
            )
        )
    }

    // Task transfer animation simulating SaaS progression logs
    val transferRotation = rememberInfiniteTransition(label = "transfer_spin")
    val transferSpinAngle by transferRotation.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin"
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header (Back button, Title, Action index)
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
                            .testTag("workflow_back_btn")
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
                            text = "Workflow Tracking",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = textPrimary,
                            letterSpacing = (-0.5).sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Monitor task progress across all stages",
                            style = MaterialTheme.typography.bodySmall,
                            color = textSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    IconButton(
                        onClick = {
                            triggerToast("Reset tracking state to Chaitra Madam phase.")
                            currentStageIndex = 1
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(cardBg)
                            .border(1.dp, borderLight, RoundedCornerShape(12.dp))
                            .testTag("workflow_reset_state_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Restart workflow timeline pipeline status",
                            tint = if (isDark) Color(0xFF60A5FA) else Color(0xFF2563EB),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Task Information Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(if (isDark) 0.dp else 1.dp, RoundedCornerShape(16.dp), ambientColor = Color.LightGray),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, borderLight)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (isDark) Color(0xFF1E3A8A).copy(alpha = 0.4f) else Color(0xFFEFF6FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🏛️", fontSize = 18.sp)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${task.category} • Created or Assigned Today",
                                style = MaterialTheme.typography.bodySmall,
                                color = textSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isDark) Color(0xFFEF4444).copy(alpha = 0.2f) else Color(0xFFFEE2E2))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "High Priority",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color(0xFFFCA5A5) else Color(0xFFEF4444)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Top Horizontal Pipeline progress stages with connector lines and check indicator stages
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    stages.forEachIndexed { index, stage ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                // Simple Line Connector
                                val isPastActive = index <= currentStageIndex
                                val isPassed = index < currentStageIndex

                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isPassed -> Color(0xFF10B981) // Green Completed
                                                index == currentStageIndex -> Color(0xFF2563EB) // Blue In Progress
                                                else -> Color(0xFFE2E8F0) // Gray Pending
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isPassed) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Completed stage",
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    } else if (index == currentStageIndex) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(Color.White)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = stage.name,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (index <= currentStageIndex) Color(0xFF1E293B) else Color(0xFF94A3B8),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = when (stage.status) {
                                    WorkflowStageStatus.COMPLETED -> "Completed"
                                    WorkflowStageStatus.IN_PROGRESS -> "In Progress"
                                    WorkflowStageStatus.PENDING -> "Pending"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Medium,
                                color = when (stage.status) {
                                    WorkflowStageStatus.COMPLETED -> Color(0xFF059669)
                                    WorkflowStageStatus.IN_PROGRESS -> Color(0xFF2563EB)
                                    WorkflowStageStatus.PENDING -> Color(0xFF64748B)
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Timeline Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Workflow Timeline",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0F172A)
                    )

                    Button(
                        onClick = { triggerToast("Displaying all enterprise graph dependency mapping structures.") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEFF6FF),
                            contentColor = Color(0xFF2563EB)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp).testTag("workflow_view_dependencies_btn")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "SaaS dependency flowchart icon",
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("View Dependencies", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Vertical timeline nodes list
                Column(modifier = Modifier.fillMaxWidth()) {
                    stages.forEachIndexed { index, stage ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            // Timeline visual line connector column with icons
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(32.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (stage.status) {
                                                WorkflowStageStatus.COMPLETED -> Color(0xFF10B981)
                                                WorkflowStageStatus.IN_PROGRESS -> Color(0xFF2563EB)
                                                WorkflowStageStatus.PENDING -> Color(0xFFF1F5F9)
                                            }
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = when (stage.status) {
                                                WorkflowStageStatus.COMPLETED -> Color(0xFF059669)
                                                WorkflowStageStatus.IN_PROGRESS -> Color(0xFF1D4ED8)
                                                WorkflowStageStatus.PENDING -> Color(0xFFCBD5E1)
                                            },
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (stage.status) {
                                            WorkflowStageStatus.COMPLETED -> Icons.Default.Check
                                            WorkflowStageStatus.IN_PROGRESS -> Icons.Default.Star
                                            WorkflowStageStatus.PENDING -> Icons.Default.Lock
                                        },
                                        contentDescription = "Step checkpoint",
                                        tint = if (stage.status == WorkflowStageStatus.PENDING) Color(0xFF64748B) else Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }

                                if (index < stages.size - 1) {
                                    Box(
                                        modifier = Modifier
                                            .width(2.dp)
                                            .height(84.dp)
                                            .background(
                                                if (index < currentStageIndex) Color(0xFF10B981) else Color(0xFFE2E8F0)
                                            )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Timeline detailed content card representing individuals and tasks
                            val activeBorder = if (stage.status == WorkflowStageStatus.IN_PROGRESS) {
                                BorderStroke(1.5.dp, Color(0xFF2563EB))
                            } else {
                                BorderStroke(1.dp, Color(0xFFE2E8F0))
                            }

                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .shadow(
                                        elevation = if (stage.status == WorkflowStageStatus.IN_PROGRESS) 4.dp else 0.dp,
                                        shape = RoundedCornerShape(16.dp),
                                        ambientColor = Color(0xFF2563EB)
                                    ),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = activeBorder,
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Custom user profile avatars
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFF1F5F9))
                                            .border(1.dp, Color(0xFFE2E8F0), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = stage.avatarEmoji, fontSize = 20.sp)
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = stage.displayName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0F172A)
                                            )
                                            if (stage.status == WorkflowStageStatus.COMPLETED) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = "Verified checkpoint green",
                                                    tint = Color(0xFF10B981),
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(3.dp))

                                        Text(
                                            text = stage.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF475569)
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DateRange,
                                                contentDescription = "Clock timeline trace",
                                                tint = Color(0xFF94A3B8),
                                                modifier = Modifier.size(10.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = stage.dateString,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF64748B),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    Column(horizontalAlignment = Alignment.End) {
                                        // Completion Badges Text indicator status capsule
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(
                                                    when (stage.status) {
                                                        WorkflowStageStatus.COMPLETED -> Color(0xFFD1FAE5)
                                                        WorkflowStageStatus.IN_PROGRESS -> Color(0xFFDBEAFE)
                                                        WorkflowStageStatus.PENDING -> Color(0xFFF1F5F9)
                                                    }
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = when (stage.status) {
                                                    WorkflowStageStatus.COMPLETED -> "Completed"
                                                    WorkflowStageStatus.IN_PROGRESS -> "In Progress"
                                                    WorkflowStageStatus.PENDING -> "Pending"
                                                },
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 8.sp,
                                                color = when (stage.status) {
                                                    WorkflowStageStatus.COMPLETED -> Color(0xFF065F46)
                                                    WorkflowStageStatus.IN_PROGRESS -> Color(0xFF1E40AF)
                                                    WorkflowStageStatus.PENDING -> Color(0xFF64748B)
                                                }
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Collaboration message list trigger bubble indicator
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFFEFF6FF))
                                                .border(1.dp, Color(0xFFDBEAFE), RoundedCornerShape(8.dp))
                                                .clickable {
                                                    showCommentsDialogForStage = stage
                                                }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Info,
                                                    contentDescription = "Comments trace count",
                                                    tint = Color(0xFF2563EB),
                                                    modifier = Modifier.size(10.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "${stage.commentCount}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Black,
                                                    color = Color(0xFF1E40AF)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Interactive Quick Transfer Card (Notion + Monday inspired design)
                val hasMoreStages = currentStageIndex < stages.size - 1
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(1.dp, RoundedCornerShape(16.dp), ambientColor = Color.LightGray),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFEFF6FF)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Task Transfer Animation Wheel
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Transfer indicator loop spinner animation",
                                    tint = if (isTransferring) Color(0xFF10B981) else Color(0xFF2563EB),
                                    modifier = Modifier
                                        .size(24.dp)
                                        .then(
                                            if (isTransferring) {
                                                Modifier.rotate(transferSpinAngle)
                                            } else {
                                                Modifier
                                            }
                                        )
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Quick Transfer Stage",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = if (hasMoreStages) {
                                            "Transfer this task to ${stages[currentStageIndex + 1].displayName}"
                                        } else {
                                            "All stages processed successfully."
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    if (hasMoreStages) {
                                        isTransferring = true
                                        coroutineScope.launch {
                                            delay(1500) // Beautiful transition animation timer delay
                                            isTransferring = false
                                            val nextIndex = currentStageIndex + 1
                                            currentStageIndex = nextIndex
                                            triggerToast("Task successfully transfered to ${stages[nextIndex].displayName}! 🚀")
                                            onTransferComplete(stages[nextIndex].name)
                                        }
                                    } else {
                                        triggerToast("Task already reached final stage! Re-initializing pipeline.")
                                        currentStageIndex = 0
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2563EB),
                                    contentColor = Color.White
                                ),
                                enabled = !isTransferring,
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("workflow_transfer_now_btn")
                            ) {
                                Text(
                                    text = if (isTransferring) "Processing..." else "Transfer Now >",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Bottom actions (Notion / Monday Style bar tabs)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .clickable { triggerToast("Add custom progress trace info logged!") }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add progress log note",
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Add Update",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E3A8A)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2563EB))
                            .clickable {
                                if (hasMoreStages) {
                                    currentStageIndex++
                                    triggerToast("Manually shifted current forward trace ✅")
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Interactive workflow trigger button indicator",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .clickable { triggerToast("Viewing all general audit logs.") }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "General task communications notes view",
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "View Comments",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E3A8A)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Collaboration feedback footer checklist summary
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    // Minimized stacked visual avatars representation
                    Row(horizontalArrangement = Arrangement.spacedBy((-6).dp)) {
                        listOf("👩", "👨‍💻", "👩‍⚕️").forEach { emoji ->
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(1.dp, Color(0xFFE2E8F0), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = emoji, fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "3 people collaborating on this task",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF475569),
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    // Green live indicator sync dot
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "Real-time sync active",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.Black,
                        fontSize = 8.sp
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))
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
                            contentDescription = "SaaS notification info toast confirmation",
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

    // High Fidelity Comments Review dialog modal
    showCommentsDialogForStage?.let { stage ->
        Dialog(onDismissRequest = { showCommentsDialogForStage = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = stage.avatarEmoji, fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "${stage.displayName} Updates",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "SLA checkpoint verification records",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Workflow Stage Action Description:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF475569)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = stage.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF0F172A),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "History & Stage Logs (${stage.commentCount}):",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF475569)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Pre-seeded lists or new comment logic
                    val defaultComments = listOf(
                        "Structure dimensions successfully verified.",
                        "Ready for next transfer stage allocation check."
                    ).take(stage.commentCount)

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (defaultComments.isEmpty()) {
                            Text(
                                text = "No comments currently posted for this progress stage.",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF64748B),
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp)
                            )
                        } else {
                            defaultComments.forEach { comment ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFEFF6FF), RoundedCornerShape(8.dp))
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Tick bullet point comment tracker info icon element",
                                        tint = Color(0xFF3B82F6),
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = comment,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF1E3A8A),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = newCommentTextState,
                        onValueChange = { newCommentTextState = it },
                        placeholder = { Text("Write additional notes info...", fontSize = 12.sp) },
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                        modifier = Modifier.fillMaxWidth().testTag("workflow_add_comment_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TextButton(
                            onClick = { showCommentsDialogForStage = null },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Close", color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                if (newCommentTextState.isNotBlank()) {
                                    newCommentTextState = ""
                                    com.example.MainActivity.triggerCustomToast(context, "Note successfully logged into trace database.")
                                    showCommentsDialogForStage = null
                                } else {
                                    com.example.MainActivity.triggerCustomToast(context, "Please enter commentary text first.")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                            modifier = Modifier.weight(1.2f).testTag("workflow_submit_comment_btn")
                        ) {
                            Text("Post Comment", fontWeight = FontWeight.Black, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
