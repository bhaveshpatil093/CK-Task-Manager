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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
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

data class FileItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val type: String, // "Image", "PDF", "Audio", "Video", "Document", "Others"
    val size: String,
    val dateString: String = "Today, 10:30 AM",
    var progress: Float = 0f,
    var isUploading: Boolean = false,
    var comments: List<String> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileAttachmentScreen(
    task: Task,
    onDismiss: () -> Unit,
    onAttachComplete: (List<FileItem>) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    
    // Dynamic Comment text typed by the user
    var attachmentComment by remember { mutableStateOf("") }

    // Mock Database for simulation
    var recentlyUploadedList by remember {
        mutableStateOf(
            mutableStateListOf(
                FileItem(
                    name = "Agreement_2024.pdf",
                    type = "PDF",
                    size = "1.8 MB",
                    dateString = "Today, 10:30 AM",
                    comments = listOf("Matches final board directives", "Ready for signature")
                ),
                FileItem(
                    name = "Call_Recording.m4a",
                    type = "Audio",
                    size = "3.2 MB",
                    dateString = "Today, 09:15 AM",
                    comments = listOf("Amruta Madam agreed on tomorrow's process")
                ),
                FileItem(
                    name = "Site_Visit_Video.mp4",
                    type = "Video",
                    size = "12.6 MB",
                    dateString = "Yesterday, 04:45 PM",
                    comments = emptyList()
                ),
                FileItem(
                    name = "Whiteboard_Notes.jpg",
                    type = "Image",
                    size = "1.1 MB",
                    dateString = "Yesterday, 03:20 PM",
                    comments = listOf("Architectural structure schema")
                )
            )
        )
    }

    // Active upload queue
    val uploadingList = remember { mutableStateListOf<FileItem>() }

    // Selected temporary files in memory (the "Drag-and-Drop" state queue, showing what the user chose before hitting "Upload & Attach")
    val selectedFilesToUpload = remember { mutableStateListOf<FileItem>() }

    // Simulation dialogue selectors
    var showSimulatorDialog by remember { mutableStateOf(false) }
    var simulatorFilterType by remember { mutableStateOf<String?>(null) }
    var customFileNameInput by remember { mutableStateOf("") }
    var selectedSizeInput by remember { mutableStateOf("1.5 MB") }

    // View Comments dialog modal
    var activeCommentReviewFile by remember { mutableStateOf<FileItem?>(null) }
    var newCommentTextState by remember { mutableStateOf("") }

    // Success Alerts Toast
    var toastMessage by remember { mutableStateOf("") }
    var showToast by remember { mutableStateOf(false) }

    fun triggerToast(msg: String) {
        toastMessage = msg
        showToast = true
    }

    // Quick Select Pre-seeding simulator options
    val sampleSimulatorFiles = remember {
        listOf(
            FileItem(name = "Client_Brief_Screenshot.png", type = "Image", size = "2.4 MB"),
            FileItem(name = "CKTM_Requirement_Specs.pdf", type = "PDF", size = "4.5 MB"),
            FileItem(name = "Board_Meeting_Audio.mp3", type = "Audio", size = "8.2 MB"),
            FileItem(name = "Production_Dry_Run.mp4", type = "Video", size = "24.1 MB"),
            FileItem(name = "Financials_Audit_Spreadsheet.xlsx", type = "Document", size = "1.2 MB"),
            FileItem(name = "Miscellaneous_Backup.zip", type = "Others", size = "55.0 MB")
        )
    }

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
            // Main content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header (Back - Title - History)
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
                            .testTag("file_attachment_back_btn")
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
                            text = "Attachments",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = textPrimary,
                            letterSpacing = (-0.5).sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Add files to this task",
                            style = MaterialTheme.typography.bodySmall,
                            color = textSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Upload history audit action
                    IconButton(
                        onClick = {
                            triggerToast("Cleared virtual upload logs! Total local list reset.")
                            recentlyUploadedList.clear()
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(cardBg)
                            .border(1.dp, borderLight, RoundedCornerShape(12.dp))
                            .testTag("file_history_clear_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Clear upload log history",
                            tint = if (isDark) Color(0xFF60A5FA) else Color(0xFF1E40AF),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Active Task Badge Card Layout
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
                                text = "${task.category} • Assigned Lead Coordinator",
                                style = MaterialTheme.typography.bodySmall,
                                color = textSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }

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
                                fontWeight = FontWeight.Bold,
                                color = when (task.priority.uppercase()) {
                                    "HIGH" -> if (isDark) Color(0xFFFCA5A5) else Color(0xFFEF4444)
                                    "MEDIUM" -> if (isDark) Color(0xFFFDE047) else Color(0xFFD97706)
                                    else -> if (isDark) Color(0xFF86EFAC) else Color(0xFF059669)
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Giant Dotted Drag-and-Drop Area Upload Zone
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isDark) Color(0xFF1E3A8A).copy(alpha = 0.15f) else Color(0xFFEFF6FF).copy(alpha = 0.4f))
                        .drawBehind {
                            val dashedStroke = Stroke(
                                width = 2.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                            )
                            drawRoundRect(
                                color = if (isDark) Color(0xFF3B82F6) else Color(0xFF3B82F6),
                                style = dashedStroke,
                                cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
                            )
                        }
                        .clickable {
                            simulatorFilterType = null
                            showSimulatorDialog = true
                        }
                        .testTag("file_drag_drop_zone"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (isDark) Color(0xFF1E293B) else Color(0xFFDBEAFE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share, // Represents cloud upload / send out
                                contentDescription = "Cloud upload representation",
                                tint = if (isDark) Color(0xFF60A5FA) else Color(0xFF2563EB),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Drag & Drop files here",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "or tap to browse",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDark) Color(0xFF60A5FA) else Color(0xFF2563EB),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Images, PDFs, Videos, Audio, Documents & more",
                            style = MaterialTheme.typography.labelSmall,
                            color = textSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Category Quick selection Row Options (Exactly matches layout categories)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val quickCategories = listOf(
                        Triple("Image", Color(0xFF10B981), Icons.Default.Add),
                        Triple("PDF", Color(0xFFEF4444), Icons.Default.Check),
                        Triple("Audio", Color(0xFF8B5CF6), Icons.Default.Refresh),
                        Triple("Video", Color(0xFF3B82F6), Icons.Default.PlayArrow),
                        Triple("Document", Color(0xFFF59E0B), Icons.Default.CheckCircle),
                        Triple("Others", Color(0xFF64748B), Icons.Default.Search)
                    )

                    quickCategories.forEach { category ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    simulatorFilterType = category.first
                                    showSimulatorDialog = true
                                }
                                .testTag("file_category_pill_${category.first}")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(1.dp, Color(0xFFE2E8F0), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                // Dynamic coloring based on category types
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(category.second.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when(category.first) {
                                            "Image" -> Icons.Default.Add
                                            "PDF" -> Icons.Default.Check
                                            "Audio" -> Icons.Default.Refresh
                                            "Video" -> Icons.Default.PlayArrow
                                            "Document" -> Icons.Default.CheckCircle
                                            else -> Icons.Default.Search
                                        },
                                        contentDescription = category.first,
                                        tint = category.second,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = category.first,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF475569)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Queue preview of files staged to undergo upload
                if (selectedFilesToUpload.isNotEmpty()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Staged to Upload (${selectedFilesToUpload.size})",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color(0xFF1E293B),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Clean All",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFEF4444),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable { selectedFilesToUpload.clear() }
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        selectedFilesToUpload.forEach { file ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // File type label preview icon
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(getFileColor(file.type).copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = getFileEmoji(file.type), fontSize = 16.sp)
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = file.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${file.type} • ${file.size}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF64748B),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    IconButton(
                                        onClick = { selectedFilesToUpload.remove(file) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Remove from staged selection",
                                            tint = Color(0xFF64748B),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                }

                // Uploading progress section (actively executing uploads)
                if (uploadingList.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "Uploading (${uploadingList.size})",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color(0xFF0F172A),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Start)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        uploadingList.forEach { file ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(getFileColor(file.type).copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = getFileEmoji(file.type), fontSize = 16.sp)
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = file.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0F172A),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = file.size,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color(0xFF64748B)
                                            )
                                        }

                                        Text(
                                            text = "${(file.progress * 100).toInt()}%",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF2563EB)
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))

                                        IconButton(
                                            onClick = { uploadingList.remove(file) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Clear,
                                                contentDescription = "Cancel upload",
                                                tint = Color(0xFFEF4444),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Custom visual progress bar
                                    LinearProgressIndicator(
                                        progress = { file.progress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(4.dp)
                                            .clip(CircleShape),
                                        color = Color(0xFF2563EB),
                                        trackColor = Color(0xFFE2E8F0)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }

                // "Recently Uploaded" list History section
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recently Uploaded",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )

                        Text(
                            text = "View All (${recentlyUploadedList.size})",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF2563EB),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable {
                                triggerToast("Displaying all completed task files!")
                            }
                        )
                    }

                    if (recentlyUploadedList.isEmpty()) {
                        // Clean empty state for premium UX empty card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            border = BorderStroke(1.dp, Color(0xFFECEFEF))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "📂",
                                    fontSize = 32.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No files attached yet",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    text = "Tap the cloud box above to stage files.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }
                    } else {
                        recentlyUploadedList.forEach { file ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Thumbnail / File representation
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(getFileColor(file.type).copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = getFileEmoji(file.type),
                                            fontSize = 20.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = file.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${file.type} • ${file.size} • ${file.dateString}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF64748B),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(4.dp))

                                    // Comment bubble badge counts
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFEFF6FF))
                                            .border(1.dp, Color(0xFFDBEAFE), RoundedCornerShape(8.dp))
                                            .clickable {
                                                activeCommentReviewFile = file
                                            }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Home, // standard speech bubble representation
                                                contentDescription = "Review Comments checklist",
                                                tint = Color(0xFF2563EB),
                                                modifier = Modifier.size(10.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "${file.comments.size}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Black,
                                                color = Color(0xFF1E40AF)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    // Context menu options (e.g., delete history item)
                                    IconButton(
                                        onClick = {
                                            recentlyUploadedList.remove(file)
                                            triggerToast("Removed ${file.name} from logs.")
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Remove file item",
                                            tint = Color(0xFFDC2626),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Optional Global Attachment comment card input
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(1.dp, RoundedCornerShape(16.dp), ambientColor = Color.LightGray),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Attachment Comment (Optional)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF344054),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        // Outlined Text Field
                        OutlinedTextField(
                            value = attachmentComment,
                            onValueChange = { attachmentComment = it },
                            placeholder = {
                                Text(
                                    text = "Add a comment description about these files...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF94A3B8)
                                )
                            },
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("file_attachment_comment_input"),
                            shape = RoundedCornerShape(12.dp),
                            maxLines = 3,
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Settings, // Writing theme indicator representation
                                    contentDescription = "Write comment indicator icon",
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFF8FAFC),
                                unfocusedContainerColor = Color(0xFFF8FAFC),
                                focusedBorderColor = Color(0xFF2563EB),
                                unfocusedBorderColor = Color(0xFFF1F5F9)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Primary trigger "Upload & Attach" button callout
                val isStagedEmpty = selectedFilesToUpload.isEmpty()
                Button(
                    onClick = {
                        if (isStagedEmpty) {
                            triggerToast("No staged files! Please select category or cloud zone above to add files first.")
                        } else {
                            // Move staged files into active upload progress
                            val stashedFiles = selectedFilesToUpload.toList()
                            selectedFilesToUpload.clear()

                            stashedFiles.forEach { item ->
                                val activeUploadItem = item.copy(isUploading = true)
                                uploadingList.add(activeUploadItem)

                                // Run dynamic progressive increments loop using Kotlin coroutines
                                coroutineScope.launch {
                                    var currentPrg = 0f
                                    while (currentPrg < 1.0f) {
                                        delay((200 + (250 * Math.random())).toLong())
                                        currentPrg += 0.15f + (0.15f * Math.random().toFloat())
                                        if (currentPrg >= 1.0f) currentPrg = 1.0f

                                        // Update state atomically to trigger recompositions
                                        val idx = uploadingList.indexOfFirst { it.id == activeUploadItem.id }
                                        if (idx != -1) {
                                            uploadingList[idx] = uploadingList[idx].copy(progress = currentPrg)
                                        }
                                    }

                                    // Complete and move to history logs list
                                    val completedIdx = uploadingList.indexOfFirst { it.id == activeUploadItem.id }
                                    if (completedIdx != -1) {
                                        val finishedItem = uploadingList[completedIdx]
                                        uploadingList.removeAt(completedIdx)

                                        // Merge the typed user comment in if present
                                        val finalComments = if (attachmentComment.isNotBlank()) {
                                            listOf(attachmentComment.trim())
                                        } else {
                                            emptyList()
                                        }

                                        val resolvedItem = finishedItem.copy(
                                            isUploading = false,
                                            progress = 1.0f,
                                            dateString = "Just Now",
                                            comments = finalComments
                                        )
                                        recentlyUploadedList.add(0, resolvedItem)
                                    }

                                    // Clear comment textbox once upload cycle is triggered
                                    attachmentComment = ""
                                    triggerToast("Files securely processed & compiled! 🚀")
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .shadow(
                            elevation = if (!isStagedEmpty) 4.dp else 0.dp,
                            shape = RoundedCornerShape(14.dp),
                            ambientColor = Color(0xFF2563EB)
                        )
                        .testTag("file_upload_attach_main_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!isStagedEmpty) Color(0xFF1E40AF) else Color(0xFFEFF6FF),
                        contentColor = if (!isStagedEmpty) Color.White else Color(0xFF94A3B8)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share, // represented upload Arrow
                            contentDescription = "Upload action",
                            tint = if (!isStagedEmpty) Color.White else Color(0xFFCBD5E1),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isStagedEmpty) "Select files to active upload" else "Upload & Attach Selected Files (${stashedFilesSizeText(selectedFilesToUpload)})",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Footer shield encryption security guarantee
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Green Shield Safety Guard",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Your files are secure and encrypted",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Bold
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
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Tick indicator success",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = toastMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                LaunchedEffect(showToast) {
                    if (showToast) {
                        delay(2000)
                        showToast = false
                    }
                }
            }
        }
    }

    // Modal Simulated File Picker selection drawer list dialog!
    if (showSimulatorDialog) {
        val filteredCategoryList = simulatorFilterType
        val matchingBrowserFiles = if (filteredCategoryList != null) {
            sampleSimulatorFiles.filter { it.type.equals(filteredCategoryList, ignoreCase = true) }
        } else {
            sampleSimulatorFiles
        }

        Dialog(onDismissRequest = { showSimulatorDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .shadow(12.dp, RoundedCornerShape(16.dp)),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (filteredCategoryList != null) "Select Mock $filteredCategoryList File" else "Browse Mock Files Simulator 🗄️",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Choose from preset enterprise files or construct a custom file metadata underneath:",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Presets scroll view
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        matchingBrowserFiles.forEach { preset ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        selectedFilesToUpload.add(preset.copy(id = java.util.UUID.randomUUID().toString()))
                                        showSimulatorDialog = false
                                        triggerToast("Staged ${preset.name} in upload queue!")
                                    }
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(getFileColor(preset.type).copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = getFileEmoji(preset.type), fontSize = 14.sp)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = preset.name,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B)
                                    )
                                    Text(
                                        text = "${preset.type} • ${preset.size}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Select",
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Or Create Custom File Metadata:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B),
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = customFileNameInput,
                        onValueChange = { customFileNameInput = it },
                        placeholder = { Text("custom_filename.png", fontSize = 12.sp) },
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_file_name_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("1.2 MB", "4.8 MB", "12.5 MB", "50 MB").forEach { size ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selectedSizeInput == size) Color(0xFFEFF6FF) else Color(0xFFF8FAFC))
                                    .border(
                                        1.dp,
                                        if (selectedSizeInput == size) Color(0xFF3B82F6) else Color(0xFFCBD5E1),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedSizeInput = size }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = size,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedSizeInput == size) Color(0xFF1E40AF) else Color(0xFF475569)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showSimulatorDialog = false }) {
                            Text("Cancel", color = Color(0xFF64748B))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val nameInput = customFileNameInput.trim().ifEmpty { "custom_attachment.png" }
                                val docType = inferDocType(nameInput)
                                
                                val newlyConstructedItem = FileItem(
                                    name = nameInput,
                                    type = docType,
                                    size = selectedSizeInput,
                                    dateString = "Today, 10:30 AM"
                                )
                                selectedFilesToUpload.add(newlyConstructedItem)
                                customFileNameInput = ""
                                showSimulatorDialog = false
                                triggerToast("Constructed & Staged $nameInput!")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E40AF)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Create & Stage")
                        }
                    }
                }
            }
        }
    }

    // Modal View Comments overlay list dialog details
    val reviewFile = activeCommentReviewFile
    if (reviewFile != null) {
        Dialog(onDismissRequest = { activeCommentReviewFile = null }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .shadow(12.dp, RoundedCornerShape(16.dp)),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Comments (${reviewFile.comments.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )

                        IconButton(
                            onClick = { activeCommentReviewFile = null },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Close", tint = Color(0xFF64748B))
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "File: ${reviewFile.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Comments history list
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 160.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (reviewFile.comments.isEmpty()) {
                            Text(
                                text = "No comments attached to this file yet. Say something below!",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8),
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        } else {
                            reviewFile.comments.forEach { comment ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFF1F5F9))
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = comment,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF1E293B)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Add Comment:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = newCommentTextState,
                        onValueChange = { newCommentTextState = it },
                        placeholder = { Text("Write high priority notes here...") },
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_attachment_comment_raw_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { activeCommentReviewFile = null }) {
                            Text("Done", color = Color(0xFF64748B))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newCommentTextState.isNotBlank()) {
                                    val updatedComments = reviewFile.comments + newCommentTextState.trim()
                                    // Update model state in list
                                    val index = recentlyUploadedList.indexOfFirst { it.id == reviewFile.id }
                                    if (index != -1) {
                                        val target = recentlyUploadedList[index]
                                        recentlyUploadedList[index] = target.copy(comments = updatedComments)
                                        // Also update active file
                                        activeCommentReviewFile = recentlyUploadedList[index]
                                    }
                                    newCommentTextState = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E40AF)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Add")
                        }
                    }
                }
            }
        }
    }
}

// Helpers
fun getFileEmoji(type: String): String {
    return when (type) {
        "Image" -> "🖼️"
        "PDF" -> "📕"
        "Audio" -> "🎧"
        "Video" -> "🎬"
        "Document" -> "📄"
        else -> "📎"
    }
}

fun getFileColor(type: String): Color {
    return when (type) {
        "Image" -> Color(0xFF10B981)
        "PDF" -> Color(0xFFEF4444)
        "Audio" -> Color(0xFF8B5CF6)
        "Video" -> Color(0xFF3B82F6)
        "Document" -> Color(0xFFF59E0B)
        else -> Color(0xFF64748B)
    }
}

fun inferDocType(fileName: String): String {
    val ext = fileName.substringAfterLast(".", "").lowercase()
    return when (ext) {
        "png", "jpg", "jpeg", "webp", "gif" -> "Image"
        "pdf" -> "PDF"
        "mp3", "wav", "m4a", "flac" -> "Audio"
        "mp4", "mkv", "avi", "mov" -> "Video"
        "doc", "docx", "xlsx", "xls", "txt", "csv" -> "Document"
        else -> "Others"
    }
}

fun stashedFilesSizeText(list: List<FileItem>): String {
    if (list.isEmpty()) return "0"
    return list.size.toString()
}
