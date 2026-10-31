package com.example.ui.teacher

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.JsonUtils
import com.example.data.model.*
import com.example.ui.AppScreen
import com.example.ui.KarmaViewModel
import com.example.ui.common.ChemicalFormulaBadge
import com.example.ui.common.ProtectedContentBadge
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherDashboardScreen(viewModel: KarmaViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allCourses by viewModel.allCourses.collectAsState()
    val allLive by viewModel.allLiveClasses.collectAsState()
    val allMaterials by viewModel.allMaterials.collectAsState()
    val allQuestions by viewModel.allQuestions.collectAsState()
    val allMockTests by viewModel.allMockTests.collectAsState()
    val allStudents by viewModel.allStudents.collectAsState()
    val securityConfig by viewModel.securityConfig.collectAsState()

    BackHandler {
        // Prevent leaving admin dashboard accidentally
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Teacher Admin Panel", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("Karma Learning Classes", style = MaterialTheme.typography.bodySmall)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.ClassSelection) },
                        modifier = Modifier.testTag("preview_student_dashboard_btn")
                    ) {
                        Icon(Icons.Default.Visibility, contentDescription = "Preview Student Screen")
                    }
                    IconButton(
                        onClick = { viewModel.logout() },
                        modifier = Modifier.testTag("teacher_logout_btn")
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = "Sign Out")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Teacher Profile Banner
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(currentUser?.name ?: "Faculty Admin", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text("Full Administrative & Security Authority", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                color = if (securityConfig?.antiScreenshotEnabled == true) Color(0xFF2E7D32) else Color(0xFFC62828),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    if (securityConfig?.antiScreenshotEnabled == true) "🛡️ Anti-Screenshot Active" else "⚠️ Protection Disabled",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Quick Stats
            item {
                Text("Operational Overview", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AdminMetricTile("Courses", "${allCourses.size}", Icons.Default.MenuBook, Modifier.weight(1f))
                    AdminMetricTile("Live", "${allLive.size}", Icons.Default.VideoCameraFront, Modifier.weight(1f))
                    AdminMetricTile("Questions", "${allQuestions.size}", Icons.Default.Quiz, Modifier.weight(1f))
                    AdminMetricTile("Students", "${allStudents.size}", Icons.Default.People, Modifier.weight(1f))
                }
            }

            // Core Admin Modules Grid
            item {
                Text("Content & Administration Modules", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    AdminModuleRow(
                        title1 = "Courses & Launch",
                        subtitle1 = "${allCourses.size} active courses",
                        icon1 = Icons.Default.MenuBook,
                        onClick1 = { viewModel.navigateTo(AppScreen.TeacherCourses) },
                        title2 = "Live Classes",
                        subtitle2 = "Google Meet scheduler",
                        icon2 = Icons.Default.VideoCameraFront,
                        onClick2 = { viewModel.navigateTo(AppScreen.TeacherLiveClasses) }
                    )

                    AdminModuleRow(
                        title1 = "Notes & PDFs",
                        subtitle1 = "${allMaterials.size} documents",
                        icon1 = Icons.Default.PictureAsPdf,
                        onClick1 = { viewModel.navigateTo(AppScreen.TeacherMaterials) },
                        title2 = "MCQ Bank",
                        subtitle2 = "Hindi & English",
                        icon2 = Icons.Default.Quiz,
                        onClick2 = { viewModel.navigateTo(AppScreen.TeacherQuestionBank) }
                    )

                    AdminModuleRow(
                        title1 = "Mock Tests",
                        subtitle1 = "Timed exam engine",
                        icon1 = Icons.Default.Assignment,
                        onClick1 = { viewModel.navigateTo(AppScreen.TeacherMockTests) },
                        title2 = "AI Explanation Hub",
                        subtitle2 = "Gemini question generator",
                        icon2 = Icons.Default.AutoAwesome,
                        onClick2 = { viewModel.navigateTo(AppScreen.TeacherAiHub) }
                    )

                    AdminModuleRow(
                        title1 = "Anti-Screenshot Security",
                        subtitle1 = "FLAG_SECURE control",
                        icon1 = Icons.Default.Security,
                        onClick1 = { viewModel.navigateTo(AppScreen.TeacherSecurity) },
                        title2 = "Announcements",
                        subtitle2 = "Broadcast notifications",
                        icon2 = Icons.Default.Campaign,
                        onClick2 = { viewModel.navigateTo(AppScreen.TeacherAnnouncements) }
                    )

                    AdminModuleRow(
                        title1 = "Student Roster",
                        subtitle1 = "${allStudents.size} enrolled students",
                        icon1 = Icons.Default.People,
                        onClick1 = { viewModel.navigateTo(AppScreen.TeacherStudents) },
                        title2 = "Student View",
                        subtitle2 = "Switch to learner mode",
                        icon2 = Icons.Default.RemoveRedEye,
                        onClick2 = { viewModel.navigateTo(AppScreen.StudentMain) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun AdminMetricTile(
    title: String,
    count: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(count, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun AdminModuleRow(
    title1: String,
    subtitle1: String,
    icon1: androidx.compose.ui.graphics.vector.ImageVector,
    onClick1: () -> Unit,
    title2: String,
    subtitle2: String,
    icon2: androidx.compose.ui.graphics.vector.ImageVector,
    onClick2: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        AdminModuleCard(title1, subtitle1, icon1, Modifier.weight(1f), onClick1)
        AdminModuleCard(title2, subtitle2, icon2, Modifier.weight(1f), onClick2)
    }
}

@Composable
fun AdminModuleCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// --- Teacher Anti-Screenshot Security Settings Screen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherSecurityScreen(viewModel: KarmaViewModel) {
    val securityConfig by viewModel.securityConfig.collectAsState()
    var antiScreenshot by remember(securityConfig) { mutableStateOf(securityConfig?.antiScreenshotEnabled ?: true) }
    var protectPaid by remember(securityConfig) { mutableStateOf(securityConfig?.protectPaidContent ?: true) }
    var protectPdfs by remember(securityConfig) { mutableStateOf(securityConfig?.protectPdfs ?: true) }
    var protectMockTests by remember(securityConfig) { mutableStateOf(securityConfig?.protectMockTests ?: true) }
    var protectAnswerKeys by remember(securityConfig) { mutableStateOf(securityConfig?.protectAnswerKeys ?: true) }
    var showWatermark by remember(securityConfig) { mutableStateOf(securityConfig?.showWatermarkNotice ?: true) }

    BackHandler { viewModel.navigateBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Anti-Screenshot & Security") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Button(
                        onClick = {
                            val updated = (securityConfig ?: AppSecurityConfigEntity()).copy(
                                antiScreenshotEnabled = antiScreenshot,
                                protectPaidContent = protectPaid,
                                protectPdfs = protectPdfs,
                                protectMockTests = protectMockTests,
                                protectAnswerKeys = protectAnswerKeys,
                                showWatermarkNotice = showWatermark,
                                lastUpdated = System.currentTimeMillis()
                            )
                            viewModel.saveSecurityConfig(updated)
                            viewModel.navigateBack()
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Text("Save Security Configuration", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Native Android Content Protection", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Secures sensitive study material by applying WindowManager.LayoutParams.FLAG_SECURE to Android windows. " +
                            "This prevents screen recording, external mirroring, and screenshots on supported Android devices.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            item {
                Text("Protection Controls", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }

            item {
                SecurityToggleRow(
                    title = "Master Anti-Screenshot Switch",
                    description = "Globally enables or disables screen capture protection across the app.",
                    checked = antiScreenshot,
                    onCheckedChange = { antiScreenshot = it }
                )
            }

            item {
                SecurityToggleRow(
                    title = "Protect Paid Courses & Video Lessons",
                    description = "Prevents screen recording of premium video lectures and paid course lessons.",
                    checked = protectPaid,
                    onCheckedChange = { protectPaid = it }
                )
            }

            item {
                SecurityToggleRow(
                    title = "Protect Premium PDF Notes & Handbooks",
                    description = "Blocks screenshots when viewing copyrighted lecture notes and chemistry books.",
                    checked = protectPdfs,
                    onCheckedChange = { protectPdfs = it }
                )
            }

            item {
                SecurityToggleRow(
                    title = "Protect Active Mock Tests",
                    description = "Disables capture while students are actively attempting timed mock test questions.",
                    checked = protectMockTests,
                    onCheckedChange = { protectMockTests = it }
                )
            }

            item {
                SecurityToggleRow(
                    title = "Protect Answer Keys & Explanations",
                    description = "Secures verified correct answer keys and step-by-step chemical explanations.",
                    checked = protectAnswerKeys,
                    onCheckedChange = { protectAnswerKeys = it }
                )
            }

            item {
                SecurityToggleRow(
                    title = "Display 'Protected Content' Indicator",
                    description = "Shows the verified security badge and warning dialog on all protected screens.",
                    checked = showWatermark,
                    onCheckedChange = { showWatermark = it }
                )
            }
        }
    }
}

@Composable
fun SecurityToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(2.dp))
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

// --- Teacher Courses Management Screen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherCoursesScreen(viewModel: KarmaViewModel) {
    val allCourses by viewModel.allCourses.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    BackHandler { viewModel.navigateBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Course Management (${allCourses.size})") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Create New Course")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(allCourses) { course ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    course.targetClass,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                if (course.isPaid) "₹${course.priceINR.toInt()}" else "FREE",
                                fontWeight = FontWeight.Bold,
                                color = if (course.isPaid) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(course.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text(course.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    val toggled = course.copy(isPublished = !course.isPublished)
                                    viewModel.saveCourse(toggled)
                                }
                            ) {
                                Text(if (course.isPublished) "Unpublish" else "Publish")
                            }
                            IconButton(
                                onClick = { viewModel.deleteCourse(course) }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Course", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        var title by remember { mutableStateOf("") }
        var targetClass by remember { mutableStateOf("CSIR-NET") }
        var subject by remember { mutableStateOf("Chemical Sciences") }
        var isPaid by remember { mutableStateOf(false) }
        var price by remember { mutableStateOf("999") }
        var description by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Launch New Course") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Course Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = targetClass,
                        onValueChange = { targetClass = it },
                        label = { Text("Target Class/Exam (e.g. CSIR-NET, Class 12)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Subject") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isPaid, onCheckedChange = { isPaid = it })
                        Text("Paid Course")
                    }
                    if (isPaid) {
                        OutlinedTextField(
                            value = price,
                            onValueChange = { price = it },
                            label = { Text("Price in INR (₹)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description & Learning Objectives") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            val newCourse = CourseEntity(
                                id = "course_${UUID.randomUUID().toString().take(8)}",
                                title = title,
                                description = description.ifBlank { "Comprehensive study course by Prof. Chandan Khandey." },
                                educationLevel = EducationLevel.COMPETITIVE_EXAM,
                                targetClass = targetClass,
                                subject = subject,
                                isPaid = isPaid,
                                priceINR = price.toDoubleOrNull() ?: 0.0,
                                isPublished = true
                            )
                            viewModel.saveCourse(newCourse)
                            showCreateDialog = false
                        }
                    }
                ) {
                    Text("Publish Course")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// --- Teacher Live Classes Screen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherLiveClassesScreen(viewModel: KarmaViewModel) {
    val allLive by viewModel.allLiveClasses.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    BackHandler { viewModel.navigateBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Live Classes (${allLive.size})") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Schedule Live Session")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(allLive) { live ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "📅 ${live.scheduledDate} (${live.startTime} - ${live.endTime})",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Surface(
                                color = if (live.status == "LIVE") Color.Red else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    live.status,
                                    fontSize = 10.sp,
                                    color = if (live.status == "LIVE") Color.White else Color.Black,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(live.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Text("Meeting URL: ${live.meetingUrl}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    val updated = live.copy(status = if (live.status == "LIVE") "COMPLETED" else "LIVE")
                                    viewModel.saveLiveClass(updated)
                                }
                            ) {
                                Text(if (live.status == "LIVE") "End Class" else "Mark Live")
                            }
                            IconButton(onClick = { viewModel.deleteLiveClass(live) }) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        var title by remember { mutableStateOf("") }
        var targetClass by remember { mutableStateOf("CSIR-NET") }
        var date by remember { mutableStateOf("2026-10-28") }
        var startTime by remember { mutableStateOf("18:00") }
        var endTime by remember { mutableStateOf("19:30") }
        var meetingUrl by remember { mutableStateOf("https://meet.google.com/zxy-karm-cls") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Schedule Live Class") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Class Title / Topic") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = targetClass,
                        onValueChange = { targetClass = it },
                        label = { Text("Target Class/Exam") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Date (YYYY-MM-DD)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = startTime,
                            onValueChange = { startTime = it },
                            label = { Text("Start Time") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = endTime,
                            onValueChange = { endTime = it },
                            label = { Text("End Time") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = meetingUrl,
                        onValueChange = { meetingUrl = it },
                        label = { Text("Google Meet / Live Link") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            val newLive = LiveClassEntity(
                                id = "live_${UUID.randomUUID().toString().take(8)}",
                                title = title,
                                targetClass = targetClass,
                                subject = "Chemical Sciences",
                                scheduledDate = date,
                                startTime = startTime,
                                endTime = endTime,
                                meetingUrl = meetingUrl,
                                status = "UPCOMING"
                            )
                            viewModel.saveLiveClass(newLive)
                            showCreateDialog = false
                        }
                    }
                ) {
                    Text("Schedule")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// --- Teacher Notes & Study Materials Screen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherMaterialsScreen(viewModel: KarmaViewModel) {
    val allMaterials by viewModel.allMaterials.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    BackHandler { viewModel.navigateBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notes & PDFs (${allMaterials.size})") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Upload Study Material")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(allMaterials) { mat ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(mat.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Text("Topic: ${mat.topic} • ${mat.pageCount} Pages • ${mat.targetClass}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        IconButton(onClick = { viewModel.deleteMaterial(mat) }) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        var title by remember { mutableStateOf("") }
        var topic by remember { mutableStateOf("Organic Chemistry") }
        var targetClass by remember { mutableStateOf("CSIR-NET") }
        var pages by remember { mutableStateOf("24") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Add PDF Study Material") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Document Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = topic,
                        onValueChange = { topic = it },
                        label = { Text("Subject Topic") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = targetClass,
                        onValueChange = { targetClass = it },
                        label = { Text("Target Class/Exam") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = pages,
                        onValueChange = { pages = it },
                        label = { Text("Page Count") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            val newMat = StudyMaterialEntity(
                                id = "pdf_${UUID.randomUUID().toString().take(8)}",
                                title = title,
                                targetClass = targetClass,
                                subject = "Chemical Sciences",
                                topic = topic,
                                pageCount = pages.toIntOrNull() ?: 12,
                                isPaidOnly = true,
                                isPublished = true
                            )
                            viewModel.saveMaterial(newMat)
                            showCreateDialog = false
                        }
                    }
                ) {
                    Text("Upload PDF")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// --- Teacher MCQ Question Bank Screen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherQuestionBankScreen(viewModel: KarmaViewModel) {
    val allQuestions by viewModel.allQuestions.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    BackHandler { viewModel.navigateBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Question Bank (${allQuestions.size})") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add New Question")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(allQuestions) { q ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${q.chapter} (${q.targetClass})", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            Text("Correct: Option ${('A' + q.correctOptionIndex)}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(q.questionEn, fontWeight = FontWeight.SemiBold)
                        if (!q.chemicalFormula.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            ChemicalFormulaBadge(formula = q.chemicalFormula)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(onClick = { viewModel.deleteQuestion(q) }) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        var questionEn by remember { mutableStateOf("") }
        var optA by remember { mutableStateOf("") }
        var optB by remember { mutableStateOf("") }
        var optC by remember { mutableStateOf("") }
        var optD by remember { mutableStateOf("") }
        var correctIndex by remember { mutableStateOf(0) }
        var chapter by remember { mutableStateOf("Organic Mechanisms") }
        var formula by remember { mutableStateOf("") }
        var explanation by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Add Question to Bank") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = questionEn,
                        onValueChange = { questionEn = it },
                        label = { Text("Question Text (English)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = chapter,
                        onValueChange = { chapter = it },
                        label = { Text("Chapter / Topic") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = formula,
                        onValueChange = { formula = it },
                        label = { Text("Chemical Formula / Reaction (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = optA,
                        onValueChange = { optA = it },
                        label = { Text("Option A") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = optB,
                        onValueChange = { optB = it },
                        label = { Text("Option B") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = optC,
                        onValueChange = { optC = it },
                        label = { Text("Option C") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = optD,
                        onValueChange = { optD = it },
                        label = { Text("Option D") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Correct Option: ", style = MaterialTheme.typography.bodySmall)
                        listOf(0 to "A", 1 to "B", 2 to "C", 3 to "D").forEach { (idx, ltr) ->
                            RadioButton(selected = correctIndex == idx, onClick = { correctIndex = idx })
                            Text(ltr, modifier = Modifier.clickable { correctIndex = idx })
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                    }
                    OutlinedTextField(
                        value = explanation,
                        onValueChange = { explanation = it },
                        label = { Text("Verified Explanation") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (questionEn.isNotBlank() && optA.isNotBlank()) {
                            val opts = listOf(optA, optB, optC, optD)
                            val newQ = McqQuestionEntity(
                                id = "mcq_${UUID.randomUUID().toString().take(8)}",
                                targetClass = "CSIR-NET",
                                subject = "Chemical Sciences",
                                chapter = chapter,
                                topic = chapter,
                                questionEn = questionEn,
                                optionsEnJson = JsonUtils.stringListToJson(opts),
                                correctOptionIndex = correctIndex,
                                explanationEn = explanation,
                                chemicalFormula = formula.ifBlank { null },
                                isApproved = true
                            )
                            viewModel.saveQuestion(newQ)
                            showCreateDialog = false
                        }
                    }
                ) {
                    Text("Save Question")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// --- Teacher Mock Tests Management Screen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherMockTestsScreen(viewModel: KarmaViewModel) {
    val allMockTests by viewModel.allMockTests.collectAsState()
    val allQuestions by viewModel.allQuestions.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    BackHandler { viewModel.navigateBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mock Tests (${allMockTests.size})") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Create Mock Test")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(allMockTests) { test ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(test.targetClass, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("Total: ${test.totalMarks.toInt()} Marks", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(test.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Text("Duration: ${test.durationMinutes} Mins • Negative Mark: -${test.negativeMarkingPerQuestion}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(onClick = { viewModel.deleteMockTest(test) }) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        var title by remember { mutableStateOf("") }
        var targetClass by remember { mutableStateOf("CSIR-NET") }
        var duration by remember { mutableStateOf("60") }
        var totalMarks by remember { mutableStateOf("50") }
        var negativeMark by remember { mutableStateOf("1.0") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create Timed Mock Test") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Mock Test Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = targetClass,
                        onValueChange = { targetClass = it },
                        label = { Text("Target Class/Exam") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = duration,
                        onValueChange = { duration = it },
                        label = { Text("Duration (Minutes)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = totalMarks,
                        onValueChange = { totalMarks = it },
                        label = { Text("Total Marks") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = negativeMark,
                        onValueChange = { negativeMark = it },
                        label = { Text("Negative Marking per wrong MCQ") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            // Assign all available questions for this class
                            val matchingQIds = allQuestions.filter { it.targetClass == targetClass || it.targetClass == "ALL" }.map { it.id }
                            val newTest = MockTestEntity(
                                id = "mock_${UUID.randomUUID().toString().take(8)}",
                                title = title,
                                targetClass = targetClass,
                                subject = "Chemical Sciences",
                                durationMinutes = duration.toIntOrNull() ?: 60,
                                totalMarks = totalMarks.toFloatOrNull() ?: 50f,
                                negativeMarkingPerQuestion = negativeMark.toFloatOrNull() ?: 1.0f,
                                questionIdsJson = JsonUtils.stringListToJson(matchingQIds),
                                isPublished = true
                            )
                            viewModel.saveMockTest(newTest)
                            showCreateDialog = false
                        }
                    }
                ) {
                    Text("Publish Test")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// --- Teacher AI Explanation Hub Screen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherAiHubScreen(viewModel: KarmaViewModel) {
    var topicPrompt by remember { mutableStateOf("Pericyclic Reactions and Sigmatropic Rearrangements") }
    var targetClass by remember { mutableStateOf("CSIR-NET") }
    var difficulty by remember { mutableStateOf("HARD") }
    var language by remember { mutableStateOf("Bilingual (Hindi + English)") }

    var isGenerating by remember { mutableStateOf(false) }
    var generatedResult by remember { mutableStateOf<String?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val aiLoading by viewModel.aiExplanationLoading.collectAsState()
    val aiText by viewModel.aiExplanationText.collectAsState()
    val aiErr by viewModel.aiExplanationError.collectAsState()

    BackHandler { viewModel.navigateBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gemini AI Explanation Lab") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AI Pedagogical Assistant", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Generate competitive chemistry MCQs, detailed orbital reaction mechanisms, or test step-by-step Hindi/English explanations before approving them for students.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = topicPrompt,
                    onValueChange = { topicPrompt = it },
                    label = { Text("Chemistry Topic / Reaction Mechanism") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = targetClass,
                        onValueChange = { targetClass = it },
                        label = { Text("Exam/Class") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = difficulty,
                        onValueChange = { difficulty = it },
                        label = { Text("Difficulty") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Button(
                    onClick = {
                        isGenerating = true
                        generatedResult = null
                        errorMsg = null
                        // Launch coroutine via viewModel
                        viewModel.requestAiExplanation(
                            question = McqQuestionEntity(
                                id = "temp",
                                targetClass = targetClass,
                                subject = "Chemical Sciences",
                                chapter = topicPrompt,
                                topic = topicPrompt,
                                questionEn = "Generate high-yield conceptual reasoning for $topicPrompt",
                                optionsEnJson = "[\"Option A\", \"Option B\", \"Option C\", \"Option D\"]",
                                correctOptionIndex = 0
                            ),
                            selectedOption = null,
                            targetLanguage = language
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Psychology, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate & Test AI Explanation")
                }
            }

            if (aiLoading) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Gemini AI is analyzing reaction pathways and drafting explanation...", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            if (aiText != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Generated AI Content Output:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(aiText ?: "", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            if (aiErr != null) {
                item {
                    Text("Notice: $aiErr", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

// --- Teacher Student Roster Screen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherStudentsScreen(viewModel: KarmaViewModel) {
    val allStudents by viewModel.allStudents.collectAsState()
    val allAttempts by viewModel.allAttempts.collectAsState()

    BackHandler { viewModel.navigateBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Enrolled Students (${allStudents.size})") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(allStudents) { student ->
                val attempts = allAttempts.filter { it.userId == student.id }
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(student.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    student.targetClass,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(student.email, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Mock Tests Attempted: ${attempts.size}", style = MaterialTheme.typography.bodySmall)
                        if (attempts.isNotEmpty()) {
                            val avgScore = attempts.map { it.score }.average()
                            Text("Average Score: %.1f".format(avgScore), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

// --- Teacher Announcements Screen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherAnnouncementsScreen(viewModel: KarmaViewModel) {
    val allAnnouncements by viewModel.allAnnouncements.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    BackHandler { viewModel.navigateBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Announcements (${allAnnouncements.size})") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Post Announcement")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(allAnnouncements) { ann ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(ann.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            IconButton(onClick = { viewModel.deleteAnnouncement(ann) }) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            }
                        }
                        Text(ann.message, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Audience: ${ann.targetClass} • Priority: ${ann.priority}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        var title by remember { mutableStateOf("") }
        var message by remember { mutableStateOf("") }
        var targetClass by remember { mutableStateOf("ALL") }
        var priority by remember { mutableStateOf("HIGH") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Broadcast Announcement") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Announcement Headline") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text("Message Body") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = targetClass,
                        onValueChange = { targetClass = it },
                        label = { Text("Target Class (e.g. ALL, CSIR-NET, Class 12)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            val newAnn = AnnouncementEntity(
                                id = "ann_${UUID.randomUUID().toString().take(8)}",
                                title = title,
                                message = message,
                                targetClass = targetClass,
                                priority = priority
                            )
                            viewModel.saveAnnouncement(newAnn)
                            showCreateDialog = false
                        }
                    }
                ) {
                    Text("Broadcast")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
