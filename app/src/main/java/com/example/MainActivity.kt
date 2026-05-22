package com.example

import android.app.Application
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.Task
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.TaskViewModel
import com.example.ui.TaskViewModelFactory
import com.example.ui.DashboardStats
import com.example.ui.CareerKattaLogo
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    companion object {
        fun triggerCustomToast(context: android.content.Context, message: String) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val app = context.applicationContext as Application
            val taskViewModel: TaskViewModel = viewModel(
                factory = TaskViewModelFactory(app)
            )
            val isDarkMode by taskViewModel.isDarkMode.collectAsState()
            
            MyApplicationTheme(darkTheme = isDarkMode) {
                MainNavigation(taskViewModel)
            }
        }
    }
}

@Composable
fun MainNavigation(viewModel: TaskViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    var showSplash by remember { mutableStateOf(true) }

    if (showSplash) {
        SplashScreen(onLoadingFinished = { showSplash = false })
    } else {
        if (currentUser == null) {
            com.example.ui.SignupScreen(viewModel = viewModel)
        } else {
            DashboardScreen(viewModel = viewModel)
        }
    }
}

// ----------------- 1. SPLASH SCREEN UI -----------------
@Composable
fun SplashScreen(onLoadingFinished: () -> Unit) {
    // Subtle loading animation values
    val infiniteTransition = rememberInfiniteTransition(label = "Loading")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Angle"
    )

    // Pulse animation for the logo glow
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutCirc),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Pulse"
    )

    // Automatically transition to workspace after a clean load cycle
    LaunchedEffect(Unit) {
        delay(2500)
        onLoadingFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E40AF)) // deep blue primary color (#1e40af)
    ) {
        // Decorative Mesh Gradient Background
        // Blob 1: Top-Left
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-80).dp, y = (-80).dp)
                .size(320.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF3B82F6).copy(alpha = 0.40f), Color.Transparent)
                    )
                )
        )
        // Blob 2: Middle-Right
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = 80.dp, y = 0.dp)
                .size(256.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF1D4ED8).copy(alpha = 0.30f), Color.Transparent)
                    )
                )
        )
        // Blob 3: Bottom-Left
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 40.dp, y = 80.dp)
                .size(384.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF1E3A8A).copy(alpha = 0.50f), Color.Transparent)
                    )
                )
        )

        // Main Layout Column (matches React Native mockup exactly)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 48.dp, horizontal = 24.dp)
        ) {
            // Top spacer
            Spacer(modifier = Modifier.height(12.dp))

            // Centered Logo & Branding
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Logo Wrapper with dynamic scale animation and shadow
                CareerKattaLogo(
                    size = 140.dp,
                    modifier = Modifier
                        .padding(bottom = 32.dp)
                        .graphicsLayer {
                            scaleX = pulseScale
                            scaleY = pulseScale
                        }
                )

                // App Identity
                Text(
                    text = "Career Katta",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 32.sp,
                        letterSpacing = (-0.5).sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "TASK MANAGER",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFDBEAFE).copy(alpha = 0.70f),
                        letterSpacing = 3.sp
                    ),
                    textAlign = TextAlign.Center
                )

                // Tagline Divider & Description from HTML mockup
                Spacer(modifier = Modifier.height(48.dp))
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(1.dp)
                        .background(Color.White.copy(alpha = 0.30f))
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Zero Pendency",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color(0xFFDBEAFE),
                        fontWeight = FontWeight.Light,
                        fontSize = 18.sp
                    ),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Workflow System",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp
                    ),
                    textAlign = TextAlign.Center
                )
            }

            // Footer Branding & Loader with WhatsApp simplicity
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                // Minimal Animated Loader Representation
                val animOffset by infiniteTransition.animateFloat(
                    initialValue = -0.5f,
                    targetValue = 1.5f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1800, easing = EaseInOutSine),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "LoaderOffset"
                )

                Box(
                    modifier = Modifier
                        .width(192.dp)
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.10f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(0.35f)
                            .align(Alignment.CenterStart)
                            .graphicsLayer {
                                translationX = animOffset * 192.dp.toPx()
                            }
                            .clip(CircleShape)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF60A5FA), Color(0xFF93C5FD), Color(0xFF60A5FA))
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "FROM",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 4.sp,
                        color = Color(0xFF93C5FD).copy(alpha = 0.50f),
                        fontWeight = FontWeight.SemiBold
                    ),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "CAREER KATTA",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        letterSpacing = 2.sp,
                        color = Color.White.copy(alpha = 0.90f),
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Fast Track Entry Button (One-hand optimized)
                Button(
                    onClick = onLoadingFinished,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.12f),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("enter_workspace_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Enter Workspace",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}


// ----------------- OTP LOGIN SCREEN UI -----------------
data class CountryCode(val name: String, val flag: String, val code: String)

@Composable
fun WorkflowIllustration() {
    val infiniteTransition = rememberInfiniteTransition(label = "Illustration")
    val translationX1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle1"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(115.dp)
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        // Subtle dotted connection path drawn in background
        Canvas(modifier = Modifier.fillMaxWidth()) {
            val width = size.width
            val height = size.height

            val p1x = 70.dp.toPx()
            val p1y = height / 2f
            val p2x = width - 70.dp.toPx()
            val p2y = height / 2f
            
            drawLine(
                color = Color(0xFFE2E8F0),
                start = androidx.compose.ui.geometry.Offset(p1x, p1y),
                end = androidx.compose.ui.geometry.Offset(p2x, p2y),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )

            // Animated pulsing data indicator traveling from left to right along the route
            drawCircle(
                color = Color(0xFF1E40AF),
                radius = 6.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(
                    p1x + (p2x - p1x) * translationX1,
                    p1y
                )
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Teammate 1 (Left bubble)
            Box(
                modifier = Modifier
                    .shadow(3.dp, CircleShape)
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(2.dp, Color(0xFF1E40AF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text("👨‍💻", fontSize = 16.sp)
                    Text("Aniket", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, fontSize = 8.sp, color = Color(0xFF1E40AF))
                }
            }

            // Central Pending Task Card (Linear style)
            Box(
                modifier = Modifier
                    .shadow(4.dp, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .widthIn(max = 130.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFFEE2E2))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("HIGH PRIORITY", style = MaterialTheme.typography.labelSmall, color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 7.sp)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Dispatch Tasks", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A), fontSize = 10.sp, maxLines = 1)
                }
            }

            // Teammate 2 (Right bubble)
            Box(
                modifier = Modifier
                    .shadow(3.dp, CircleShape)
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(2.dp, Color(0xFF10B981), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text("👩‍💼", fontSize = 16.sp)
                    Text("Priya", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, fontSize = 8.sp, color = Color(0xFF10B981))
                }
            }
        }
    }
}

@Composable
fun OtpLoginScreen(
    onLoginSuccess: () -> Unit,
    onBackToSplash: () -> Unit
) {
    val context = LocalContext.current
    
    // Auto-forward immediately to bypass login auth
    LaunchedEffect(Unit) {
        onLoginSuccess()
    }
    
    // Country Codes Dataset
    val countryList = listOf(
        CountryCode("India", "🇮🇳", "+91"),
        CountryCode("United States", "🇺🇸", "+1"),
        CountryCode("United Kingdom", "🇬🇧", "+44"),
        CountryCode("UAE", "🇦🇪", "+971"),
        CountryCode("Singapore", "🇸🇬", "+65"),
        CountryCode("Canada", "🇨🇦", "+1")
    )

    var selectedCountry by remember { mutableStateOf(countryList[0]) }
    var phoneNumber by remember { mutableStateOf("") }
    var showCountryPicker by remember { mutableStateOf(false) }
    
    // OTP dispatch cycle states
    var isOtpSent by remember { mutableStateOf(false) }
    var otpCode by remember { mutableStateOf("") }
    var isVerifying by remember { mutableStateOf(false) }
    var isSending by remember { mutableStateOf(false) }
    var countdownSeconds by remember { mutableStateOf(30) }

    val focusRequester = remember { FocusRequester() }

    // Countdown Timer logic for premium feel
    LaunchedEffect(isOtpSent) {
        if (isOtpSent) {
            countdownSeconds = 30
            while (countdownSeconds > 0) {
                delay(1000)
                countdownSeconds--
            }
        }
    }

    LaunchedEffect(isOtpSent) {
        if (isOtpSent) {
            delay(300)
            try {
                focusRequester.requestFocus()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)) // Minimal clean white-gray (#f8fafc)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .statusBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top minimal Navigation header (WhatsApp simplicity)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = {
                        if (isOtpSent) {
                            isOtpSent = false
                            otpCode = ""
                        } else {
                            onBackToSplash()
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White, CircleShape)
                        .border(1.dp, Color(0xFFE2E8F0), CircleShape)
                        .testTag("login_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Navigate Back",
                        tint = Color(0xFF0F172A),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = "Career Katta Workspace",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF475569)
                )

                // Placeholder empty spacer for horizontal alignment balance
                Spacer(modifier = Modifier.width(44.dp))
            }

            // Beautiful Workflow dynamic visual elements
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                CareerKattaLogo(size = 110.dp)
                
                Spacer(modifier = Modifier.height(12.dp))

                WorkflowIllustration()
                
                Spacer(modifier = Modifier.height(12.dp))

                // Welcome message
                Text(
                    text = "Unlock Clean Productivity",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0F172A),
                        fontSize = 24.sp,
                        letterSpacing = (-0.5).sp
                    ),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (isOtpSent) 
                        "We mailed a secure verification code to your device."
                    else 
                        "Complete verification with your phone number to access tasks.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Main Authentication Form Component
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .shadow(
                        elevation = 3.dp,
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = Color.LightGray
                    ),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (!isOtpSent) {
                        // STATE 1: Enter Mobile Number
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFF1F5F9))
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                                .clickable { showCountryPicker = true }
                                .padding(horizontal = 14.dp)
                        ) {
                            Text(
                                text = "${selectedCountry.flag}  ${selectedCountry.code}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .fillMaxHeight(0.5f)
                                    .background(Color(0xFFCBD5E1))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = selectedCountry.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF64748B)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Large phone input field
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() } && input.length <= 10) {
                                    phoneNumber = input
                                }
                            },
                            placeholder = {
                                Text(
                                    text = "Phone Number (10 digits)",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color(0xFF94A3B8)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = "Phone Icon",
                                    tint = Color(0xFF1E40AF),
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Send
                            ),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF1E40AF),
                                unfocusedBorderColor = Color(0xFFE2E8F0),
                                focusedContainerColor = Color(0xFFF8FAFC),
                                unfocusedContainerColor = Color(0xFFF8FAFC),
                                focusedTextColor = Color(0xFF0F172A),
                                unfocusedTextColor = Color(0xFF0F172A)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("phone_number_input")
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Send OTP Button
                        Button(
                            onClick = {
                                if (phoneNumber.trim().length < 8) {
                                    Toast.makeText(context, "Please enter a valid phone number", Toast.LENGTH_SHORT).show()
                                } else {
                                    isSending = true
                                    // Simulated rapid loading dispatch
                                    isSending = false
                                    isOtpSent = true
                                    Toast.makeText(context, "Verification code sent successfully!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = phoneNumber.trim().length >= 8 && !isSending,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1E40AF),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("send_otp_button")
                        ) {
                            if (isSending) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Text("Send Verification Code", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        // STATE 2: Enter Verification Code
                        Text(
                            text = "Enter 4-Digit Code",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        
                        // Show simulated OTP suggestion
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFEFF6FF))
                                .clickable {
                                    otpCode = "7788"
                                    try {
                                        focusRequester.requestFocus()
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "🔒 Fast test code: Tap here to prefill code '7788'",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E40AF)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Custom formatted 4-digit code fields container
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                        ) {
                            // Custom formatted 4-digit code fields (underneath)
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                for (i in 0 until 4) {
                                    val char = otpCode.getOrNull(i)?.toString() ?: ""
                                    val isFocused = otpCode.length == i
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isFocused) Color(0xFFEFF6FF) else Color(0xFFF1F5F9))
                                            .border(
                                                width = if (isFocused) 2.dp else 1.dp,
                                                color = if (isFocused) Color(0xFF1E40AF) else Color(0xFFCBD5E1),
                                                shape = RoundedCornerShape(12.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = char,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFF1E40AF),
                                            fontSize = 22.sp
                                        )
                                    }
                                }
                            }

                            // Invisible actual underlying numerical field for typing input (on top)
                            BasicTextField(
                                value = otpCode,
                                onValueChange = { input ->
                                    if (input.all { it.isDigit() } && input.length <= 4) {
                                        otpCode = input
                                    }
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    color = Color.Transparent,
                                    fontSize = 1.sp
                                ),
                                cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.Transparent),
                                modifier = Modifier
                                    .focusRequester(focusRequester)
                                    .matchParentSize()
                                    .testTag("otp_code_typed_input")
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Resend Countdown state
                        if (countdownSeconds > 0) {
                            Text(
                                text = "Resend OTP in ${countdownSeconds}s",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF64748B)
                            )
                        } else {
                            Text(
                                text = "Resend Code",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E40AF),
                                modifier = Modifier
                                    .clickable {
                                        countdownSeconds = 30
                                        Toast.makeText(context, "New code sent!", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Verify & Submit Button
                        Button(
                            onClick = {
                                if (otpCode == "7788" || otpCode.length == 4) {
                                    isVerifying = true
                                    onLoginSuccess()
                                    isVerifying = false
                                } else {
                                    Toast.makeText(context, "Invalid OTP code. Enter 7788 to verify successfully.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = otpCode.length == 4 && !isVerifying,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF10B981), // Dynamic success green color
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("verify_otp_button")
                        ) {
                            Text("Verify & Launch Workspace", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Bottom brand compliance and security signature
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Shield SECURED",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Fast Secure Login",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Career Katta • Zero Pendency workflow standard Pro v4.0",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }

    // Country Picker Dialog pop-up
    if (showCountryPicker) {
        AlertDialog(
            onDismissRequest = { showCountryPicker = false },
            title = {
                Text(
                    text = "Select Country Profile",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 240.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(countryList) { country ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedCountry = country
                                    showCountryPicker = false
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = country.flag, fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = country.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF0F172A),
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = country.code,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E40AF)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCountryPicker = false }) {
                    Text("Close", style = MaterialTheme.typography.labelLarge)
                }
            }
        )
    }
}


// ----------------- 2. ENTERPRISE TASK DASHBOARD SCREEN -----------------
@Composable
fun DashboardScreen(viewModel: TaskViewModel) {
    val context = LocalContext.current
    val tasks by viewModel.filteredTasks.collectAsStateWithLifecycle()
    val allTasksList by viewModel.allTasks.collectAsStateWithLifecycle()
    val stats by viewModel.dashboardStats.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedPriority by viewModel.selectedPriority.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf("Home") }
    var showAddTaskSheet by remember { mutableStateOf(false) }
    var voiceReportingTask by remember { mutableStateOf<com.example.data.Task?>(null) }
    var fileAttachingTask by remember { mutableStateOf<com.example.data.Task?>(null) }
    var countdownReportingTask by remember { mutableStateOf<com.example.data.Task?>(null) }
    var workflowTrackingTask by remember { mutableStateOf<com.example.data.Task?>(null) }
    var discussionTask by remember { mutableStateOf<com.example.data.Task?>(null) }

    // Dialog state for avatar and notifications
    var showProfileDialog by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var showPerformanceScreen by remember { mutableStateOf(false) }

    // Live ticking ticker (for live timers)
    var tickerCount by remember { mutableStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            tickerCount++
        }
    }

    val isDark = MaterialTheme.colorScheme.background == Color(0xFF0A0F1D)
    val bgScreen = if (isDark) Color(0xFF0A0F1D) else Color(0xFFF8FAFC)
    val cardBg = if (isDark) Color(0xFF131B2E) else Color.White
    val borderLight = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9)
    val activeColor = if (isDark) Color(0xFF3B82F6) else Color(0xFF1E40AF)
    val indicatorColorVal = if (isDark) Color(0xFF1E293B) else Color(0xFFEFF6FF)

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_root"),
        bottomBar = {
            NavigationBar(
                containerColor = cardBg,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .height(68.dp)
                    .border(1.dp, borderLight, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .navigationBarsPadding()
            ) {
                NavigationBarItem(
                    selected = selectedTab == "Home",
                    onClick = { selectedTab = "Home" },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home Tab") },
                    label = { Text("Home", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = activeColor,
                        selectedTextColor = activeColor,
                        indicatorColor = indicatorColorVal,
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = {
                        val user = viewModel.currentUser.value
                        if (user != null && user.role.equals("Employee", ignoreCase = true)) {
                            android.widget.Toast.makeText(context, "Access Denied: Employees cannot create or assign tasks 🛡️", android.widget.Toast.LENGTH_LONG).show()
                        } else {
                            showAddTaskSheet = true
                        }
                    },
                    icon = { 
                        Box(
                             modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(activeColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Create Task Icon", tint = Color.White)
                        }
                    },
                    label = { Text("Create", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedTextColor = Color(0xFF94A3B8)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == "Reports",
                    onClick = { selectedTab = "Reports" },
                    icon = { Icon(Icons.Default.Info, contentDescription = "Reports Tab") },
                    label = { Text("Reports", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = activeColor,
                        selectedTextColor = activeColor,
                        indicatorColor = indicatorColorVal,
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == "Profile",
                    onClick = { selectedTab = "Profile" },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile Tab") },
                    label = { Text("Profile", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = activeColor,
                        selectedTextColor = activeColor,
                        indicatorColor = indicatorColorVal,
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    )
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(bgScreen)
        ) {
            when (selectedTab) {
                "Home" -> {
                    DashboardHomeContent(
                        tasks = tasks,
                        allTasksList = allTasksList,
                        stats = stats,
                        searchQuery = searchQuery,
                        selectedCategory = selectedCategory,
                        selectedPriority = selectedPriority,
                        viewModel = viewModel,
                        tickerCount = tickerCount,
                        onAvatarClick = { showProfileDialog = true },
                        onNotificationClick = { showNotificationsDialog = true },
                        onVoiceReport = { voiceReportingTask = it },
                        onAttachmentClick = { fileAttachingTask = it },
                        onCountdownClick = { countdownReportingTask = it },
                        onWorkflowClick = { workflowTrackingTask = it },
                        onDiscussionClick = { discussionTask = it }
                    )
                }
                "Reports" -> {
                    DashboardReportsContent(
                        allTasksList = tasks,
                        stats = stats,
                        onViewPerformance = { showPerformanceScreen = true }
                    )
                }
                "Profile" -> {
                    com.example.ui.ProfileAndSettingsScreen(
                        stats = stats,
                        viewModel = viewModel,
                        onViewPerformance = { showPerformanceScreen = true }
                    )
                }
            }
        }
    }

    // Modal Add Task Sheet
    if (showAddTaskSheet) {
        AddTaskDialog(
            viewModel = viewModel,
            onDismiss = { showAddTaskSheet = false },
            onConfirm = { title, desc, priority, category, due, assignee ->
                viewModel.addTask(title, desc, priority, category, due, assignee)
                showAddTaskSheet = false
            }
        )
    }

    // Profile Dialog
    if (showProfileDialog) {
        ProfileDetailDialog(
            stats = stats,
            onDismiss = { showProfileDialog = false }
        )
    }

    // Notifications Overlay
    AnimatedVisibility(
        visible = showNotificationsDialog,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
    ) {
        com.example.ui.NotificationsScreen(
            onDismiss = { showNotificationsDialog = false }
        )
    }

    // Gamified Performance Screen overlay
    AnimatedVisibility(
        visible = showPerformanceScreen,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
    ) {
        com.example.ui.PerformanceScreen(
            allTasksList = allTasksList,
            stats = stats,
            onDismiss = { showPerformanceScreen = false }
        )
    }

    AnimatedVisibility(
        visible = voiceReportingTask != null,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
    ) {
        val currentTask = voiceReportingTask
        if (currentTask != null) {
            com.example.ui.VoiceReportScreen(
                task = currentTask,
                onDismiss = { voiceReportingTask = null },
                onSendUpdate = { transcription, markAsCompleted ->
                    val updatedTask = currentTask.copy(
                        description = transcription,
                        isCompleted = markAsCompleted
                    )
                    viewModel.updateTask(updatedTask)
                    voiceReportingTask = null
                    Toast.makeText(context, "Voice report sent! Task updated successfully 🎙️🚀", Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    AnimatedVisibility(
        visible = fileAttachingTask != null,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
    ) {
        val currentTask = fileAttachingTask
        if (currentTask != null) {
            com.example.ui.FileAttachmentScreen(
                task = currentTask,
                onDismiss = { fileAttachingTask = null },
                onAttachComplete = { stagedFiles ->
                    fileAttachingTask = null
                }
            )
        }
    }

    AnimatedVisibility(
        visible = countdownReportingTask != null,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
    ) {
        val currentTask = countdownReportingTask
        if (currentTask != null) {
            com.example.ui.TaskCountdownScreen(
                task = currentTask,
                onDismiss = { countdownReportingTask = null },
                onMarkCompleted = {
                    viewModel.toggleTaskCompletion(currentTask)
                    countdownReportingTask = null
                },
                onAddUpdate = { updateText ->
                    val updatedTask = currentTask.copy(
                        description = updateText
                    )
                    viewModel.updateTask(updatedTask)
                }
            )
        }
    }

    AnimatedVisibility(
        visible = workflowTrackingTask != null,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
    ) {
        val currentTask = workflowTrackingTask
        if (currentTask != null) {
            com.example.ui.WorkflowTrackingScreen(
                task = currentTask,
                onDismiss = { workflowTrackingTask = null },
                onTransferComplete = { nextStageName ->
                    // Optionally update state, log or toast
                }
            )
        }
    }

    AnimatedVisibility(
        visible = discussionTask != null,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
    ) {
        val currentTask = discussionTask
        if (currentTask != null) {
            com.example.ui.TaskDiscussionScreen(
                task = currentTask,
                viewModel = viewModel,
                onDismiss = { discussionTask = null },
                onStatusUpdated = { nextStage ->
                    val updatedTask = currentTask.copy(
                        description = "Milestone Stage $nextStage: " + currentTask.description.substringAfter(": ")
                    )
                    viewModel.updateTask(updatedTask)
                }
            )
        }
    }
}

@Composable
fun DashboardHomeContent(
    tasks: List<Task>,
    allTasksList: List<Task>,
    stats: DashboardStats,
    searchQuery: String,
    selectedCategory: String,
    selectedPriority: String,
    viewModel: TaskViewModel,
    tickerCount: Long,
    onAvatarClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onVoiceReport: (Task) -> Unit,
    onAttachmentClick: (Task) -> Unit,
    onCountdownClick: (Task) -> Unit,
    onWorkflowClick: (Task) -> Unit,
    onDiscussionClick: (Task) -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allowedTasks = remember(allTasksList, currentUser) {
        allTasksList.filter { task ->
            if (currentUser != null && currentUser!!.role.equals("Employee", ignoreCase = true)) {
                task.assignedTo.isNotBlank() && (
                    task.assignedTo.equals(currentUser!!.name, ignoreCase = true) ||
                    task.assignedTo.equals(currentUser!!.email, ignoreCase = true)
                )
            } else {
                true
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("task_list_container")
    ) {
        // top bar content
        item {
            val isDark = MaterialTheme.colorScheme.background == Color(0xFF0A0F1D)
            val headerBg = if (isDark) Color(0xFF131B2E) else Color.White
            val textPrimary = if (isDark) Color.White else Color(0xFF0F172A)
            val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
            val bellBg = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9)
            val avatarBorder = if (isDark) Color(0xFF3B82F6) else Color(0xFFDBEAFE)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerBg)
                    .padding(top = 40.dp, bottom = 18.dp)
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CareerKattaLogo(size = 46.dp)

                    Column {
                        Text(
                            text = "Good Morning, Bhavesh! 👋",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = textPrimary,
                            letterSpacing = (-0.5).sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Career Katta Task Manager • Admin",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Notification Bell
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(bellBg)
                            .clickable { onNotificationClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications bell trigger",
                            tint = textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444))
                                .align(Alignment.TopEnd)
                        )
                    }

                    // Profile Avatar
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E40AF))
                            .border(2.dp, avatarBorder, CircleShape)
                            .clickable { onAvatarClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "BP",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // Live status metrics section - 6 high fidelity cards
        item {
            val isDark = MaterialTheme.colorScheme.background == Color(0xFF0A0F1D)
            val titleColor = if (isDark) Color.White else Color(0xFF334155)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "System Cockpit Analytics",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = titleColor,
                    modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
                )

                val total = allowedTasks.size
                val pending = allowedTasks.count { !it.isCompleted }
                val solvedToday = allowedTasks.count { it.isCompleted && (it.dueDate.equals("Today", ignoreCase = true) || it.dueDate.equals("Yesterday", ignoreCase = true)) }
                val todayTasks = allowedTasks.count { it.dueDate.equals("Today", ignoreCase = true) }
                val pendencyPercentage = if (total > 0) (pending.toFloat() / total * 100f).toInt() else 0
                val zeroPendencyIndex = 100 - pendencyPercentage
                val performanceScore = if (total > 0) ((stats.completed.toFloat() / total * 80) + 20).toInt() else 100
                val delayedTasks = allowedTasks.count { !it.isCompleted && (it.dueDate.equals("Yesterday", ignoreCase = true) || it.dueDate.contains("Yesterday", ignoreCase = true)) }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DashboardStatCard(
                            title = "Today's Tasks",
                            value = todayTasks.toString(),
                            subtitle = "Due before midnight",
                            colorAccent = Color(0xFF1E40AF),
                            bgColor = Color(0xFFEFF6FF),
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setSearchQuery("") }
                        )
                        DashboardStatCard(
                            title = "Pending Issues",
                            value = pending.toString(),
                            subtitle = "Requires resolution",
                            colorAccent = Color(0xFFEF4444),
                            bgColor = Color(0xFFFEE2E2),
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setSelectedPriority("All") }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DashboardStatCard(
                            title = "Solved Today",
                            value = solvedToday.toString(),
                            subtitle = "Closed & archived",
                            colorAccent = Color(0xFF10B981),
                            bgColor = Color(0xFFD1FAE5),
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setSearchQuery("") }
                        )
                        DashboardStatCard(
                            title = "Zero Pendency %",
                            value = "${zeroPendencyIndex}%",
                            subtitle = "Cleanliness indicator",
                            colorAccent = Color(0xFF8B5CF6),
                            bgColor = Color(0xFFF3E8FF),
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setSelectedCategory("All") }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DashboardStatCard(
                            title = "Performance Score",
                            value = "$performanceScore/100",
                            subtitle = "Workspace velocity",
                            colorAccent = Color(0xFFF59E0B),
                            bgColor = Color(0xFFFEF3C7),
                            modifier = Modifier.weight(1f),
                            onClick = {}
                        )
                        DashboardStatCard(
                            title = "Delayed Tasks",
                            value = delayedTasks.toString(),
                            subtitle = "Overdue blocks",
                            colorAccent = Color(0xFFEA580C),
                            bgColor = Color(0xFFFFEDD5),
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setSelectedPriority("HIGH") }
                        )
                    }
                }
            }
        }

        // Minimal Canvas velocity graph
        item {
            WorkloadVelocityGraph(allowedTasks)
        }

        // Search Bar Section
        item {
            Spacer(modifier = Modifier.height(10.dp))
            SearchBarSection(query = searchQuery, onQueryChanged = { viewModel.setSearchQuery(it) })
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Category Selection
        item {
            CategoryFilterBar(selectedCategory = selectedCategory, onCategorySelected = { viewModel.setSelectedCategory(it) })
            Spacer(modifier = Modifier.height(4.dp))
            PriorityFilterBar(selectedPriority = selectedPriority, onPrioritySelected = { viewModel.setSelectedPriority(it) })
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Section Title
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Active Tasks Backlog (${tasks.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )

                if (selectedCategory != "All" || selectedPriority != "All" || searchQuery.isNotEmpty()) {
                    Text(
                        text = "Clear Filters",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF1E40AF),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable {
                            viewModel.setSelectedCategory("All")
                            viewModel.setSelectedPriority("All")
                            viewModel.setSearchQuery("")
                        }
                    )
                }
            }
        }

        // Tasks items
        if (tasks.isEmpty()) {
            item {
                EmptyStateSection(
                    hasFilters = selectedCategory != "All" || selectedPriority != "All" || searchQuery.isNotEmpty()
                )
            }
        } else {
            items(tasks, key = { it.id }) { task ->
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    TaskCardWithTimer(
                        task = task,
                        tickerCount = tickerCount,
                        onToggleCompletion = { viewModel.toggleTaskCompletion(task) },
                        onVoiceReport = { onVoiceReport(task) },
                        onAttachmentClick = { onAttachmentClick(task) },
                        onCountdownClick = { onCountdownClick(task) },
                        onWorkflowClick = { onWorkflowClick(task) },
                        onDiscussionClick = { onDiscussionClick(task) },
                        onDelete = { viewModel.deleteTask(task) }
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(90.dp))
        }
    }
}

@Composable
fun DashboardStatCard(
    title: String,
    value: String,
    subtitle: String,
    colorAccent: Color,
    bgColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background == Color(0xFF0A0F1D)
    val cardBgColor = if (isDark) Color(0xFF131B2E) else Color.White
    val borderColor = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9)
    val titleTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val subtitleTextColor = if (isDark) Color(0xFF475569) else Color(0xFF94A3B8)

    Card(
        modifier = modifier
            .shadow(if (isDark) 0.dp else 1.dp, RoundedCornerShape(16.dp), ambientColor = Color.LightGray)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = titleTextColor
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(colorAccent)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = colorAccent,
                fontSize = 22.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = subtitleTextColor,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun TaskCardWithTimer(
    task: Task,
    tickerCount: Long,
    onToggleCompletion: () -> Unit,
    onVoiceReport: () -> Unit,
    onAttachmentClick: () -> Unit,
    onCountdownClick: () -> Unit,
    onWorkflowClick: () -> Unit,
    onDiscussionClick: () -> Unit,
    onDelete: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background == Color(0xFF0A0F1D)

    val priorityColor = when (task.priority.uppercase()) {
        "HIGH" -> if (isDark) Color(0xFFFCA5A5) else Color(0xFFEF4444)
        "MEDIUM" -> if (isDark) Color(0xFFFDE047) else Color(0xFFF59E0B)
        "LOW" -> if (isDark) Color(0xFF86EFAC) else Color(0xFF10B981)
        else -> if (isDark) Color(0xFF93C5FD) else Color(0xFF3B82F6)
    }

    val priorityBg = when (task.priority.uppercase()) {
        "HIGH" -> if (isDark) Color(0xFFEF4444).copy(alpha = 0.2f) else Color(0xFFFEE2E2)
        "MEDIUM" -> if (isDark) Color(0xFFF59E0B).copy(alpha = 0.2f) else Color(0xFFFEF3C7)
        "LOW" -> if (isDark) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFFD1FAE5)
        else -> if (isDark) Color(0xFF3B82F6).copy(alpha = 0.2f) else Color(0xFFDBEAFE)
    }

    val cardBackground = if (isDark) {
        if (task.isCompleted) Color(0xFF1E293B).copy(alpha = 0.4f) else Color(0xFF131B2E)
    } else {
        if (task.isCompleted) Color(0xFFF8FAFC) else Color.White
    }

    val borderColor = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)

    val textTitleColor = if (task.isCompleted) {
        if (isDark) Color(0xFF475569) else Color(0xFF94A3B8)
    } else {
        if (isDark) Color.White else Color(0xFF0F172A)
    }

    val textDescColor = if (task.isCompleted) {
        if (isDark) Color(0xFF475569) else Color(0xFF94A3B8)
    } else {
        if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)
    }

    // Category Badge
    val categoryBg = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9)
    val categoryTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    // Collaborative Workflow Badge
    val workflowBg = if (isDark) Color(0xFF1E3A8A).copy(alpha = 0.4f) else Color(0xFFEFF6FF)
    val workflowIconTint = if (isDark) Color(0xFF60A5FA) else Color(0xFF2563EB)
    val workflowTextCol = if (isDark) Color(0xFF93C5FD) else Color(0xFF1E40AF)

    // Stopwatch/Timer
    val timerBg = if (isDark) Color(0xFF1E3A8A).copy(alpha = 0.4f) else Color(0xFFEFF6FF)
    val timerIconTint = if (isDark) Color(0xFF60A5FA) else Color(0xFF1E40AF)
    val timerTextCol = if (isDark) Color(0xFF93C5FD) else Color(0xFF1A365D)

    // Action button backgrounds and icons
    val buttonVoiceBg = if (isDark) Color(0xFF1E293B) else Color(0xFFEFF6FF)
    val buttonVoiceIcon = if (isDark) Color(0xFF60A5FA) else Color(0xFF1D4ED8)

    val buttonAttachBg = if (isDark) Color(0xFF1E293B) else Color(0xFFEFF6FF)
    val buttonAttachIcon = if (isDark) Color(0xFF60A5FA) else Color(0xFF1D4ED8)

    val buttonSlaBg = if (isDark) Color(0xFF451A03) else Color(0xFFFEF3C7)
    val buttonSlaIcon = if (isDark) Color(0xFFF59E0B) else Color(0xFFD97706)

    val buttonWorkflowBg = if (isDark) Color(0xFF0C4A6E) else Color(0xFFE0F2FE)
    val buttonWorkflowIcon = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)

    val buttonChatBg = if (isDark) Color(0xFF064E3B) else Color(0xFFDCF8C6)
    val buttonChatIcon = if (isDark) Color(0xFF34D399) else Color(0xFF075E54)

    val buttonDeleteBg = if (isDark) Color(0xFF450A0A) else Color(0xFFFEF2F2)
    val buttonDeleteIcon = if (isDark) Color(0xFFFCA5A5) else Color(0xFFEF4444)

    val checkActiveColor = if (isDark) Color(0xFF3B82F6) else Color(0xFF1E40AF)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (task.isCompleted || isDark) 0.dp else 1.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color.LightGray
            ),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Checked indicator trigger
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (task.isCompleted) checkActiveColor else Color.Transparent)
                    .border(
                        width = 2.dp,
                        color = if (task.isCompleted) Color.Transparent else Color(0xFF94A3B8),
                        shape = CircleShape
                    )
                    .clickable { onToggleCompletion() },
                contentAlignment = Alignment.Center
            ) {
                if (task.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed mark check icon",
                        tint = if (isDark) Color(0xFF0A0F1D) else Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textTitleColor,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )
                if (task.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = textDescColor,
                        maxLines = 2,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.Start)
                ) {
                    // Category Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(categoryBg)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = task.category,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = categoryTextColor
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Priority Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(priorityBg)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = task.priority,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = priorityColor
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Collaborative Workflow Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(workflowBg)
                            .clickable { onWorkflowClick() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Active progress workflow tracker icon",
                                tint = workflowIconTint,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Workflow Stages ↗",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = workflowTextCol
                            )
                        }
                    }

                    // Ticking Stopwatch Timer
                    if (!task.isCompleted) {
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        val diffMillis = System.currentTimeMillis() - task.createdAt
                        val totalSeconds = (diffMillis / 1000).coerceAtLeast(0L) + (tickerCount % 3600L)
                        val hrs = totalSeconds / 3600
                        val mins = (totalSeconds % 3600) / 60
                        val secs = totalSeconds % 60
                        val timerString = String.format("%02d:%02d:%02d", hrs, mins, secs)

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(timerBg)
                                .clickable { onCountdownClick() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Playing clock timer icon",
                                    tint = timerIconTint,
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = timerString,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = timerTextCol,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            IconButton(
                onClick = onVoiceReport,
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(buttonVoiceBg)
                    .testTag("task_voice_report_trigger_${task.id}")
            ) {
                com.example.ui.MicrophoneIcon(
                    tint = buttonVoiceIcon,
                    modifier = Modifier.size(14.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onAttachmentClick,
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(buttonAttachBg)
                    .testTag("task_attachment_trigger_${task.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Info, // clean info layout representing attachments
                    contentDescription = "Attach files to task",
                    tint = buttonAttachIcon,
                    modifier = Modifier.size(14.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onCountdownClick,
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(buttonSlaBg)
                    .testTag("task_countdown_trigger_${task.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Task Countdown SLA escalations",
                    tint = buttonSlaIcon,
                    modifier = Modifier.size(14.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onWorkflowClick,
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(buttonWorkflowBg)
                    .testTag("task_workflow_trigger_${task.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Task Collaborative Workflow progress timeline tracking",
                    tint = buttonWorkflowIcon,
                    modifier = Modifier.size(14.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onDiscussionClick,
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(buttonChatBg)
                    .testTag("task_discussion_trigger_${task.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Task Chat Discussion WhatsApp styled thread",
                    tint = buttonChatIcon,
                    modifier = Modifier.size(14.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(buttonDeleteBg)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete task item from list",
                    tint = buttonDeleteIcon,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun WorkloadVelocityGraph(tasks: List<Task>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(
                elevation = 1.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color.LightGray
            ),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Workload Velocity",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "Daily closed tasks frequency index",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF64748B)
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFEFF6FF))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Active Peak",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E40AF)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
            ) {
                val width = size.width
                val height = size.height

                val points = listOf(0.15f, 0.45f, 0.30f, 0.75f, 0.55f, 0.90f, 0.60f)
                val stepX = width / (points.size - 1)

                val gridLines = 3
                for (i in 0..gridLines) {
                    val y = height * i / gridLines
                    drawLine(
                        color = Color(0xFFF1F5F9),
                        start = androidx.compose.ui.geometry.Offset(0f, y),
                        end = androidx.compose.ui.geometry.Offset(width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                val path = androidx.compose.ui.graphics.Path()
                val bgPath = androidx.compose.ui.graphics.Path()

                for (i in points.indices) {
                    val x = i * stepX
                    val y = height - (points[i] * height)

                    if (i == 0) {
                        path.moveTo(x, y)
                        bgPath.moveTo(x, height)
                        bgPath.lineTo(x, y)
                    } else {
                        val prevX = (i - 1) * stepX
                        val prevY = height - (points[i - 1] * height)
                        val controlX1 = prevX + stepX / 2f
                        val controlY1 = prevY
                        val controlX2 = prevX + stepX / 2f
                        val controlY2 = y

                        path.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
                        bgPath.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
                    }
                }
                bgPath.lineTo(width, height)
                bgPath.close()

                drawPath(
                    path = bgPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFDBEAFE).copy(alpha = 0.6f), Color.Transparent)
                    )
                )

                drawPath(
                    path = path,
                    color = Color(0xFF1E40AF),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                for (i in points.indices) {
                    val x = i * stepX
                    val y = height - (points[i] * height)
                    
                    if (i == 5) {
                        drawCircle(
                            color = Color(0xFF1E40AF),
                            radius = 6.dp.toPx(),
                            center = androidx.compose.ui.geometry.Offset(x, y)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 3.dp.toPx(),
                            center = androidx.compose.ui.geometry.Offset(x, y)
                        )
                    } else {
                        drawCircle(
                            color = Color(0xFF94A3B8),
                            radius = 3.dp.toPx(),
                            center = androidx.compose.ui.geometry.Offset(x, y)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                for (day in days) {
                    Text(
                        text = day,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(36.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardReportsContent(
    allTasksList: List<Task>,
    stats: DashboardStats,
    onViewPerformance: () -> Unit = {}
) {
    com.example.ui.PremiumAnalyticsScreen(
        allTasksList = allTasksList,
        stats = stats,
        onViewPerformance = onViewPerformance
    )
}

@Composable
fun DashboardProfileContent(
    stats: DashboardStats,
    viewModel: TaskViewModel,
    onViewPerformance: () -> Unit = {}
) {
    var toggleNotifications by remember { mutableStateOf(true) }
    var toggleSync by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("profile_container"),
        contentPadding = PaddingValues(top = 40.dp, bottom = 24.dp, start = 16.dp, end = 16.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E40AF))
                        .border(4.dp, Color(0xFFDBEAFE), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "BP", color = Color.White, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold, fontSize = 32.sp)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Bhavesh Patil",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "bhaveshpatiltech@gmail.com",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color(0xFFD1FAE5))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Workspace Owner 🏆",
                        color = Color(0xFF065F46),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .shadow(2.dp, RoundedCornerShape(16.dp), ambientColor = Color.LightGray)
                    .clickable { onViewPerformance() },
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E40AF)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "My Gamified Performance 🏆",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Track your daily score, badges, and view global team leaderboard.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF93C5FD)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "View Performance Profile",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .shadow(1.dp, RoundedCornerShape(16.dp), ambientColor = Color.LightGray),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Organizational Framework",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    ProfileMetadataRow(label = "Platform Instance ID", value = "CKTM-AF-88392")
                    ProfileMetadataRow(label = "Primary Tenant", value = "Career Katta Trust")
                    ProfileMetadataRow(label = "Access Token Tier", value = "Enterprise Secured v4.0")
                    ProfileMetadataRow(label = "Maximum Priority Nodes", value = "No limit (Verified)")
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .shadow(1.dp, RoundedCornerShape(16.dp), ambientColor = Color.LightGray),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Interactive Configuration",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Push Notification Tickers", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF475569))
                        Switch(
                            checked = toggleNotifications,
                            onCheckedChange = { toggleNotifications = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF1E40AF), checkedTrackColor = Color(0xFFDBEAFE))
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Offline Background Task Sync", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF475569))
                        Switch(
                            checked = toggleSync,
                            onCheckedChange = { toggleSync = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF1E40AF), checkedTrackColor = Color(0xFFDBEAFE))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileMetadataRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF64748B))
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
    }
}

@Composable
fun ProfileDetailDialog(
    stats: DashboardStats,
    onDismiss: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background == Color(0xFF0A0F1D)
    val textPrimary = if (isDark) Color.White else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val textBodyColor = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
    val buttonBgColor = if (isDark) Color(0xFF3B82F6) else Color(0xFF1E40AF)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Workspace Member Card",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = textPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(buttonBgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "BP", color = if (isDark) Color(0xFF0A0F1D) else Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = "Bhavesh Patil", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = textPrimary)
                        Text(text = "bhaveshpatiltech@gmail.com", style = MaterialTheme.typography.bodySmall, color = textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "You are running Career Katta Task Manager as verified workspace owner. Resolving backlogs keeps system cleanliness high.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = textBodyColor
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Active Backlog", style = MaterialTheme.typography.bodySmall, color = textSecondary)
                        Text("${stats.pending} Nodes", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                    }
                    Column {
                        Text("Cleanliness rating", style = MaterialTheme.typography.bodySmall, color = textSecondary)
                        Text("${stats.cleanlinessScore}% Clean", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = buttonBgColor)
            ) {
                Text("Close Workspace Card", color = if (isDark) Color(0xFF0A0F1D) else Color.White)
            }
        }
    )
}

@Composable
fun NotificationsDetailDialog(
    onDismiss: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background == Color(0xFF0A0F1D)
    val textPrimary = if (isDark) Color.White else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val infoCardBg = if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC)
    val infoCardBorder = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
    val bulletColor = if (isDark) Color(0xFF3B82F6) else Color(0xFF1E40AF)
    val buttonBgColor = if (isDark) Color(0xFF3B82F6) else Color(0xFF1E40AF)

    val alertsList = listOf(
        "Aniket assigned new Work node: 'Configure CKTM Application Profile'",
        "Priya completed Hot Pendency 'Verify WhatsApp Style Simplicity UI'",
        "System cleanliness optimized to 85% score",
        "Backup synchronization complete on local SQLite db"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Workspace Activity Logs",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = textPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                     text = "Recent workflow transitions in Career Katta Trust workspace:",
                     style = MaterialTheme.typography.bodySmall,
                     color = textSecondary,
                     modifier = Modifier.padding(bottom = 6.dp)
                )

                for (alert in alertsList) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = infoCardBg),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, infoCardBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(bulletColor)
                                    .padding(top = 4.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = alert,
                                style = MaterialTheme.typography.bodyMedium,
                                color = textPrimary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = buttonBgColor)
            ) {
                Text("Clear Alerts View", color = if (isDark) Color(0xFF0A0F1D) else Color.White)
            }
        }
    )
}

// ----------------- METRICS DASHBOARD CARD -----------------
@Composable
fun MetricsSection(stats: DashboardStats) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(
                elevation = 1.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color.LightGray
            ),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "System Cleanliness",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${stats.cleanlinessScore}% Resolved",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0F172A),
                        fontSize = 28.sp
                    )
                }

                // Total backlog badge
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Active Backlog",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (stats.pending > 0) Color(0xFFEF4444) else Color(0xFF10B981))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${stats.pending} Tasks",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (stats.pending > 0) Color(0xFFEF4444) else Color(0xFF10B981)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Neat Linear progress bar showing active progress
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF1F5F9))
            ) {
                val animatedProgress by animateFloatAsState(
                    targetValue = stats.cleanlinessScore / 100f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
                    label = "ScoreProgress"
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedProgress)
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF3B82F6), Color(0xFF1E40AF))
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Subtitle status detail
            Text(
                text = when {
                    stats.cleanlinessScore == 100 -> "✨ Perfect backlog state! Your system is fully clean."
                    stats.cleanlinessScore > 70 -> "⚡ High velocity. Only minor issues await resolution."
                    stats.cleanlinessScore > 40 -> "🚀 Active workspace. Check primary hot vectors."
                    else -> "🚨 Extreme workload. Focus on high-priority pendencies."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF475569),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ----------------- SEARCH BAR SECTION -----------------
@Composable
fun SearchBarSection(query: String, onQueryChanged: (String) -> Unit) {
    TextField(
        value = query,
        onValueChange = onQueryChanged,
        placeholder = {
            Text(
                text = "Search dynamic backlog...",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF94A3B8)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search Icon",
                tint = Color(0xFF64748B),
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChanged("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear Search",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(52.dp)
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
            .testTag("workspace_search_bar")
    )
}

// ----------------- CUSTOM CATEGORY SELECTOR -----------------
@Composable
fun CategoryFilterBar(selectedCategory: String, onCategorySelected: (String) -> Unit) {
    val categories = listOf("All", "Inbox", "Work", "Personal", "Meeting")

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            val isSelected = selectedCategory == category
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) Color(0xFF1E40AF) else Color.White)
                    .clickable { onCategorySelected(category) }
                    .border(
                        width = 1.dp,
                        color = if (isSelected) Color.Transparent else Color(0xFFE2E8F0),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = category,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color.White else Color(0xFF475569)
                )
            }
        }
    }
}

// ----------------- PRIORITY FILTER BAR -----------------
@Composable
fun PriorityFilterBar(selectedPriority: String, onPrioritySelected: (String) -> Unit) {
    val priorities = listOf("All", "HIGH", "MEDIUM", "LOW")

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(priorities) { priority ->
            val isSelected = selectedPriority == priority
            val colorIndicator = when (priority) {
                "HIGH" -> Color(0xFFEF4444)
                "MEDIUM" -> Color(0xFFF59E0B)
                "LOW" -> Color(0xFF10B981)
                else -> Color(0xFF3B82F6)
            }
            
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) colorIndicator.copy(alpha = 0.15f) else Color.White)
                    .clickable { onPrioritySelected(priority) }
                    .border(
                        width = 1.dp,
                        color = if (isSelected) colorIndicator else Color(0xFFE2E8F0),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (priority != "All") {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(colorIndicator)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = if (priority == "All") "All Priorities" else priority,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color(0xFF0F172A) else Color(0xFF475569)
                    )
                }
            }
        }
    }
}

// ----------------- TASK CARD LIST-ITEM COMPOSABLE -----------------
@Composable
fun TaskCard(
    task: Task,
    onToggleCompletion: () -> Unit,
    onDelete: () -> Unit
) {
    val priorityColor = when (task.priority.uppercase()) {
        "HIGH" -> Color(0xFFEF4444)
        "MEDIUM" -> Color(0xFFF59E0B)
        "LOW" -> Color(0xFF10B981)
        else -> Color(0xFF3B82F6)
    }

    val priorityBg = when (task.priority.uppercase()) {
        "HIGH" -> Color(0xFFFEE2E2)
        "MEDIUM" -> Color(0xFFFEF3C7)
        "LOW" -> Color(0xFFD1FAE5)
        else -> Color(0xFFDBEAFE)
    }

    val cardBackground = if (task.isCompleted) Color(0xFFF8FAFC) else Color.White

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (task.isCompleted) 0.dp else 1.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color.LightGray
            )
            .testTag("task_item_${task.id}"),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Checkbox Mark Completed (WhatsApp simplicity tap state)
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (task.isCompleted) Color(0xFF1E40AF) else Color.Transparent)
                    .border(
                        width = 2.dp,
                        color = if (task.isCompleted) Color.Transparent else Color(0xFF94A3B8),
                        shape = CircleShape
                    )
                    .clickable { onToggleCompletion() }
                    .testTag("task_checkbox_${task.id}"),
                contentAlignment = Alignment.Center
            ) {
                if (task.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Body text / Metadata (Linear inspired)
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (task.isCompleted) Color(0xFF94A3B8) else Color(0xFF0F172A),
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )
                
                if (task.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (task.isCompleted) Color(0xFFCBD5E1) else Color(0xFF475569),
                        maxLines = 2,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Information Footer (Category pill, Priority Tag, Timeframe alert)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Category Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = task.category,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF475569)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Priority Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(priorityBg)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = task.priority,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = priorityColor
                        )
                    }

                    if (task.dueDate.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        // Due date indicator
                        Text(
                            text = "🗓️ ${task.dueDate}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (task.dueDate.contains("Yesterday", ignoreCase = true)) Color(0xFFEF4444) else Color(0xFF64748B)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Rapid Delete Button (Touch-optimized one-hand accessible corner action)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFFEF2F2))
                    .clickable { onDelete() }
                    .testTag("task_delete_${task.id}"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Active Backlog Item",
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ----------------- EMPTY WORKSPACE STATE UI -----------------
@Composable
fun EmptyStateSection(hasFilters: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Elegant drawn clean radar graphic
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEFF6FF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Fully Clean",
                    tint = Color(0xFF1E40AF),
                    modifier = Modifier.size(46.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = if (hasFilters) "No Matching Backlog items" else "Fully Clean System",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (hasFilters) 
                    "No items match the active categories. Tap 'Clear Filters' to restore standard items."
                else 
                    "You have zero pendency. All task vectors are successfully resolved to active production status.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = Color(0xFF64748B)
            )
        }
    }
}

// ----------------- ADD TASK DIALOG (CLEAN BOTTOM POPUP ACTION) -----------------
@Composable
fun AddTaskDialog(
    viewModel: TaskViewModel,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String, String) -> Unit
) {
    var currentStep by remember { mutableStateOf(1) }
    
    // Core Fields
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf("MEDIUM") }
    var selectedCategory by remember { mutableStateOf("Work") }
    var dueDate by remember { mutableStateOf("Today") }
    
    // Extra Rich Fields
    var selectedAssignee by remember { mutableStateOf("Bhavesh Patil") }
    var selectedCollaborators by remember { mutableStateOf(setOf<String>()) }
    var attachedFiles by remember { mutableStateOf(listOf<String>()) }
    
    var titleError by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showToast by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf("") }

    val registeredEmployees by viewModel.registeredEmployees.collectAsState()
    val teamList = registeredEmployees.map { emp ->
        val initials = emp.name.split(" ")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .joinToString("")
            .take(2)
        emp.name to (if (initials.isEmpty()) "EM" else initials)
    }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC)),
            color = Color(0xFFF8FAFC)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                
                // Content Layout
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                ) {
                    
                    // Header Bar (Back Button - Title - Help)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (currentStep > 1) {
                                    currentStep--
                                } else {
                                    onDismiss()
                                }
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(1.dp, Color(0xFFE2E8F0), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Go Back/Dismiss",
                                tint = Color(0xFF0F172A),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Create New Task",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF0F172A),
                                letterSpacing = (-0.5).sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "3 simple steps to add a new task",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        IconButton(
                            onClick = { showHelpDialog = true },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(1.dp, Color(0xFFE2E8F0), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Show Info Help",
                                tint = Color(0xFF1E40AF),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Steps Progress Indicator
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                            .shadow(1.dp, RoundedCornerShape(16.dp), ambientColor = Color.LightGray),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 14.dp, horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Step 1 Widget
                            StepBlock(
                                stepNum = 1,
                                label = "Task Details",
                                isActive = currentStep == 1,
                                isCompleted = currentStep > 1,
                                onClick = { currentStep = 1 }
                            )

                            // Connective Dotted Progress Line
                            DottedProgressConnector(isHalfway = currentStep > 1)

                            // Step 2 Widget
                            StepBlock(
                                stepNum = 2,
                                label = "Assign Team",
                                isActive = currentStep == 2,
                                isCompleted = currentStep > 2,
                                onClick = { currentStep = 2 }
                            )

                            // Connective Dotted Progress Line
                            DottedProgressConnector(isHalfway = currentStep > 2)

                            // Step 3 Widget
                            StepBlock(
                                stepNum = 3,
                                label = "Review",
                                isActive = currentStep == 3,
                                isCompleted = false,
                                onClick = { currentStep = 3 }
                            )
                        }
                    }

                    // Main Form Scroll Workspace
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        when (currentStep) {
                            1 -> {
                                // STEP 1: TASK DETAILS
                                Text(
                                    text = "Task Title Summary",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF334155),
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )

                                OutlinedTextField(
                                    value = title,
                                    onValueChange = {
                                        title = it
                                        if (it.isNotBlank()) titleError = false
                                    },
                                    placeholder = { Text("E.g., Design UI audit checklist") },
                                    singleLine = true,
                                    isError = titleError,
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = "Edit Pencil Icon",
                                            tint = Color(0xFF94A3B8)
                                        )
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White,
                                        focusedBorderColor = Color(0xFF1E40AF),
                                        unfocusedBorderColor = Color(0xFFE2E8F0)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("task_title_input")
                                )
                                if (titleError) {
                                    Text(
                                        text = "Please enter a valid task title summary",
                                        color = Color(0xFFEF4444),
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Minimal Typing Suggester presets
                                Text(
                                    text = "Quick Suggetions Presets",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFF64748B),
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    val presets = listOf("Complete UI Mockup Audit", "Coordinate Career Fair", "Deploy Hotfix Code", "Finalize Syllabus V3", "Draft Release Post")
                                    items(presets) { preset ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(32.dp))
                                                .background(Color(0xFFEFF6FF))
                                                .border(1.dp, Color(0xFFDBEAFE), RoundedCornerShape(32.dp))
                                                .clickable {
                                                    title = preset
                                                    titleError = false
                                                }
                                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                        ) {
                                            Text(
                                                text = preset,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = Color(0xFF1E40AF),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                // Category bucket & Priority Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Category Select
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Category Bucket",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF334155),
                                            modifier = Modifier.padding(bottom = 6.dp)
                                        )
                                        val cats = listOf("Work", "Meeting", "Personal", "Inbox")
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(52.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(Color.White)
                                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                                                .clickable {
                                                    selectedCategory = when (selectedCategory) {
                                                        "Work" -> "Meeting"
                                                        "Meeting" -> "Personal"
                                                        "Personal" -> "Inbox"
                                                        else -> "Work"
                                                    }
                                                }
                                                .padding(horizontal = 14.dp),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            Row(
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = selectedCategory,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF0F172A)
                                                )
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = "Drop Indicator",
                                                    tint = Color(0xFF64748B),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }

                                    // Priority CHOICE
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Priority Level",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF334155),
                                            modifier = Modifier.padding(bottom = 6.dp)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(52.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(Color.White)
                                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                                                .clickable {
                                                    selectedPriority = when (selectedPriority) {
                                                        "LOW" -> "MEDIUM"
                                                        "MEDIUM" -> "HIGH"
                                                        else -> "LOW"
                                                    }
                                                }
                                                .padding(horizontal = 14.dp),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            Row(
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                val col = when (selectedPriority) {
                                                    "HIGH" -> Color(0xFFEF4444)
                                                    "MEDIUM" -> Color(0xFFF59E0B)
                                                    else -> Color(0xFF10B981)
                                                }
                                                Text(
                                                    text = selectedPriority,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = col
                                                )
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = "Drop Priority Indicator",
                                                    tint = Color(0xFF64748B),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                // Due Date
                                Text(
                                    text = "Set Deadline Target",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF334155),
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("Today", "Tomorrow", "May 25", "May 28").forEach { d ->
                                        val isSelected = dueDate == d
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (isSelected) Color(0xFFEFF6FF) else Color.White)
                                                .border(
                                                    width = if (isSelected) 2.dp else 1.dp,
                                                    color = if (isSelected) Color(0xFF1E40AF) else Color(0xFFE2E8F0),
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                                .clickable { dueDate = d }
                                                .padding(vertical = 12.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = d,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color(0xFF1E40AF) else Color(0xFF475569)
                                            )
                                        }
                                    }
                                }
                            }

                            2 -> {
                                // STEP 2: ASSIGNMENT DETAILS
                                Text(
                                    text = "Assign Core Owner",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF334155),
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                Text(
                                    text = "Select primary leader responsible for the task deployment",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF64748B),
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(teamList) { (name, initials) ->
                                        val isSelected = selectedAssignee == name
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier
                                                .clickable { selectedAssignee = name }
                                                .padding(6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(62.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isSelected) Color(0xFF1E40AF) else Color(0xFFF1F5F9))
                                                    .border(
                                                        width = if (isSelected) 3.dp else 1.dp,
                                                        color = if (isSelected) Color(0xFFDBEAFE) else Color(0xFFCBD5E1),
                                                        shape = CircleShape
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = initials,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Black,
                                                    color = if (isSelected) Color.White else Color(0xFF475569)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = name.substringBefore(" "),
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color(0xFF1E40AF) else Color(0xFF64748B)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                // MULTI SELECT COLLABORATORS
                                Text(
                                    text = "Team Collaborators (Optional)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF334155),
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                Text(
                                    text = "Add team specialists to receive notifications & provide feedback",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF64748B),
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(teamList) { (name, initials) ->
                                        if (name != selectedAssignee) {
                                            val isSelected = selectedCollaborators.contains(name)
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier
                                                    .clickable {
                                                        selectedCollaborators = if (isSelected) {
                                                            selectedCollaborators - name
                                                        } else {
                                                            selectedCollaborators + name
                                                        }
                                                    }
                                                    .padding(6.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(62.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isSelected) Color(0xFF0D9488) else Color(0xFFF1F5F9))
                                                        .border(
                                                            width = if (isSelected) 3.dp else 1.dp,
                                                            color = if (isSelected) Color(0xFFCCFBF1) else Color(0xFFCBD5E1),
                                                            shape = CircleShape
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = initials,
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Black,
                                                        color = if (isSelected) Color.White else Color(0xFF475569)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = name.substringBefore(" "),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) Color(0xFF0D9488) else Color(0xFF64748B)
                                                )
                                            }
                                        }
                                    }
                                    // Custom plus builder representation
                                    item {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier
                                                .clickable {
                                                    toastMessage = "Simulated custom collaborator invitations list!"
                                                    showToast = true
                                                }
                                                .padding(6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(62.dp)
                                                    .clip(CircleShape)
                                                    .border(2.dp, Color(0xFF94A3B8), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = "Add More Custom",
                                                    tint = Color(0xFF475569)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "Invite",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF64748B)
                                            )
                                        }
                                    }
                                }
                            }

                            3 -> {
                                // STEP 3: WORKFLOW FILES & MEDIA CAPTURE
                                Text(
                                    text = "Quick Media Uploads (Choose any)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF334155),
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )

                                val uploads = listOf(
                                    Triple("Voice Note", Color(0xFF10B981), Color(0xFFE6FDF4)),
                                    Triple("Camera", Color(0xFF3B82F6), Color(0xFFEFF6FF)),
                                    Triple("Screenshot", Color(0xFFF59E0B), Color(0xFFFFFBEB)),
                                    Triple("PDF Upload", Color(0xFFEF4444), Color(0xFFFEF2F2)),
                                    Triple("Audio Upload", Color(0xFF8B5CF6), Color(0xFFF5F3FF))
                                )

                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                ) {
                                    items(uploads) { (label, iCol, bCol) ->
                                        Card(
                                            modifier = Modifier
                                                .size(92.dp)
                                                .clickable {
                                                    val attachment = when (label) {
                                                        "Voice Note" -> "voice_snippet_rec.mp3"
                                                        "Camera" -> "onsite_incident_sn.jpg"
                                                        "Screenshot" -> "cockpit_view_audit.png"
                                                        "PDF Upload" -> "guideline_compliance.pdf"
                                                        else -> "telemetry_call_record.wav"
                                                    }
                                                    attachedFiles = attachedFiles + attachment
                                                    toastMessage = "Successfully attached: $attachment 📎"
                                                    showToast = true
                                                },
                                            colors = CardDefaults.cardColors(containerColor = bCol),
                                            shape = RoundedCornerShape(16.dp),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier.fillMaxSize().padding(8.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(38.dp)
                                                        .clip(CircleShape)
                                                        .background(Color.White),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    val sym = when (label) {
                                                        "Voice Note" -> Icons.Default.Phone
                                                        "Camera" -> Icons.Default.CheckCircle
                                                        "Screenshot" -> Icons.Default.Search
                                                        "PDF Upload" -> Icons.Default.Info
                                                        else -> Icons.Default.Notifications
                                                    }
                                                    Icon(
                                                        imageVector = sym,
                                                        contentDescription = label,
                                                        tint = iCol,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                    text = label,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = iCol,
                                                    fontSize = 11.sp,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                // Drag & Drop Sandbox Zone
                                Text(
                                    text = "System Sandbox Upload Staging Area",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF64748B),
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White, RoundedCornerShape(16.dp))
                                        .border(
                                            width = 1.6.dp,
                                            color = Color(0xFFCBD5E1),
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .padding(16.dp)
                                ) {
                                    if (attachedFiles.isEmpty()) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Home,
                                                contentDescription = "Cloud Upload Arrow Icon",
                                                tint = Color(0xFF1E40AF),
                                                modifier = Modifier.size(32.dp)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = "Drag & Drop files here or tap to upload",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF334155)
                                                )
                                                Text(
                                                    text = "Images, PDFs, Videos, Audio & more",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color(0xFF64748B)
                                                )
                                            }
                                        }
                                    } else {
                                        Text(
                                            text = "Active System Attachments (${attachedFiles.size})",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFF1E293B)
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        attachedFiles.forEach { doc ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp)
                                                    .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
                                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.CheckCircle,
                                                        contentDescription = "Verified Attachment Approved",
                                                        tint = Color(0xFF10B981),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Text(
                                                        text = doc,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = Color(0xFF334155),
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                                IconButton(
                                                    onClick = { attachedFiles = attachedFiles - doc },
                                                    modifier = Modifier.size(20.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Decline attachment file",
                                                        tint = Color(0xFFEF4444),
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                // Dynamic Metadata Summary Panel
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = "CKTM Deployment Summary",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFF1E40AF)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "• Title: ${title.ifEmpty { "Untitled Node" }}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF1E3A8A),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "• Owner: $selectedAssignee • Collaborators: ${if (selectedCollaborators.isNotEmpty()) selectedCollaborators.joinToString(", ") else "None"}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF1E3A8A),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "• Settings: Bucket ($selectedCategory), Priority ($selectedPriority), Due ($dueDate)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF1E3A8A),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Bottom Control Action Trigger Header
                    Divider(color = Color(0xFFF1F5F9))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(20.dp)
                    ) {
                        Button(
                            onClick = {
                                if (currentStep < 3) {
                                    currentStep++
                                } else {
                                    if (title.isBlank()) {
                                        titleError = true
                                        currentStep = 1
                                        toastMessage = "Task Title Summary is required!"
                                        showToast = true
                                    } else {
                                        // Build rich composite descriptive stream
                                        val compositeDescription = buildString {
                                            append("Primary Assignee: $selectedAssignee\n")
                                            if (selectedCollaborators.isNotEmpty()) {
                                                append("Collaborators: ${selectedCollaborators.joinToString(", ")}\n")
                                            }
                                            if (attachedFiles.isNotEmpty()) {
                                                append("Attachments: ${attachedFiles.joinToString(", ")}\n")
                                            }
                                            if (description.isNotEmpty()) {
                                                append("\nInstructions: $description")
                                            } else {
                                                append("\nInstructions: Created via premium CKTM creation form system.")
                                            }
                                        }
                                        onConfirm(title, compositeDescription, selectedPriority, selectedCategory, dueDate, selectedAssignee)
                                    }
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E40AF)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) {
                            Text(
                                text = if (currentStep < 3) "Next Step →" else "Deploy CKTM Task 🚀",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Security Active Shield Badge Icon",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "All data is secure and encrypted",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // AI Sparkles Wizard Autofill floating FAB Action button (Exact mockup matches!)
                FloatingActionButton(
                    onClick = {
                        val aiProjects = listOf(
                            Triple("Finalize District Career Counseling Poster", "Work", "HIGH"),
                            Triple("Pre-audit Syllabus Compliance Guide v4.2", "Meeting", "MEDIUM"),
                            Triple("Review Government Education Onboarding Flow", "Inbox", "LOW"),
                            Triple("Weekly Mentorship Sync & Feedback Record", "Meeting", "MEDIUM"),
                            Triple("Coordinate Principal Training Web Conference", "Personal", "HIGH")
                        )
                        val item = aiProjects.random()
                        title = item.first
                        selectedCategory = item.second
                        selectedPriority = item.third
                        dueDate = listOf("Today", "Tomorrow", "May 25", "May 28").random()
                        selectedAssignee = teamList.random().first
                        selectedCollaborators = teamList.map { it.first }.shuffled().take(2).toSet() - selectedAssignee
                        attachedFiles = listOf("incident_record.wav", "checklist_guide.pdf", "student_flow_draft.png").shuffled().take(2)
                        toastMessage = "AI populated task details instantly! ✨"
                        showToast = true
                    },
                    containerColor = Color(0xFF1E40AF),
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 120.dp, end = 20.dp)
                        .size(54.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "AI Autofill Sparkle Trigger Symbol",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Custom premium high fidelity Toast Overlay Slide-In
                AnimatedVisibility(
                    visible = showToast,
                    enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 20.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Active Indicator",
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
                    LaunchedEffect(showToast) {
                        if (showToast) {
                            delay(2200)
                            showToast = false
                        }
                    }
                }
            }
        }
    }
}

// ----------------- SUB-HELPER COMPOSABLES FOR ADD_TASK_DIALOG Flow -----------------

@Composable
fun StepBlock(
    stepNum: Int,
    label: String,
    isActive: Boolean,
    isCompleted: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (isActive || isCompleted) Color(0xFF1E40AF) else Color(0xFFF1F5F9)
    val numberColor = if (isActive || isCompleted) Color.White else Color(0xFF64748B)
    val labelColor = if (isActive || isCompleted) Color(0xFF1E40AF) else Color(0xFF94A3B8)
    val fw = if (isActive) FontWeight.ExtraBold else FontWeight.Bold

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(containerColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNum.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Black,
                color = numberColor
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = labelColor,
            fontWeight = fw,
            fontSize = 11.sp
        )
    }
}

@Composable
fun DottedProgressConnector(isHalfway: Boolean) {
    Canvas(
        modifier = Modifier
            .width(50.dp)
            .height(4.dp)
            .padding(horizontal = 4.dp)
    ) {
        val steps = 6
        val dotWidth = 3f
        val gap = (size.width - (steps * dotWidth)) / (steps - 1)
        for (i in 0 until steps) {
            drawCircle(
                color = if (isHalfway) Color(0xFF93C5FD) else Color(0xFFE2E8F0),
                radius = dotWidth,
                center = androidx.compose.ui.geometry.Offset(i * (dotWidth + gap) + dotWidth / 2f, size.height / 2f)
            )
        }
    }
}

