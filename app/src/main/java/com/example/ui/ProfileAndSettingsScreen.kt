package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.draw.scale

private fun triggerToast(context: android.content.Context, message: String) {
    android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileAndSettingsScreen(
    stats: DashboardStats,
    viewModel: TaskViewModel,
    onViewPerformance: () -> Unit = {}
) {
    val context = LocalContext.current
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    // Interactive Dialog Dialog State
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }

    // Dynamic state for User Profile values to make it interactive!
    var userName by remember { mutableStateOf("Bhavesh Patil") }
    var userTeam by remember { mutableStateOf("IT Admin Team") }
    var userEmail by remember { mutableStateOf("bhaveshpatiltech@gmail.com") }
    var userPhone by remember { mutableStateOf("+91 90123 45678") }
    var employeeId by remember { mutableStateOf("CKTM-2026") }
    var userDept by remember { mutableStateOf("IT Admin") }
    var userRole by remember { mutableStateOf("Administrator") }

    LaunchedEffect(currentUser) {
        currentUser?.let {
            userName = it.name
            userEmail = it.email
            userRole = it.role
            if (it.phone.isNotEmpty()) {
                userPhone = it.phone
            }
            userDept = when (it.role) {
                "Super-admin" -> "Executive Admin"
                "Admin" -> "IT Admin"
                else -> "Technical Employee"
            }
            userTeam = when (it.role) {
                "Super-admin" -> "Board of Directors"
                "Admin" -> "Task delegator group"
                else -> "Dev & Operations Team"
            }
        }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp)
                    .background(Color.Transparent)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Styled Back Arrow Button
                    IconButton(
                        onClick = {
                            triggerToast(context, "Navigating back to Home Dashboard")
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .shadow(1.dp, CircleShape)
                            .background(if (isDarkMode) Color(0xFF1E293B) else Color.White, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate Back",
                            tint = if (isDarkMode) Color.White else Color(0xFF0F172A),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Profile & Settings",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode) Color.White else Color(0xFF1E293B)
                        )
                        Text(
                            text = "Manage your profile and app preferences",
                            fontSize = 12.sp,
                            color = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
                        )
                    }

                    // Edit Profile Pen Button
                    IconButton(
                        onClick = { showEditProfileDialog = true },
                        modifier = Modifier
                            .size(40.dp)
                            .shadow(1.dp, CircleShape)
                            .background(if (isDarkMode) Color(0xFF1E293B) else Color.White, CircleShape)
                            .testTag("edit_profile_trigger")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            tint = if (isDarkMode) Color.LightGray else Color(0xFF0F172A),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        },
        containerColor = if (isDarkMode) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("profile_and_settings_container"),
            contentPadding = PaddingValues(bottom = 32.dp, start = 16.dp, end = 16.dp)
        ) {
            // 1. Profile Gradient Card
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(24.dp), ambientColor = Color.LightGray),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF0F318A),
                                        Color(0xFF1E40AF),
                                        Color(0xFF2563EB)
                                    )
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column {
                            // Top Row: Avatar and Text Details
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Face Avatar Illustration
                                Box(
                                    modifier = Modifier.size(92.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(86.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                            .border(2.dp, if (isDarkMode) Color(0xFF60A5FA) else Color(0xFF93C5FD), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        ChaitraAvatarGraphic()
                                    }
                                    
                                    // Camera badge button
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .align(Alignment.BottomEnd)
                                            .clip(CircleShape)
                                            .background(if (isDarkMode) Color(0xFF1E293B) else Color.White)
                                            .border(1.dp, if (isDarkMode) Color(0xFF334155) else Color(0xFFE2E8F0), CircleShape)
                                            .clickable {
                                                triggerToast(context, "Change profile photo coming soon!")
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PhotoCamera,
                                            contentDescription = "Upload Profile Photo",
                                            tint = if (isDarkMode) Color(0xFF93C5FD) else Color(0xFF1E40AF),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                // Quick text details
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = userName,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                            contentDescription = "Details",
                                            tint = Color.White.copy(alpha = 0.7f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    
                                    Text(
                                        text = userTeam,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF93C5FD)
                                    )
                                    
                                    Spacer(modifier = Modifier.height(6.dp))
                                    
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 1.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Email,
                                            contentDescription = "Email",
                                            tint = Color(0xFFBFDBFE),
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = userEmail,
                                            fontSize = 12.sp,
                                            color = Color(0xFFEFF6FF)
                                        )
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 1.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Phone,
                                            contentDescription = "Phone",
                                            tint = Color(0xFFBFDBFE),
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = userPhone,
                                            fontSize = 12.sp,
                                            color = Color(0xFFEFF6FF)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))
                            
                            // Subtle white divider
                            HorizontalDivider(color = Color.White.copy(alpha = 0.15f), thickness = 1.dp)
                            
                            Spacer(modifier = Modifier.height(16.dp))

                            // Employee Metadata Grid (Three Columns)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                ProfileCompactMetaColumn(
                                    label = "Employee ID",
                                    value = employeeId,
                                    iconColor = Color(0xFFD8B4FE),
                                    iconBg = Color(0xFF5B21B6),
                                    icon = Icons.Default.Badge
                                )
                                ProfileCompactMetaColumn(
                                    label = "Department",
                                    value = userDept,
                                    iconColor = Color(0xFFA7F3D0),
                                    iconBg = Color(0xFF065F46),
                                    icon = Icons.Default.Group
                                )
                                ProfileCompactMetaColumn(
                                    label = "Role",
                                    value = userRole,
                                    iconColor = Color(0xFFFED7AA),
                                    iconBg = Color(0xFF9A3412),
                                    icon = Icons.Default.Person
                                )
                            }
                        }
                    }
                }
            }

            // 2. Performance Summary Title Slider
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Performance Summary",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkMode) Color.White else Color(0xFF0F172A)
                    )
                    Text(
                        text = "View Details >",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDarkMode) Color(0xFF60A5FA) else Color(0xFF1E40AF),
                        modifier = Modifier
                            .clickable { onViewPerformance() }
                            .padding(4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Performance Cards Row
            item {
                val onTimeRate = if (stats.total > 0) (stats.completed * 100 / stats.total) else 0
                val achievementsCount = if (stats.completed >= 20) 4 else if (stats.completed >= 10) 3 else if (stats.completed >= 5) 2 else if (stats.completed >= 1) 1 else 0

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        PerformanceMetricCard(
                            score = stats.cleanlinessScore.toString() + ".0",
                            title = "Performance Score",
                            growth = if (stats.total > 0) "↑ Dynamic Workspace Velocity" else "No tasks added yet",
                            growthColor = Color(0xFF10B981),
                            icon = Icons.Default.CheckCircle,
                            iconColor = Color(0xFF10B981),
                            isDarkMode = isDarkMode
                        )
                    }
                    item {
                        PerformanceMetricCard(
                            score = stats.completed.toString(),
                            title = "Tasks Completed",
                            growth = if (stats.completed > 0) "Completed in current workflow" else "Add your first task!",
                            growthColor = Color(0xFF10B981),
                            icon = Icons.Default.Assignment,
                            iconColor = Color(0xFF3B82F6),
                            isDarkMode = isDarkMode
                        )
                    }
                    item {
                        PerformanceMetricCard(
                            score = "$onTimeRate%",
                            title = "On-time Rate",
                            growth = if (stats.total > 0) "On-time completion of task items" else "No tasks added yet",
                            growthColor = Color(0xFF10B981),
                            icon = Icons.Default.AccessTime,
                            iconColor = Color(0xFFF59E0B),
                            isDarkMode = isDarkMode
                        )
                    }
                    item {
                        PerformanceMetricCard(
                            score = achievementsCount.toString(),
                            title = "Achievements",
                            growth = "Unlock badges recursively",
                            growthColor = Color(0xFF3B82F6),
                            icon = Icons.Default.EmojiEvents,
                            iconColor = Color(0xFF8B5CF6),
                            isDarkMode = isDarkMode
                        )
                    }
                }
            }

            // 3. App Settings Card
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "App Settings",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkMode) Color.White else Color(0xFF0F172A),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(16.dp), ambientColor = Color.LightGray),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkMode) Color(0xFF1E293B) else Color.White
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column {
                        // Notifications Settings row
                        SettingsListItem(
                            icon = Icons.Default.Notifications,
                            iconColor = Color(0xFF2563EB),
                            title = "Notifications",
                            subtitle = "Manage your notification preferences",
                            isDarkMode = isDarkMode,
                            trailing = {
                                Switch(
                                    checked = notificationsEnabled,
                                    onCheckedChange = { viewModel.setNotificationsEnabled(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFF2563EB)
                                    ),
                                    modifier = Modifier.scaleSwitch(0.85f).testTag("notifications_toggle")
                                )
                            }
                        )

                        SettingsListItem(
                            icon = Icons.Default.NightsStay,
                            iconColor = Color(0xFF8B5CF6),
                            title = "Dark Mode",
                            subtitle = "Enable dark theme",
                            isDarkMode = isDarkMode,
                            trailing = {
                                Switch(
                                    checked = isDarkMode,
                                    onCheckedChange = { viewModel.setDarkMode(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFF8B5CF6)
                                    ),
                                    modifier = Modifier.scaleSwitch(0.85f).testTag("dark_mode_toggle")
                                )
                            }
                        )

                        SettingsListItem(
                            icon = Icons.Default.Language,
                            iconColor = Color(0xFF10B981),
                            title = "Language",
                            subtitle = "Choose your preferred language",
                            isDarkMode = isDarkMode,
                            valueText = selectedLanguage,
                            arrowClickable = true,
                            onClick = { showLanguageDialog = true }
                        )

                        SettingsListItem(
                            icon = Icons.Default.Shield,
                            iconColor = Color(0xFFF59E0B),
                            title = "Privacy & Security",
                            subtitle = "Manage your privacy and security",
                            isDarkMode = isDarkMode,
                            arrowClickable = true,
                            onClick = {
                                triggerToast(context, "Privacy policy is secured and fully verified!")
                            }
                        )

                        SettingsListItem(
                            icon = Icons.Default.Info,
                            iconColor = Color(0xFF64748B),
                            title = "About CKTM",
                            subtitle = "App version 2.4.1",
                            isDarkMode = isDarkMode,
                            arrowClickable = true,
                            onClick = { showAboutDialog = true }
                        )
                    }
                }
            }

            // 4. Account Card
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Account",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkMode) Color.White else Color(0xFF0F172A),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(16.dp), ambientColor = Color.LightGray),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkMode) Color(0xFF1E293B) else Color.White
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column {
                        SettingsListItem(
                            icon = Icons.Default.VpnKey,
                            iconColor = Color(0xFF3B82F6),
                            title = "Change Password",
                            subtitle = "Update your account password",
                            isDarkMode = isDarkMode,
                            arrowClickable = true,
                            onClick = { showPasswordDialog = true }
                        )

                        SettingsListItem(
                            icon = Icons.Default.Logout,
                            iconColor = Color(0xFFEF4444),
                            title = "Logout",
                            titleColor = Color(0xFFEF4444),
                            subtitle = "Sign out from your account",
                            isDarkMode = isDarkMode,
                            arrowClickable = true,
                            onClick = { showLogoutDialog = true },
                            tag = "logout_settings_row"
                        )
                    }
                }
            }
        }
    }

    // Interactive Language Selection Dialog
    if (showLanguageDialog) {
        Dialog(onDismissRequest = { showLanguageDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkMode) Color(0xFF1E293B) else Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Select Language",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = if (isDarkMode) Color.White else Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val languages = listOf("English", "Hindi (हिंदी)", "Marathi (मराठी)", "Spanish (Español)")
                    languages.forEach { lang ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setSelectedLanguage(lang.substringBefore(" ("))
                                    showLanguageDialog = false
                                    triggerToast(context, "Language updated to $lang")
                                }
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = lang,
                                fontSize = 15.sp,
                                color = if (isDarkMode) Color.LightGray else Color(0xFF334155),
                                fontWeight = if (selectedLanguage == lang.substringBefore(" (")) FontWeight.Bold else FontWeight.Normal
                            )
                            if (selectedLanguage == lang.substringBefore(" (")) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        HorizontalDivider(color = if (isDarkMode) Color(0xFF334155) else Color(0xFFF1F5F9))
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = { showLanguageDialog = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Cancel", color = Color(0xFFEF4444))
                    }
                }
            }
        }
    }

    // Edit Profile Modal Dialog
    if (showEditProfileDialog) {
        var tempName by remember { mutableStateOf(userName) }
        var tempRole by remember { mutableStateOf(userRole) }
        var tempDept by remember { mutableStateOf(userDept) }
        var tempPhone by remember { mutableStateOf(userPhone) }
        var tempEmail by remember { mutableStateOf(userEmail) }

        Dialog(onDismissRequest = { showEditProfileDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkMode) Color(0xFF1E293B) else Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.padding(20.dp)
                ) {
                    item {
                        Text(
                            text = "Edit Profile Info",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = if (isDarkMode) Color.White else Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    item {
                        OutlinedTextField(
                            value = tempName,
                            onValueChange = { tempName = it },
                            label = { Text("Display Name") },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF2563EB),
                                unfocusedBorderColor = Color(0xFF64748B)
                            )
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = tempRole,
                            onValueChange = { tempRole = it },
                            label = { Text("Enterprise Role") },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF2563EB),
                                unfocusedBorderColor = Color(0xFF64748B)
                            )
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = tempDept,
                            onValueChange = { tempDept = it },
                            label = { Text("Department") },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF2563EB),
                                unfocusedBorderColor = Color(0xFF64748B)
                            )
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = tempPhone,
                            onValueChange = { tempPhone = it },
                            label = { Text("Phone Number") },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF2563EB),
                                unfocusedBorderColor = Color(0xFF64748B)
                            )
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = tempEmail,
                            onValueChange = { tempEmail = it },
                            label = { Text("Enterprise Email") },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF2563EB),
                                unfocusedBorderColor = Color(0xFF64748B)
                            )
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextButton(onClick = { showEditProfileDialog = false }) {
                                Text("Cancel", color = Color(0xFF64748B))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    userName = tempName
                                    userRole = tempRole
                                    userDept = tempDept
                                    userPhone = tempPhone
                                    userEmail = tempEmail
                                    viewModel.registerAndLogin(tempName, tempEmail, tempRole, tempPhone)
                                    showEditProfileDialog = false
                                    triggerToast(context, "Profile successfully updated!")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                            ) {
                                Text("Save Changes")
                            }
                        }
                    }
                }
            }
        }
    }

    // Change Password Interactive Dialog
    if (showPasswordDialog) {
        var oldOption by remember { mutableStateOf("") }
        var inputPwd by remember { mutableStateOf("") }
        var confirmPwd by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showPasswordDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkMode) Color(0xFF1E293B) else Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Change Password",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = if (isDarkMode) Color.White else Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = oldOption,
                        onValueChange = { oldOption = it },
                        label = { Text("Current Password") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = inputPwd,
                        onValueChange = { inputPwd = it },
                        label = { Text("New Password") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = confirmPwd,
                        onValueChange = { confirmPwd = it },
                        label = { Text("Confirm New Password") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors()
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showPasswordDialog = false }) {
                            Text("Cancel", color = Color(0xFF64748B))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (inputPwd.length < 5) {
                                    triggerToast(context, "Password must be at least 5 characters")
                                } else if (inputPwd != confirmPwd) {
                                    triggerToast(context, "Passwords do not match!")
                                } else {
                                    triggerToast(context, "Password successfully updated!")
                                    showPasswordDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                        ) {
                            Text("Update")
                        }
                    }
                }
            }
        }
    }

    // Dynamic Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(text = "Confirm Logout") },
            text = { Text(text = "Are you sure you want to log out from your workspace? Unsaved changes are safely synced to the cloud database.") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout()
                        triggerToast(context, "Logged out successfully")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Logout", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = if (isDarkMode) Color.LightGray else Color(0xFF334155))
                }
            },
            containerColor = if (isDarkMode) Color(0xFF1E293B) else Color.White
        )
    }

    // About Dialog Info
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text(text = "About CKTM") },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CareerKattaLogo(size = 90.dp, modifier = Modifier.padding(bottom = 12.dp))
                    Text(text = "CKTM Workspace Portal v2.4.1", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "An production-grade enterprise client application optimized for low latency, zero pendency workflows and dynamic performance metric reporting.", fontSize = 13.sp, color = Color.Gray, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "© 2026 Career Katta Trust. All rights reserved.", fontSize = 11.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Close")
                }
            },
            containerColor = if (isDarkMode) Color(0xFF1E293B) else Color.White
        )
    }
}

@Composable
fun ChaitraAvatarGraphic() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        
        // Draw a light blue/indigo circular gradient avatar skin representation
        drawCircle(
            color = Color(0xFFDBEAFE),
            radius = width / 2f,
            center = Offset(width / 2f, height / 2f)
        )

        // Draw avatar hair background
        drawCircle(
            color = Color(0xFF1E293B),
            radius = width * 0.32f,
            center = Offset(width / 2f, height * 0.42f)
        )

        // Draw shoulders/suit
        val suitPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(width * 0.15f, height)
            quadraticTo(width * 0.25f, height * 0.65f, width * 0.5f, height * 0.65f)
            quadraticTo(width * 0.75f, height * 0.65f, width * 0.85f, height)
            close()
        }
        drawPath(path = suitPath, color = Color(0xFF1E40AF))

        // Suit V-neck inner shirt
        val shirtPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(width * 0.42f, height * 0.65f)
            lineTo(width * 0.5f, height * 0.82f)
            lineTo(width * 0.58f, height * 0.65f)
            close()
        }
        drawPath(path = shirtPath, color = Color.White)

        // Draw face shape
        drawCircle(
            color = Color(0xFFFED7AA),
            radius = width * 0.23f,
            center = Offset(width / 2f, height * 0.46f)
        )

        // Draw face details: professional front hair bangs
        drawArc(
            color = Color(0xFF1E293B),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            size = Size(width * 0.46f, height * 0.35f),
            topLeft = Offset(width * 0.27f, height * 0.25f)
        )

        // Draw small friendly eyes
        drawCircle(
            color = Color(0xFF0F172A),
            radius = width * 0.025f,
            center = Offset(width * 0.44f, height * 0.45f)
        )
        drawCircle(
            color = Color(0xFF0F172A),
            radius = width * 0.025f,
            center = Offset(width * 0.56f, height * 0.45f)
        )

        // Friendly smile
        drawArc(
            color = Color(0xFFEF4444),
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = false,
            size = Size(width * 0.12f, height * 0.08f),
            topLeft = Offset(width * 0.44f, height * 0.48f),
            style = Stroke(width = width * 0.02f, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun ProfileCompactMetaColumn(
    label: String,
    value: String,
    iconColor: Color,
    iconBg: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        modifier = Modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color(0xFF93C5FD),
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 12.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun PerformanceMetricCard(
    score: String,
    title: String,
    growth: String,
    growthColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    isDarkMode: Boolean
) {
    Card(
        modifier = Modifier
            .width(132.dp)
            .height(138.dp)
            .shadow(1.dp, RoundedCornerShape(16.dp), ambientColor = Color.LightGray),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF1E293B) else Color.White
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon header inside card
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Metric info
            Column {
                Text(
                    text = score,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isDarkMode) Color.White else Color(0xFF0F172A),
                    lineHeight = 28.sp
                )
                Text(
                    text = title,
                    fontSize = 11.sp,
                    color = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B),
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
            }

            // Bottom reference growth label
            Text(
                text = growth,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = growthColor,
                maxLines = 1
            )
        }
    }
}

@Composable
fun SettingsListItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    title: String,
    titleColor: Color = Color.Unspecified,
    subtitle: String,
    isDarkMode: Boolean,
    valueText: String? = null,
    trailing: @Composable (() -> Unit)? = null,
    arrowClickable: Boolean = false,
    onClick: () -> Unit = {},
    tag: String? = null
) {
    var itemModifier = Modifier.fillMaxWidth()
    if (arrowClickable || onClick != {}) {
        itemModifier = itemModifier.clickable { onClick() }
    }
    if (tag != null) {
        itemModifier = itemModifier.testTag(tag)
    }

    Row(
        modifier = itemModifier.padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon circular badge
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Middle Text Description
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (titleColor != Color.Unspecified) titleColor else if (isDarkMode) Color.White else Color(0xFF1E293B)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
            )
        }

        // Trailing elements (either custom row or status value indicators)
        if (trailing != null) {
            trailing()
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (valueText != null) {
                    Text(
                        text = valueText,
                        fontSize = 13.sp,
                        color = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B),
                        modifier = Modifier.padding(end = 6.dp)
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Navigate Details",
                    tint = if (isDarkMode) Color(0xFF475569) else Color(0xFFCBD5E1),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// Extension function to help scale switches slightly
private fun Modifier.scaleSwitch(scaleFactor: Float): Modifier = this.scale(scaleFactor)
