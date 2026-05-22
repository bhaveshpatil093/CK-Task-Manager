package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Task
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Discussion Message entity
data class ChatMessage(
    val id: String,
    val senderName: String,
    val senderInitials: String,
    val senderAvatarBg: Color,
    val senderTitleColor: Color,
    val isUser: Boolean,
    val text: String,
    val time: String,
    val type: MessageType = MessageType.TEXT,
    val attachmentName: String = "",
    val attachmentSize: String = "",
    val voiceDurationSeconds: Int = 0,
    val voiceWaveform: List<Float> = emptyList(),
    val replyTo: ReplyReference? = null,
    val isRead: Boolean = true,
    val isVoicePlaying: Boolean = false,
    val voicePlaybackProgress: Float = 0f,
    val statusText: String = ""
)

enum class MessageType {
    TEXT, SCREENSHOT, VOICE_NOTE, PDF_UPLOAD, STATUS_CHANGE
}

data class ReplyReference(
    val senderName: String,
    val messageText: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDiscussionScreen(
    task: Task,
    viewModel: TaskViewModel,
    onDismiss: () -> Unit,
    onStatusUpdated: (String) -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberLazyListState()

    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }

    var activeChatTarget by remember { mutableStateOf<UserProfile?>(null) }
    var directChatsMap by remember {
        mutableStateOf(
            mapOf<String, List<ChatMessage>>()
        )
    }

    // Dropdown settings menu
    var showMenu by remember { mutableStateOf(false) }

    // Simulated task current status
    var currentStatus by remember { mutableStateOf(if (task.isCompleted) "Completed" else "In Progress") }
    var dueDateText by remember { mutableStateOf(if (task.dueDate.isNotEmpty()) task.dueDate else "25 May 2026") }

    // Quoted reply message tracking
    var activeReplyToMessage by remember { mutableStateOf<ChatMessage?>(null) }

    // Input messages state
    var currentInputText by remember { mutableStateOf("") }

    // Local Toast Alert state
    var toastMessage by remember { mutableStateOf("") }
    var showToast by remember { mutableStateOf(false) }

    // Media status
    var voiceRecordingState by remember { mutableStateOf(false) }
    var voiceRecordDuration by remember { mutableStateOf(0) }

    fun triggerToast(msg: String) {
        toastMessage = msg
        showToast = true
    }

    LaunchedEffect(showToast) {
        if (showToast) {
            delay(2000)
            showToast = false
        }
    }

    // List of chat messages starting with pre-populated realistic content matching screenshot
    var messagesList by remember {
        mutableStateOf(
            listOf(
                ChatMessage(
                    id = "msg1",
                    senderName = "Chaitra Madam",
                    senderInitials = "CM",
                    senderAvatarBg = Color(0xFFE0F2FE),
                    senderTitleColor = Color(0xFF8B5CF6),
                    isUser = false,
                    text = "Please find the latest homepage design for review.",
                    time = "09:15 AM"
                ),
                ChatMessage(
                    id = "msg2",
                    senderName = "Chaitra Madam",
                    senderInitials = "CM",
                    senderAvatarBg = Color(0xFFE0F2FE),
                    senderTitleColor = Color(0xFF8B5CF6),
                    isUser = false,
                    text = "Desktop Mockup Screenshot",
                    time = "09:15 AM",
                    type = MessageType.SCREENSHOT
                ),
                ChatMessage(
                    id = "msg3",
                    senderName = "You",
                    senderInitials = "U",
                    senderAvatarBg = Color(0xFF4F46E5),
                    senderTitleColor = Color(0xFF16A34A),
                    isUser = true,
                    text = "Looks great! 👍 Just a small change in the banner text. I'll share the suggestion.",
                    time = "09:18 AM"
                ),
                ChatMessage(
                    id = "msg4",
                    senderName = "Kiran Madam",
                    senderInitials = "KM",
                    senderAvatarBg = Color(0xFFFEF3C7),
                    senderTitleColor = Color(0xFF0369A1),
                    isUser = false,
                    text = "Sure, please share.",
                    time = "09:20 AM",
                    replyTo = ReplyReference(
                        senderName = "You",
                        messageText = "Looks great! 👍 Just a small change in the banner text. I'll share the suggestion."
                    )
                ),
                ChatMessage(
                    id = "msg5",
                    senderName = "You",
                    senderInitials = "U",
                    senderAvatarBg = Color(0xFF4F46E5),
                    senderTitleColor = Color(0xFF16A34A),
                    isUser = true,
                    text = "Audionote track suggestion details to change CTA titles",
                    time = "09:21 AM",
                    type = MessageType.VOICE_NOTE,
                    voiceDurationSeconds = 18,
                    voiceWaveform = listOf(
                        0.2f, 0.4f, 0.6f, 0.3f, 0.8f, 0.5f, 0.7f, 0.4f, 0.8f, 0.6f,
                        0.5f, 0.3f, 0.9f, 0.7f, 0.4f, 0.5f, 0.2f, 0.6f, 0.8f, 0.5f,
                        0.6f, 0.4f, 0.7f, 0.3f, 0.8f, 0.4f, 0.5f, 0.2f, 0.6f, 0.4f
                    )
                ),
                ChatMessage(
                    id = "msg6",
                    senderName = "Amruta Madam",
                    senderInitials = "AM",
                    senderAvatarBg = Color(0xFFFCE7F3),
                    senderTitleColor = Color(0xFFDB2777),
                    isUser = false,
                    text = "Updated banner text as discussed.",
                    time = "09:25 AM"
                ),
                ChatMessage(
                    id = "msg7",
                    senderName = "Amruta Madam",
                    senderInitials = "AM",
                    senderAvatarBg = Color(0xFFFCE7F3),
                    senderTitleColor = Color(0xFFDB2777),
                    isUser = false,
                    text = "Updated file upload details",
                    time = "09:25 AM",
                    type = MessageType.PDF_UPLOAD,
                    attachmentName = "Homepage_Banner_Updated.pdf",
                    attachmentSize = "2.4 MB • PDF"
                ),
                ChatMessage(
                    id = "msg8",
                    senderName = "You",
                    senderInitials = "U",
                    senderAvatarBg = Color(0xFF4F46E5),
                    senderTitleColor = Color(0xFF16A34A),
                    isUser = true,
                    text = "Thanks! Looks perfect now.",
                    time = "09:30 AM"
                ),
                ChatMessage(
                    id = "msg9",
                    senderName = "Chaitra Madam",
                    senderInitials = "CM",
                    senderAvatarBg = Color(0xFFE0F2FE),
                    senderTitleColor = Color(0xFF8B5CF6),
                    isUser = false,
                    text = "Great! Moving this to review stage.",
                    time = "09:32 AM"
                ),
                ChatMessage(
                    id = "msg10",
                    senderName = "System",
                    senderInitials = "S",
                    senderAvatarBg = Color(0xFFEFF6FF),
                    senderTitleColor = Color(0xFF2563EB),
                    isUser = false,
                    text = "Status updated to In Review",
                    time = "09:32 AM",
                    type = MessageType.STATUS_CHANGE,
                    statusText = "In Review"
                )
            )
        )
    }

    val activeMessages = if (activeChatTarget == null) {
        messagesList
    } else {
        directChatsMap[activeChatTarget!!.email] ?: listOf(
            ChatMessage(
                id = "welcome_${activeChatTarget!!.email}",
                senderName = activeChatTarget!!.name,
                senderInitials = activeChatTarget!!.name.split(" ").map { it.trim() }.filter { it.isNotEmpty() }.mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("").take(2),
                senderAvatarBg = Color(0xFFE0F2FE),
                senderTitleColor = Color(0xFF8B5CF6),
                isUser = false,
                text = "Hi! This is ${activeChatTarget!!.name} (${activeChatTarget!!.role}). Let's chat about our tasks! My phone is ${activeChatTarget!!.phone} and email is ${activeChatTarget!!.email}.",
                time = "Just Now"
            )
        )
    }

    // Filtered messages
    val filteredMessages = if (searchQuery.trim().isEmpty()) {
        activeMessages
    } else {
        activeMessages.filter {
            it.text.contains(searchQuery, ignoreCase = true) ||
            it.senderName.contains(searchQuery, ignoreCase = true) ||
            it.attachmentName.contains(searchQuery, ignoreCase = true) ||
            it.statusText.contains(searchQuery, ignoreCase = true)
        }
    }

    // Scroll to bottom helper
    val scrollToBottom: () -> Unit = {
        coroutineScope.launch {
            if (filteredMessages.isNotEmpty()) {
                scrollState.animateScrollToItem(filteredMessages.size - 1)
            }
        }
    }

    // Trigger initial scrolling
    LaunchedEffect(messagesList.size) {
        delay(200)
        scrollToBottom()
    }

    // Handle sending a new message
    val sendMessage: (String, MessageType, String, String) -> Unit = { txt, type, filename, size ->
        val rightNow = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date())
        val newMsg = ChatMessage(
            id = "user_msg_${System.currentTimeMillis()}",
            senderName = "You",
            senderInitials = "U",
            senderAvatarBg = Color(0xFF4F46E5),
            senderTitleColor = Color(0xFF16A34A),
            isUser = true,
            text = txt,
            time = rightNow,
            type = type,
            attachmentName = filename,
            attachmentSize = size,
            voiceDurationSeconds = if (type == MessageType.VOICE_NOTE) 12 else 0,
            voiceWaveform = if (type == MessageType.VOICE_NOTE) listOf(
                0.3f, 0.5f, 0.2f, 0.7f, 0.6f, 0.4f, 0.8f, 0.9f, 0.4f, 0.3f, 0.7f, 0.5f, 0.6f, 0.4f, 0.3f
            ) else emptyList(),
            replyTo = activeReplyToMessage?.let { ReplyReference(it.senderName, it.text) },
            isRead = false
        )
        
        if (activeChatTarget == null) {
            messagesList = messagesList + newMsg
            currentInputText = ""
            activeReplyToMessage = null
            scrollToBottom()

            // Double ticks visual animation feedback mock delay
            coroutineScope.launch {
                delay(1500)
                messagesList = messagesList.map {
                    if (it.id == newMsg.id) it.copy(isRead = true) else it
                }
                triggerToast("Deliveries ticked blue ✔️")
            }

            // Realistic automated replies
            coroutineScope.launch {
                delay(3000)
                val responseText = when (txt.lowercase()) {
                    "hello", "hi", "hey" -> "Hello! How can I assist with ${task.title} design assets today?"
                    "looks perfect", "perfect now", "looks great" -> "Glad we got it customized appropriately! I will inform key departments."
                    else -> "Got it! Adding this pointer to the Scrum agenda for our Q2 sync."
                }
                val replyNow = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date())
                messagesList = messagesList + ChatMessage(
                    id = "auto_reply_${System.currentTimeMillis()}",
                    senderName = "Kiran Madam",
                    senderInitials = "KM",
                    senderAvatarBg = Color(0xFFFEF3C7),
                    senderTitleColor = Color(0xFF0369A1),
                    isUser = false,
                    text = responseText,
                    time = replyNow
                )
                scrollToBottom()
            }
        } else {
            val target = activeChatTarget!!
            val key = target.email
            val currentList = directChatsMap[key] ?: emptyList()
            directChatsMap = directChatsMap + (key to (currentList + newMsg))
            currentInputText = ""
            activeReplyToMessage = null
            scrollToBottom()

            // Simulation of Real-time direct chat typing & reply from team member
            coroutineScope.launch {
                delay(1000)
                // Mark user sent message as read
                val currentDirects = directChatsMap[key] ?: emptyList()
                directChatsMap = directChatsMap + (key to currentDirects.map {
                    if (it.id == newMsg.id) it.copy(isRead = true) else it
                })
                triggerToast("${target.name} is typing... 💬")
                
                delay(1500)
                val replyText = when {
                    txt.contains("hi", ignoreCase = true) || txt.contains("hello", ignoreCase = true) -> {
                        "Hello! Team member ${target.name} here. Ready to collaborate on Career Katta tasks! 🚀"
                    }
                    txt.contains("status", ignoreCase = true) || txt.contains("update", ignoreCase = true) -> {
                        "I'm currently updating the task status. Please check the dashboard under my reports tab!"
                    }
                    txt.contains("phone", ignoreCase = true) || txt.contains("call", ignoreCase = true) -> {
                        "You can call me at ${target.phone} any time to discuss our task zero pendency targets."
                    }
                    else -> {
                        "Thanks for your message! As an active ${target.role} at Career Katta, I'm working on task pendency. Let's close this out soon!"
                    }
                }
                
                val replyMsg = ChatMessage(
                    id = "bot_msg_${System.currentTimeMillis()}",
                    senderName = target.name,
                    senderInitials = target.name.split(" ").map { it.trim() }.filter { it.isNotEmpty() }.mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("").take(2),
                    senderAvatarBg = Color(0xFFFCE7F3),
                    senderTitleColor = Color(0xFFDB2777),
                    isUser = false,
                    text = replyText,
                    time = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date()),
                    type = MessageType.TEXT
                )
                
                val updatedDirects = directChatsMap[key] ?: emptyList()
                directChatsMap = directChatsMap + (key to (updatedDirects + replyMsg))
                delay(100)
                scrollToBottom()
            }
        }
    }

    // Simulated Voice Note Animation Loop
    var activeVoicePlayingId by remember { mutableStateOf<String?>(null) }
    var activeVoiceProgress by remember { mutableStateOf(0f) }
    LaunchedEffect(activeVoicePlayingId) {
        if (activeVoicePlayingId != null) {
            while (activeVoiceProgress < 1f) {
                delay(100)
                activeVoiceProgress += 0.05f
            }
            // Finished playing
            messagesList = messagesList.map {
                if (it.id == activeVoicePlayingId) {
                    it.copy(isVoicePlaying = false, voicePlaybackProgress = 1f)
                } else it
            }
            activeVoicePlayingId = null
            activeVoiceProgress = 0f
            triggerToast("Voice note completed playback 🎧")
        }
    }

    val isDark = MaterialTheme.colorScheme.background != Color(0xFFF8FAFC)
    val whatsappBg = if (isDark) Color(0xFF0B141A) else Color(0xFFECE5DD)
    val doodleColor = if (isDark) Color(0xFF1E2A32) else Color(0xFFE5DDD5)
    val doodleColorAlt = if (isDark) Color(0xFF1E2A32) else Color(0xFFDFD7CE)
    val headerBg = if (isDark) Color(0xFF1F2C34) else Color.White
    val textPrimary = if (isDark) Color.White else Color(0xFF0F172A)

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("task_discussion_screen"),
        color = whatsappBg
    ) {
        // Overlay doodle background patterns
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    // Draw a couple of stylish ambient geometric shapes or lines representing WhatsApp textures
                    drawCircle(
                        color = doodleColor.copy(alpha = 0.5f),
                        radius = 200f,
                        center = androidx.compose.ui.geometry.Offset(size.width * 0.2f, size.height * 0.3f)
                    )
                    drawCircle(
                        color = doodleColorAlt.copy(alpha = 0.4f),
                        radius = 350f,
                        center = androidx.compose.ui.geometry.Offset(size.width * 0.8f, size.height * 0.7f)
                    )
                }
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Professional Task Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(headerBg)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Dismiss chat screen",
                            tint = if (isDark) Color.White else Color(0xFF1E293B)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Task Icon Visual (WhatsApp matching avatar group icon style)
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2563EB)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Website Redesign task marker description icon",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = task.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1E293B),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Task #${task.id}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2563EB)
                        )
                    }

                    // Top Action elements
                    IconButton(onClick = {
                        sendMessage("Dialing corporate VoIP conferencing line with project sponsors...", MessageType.TEXT, "", "")
                        triggerToast("Dialing team conference VoIP bridge 📞")
                    }) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Call Sponsor team phone conference lines",
                            tint = Color(0xFF1E293B),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Box {
                        IconButton(onClick = { showMenu = !showMenu }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Discussion options dropdown list trigger",
                                tint = Color(0xFF1E293B),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Search Messages") },
                                onClick = {
                                    isSearching = true
                                    showMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            )
                            DropdownMenuItem(
                                text = { Text("Clear Chat") },
                                onClick = {
                                    messagesList = emptyList()
                                    showMenu = false
                                    triggerToast("Chat historical session cleared.")
                                },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            )
                            DropdownMenuItem(
                                text = { Text("Update Due Date") },
                                onClick = {
                                    dueDateText = "31 May 2026"
                                    showMenu = false
                                    triggerToast("Timeline extended to 31 May 2026.")
                                },
                                leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            )
                        }
                    }
                }

                // Sub-header displaying Status and Timeline info
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF1F5F9))
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2563EB))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Status: ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = currentStatus.ifEmpty { "In Progress" },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF2563EB),
                            modifier = Modifier.clickable {
                                // Rotate status
                                val options = listOf("Pending", "In Progress", "In Review", "Approved", "Testing")
                                val nextIndex = (options.indexOf(currentStatus) + 1) % options.size
                                val nextStatus = options[nextIndex]
                                currentStatus = nextStatus
                                onStatusUpdated(nextStatus)

                                // Add change logs inline
                                val todayTime = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date())
                                messagesList = messagesList + ChatMessage(
                                    id = "status_change_${System.currentTimeMillis()}",
                                    senderName = "System",
                                    senderInitials = "S",
                                    senderAvatarBg = Color(0xFFEFF6FF),
                                    senderTitleColor = Color(0xFF2563EB),
                                    isUser = false,
                                    text = "Status updated to $nextStatus",
                                    time = todayTime,
                                    type = MessageType.STATUS_CHANGE,
                                    statusText = nextStatus
                                )
                                scrollToBottom()
                                triggerToast("Task milestone stage moved to $nextStatus 🚀")
                            }
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Due date schedule calendar",
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Due: ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = dueDateText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFEF4444)
                        )
                    }
                }

                // ----------------- DYNAMIC REGISTERED ENTITIES (CHANNELS) ROW -----------------
                val registeredUsers by viewModel.registeredEmployees.collectAsState()
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isDark) Color(0xFF1F2C34) else Color(0xFFF1F5F9))
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "DIRECT WORKSPACE CHATS (REAL-TIME)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isDark) Color(0xFFA1A1AA) else Color(0xFF475569),
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 6.dp),
                        letterSpacing = 1.sp
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Option 1: GENERAL Task Channel
                        val isGroupSelected = activeChatTarget == null
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { activeChatTarget = null }
                                .padding(4.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(if (isGroupSelected) Color(0xFF2563EB) else (if (isDark) Color(0xFF2A3942) else Color(0xFFE2E8F0)))
                                    .border(if (isGroupSelected) 2.dp else 0.dp, Color.White, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Groups,
                                    contentDescription = "Task Group Chat",
                                    tint = if (isGroupSelected) Color.White else (if (isDark) Color.White else Color(0xFF475569)),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Group Chat",
                                fontSize = 10.sp,
                                fontWeight = if (isGroupSelected) FontWeight.ExtraBold else FontWeight.Normal,
                                color = if (isGroupSelected) (if (isDark) Color.White else Color(0xFF2563EB)) else (if (isDark) Color(0xFFE2E8F0) else Color(0xFF475569)),
                                maxLines = 1
                            )
                        }
                        
                        // Option 2...N: Registered Entities
                        registeredUsers.forEach { user ->
                            val isSelected = activeChatTarget?.email == user.email
                            val initials = user.name.split(" ")
                                .map { it.trim() }
                                .filter { it.isNotEmpty() }
                                .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                                .joinToString("")
                                .take(2)
                            val avatarBg = when (user.role) {
                                "Super-admin" -> Color(0xFFFEF3C7)
                                "Admin" -> Color(0xFFE0F2FE)
                                else -> Color(0xFFFCE7F3)
                            }
                            val avatarTextColor = when (user.role) {
                                "Super-admin" -> Color(0xFFB45309)
                                "Admin" -> Color(0xFF0369A1)
                                else -> Color(0xFFDB2777)
                            }
                            
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable { activeChatTarget = user }
                                    .padding(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) Color(0xFF2563EB) else avatarBg)
                                        .border(if (isSelected) 2.dp else 0.dp, Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Live dot indicator
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF22C55E))
                                            .border(1.5.dp, if (isSelected) Color(0xFF2563EB) else avatarBg, CircleShape)
                                    )
                                    
                                    Text(
                                        text = if (initials.isEmpty()) "EM" else initials,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else avatarTextColor
                                    )
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = user.name.substringBefore(" "),
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal,
                                    color = if (isSelected) (if (isDark) Color.White else Color(0xFF2563EB)) else (if (isDark) Color(0xFFE2E8F0) else Color(0xFF475569)),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // Interactive search criteria text input box
                AnimatedVisibility(
                    visible = isSearching,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.width(8.dp))
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            decorationBox = { innerTextField ->
                                if (searchQuery.isEmpty()) {
                                    Text("Find text context, names, attachments...", fontSize = 12.sp, color = Color.LightGray)
                                }
                                innerTextField()
                            }
                        )
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(14.dp))
                            }
                        }
                        TextButton(onClick = {
                            searchQuery = ""
                            isSearching = false
                        }) {
                            Text("Cancel", fontSize = 11.sp)
                        }
                    }
                }

                // Scrollable Chat Message Area
                LazyColumn(
                    state = scrollState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Day Pill Category Row Indicator (Today)
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFE1F3FE))
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Today",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF475569)
                                )
                            }
                        }
                    }

                    // Render list of filtered conversation nodes
                    itemsIndexed(filteredMessages) { index, chatItem ->
                        when (chatItem.type) {
                            MessageType.STATUS_CHANGE -> {
                                StatusChangeBubble(chatItem)
                            }
                            else -> {
                                MessageRowItem(
                                    msg = chatItem,
                                    onReplySelected = {
                                        activeReplyToMessage = chatItem
                                        triggerToast("Replying to ${chatItem.senderName} 💬")
                                    },
                                    onPlayVoiceNote = { audioId ->
                                        if (activeVoicePlayingId == audioId) {
                                            activeVoicePlayingId = null
                                            activeVoiceProgress = 0f
                                            triggerToast("Audio playback paused.")
                                        } else {
                                            activeVoicePlayingId = audioId
                                            activeVoiceProgress = 0f
                                            triggerToast("Playing digital voice note timeline segment...")
                                        }
                                    },
                                    activePlayingId = activeVoicePlayingId,
                                    playbackProgress = activeVoiceProgress
                                )
                            }
                        }
                    }
                }

                // Bottom Quote Reply banner bar if active replying
                AnimatedVisibility(
                    visible = activeReplyToMessage != null,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    val targetMsg = activeReplyToMessage
                    if (targetMsg != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF1F5F9))
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(36.dp)
                                    .background(targetMsg.senderTitleColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = targetMsg.senderName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = targetMsg.senderTitleColor
                                )
                                Text(
                                    text = targetMsg.text,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = Color(0xFF64748B)
                                )
                            }
                            IconButton(
                                onClick = { activeReplyToMessage = null },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear quote reply references", modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }

                // Voice Recording Display Banner if typing hands-free audio suggestions
                AnimatedVisibility(
                    visible = voiceRecordingState,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFEF2F2))
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color.Red)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Recording Voice Suggestion Note... (${voiceRecordDuration}s)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Red
                            )
                        }
                        Row {
                            TextButton(onClick = {
                                voiceRecordingState = false
                                triggerToast("Recording canceled.")
                            }) {
                                Text("Discard", color = Color.Red, fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Button(
                                onClick = {
                                    voiceRecordingState = false
                                    sendMessage(
                                        "Voice memo update (${voiceRecordDuration}s)",
                                        MessageType.VOICE_NOTE,
                                        "",
                                        ""
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("Send Note", fontSize = 10.sp, color = Color.White)
                            }
                        }
                    }
                }

                // Interactive Keyboard Attachment Input Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF0F0F0))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Attachment Actions and Emojis triggers
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = {
                                val emojis = listOf("👍", "🙌", "✔️", "🥇", "💯", "🚀", "💡", "❓")
                                currentInputText += emojis.random()
                            },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Face,
                                contentDescription = "Emoji picker panel",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // File Attachment options
                        var showAttachmentSheet by remember { mutableStateOf(false) }
                        IconButton(
                            onClick = { showAttachmentSheet = !showAttachmentSheet },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Simulated documents clip menu panel",
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        if (showAttachmentSheet) {
                            DropdownMenu(
                                expanded = showAttachmentSheet,
                                onDismissRequest = { showAttachmentSheet = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Upload PDF Bill / Spec") },
                                    onClick = {
                                        showAttachmentSheet = false
                                        sendMessage(
                                            "Mock Document summary specification attachment",
                                            MessageType.PDF_UPLOAD,
                                            "Project_Statement_Of_Work_Final.pdf",
                                            "4.1 MB • PDF"
                                        )
                                        triggerToast("Uploaded new PDF scope document attachment.")
                                    },
                                    leadingIcon = { Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Upload Desktop Mockup") },
                                    onClick = {
                                        showAttachmentSheet = false
                                        sendMessage(
                                            "Homepage desktop landing page update file mockup screen",
                                            MessageType.SCREENSHOT,
                                            "Homepage_Desktop_Revision_2.png",
                                            "1.8 MB • PNG"
                                        )
                                        triggerToast("Attached homepage mockup layout.")
                                    },
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Simulate Voice Note") },
                                    onClick = {
                                        showAttachmentSheet = false
                                        sendMessage(
                                            "Simulated project briefing",
                                            MessageType.VOICE_NOTE,
                                            "",
                                            ""
                                        )
                                    },
                                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                )
                            }
                        }
                    }

                    // Main typing text box
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(24.dp))
                            .padding(horizontal = 14.dp, vertical = 9.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            BasicTextField(
                                value = currentInputText,
                                onValueChange = { currentInputText = it },
                                modifier = Modifier.weight(1f),
                                singleLine = false,
                                maxLines = 4,
                                textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF1E293B)),
                                decorationBox = { innerTextField ->
                                    if (currentInputText.isEmpty()) {
                                        Text(
                                            text = "Type a message",
                                            fontSize = 13.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                    innerTextField()
                                }
                            )

                            // Dynamic camera snapshot simulated button inside input field
                            IconButton(
                                onClick = {
                                    sendMessage("Snap camera shot revision file logged", MessageType.SCREENSHOT, "Camera_Upload_Shot.png", "800 KB • JPG")
                                    triggerToast("Captured camera photo snapshot.")
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle, // Clean mock placeholder representing camera actions
                                    contentDescription = "Simulated snapshot frame upload actions",
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Green WhatsApp action bubble button (Send or Record)
                    val speakOrSendIcon = if (currentInputText.trim().isNotEmpty()) Icons.AutoMirrored.Filled.Send else Icons.Default.Phone
                    IconButton(
                        onClick = {
                            if (currentInputText.trim().isNotEmpty()) {
                                sendMessage(currentInputText, MessageType.TEXT, "", "")
                            } else {
                                // Simulate recording voice note
                                if (!voiceRecordingState) {
                                    voiceRecordingState = true
                                    voiceRecordDuration = 0
                                    coroutineScope.launch {
                                        while (voiceRecordingState) {
                                            delay(1000)
                                            voiceRecordDuration++
                                        }
                                    }
                                } else {
                                    voiceRecordingState = false
                                }
                            }
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF075E54)) // Green WhatsApp branding green tint
                    ) {
                        Icon(
                            imageVector = speakOrSendIcon,
                            contentDescription = "Type message or record voice suggest file description trigger button",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Splash Toast alerts feedback overlay banner
            AnimatedVisibility(
                visible = showToast,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .shadow(6.dp, RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Confirmation check alert indicator icon",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
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
}

@Composable
fun StatusChangeBubble(msg: ChatMessage) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .border(1.dp, Color(0xFFD4E6FC), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2563EB)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(10.dp)
                    )
                }

                Column {
                    Text(
                        text = msg.text,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1E40AF)
                    )
                    Text(
                        text = "Today, ${msg.time}",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                }
            }
        }
    }
}

@Composable
fun MessageRowItem(
    msg: ChatMessage,
    onReplySelected: () -> Unit,
    onPlayVoiceNote: (String) -> Unit,
    activePlayingId: String?,
    playbackProgress: Float
) {
    val isDark = MaterialTheme.colorScheme.background != Color(0xFFF8FAFC)
    val alignment = if (msg.isUser) Alignment.CenterEnd else Alignment.CenterStart
    val bubbleBgColor = if (msg.isUser) {
        if (isDark) Color(0xFF005C4B) else Color(0xFFDCF8C6)
    } else {
        if (isDark) Color(0xFF202C33) else Color.White
    }
    
    val textPrimary = if (isDark) Color.White else Color(0xFF1E293B)
    val textSecondary = if (isDark) Color(0xFF8696A0) else Color(0xFF64748B)
    val quoteNameColor = if (isDark) Color(0xFF53BDEB) else Color(0xFF1E3A8A)
    val quoteTextColor = if (isDark) Color(0xFF8696A0) else Color(0xFF475569)
    val quoteBg = if (isDark) Color(0xFF000000).copy(alpha = 0.15f) else Color(0xFF000000).copy(alpha = 0.05f)
    val quoteBarColor = if (isDark) Color(0xFF53BDEB) else Color(0xFF2563EB)
    
    val bubbleShape = if (msg.isUser) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomEnd = 16.dp, bottomStart = 16.dp)
    } else {
        RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = alignment
    ) {
        Row(
            horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            if (!msg.isUser) {
                // Sender Avatar Icon display circular Initials
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(msg.senderAvatarBg)
                        .border(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = msg.senderInitials,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = msg.senderTitleColor
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            // Message Bubble Card
            Card(
                colors = CardDefaults.cardColors(containerColor = bubbleBgColor),
                shape = bubbleShape,
                modifier = Modifier
                    .shadow(if (isDark) 0.dp else 1.dp, bubbleShape, ambientColor = Color.LightGray)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = { onReplySelected() },
                            onTap = { onReplySelected() }
                        )
                    }
            ) {
                Column(
                    modifier = Modifier.padding(10.dp)
                ) {
                    // Header label metadata if sender is colleague
                    if (!msg.isUser) {
                        Text(
                            text = msg.senderName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = msg.senderTitleColor
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    }

                    // Replied quote reference box
                    if (msg.replyTo != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(quoteBg)
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(24.dp)
                                        .background(quoteBarColor)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = msg.replyTo.senderName,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = quoteNameColor
                                    )
                                    Text(
                                        text = msg.replyTo.messageText,
                                        fontSize = 9.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = quoteTextColor
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    // Message Types routing body
                    when (msg.type) {
                        MessageType.SCREENSHOT -> {
                            Column {
                                Text(
                                    text = msg.text,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textSecondary,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )

                                // Custom styled Compose Mock Website Template Screen Design Drawing
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(130.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF3B82F6))
                                ) {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(10.dp),
                                            verticalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(Color.White))
                                                    Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(Color.White))
                                                    Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(Color.White))
                                                }
                                                Text("HDFC Follow-up", fontSize = 8.sp, color = Color.White.copy(alpha = 0.8f))
                                            }

                                            Column(modifier = Modifier.fillMaxWidth()) {
                                                Text(
                                                    text = "Building Digital Experiences",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = Color.White
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "Explore brand guidelines and visual metrics.",
                                                    fontSize = 8.sp,
                                                    color = Color.White.copy(alpha = 0.9f)
                                                )
                                            }

                                            Row(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color.White)
                                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                            ) {
                                                Text("Discuss Now", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3B82F6))
                                            }
                                        }

                                        // Subtle watermarks design representing complex web page layouts
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .size(60.dp)
                                                .clip(RoundedCornerShape(topStart = 60.dp))
                                                .background(Color.White.copy(alpha = 0.15f))
                                        )
                                    }
                                }
                            }
                        }

                        MessageType.VOICE_NOTE -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                val isPlaying = activePlayingId == msg.id
                                val audioBtnBg = if (isPlaying) (if (isDark) Color(0xFF2563EB) else Color(0xFF2563EB)) else (if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0))
                                val audioIconTint = if (isPlaying) Color.White else (if (isDark) Color(0xFF53BDEB) else Color(0xFF2563EB))
                                IconButton(
                                    onClick = { onPlayVoiceNote(msg.id) },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(audioBtnBg)
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Close else Icons.Default.PlayArrow,
                                        contentDescription = "Play voice audio clip",
                                        tint = audioIconTint,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Draw simulated waveform tracks
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        msg.voiceWaveform.forEachIndexed { idx, height ->
                                            val progress = if (isPlaying) playbackProgress else 0f
                                            val isActive = idx.toFloat() / msg.voiceWaveform.size <= progress
                                            val barColor = if (isActive) (if (isDark) Color(0xFF53BDEB) else Color(0xFF2563EB)) else (if (isDark) Color(0xFF4F5D64) else Color(0xFF94A3B8))
                                            Box(
                                                modifier = Modifier
                                                    .width(2.dp)
                                                    .height((height * 20).dp)
                                                    .clip(RoundedCornerShape(1.dp))
                                                    .background(barColor)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = if (isPlaying) "0:${String.format("%02d", (playbackProgress * msg.voiceDurationSeconds).toInt())}" else "0:${msg.voiceDurationSeconds}",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = textSecondary
                                        )
                                    }
                                }
                            }
                        }

                        MessageType.PDF_UPLOAD -> {
                            val pdfCardBg = if (isDark) Color(0xFF182329) else Color(0xFFF1F5F9)
                            val pdfCardBorder = if (isDark) Color(0xFF233138) else Color(0xFFE2E8F0)
                            Card(
                                colors = CardDefaults.cardColors(containerColor = pdfCardBg),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, pdfCardBorder, RoundedCornerShape(8.dp)),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Simulated red PDF logo box icon
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFFEF4444)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "PDF",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = msg.attachmentName,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = textPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = msg.attachmentSize,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = textSecondary
                                        )
                                    }

                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowRight,
                                        contentDescription = "Details description doc trigger",
                                        tint = textSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        else -> {
                            Text(
                                text = msg.text,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Footer displaying double ticks and time Stamp
                    Row(
                        modifier = Modifier.align(Alignment.End),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = msg.time,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = textSecondary
                        )

                        if (msg.isUser) {
                            val ticksColor = if (msg.isRead) Color(0xFF53BDEB) else textSecondary
                            Text(
                                text = if (msg.isRead) "✓✓" else "✓",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = ticksColor
                            )
                        }
                    }
                }
            }
        }
    }
}
