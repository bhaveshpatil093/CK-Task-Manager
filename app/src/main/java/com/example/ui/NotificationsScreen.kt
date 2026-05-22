package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class NotificationItem(
    val id: String,
    val type: String, // task_assigned, overdue, mention, escalation, reminder, updated, comment
    val title: String,
    val description: String,
    val taskName: String,
    val time: String,
    val dateGroup: String, // Today, Yesterday, Earlier
    val isUnread: Boolean,
    val priority: String, // High, Medium, Urgent, Low, Reminder
    val priorityColor: Color,
    val priorityBg: Color,
    val icon: ImageVector,
    val iconTint: Color,
    val iconBg: Color,
    var isSwiped: Boolean = false
)

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedFilter by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var showSearchBox by remember { mutableStateOf(false) }

    // Interactive Toast Alerts
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

    // Interactive list state
    var notificationsList by remember {
        mutableStateOf(
            listOf(
                // TODAY SECTION
                NotificationItem(
                    id = "n1",
                    type = "task_assigned",
                    title = "Task Assigned",
                    description = "You have been assigned a new task",
                    taskName = "HDFC Follow-up Call",
                    time = "10:30 AM",
                    dateGroup = "Today",
                    isUnread = true,
                    priority = "High Priority",
                    priorityColor = Color(0xFFEF4444),
                    priorityBg = Color(0xFFFEF2F2),
                    icon = Icons.Default.Edit,
                    iconTint = Color(0xFF2563EB),
                    iconBg = Color(0xFFEFF6FF)
                ),
                NotificationItem(
                    id = "n2",
                    type = "overdue",
                    title = "Overdue Warning",
                    description = "Marketing Report is overdue by 2h 15m",
                    taskName = "Marketing Report",
                    time = "09:15 AM",
                    dateGroup = "Today",
                    isUnread = true,
                    priority = "Overdue",
                    priorityColor = Color(0xFFEF4444),
                    priorityBg = Color(0xFFFEF2F2),
                    icon = Icons.Default.Warning,
                    iconTint = Color(0xFFEF4444),
                    iconBg = Color(0xFFFEF2F2)
                ),
                NotificationItem(
                    id = "n3",
                    type = "mention",
                    title = "Collaborator Mention",
                    description = "Kiran Madam mentioned you in a comment",
                    taskName = "Q2 Planning Discussion",
                    time = "08:45 AM",
                    dateGroup = "Today",
                    isUnread = true,
                    priority = "Medium Priority",
                    priorityColor = Color(0xFF8B5CF6),
                    priorityBg = Color(0xFFF5F3FF),
                    icon = Icons.Default.Person,
                    iconTint = Color(0xFF8B5CF6),
                    iconBg = Color(0xFFF5F3FF)
                ),
                NotificationItem(
                    id = "n4",
                    type = "escalation",
                    title = "Escalation Alert",
                    description = "Budget Approval has been escalated to you",
                    taskName = "Budget Approval",
                    time = "08:10 AM",
                    dateGroup = "Today",
                    isUnread = true,
                    priority = "Urgent",
                    priorityColor = Color(0xFFEF4444),
                    priorityBg = Color(0xFFFEF2F2),
                    icon = Icons.Default.Info,
                    iconTint = Color(0xFFD97706),
                    iconBg = Color(0xFFFFFBEB)
                ),
                NotificationItem(
                    id = "n5",
                    type = "reminder",
                    title = "Daily Reporting Reminder",
                    description = "Don't forget to submit your daily report",
                    taskName = "Daily Report",
                    time = "07:30 AM",
                    dateGroup = "Today",
                    isUnread = true,
                    priority = "Reminder",
                    priorityColor = Color(0xFF10B981),
                    priorityBg = Color(0xFFECFDF5),
                    icon = Icons.Default.Notifications,
                    iconTint = Color(0xFF10B981),
                    iconBg = Color(0xFFECFDF5)
                ),

                // YESTERDAY SECTION
                NotificationItem(
                    id = "n6",
                    type = "updated",
                    title = "Task Updated",
                    description = "Amruta Madam updated the task details",
                    taskName = "Website Redesign",
                    time = "Yesterday, 06:40 PM",
                    dateGroup = "Yesterday",
                    isUnread = false,
                    priority = "Low Priority",
                    priorityColor = Color(0xFF64748B),
                    priorityBg = Color(0xFFF8FAFC),
                    icon = Icons.Default.CheckCircle,
                    iconTint = Color(0xFF10B981),
                    iconBg = Color(0xFFECFDF5)
                ),
                NotificationItem(
                    id = "n7",
                    type = "comment",
                    title = "Comment Added",
                    description = "Chaitra Madam added a comment",
                    taskName = "Content Calendar",
                    time = "Yesterday, 04:20 PM",
                    dateGroup = "Yesterday",
                    isUnread = false,
                    priority = "Medium Priority",
                    priorityColor = Color(0xFF8B5CF6),
                    priorityBg = Color(0xFFF5F3FF),
                    icon = Icons.Default.Check,
                    iconTint = Color(0xFF3B82F6),
                    iconBg = Color(0xFFEFF6FF)
                ),
                NotificationItem(
                    id = "n8",
                    type = "assigned_yesterday",
                    title = "Task Assigned",
                    description = "Finance audit file assigned by Kiran Madam",
                    taskName = "Annual Corporate Taxes Review",
                    time = "Yesterday, 01:10 PM",
                    dateGroup = "Yesterday",
                    isUnread = true,
                    priority = "High Priority",
                    priorityColor = Color(0xFFEF4444),
                    priorityBg = Color(0xFFFEF2F2),
                    icon = Icons.Default.Edit,
                    iconTint = Color(0xFF8B5CF6),
                    iconBg = Color(0xFFF5F3FF)
                ),

                // EARLIER SECTION
                NotificationItem(
                    id = "n9",
                    type = "reminder",
                    title = "Security Compliance Sync",
                    description = "Mandatory key rotations report submitted",
                    taskName = "Identity Verification Key Rotation",
                    time = "3 Days Ago",
                    dateGroup = "Earlier",
                    isUnread = false,
                    priority = "Reminder",
                    priorityColor = Color(0xFF10B981),
                    priorityBg = Color(0xFFECFDF5),
                    icon = Icons.Default.Lock,
                    iconTint = Color(0xFF0F172A),
                    iconBg = Color(0xFFF1F5F9)
                )
            )
        )
    }

    // Toggle Section collapsible states
    var todayExpanded by remember { mutableStateOf(true) }
    var yesterdayExpanded by remember { mutableStateOf(true) }
    var earlierExpanded by remember { mutableStateOf(true) }

    // Calculate aggregated values
    val currentUnreadCount = notificationsList.count { it.isUnread }
    val currentMentionsCount = notificationsList.count { it.type == "mention" }

    // Handle interactive operations
    val markAllAsRead: () -> Unit = {
        notificationsList = notificationsList.map { it.copy(isUnread = false) }
        triggerLocalToast("All active notifications marked as read! ✔️")
    }

    val toggleReadStatus: (String) -> Unit = { id ->
        notificationsList = notificationsList.map {
            if (it.id == id) {
                val nextStatus = !it.isUnread
                triggerLocalToast(if (nextStatus) "Marked as unread" else "Marked as read")
                it.copy(isUnread = nextStatus, isSwiped = false)
            } else it
        }
    }

    val snoozeNotification: (String) -> Unit = { id ->
        notificationsList = notificationsList.map {
            if (it.id == id) {
                triggerLocalToast("Notification snoozed for 1 hour ⏰")
                it.copy(isSwiped = false)
            } else it
        }
    }

    val deleteNotification: (String) -> Unit = { id ->
        val item = notificationsList.find { it.id == id }
        notificationsList = notificationsList.filter { it.id != id }
        triggerLocalToast("Deleted: ${item?.title ?: "Notification"}")
    }

    // Filter list
    val filteredNotifications = notificationsList.filter { item ->
        val queryMatches = if (searchQuery.isNotEmpty()) {
            item.title.contains(searchQuery, ignoreCase = true) ||
            item.description.contains(searchQuery, ignoreCase = true) ||
            item.taskName.contains(searchQuery, ignoreCase = true)
        } else {
            true
        }

        val filterMatches = when (selectedFilter) {
            "Unread" -> item.isUnread
            "Mentions" -> item.type == "mention"
            "Priority" -> item.priority.contains("High", ignoreCase = true) || item.priority.contains("Urgent", ignoreCase = true) || item.priority.contains("Overdue", ignoreCase = true)
            else -> true
        }

        queryMatches && filterMatches
    }

    val isDark = MaterialTheme.colorScheme.background != Color(0xFFF8FAFC)
    val bgScreen = if (isDark) Color(0xFF0A0F1D) else Color(0xFFFCFDFD)
    val textPrimary = if (isDark) Color.White else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val borderLight = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
    val cardBg = if (isDark) Color(0xFF131B2E) else Color.White
    val badgeBg = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9)

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(bgScreen)
            .testTag("notification_center_screen"),
        color = bgScreen
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top Header Section
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(cardBg)
                                .border(1.dp, borderLight, RoundedCornerShape(10.dp))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Navigate back",
                                tint = textPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "Notifications",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = textPrimary
                        )

                        if (currentUnreadCount > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color(0xFFEF4444))
                                    .padding(horizontal = 7.dp, vertical = 3.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$currentUnreadCount",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { showSearchBox = !showSearchBox },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(cardBg)
                                .border(1.dp, borderLight, RoundedCornerShape(10.dp))
                        ) {
                            Icon(
                                imageVector = if (showSearchBox) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = "Search notifications",
                                tint = textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                selectedFilter = "All"
                                triggerLocalToast("Filters reset to default view.")
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(cardBg)
                                .border(1.dp, borderLight, RoundedCornerShape(10.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.List,
                                contentDescription = "Filter menu list",
                                tint = textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Search Box collapsible block
                AnimatedVisibility(
                    visible = showSearchBox,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search alerts, team actions, tasks...", fontSize = 13.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    )
                }

                // Horizontal Filters Segment Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterTabChip(
                        label = "All",
                        badge = notificationsList.size,
                        isSelected = selectedFilter == "All",
                        onClick = { selectedFilter = "All" }
                    )

                    FilterTabChip(
                        label = "Unread",
                        badge = currentUnreadCount,
                        isSelected = selectedFilter == "Unread",
                        onClick = { selectedFilter = "Unread" }
                    )

                    FilterTabChip(
                        label = "Mentions",
                        badge = currentMentionsCount,
                        isSelected = selectedFilter == "Mentions",
                        onClick = { selectedFilter = "Mentions" }
                    )

                    FilterTabChip(
                        label = "Priority",
                        badge = notificationsList.count { it.priority.contains("High", ignoreCase = true) || it.priority.contains("Urgent", ignoreCase = true) || it.priority.contains("Overdue", ignoreCase = true) },
                        isSelected = selectedFilter == "Priority",
                        onClick = { selectedFilter = "Priority" }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Notifications ListView
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(bottom = 76.dp)
                ) {
                    // "Today" Section
                    val todayNotifications = filteredNotifications.filter { it.dateGroup == "Today" }
                    if (todayNotifications.isNotEmpty()) {
                        item {
                            SectionHeaderRow(
                                title = "Today",
                                showMarkAll = true,
                                iconBadgeCount = todayNotifications.count { it.isUnread },
                                isExpanded = todayExpanded,
                                onHeaderClick = { todayExpanded = !todayExpanded },
                                onMarkAllRead = markAllAsRead
                            )
                        }

                        if (todayExpanded) {
                            items(todayNotifications) { item ->
                                SwipeableNotificationCard(
                                    item = item,
                                    onToggleRead = { toggleReadStatus(item.id) },
                                    onSnooze = { snoozeNotification(item.id) },
                                    onDelete = { deleteNotification(item.id) },
                                    onDragStateChanged = { id, swiped ->
                                        notificationsList = notificationsList.map {
                                            if (it.id == id) it.copy(isSwiped = swiped) else it
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // "Yesterday" Section
                    val yesterdayNotifications = filteredNotifications.filter { it.dateGroup == "Yesterday" }
                    if (yesterdayNotifications.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                            SectionHeaderRow(
                                title = "Yesterday",
                                showMarkAll = false,
                                iconBadgeCount = yesterdayNotifications.count { it.isUnread },
                                isExpanded = yesterdayExpanded,
                                onHeaderClick = { yesterdayExpanded = !yesterdayExpanded }
                            )
                        }

                        if (yesterdayExpanded) {
                            items(yesterdayNotifications) { item ->
                                SwipeableNotificationCard(
                                    item = item,
                                    onToggleRead = { toggleReadStatus(item.id) },
                                    onSnooze = { snoozeNotification(item.id) },
                                    onDelete = { deleteNotification(item.id) },
                                    onDragStateChanged = { id, swiped ->
                                        notificationsList = notificationsList.map {
                                            if (it.id == id) it.copy(isSwiped = swiped) else it
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // "Earlier" Section
                    val earlierNotifications = filteredNotifications.filter { it.dateGroup == "Earlier" }
                    if (earlierNotifications.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                            SectionHeaderRow(
                                title = "Earlier",
                                showMarkAll = false,
                                iconBadgeCount = earlierNotifications.count { it.isUnread },
                                isExpanded = earlierExpanded,
                                onHeaderClick = { earlierExpanded = !earlierExpanded }
                            )
                        }

                        if (earlierExpanded) {
                            items(earlierNotifications) { item ->
                                SwipeableNotificationCard(
                                    item = item,
                                    onToggleRead = { toggleReadStatus(item.id) },
                                    onSnooze = { snoozeNotification(item.id) },
                                    onDelete = { deleteNotification(item.id) },
                                    onDragStateChanged = { id, swiped ->
                                        notificationsList = notificationsList.map {
                                            if (it.id == id) it.copy(isSwiped = swiped) else it
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // Empty State for Filtered Notification List
                    if (filteredNotifications.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 60.dp, horizontal = 24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFF1F5F9)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = null,
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "All Caught Up!",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF0F172A)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "There are no notifications matching your search or segment. Take a break!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF64748B),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
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
}

@Composable
fun FilterTabChip(
    label: String,
    badge: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val containerBg = if (isSelected) Color(0xFFEFF6FF) else Color(0xFFF8FAFC)
    val textAndBorderColor = if (isSelected) Color(0xFF2563EB) else Color(0xFF64748B)

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(32.dp))
            .background(containerBg)
            .border(
                1.dp,
                if (isSelected) Color(0xFFBFDBFE) else Color(0xFFE2E8F0),
                RoundedCornerShape(32.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
            color = textAndBorderColor
        )

        // Circle pill count
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(if (isSelected) Color(0xFF2563EB) else Color(0xFFCBD5E1))
                .padding(horizontal = 6.dp, vertical = 1.dp)
        ) {
            Text(
                text = "$badge",
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }
    }
}

@Composable
fun SectionHeaderRow(
    title: String,
    showMarkAll: Boolean,
    iconBadgeCount: Int,
    isExpanded: Boolean,
    onHeaderClick: () -> Unit,
    onMarkAllRead: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onHeaderClick() }
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = Color(0xFF0F172A)
            )

            if (iconBadgeCount > 0) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFF2563EB))
                        .padding(horizontal = 6.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "$iconBadgeCount",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                contentDescription = null,
                tint = Color(0xFF64748B),
                modifier = Modifier.size(16.dp)
            )
        }

        if (showMarkAll && iconBadgeCount > 0) {
            Text(
                text = "Mark all as read",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF2563EB),
                modifier = Modifier.clickable { onMarkAllRead() }
            )
        }
    }
}

@Composable
fun SwipeableNotificationCard(
    item: NotificationItem,
    onToggleRead: () -> Unit,
    onSnooze: () -> Unit,
    onDelete: () -> Unit,
    onDragStateChanged: (String, Boolean) -> Unit
) {
    // Dynamic swipe offsets
    val targetHorizontalOffset = if (item.isSwiped) -210f else 0f
    val animatedOffset by animateFloatAsState(
        targetValue = targetHorizontalOffset,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium)
    )

    // Fallback UI default configs if item properties are undefined
    val itemIcon = when (item.type) {
        "mention" -> Icons.Default.Person
        "assigned_yesterday" -> Icons.Default.Edit
        else -> item.icon
    }

    val itemIconTint = when (item.type) {
        "mention" -> Color(0xFF8B5CF6)
        "assigned_yesterday" -> Color(0xFF2563EB)
        else -> item.iconTint
    }

    val itemIconBg = when (item.type) {
        "mention" -> Color(0xFFF5F3FF)
        "assigned_yesterday" -> Color(0xFFEFF6FF)
        else -> item.iconBg
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF1F5F9))
    ) {
        // Red, Orange, Blue Swipe background action buttons
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .align(Alignment.CenterEnd)
                .width(210.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Blue Button "Mark Read"
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .background(Color(0xFF2563EB))
                    .clickable { onToggleRead() },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if (item.isUnread) Icons.Default.Email else Icons.Default.Check,
                        contentDescription = "Mark status",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (item.isUnread) "Read" else "Unread",
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Orange Button "Snooze"
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .background(Color(0xFFFFB703))
                    .clickable { onSnooze() },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Snooze timer alert",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Snooze",
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Red Button "Delete"
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .background(Color(0xFFEF4444))
                    .clickable { onDelete() },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete alarm notification",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Delete",
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Swipable card foreground body
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { change, dragAmount ->
                        change.consume()
                        if (dragAmount < -10) {
                            onDragStateChanged(item.id, true)
                        } else if (dragAmount > 10) {
                            onDragStateChanged(item.id, false)
                        }
                    }
                }
                .shadow(0.5.dp, RoundedCornerShape(16.dp), ambientColor = Color.LightGray),
            colors = CardDefaults.cardColors(
                containerColor = if (item.isUnread && item.isSwiped.not()) Color.White else Color(0xFFF8FAFC)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDragStateChanged(item.id, !item.isSwiped) }
                    .padding(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Left Icon Block with Unread Status Indicator Circle
                Box {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(itemIconBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = itemIcon,
                            contentDescription = null,
                            tint = itemIconTint,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    if (item.isUnread) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .padding(1.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(
                                        if (item.priority.contains("Urgent") || item.priority.contains("Overdue")) Color(0xFFEF4444)
                                        else Color(0xFF2563EB)
                                    )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Detail body
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0F172A)
                        )

                        Text(
                            text = item.time,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = item.description,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Clickable task name identifier
                    Text(
                        text = item.taskName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF2563EB),
                        modifier = Modifier.clickable { onToggleRead() }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Badge status
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(item.priorityBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.priority,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = item.priorityColor
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color(0xFFCBD5E1),
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.CenterVertically)
                )
            }
        }
    }
}
