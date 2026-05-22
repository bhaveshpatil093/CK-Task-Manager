package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CareerKattaLogo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(viewModel: TaskViewModel) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("Employee") } // "Super-admin", "Admin", "Employee"

    var nameError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }

    val rolesList = listOf(
        Triple("Super-admin", "Super-admin 👑", "Full system control and privilege of assigning tasks"),
        Triple("Admin", "Admin 🛡️", "Administrative access to create and assign tasks"),
        Triple("Employee", "Employee 👤", "Access assigned tasks, track goals, and update statuses")
    )

    val infiniteTransition = rememberInfiniteTransition(label = "SignupGlow")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)) // Minimal clean background
    ) {
        // Decorative background gradient blobs
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 60.dp, y = (-60).dp)
                .size(240.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFFE0F2FE).copy(alpha = 0.6f), Color.Transparent)
                    )
                )
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-40).dp, y = 40.dp)
                .size(280.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFFFDF4FF).copy(alpha = 0.6f), Color.Transparent)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Brand Logo Header
            CareerKattaLogo(
                size = 130.dp,
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = pulseScale
                        scaleY = pulseScale
                    }
                    .testTag("signup_logo")
            )

            Text(
                text = "Welcome to Career Katta",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0F172A),
                    fontSize = 24.sp,
                    letterSpacing = (-0.5).sp
                ),
                textAlign = TextAlign.Center
            )

            Text(
                text = "Create your professional workforce profile to connect, complete, and track tasks seamlessly.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Main Signup Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(3.dp, RoundedCornerShape(24.dp), ambientColor = Color.LightGray),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Register Profile",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )

                    // Name Input
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Full Name",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF475569)
                        )
                        OutlinedTextField(
                            value = name,
                            onValueChange = {
                                name = it
                                nameError = false
                            },
                            placeholder = { Text("Enter your full name") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "User Icon",
                                    tint = Color(0xFFD946EF)
                                )
                            },
                            isError = nameError,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("signup_name_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF1E40AF),
                                unfocusedBorderColor = Color(0xFFE2E8F0)
                            )
                        )
                        if (nameError) {
                            Text(
                                text = "Name cannot be empty",
                                color = Color.Red,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }

                    // Email Input
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Email Address",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF475569)
                        )
                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                emailError = false
                            },
                            placeholder = { Text("name@example.com") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "Email Icon",
                                    tint = Color(0xFF1E40AF)
                                )
                            },
                            isError = emailError,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Done
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("signup_email_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF1E40AF),
                                unfocusedBorderColor = Color(0xFFE2E8F0)
                            )
                        )
                        if (emailError) {
                            Text(
                                text = "Please enter a valid email address",
                                color = Color.Red,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }

                    // Phone Number Input
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Phone Number",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF475569)
                        )
                        OutlinedTextField(
                            value = phone,
                            onValueChange = {
                                phone = it
                                phoneError = false
                            },
                            placeholder = { Text("+91 98765 43210") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = "Phone Icon",
                                    tint = Color(0xFF16A34A)
                                )
                            },
                            isError = phoneError,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Done
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("signup_phone_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF1E40AF),
                                unfocusedBorderColor = Color(0xFFE2E8F0)
                            )
                        )
                        if (phoneError) {
                            Text(
                                text = "Please enter a valid phone number",
                                color = Color.Red,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }

                    // Role Select
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Select Workspace Role",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF475569)
                        )

                        rolesList.forEach { (roleKey, roleLabel, roleDesc) ->
                            val isSelected = selectedRole == roleKey
                            Card(
                                onClick = { selectedRole = roleKey },
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) Color(0xFF1E40AF) else Color(0xFFE2E8F0)
                                ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color(0xFFEFF6FF) else Color(0xFFF8FAFC)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("signup_role_card_$roleKey")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedRole = roleKey },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = Color(0xFF1E40AF),
                                            unselectedColor = Color(0xFF94A3B8)
                                        )
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = roleLabel,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = if (isSelected) Color(0xFF1E40AF) else Color(0xFF0F172A)
                                        )
                                        Text(
                                            text = roleDesc,
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B),
                                            lineHeight = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Launch Workspace Button
                    Button(
                        onClick = {
                            var hasError = false
                            if (name.trim().isEmpty()) {
                                nameError = true
                                hasError = true
                            }
                            if (email.trim().isEmpty() || !email.contains("@")) {
                                emailError = true
                                hasError = true
                            }
                            if (phone.trim().isEmpty()) {
                                phoneError = true
                                hasError = true
                            }

                            if (!hasError) {
                                viewModel.registerAndLogin(name, email, selectedRole, phone)
                                Toast.makeText(context, "Welcome, ${name}! Profile created successfully 🎉", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E40AF),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("signup_submit_button")
                    ) {
                        Text(
                            text = "Register & Launch Workspace",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer branding security signature lock
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Shield Secured Icon",
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Career Katta Trust • Enterprise Portal",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
