package com.example.ui.student

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.JsonUtils
import com.example.data.model.*
import com.example.ui.AppScreen
import com.example.ui.KarmaViewModel
import com.example.ui.common.ChemicalFormulaBadge
import com.example.ui.common.LanguageToggleChip
import com.example.ui.common.ProtectedContentBadge

enum class StudentTab(val label: String) {
    HOME("Home"),
    COURSES("Courses"),
    LIVE("Live"),
    PRACTICE("Practice"),
    TESTS("Tests"),
    NOTES("Notes"),
    PROFILE("Profile")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentMainScreen(viewModel: KarmaViewModel) {
    var selectedTab by remember { mutableStateOf(StudentTab.HOME) }
    val currentUser by viewModel.currentUser.collectAsState()
    val allCourses by viewModel.allCourses.collectAsState()
    val allLive by viewModel.allLiveClasses.collectAsState()
    val allMaterials by viewModel.allMaterials.collectAsState()
    val allQuestions by viewModel.allQuestions.collectAsState()
    val allMockTests by viewModel.allMockTests.collectAsState()
    val allAnnouncements by viewModel.allAnnouncements.collectAsState()
    val allAttempts by viewModel.allAttempts.collectAsState()
    val securityConfig by viewModel.securityConfig.collectAsState()

    val targetClass = currentUser?.targetClass ?: "CSIR-NET"
    val preferredLang = currentUser?.languagePreference ?: LanguagePreference.BILINGUAL
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Karma Learning",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = targetClass,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = currentUser?.selectedSubject ?: "Chemical Sciences",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    ProtectedContentBadge(isProtected = securityConfig?.antiScreenshotEnabled == true)
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.ClassSelection) },
                        modifier = Modifier.testTag("change_class_top_btn")
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = "Change Class/Exam")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                listOf(
                    Triple(StudentTab.HOME, Icons.Default.Home, "Home"),
                    Triple(StudentTab.COURSES, Icons.Default.MenuBook, "Courses"),
                    Triple(StudentTab.LIVE, Icons.Default.VideoCameraFront, "Live"),
                    Triple(StudentTab.PRACTICE, Icons.Default.Quiz, "Practice"),
                    Triple(StudentTab.TESTS, Icons.Default.Assignment, "Tests"),
                    Triple(StudentTab.NOTES, Icons.Default.Description, "Notes"),
                    Triple(StudentTab.PROFILE, Icons.Default.Person, "Profile")
                ).forEach { (tab, icon, label) ->
                    val isSelected = selectedTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label, fontSize = 10.sp, maxLines = 1) },
                        modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                StudentTab.HOME -> HomeTabContent(
                    currentUser = currentUser,
                    courses = allCourses.filter { it.isPublished && (it.targetClass == targetClass || it.targetClass == "ALL") },
                    liveClasses = allLive.filter { it.targetClass == targetClass || it.targetClass == "ALL" },
                    materials = allMaterials.filter { it.isPublished && (it.targetClass == targetClass || it.targetClass == "ALL") },
                    announcements = allAnnouncements.filter { it.targetClass == "ALL" || it.targetClass == targetClass },
                    mockTests = allMockTests.filter { it.isPublished && (it.targetClass == targetClass || it.targetClass == "ALL") },
                    viewModel = viewModel,
                    onNavigateTab = { selectedTab = it }
                )
                StudentTab.COURSES -> CoursesTabContent(
                    courses = allCourses.filter { it.isPublished && (it.targetClass == targetClass || it.targetClass == "ALL") },
                    currentUser = currentUser,
                    viewModel = viewModel
                )
                StudentTab.LIVE -> LiveClassesTabContent(
                    liveClasses = allLive.filter { it.targetClass == targetClass || it.targetClass == "ALL" },
                    onJoinMeeting = { url ->
                        runCatching {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        }
                    }
                )
                StudentTab.PRACTICE -> PracticeTabContent(
                    questions = allQuestions.filter { it.isApproved && (it.targetClass == targetClass || it.targetClass == "ALL") },
                    preferredLang = preferredLang,
                    viewModel = viewModel
                )
                StudentTab.TESTS -> MockTestsTabContent(
                    mockTests = allMockTests.filter { it.isPublished && (it.targetClass == targetClass || it.targetClass == "ALL") },
                    myAttempts = allAttempts.filter { it.userId == currentUser?.id },
                    viewModel = viewModel
                )
                StudentTab.NOTES -> NotesTabContent(
                    materials = allMaterials.filter { it.isPublished && (it.targetClass == targetClass || it.targetClass == "ALL") },
                    viewModel = viewModel
                )
                StudentTab.PROFILE -> ProfileTabContent(
                    user = currentUser,
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun HomeTabContent(
    currentUser: UserEntity?,
    courses: List<CourseEntity>,
    liveClasses: List<LiveClassEntity>,
    materials: List<StudyMaterialEntity>,
    announcements: List<AnnouncementEntity>,
    mockTests: List<MockTestEntity>,
    viewModel: KarmaViewModel,
    onNavigateTab: (StudentTab) -> Unit
) {
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Hero Welcome Banner
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFFFFD54F)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Karma Learning Classes",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Master Chemistry & Science with Prof. Chandan Khandey",
                            color = Color.White.copy(alpha = 0.95f),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                color = Color.White.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(
                                    "🎯 Target: ${currentUser?.targetClass}",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Surface(
                                color = Color.White.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(
                                    "✨ AI Explanation Ready",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Announcements
        if (announcements.isNotEmpty()) {
            item {
                val latest = announcements.first()
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (latest.priority == "URGENT") MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (latest.priority == "URGENT") Icons.Default.Campaign else Icons.Default.Notifications,
                            contentDescription = null,
                            tint = if (latest.priority == "URGENT") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                latest.title,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                latest.message,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // Next Upcoming Live Class
        val nextLive = liveClasses.firstOrNull { it.status == "UPCOMING" || it.status == "LIVE" }
        if (nextLive != null) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🔴 Live Masterclass", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    TextButton(onClick = { onNavigateTab(StudentTab.LIVE) }) {
                        Text("View All")
                    }
                }
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = if (nextLive.status == "LIVE") Color.Red else MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = if (nextLive.status == "LIVE") "● LIVE NOW" else "UPCOMING",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "📅 ${nextLive.scheduledDate} at ${nextLive.startTime} - ${nextLive.endTime}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            nextLive.title,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            "Faculty: ${nextLive.teacherName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                runCatching {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(nextLive.meetingUrl))
                                    context.startActivity(intent)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.VideoCall, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Join Google Meet Class")
                        }
                    }
                }
            }
        }

        // Quick Feature Cards
        item {
            Text("Quick Study Hub", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionCard(
                    title = "MCQ Practice",
                    subtitle = "With AI Solution",
                    icon = Icons.Default.Psychology,
                    containerColor = Color(0xFFE8F5E9),
                    contentColor = Color(0xFF2E7D32),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTab(StudentTab.PRACTICE) }
                )
                QuickActionCard(
                    title = "Mock Tests",
                    subtitle = "Timed Exam",
                    icon = Icons.Default.Timer,
                    containerColor = Color(0xFFFFF3E0),
                    contentColor = Color(0xFFE65100),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTab(StudentTab.TESTS) }
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionCard(
                    title = "PDF Notes",
                    subtitle = "Handbooks & PYQ",
                    icon = Icons.Default.PictureAsPdf,
                    containerColor = Color(0xFFE3F2FD),
                    contentColor = Color(0xFF1565C0),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTab(StudentTab.NOTES) }
                )
                QuickActionCard(
                    title = "My Courses",
                    subtitle = "Video Modules",
                    icon = Icons.Default.PlayCircle,
                    containerColor = Color(0xFFF3E5F5),
                    contentColor = Color(0xFF6A1B9A),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTab(StudentTab.COURSES) }
                )
            }
        }

        // Popular Courses for this class
        if (courses.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Featured Courses", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    TextButton(onClick = { onNavigateTab(StudentTab.COURSES) }) {
                        Text("See All")
                    }
                }
            }

            items(courses) { course ->
                val isEnrolled = viewModel.isUserEnrolled(course.id)
                CourseItemCard(
                    course = course,
                    isEnrolled = isEnrolled,
                    onEnroll = { viewModel.enrollInCourse(course.id) },
                    onOpen = { viewModel.navigateTo(AppScreen.StudentCourseDetail(course.id)) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, color = contentColor)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = contentColor.copy(alpha = 0.8f))
        }
    }
}

@Composable
fun CourseItemCard(
    course: CourseEntity,
    isEnrolled: Boolean,
    onEnroll: () -> Unit,
    onOpen: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() }
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
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontWeight = FontWeight.Bold
                    )
                }

                if (isEnrolled) {
                    Surface(
                        color = Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            "✓ ENROLLED",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                } else {
                    Text(
                        if (course.isPaid) "₹${course.priceINR.toInt()}" else "FREE",
                        fontWeight = FontWeight.Bold,
                        color = if (course.isPaid) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(course.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Text(
                course.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "⏱ ${course.durationText} | 🗣 ${course.language}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (isEnrolled) {
                    FilledTonalButton(
                        onClick = onOpen,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text("Continue Study", fontSize = 12.sp)
                    }
                } else {
                    Button(
                        onClick = onEnroll,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                    ) {
                        Text(if (course.isPaid) "Enroll Now" else "Join Free", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun CoursesTabContent(
    courses: List<CourseEntity>,
    currentUser: UserEntity?,
    viewModel: KarmaViewModel
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Courses for ${currentUser?.targetClass}",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                "Access structured video lectures, chemical reaction mechanism masterclasses, and PDF notes.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (courses.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "No published courses currently available for ${currentUser?.targetClass}. Please check back soon or change your target class.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            items(courses) { course ->
                val isEnrolled = viewModel.isUserEnrolled(course.id)
                CourseItemCard(
                    course = course,
                    isEnrolled = isEnrolled,
                    onEnroll = { viewModel.enrollInCourse(course.id) },
                    onOpen = { viewModel.navigateTo(AppScreen.StudentCourseDetail(course.id)) }
                )
            }
        }
    }
}

@Composable
fun LiveClassesTabContent(
    liveClasses: List<LiveClassEntity>,
    onJoinMeeting: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Live Classroom Schedule", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Text(
                "Join live interactive chemistry sessions with two-way doubt solving and real-time screen sharing via Google Meet.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (liveClasses.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Text("No live classes scheduled for today. Check announcements for upcoming dates.", modifier = Modifier.padding(16.dp))
                }
            }
        } else {
            items(liveClasses) { live ->
                Card(
                    shape = RoundedCornerShape(14.dp),
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
                                color = if (live.status == "LIVE") Color.Red else MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = if (live.status == "LIVE") "● LIVE NOW" else "SCHEDULED",
                                    color = if (live.status == "LIVE") Color.White else MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                "📅 ${live.scheduledDate}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(live.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        if (live.description.isNotBlank()) {
                            Text(live.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("⏰ Time: ${live.startTime} - ${live.endTime} | Faculty: ${live.teacherName}", style = MaterialTheme.typography.bodySmall)

                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { onJoinMeeting(live.meetingUrl) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.VideoCameraFront, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Launch Live Session")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PracticeTabContent(
    questions: List<McqQuestionEntity>,
    preferredLang: LanguagePreference,
    viewModel: KarmaViewModel
) {
    var selectedQuestionIndex by remember { mutableStateOf(0) }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var showExplanation by remember { mutableStateOf(false) }

    val aiLoading by viewModel.aiExplanationLoading.collectAsState()
    val aiText by viewModel.aiExplanationText.collectAsState()
    val aiError by viewModel.aiExplanationError.collectAsState()

    val currentQ = questions.getOrNull(selectedQuestionIndex)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Practice Set & Question Bank", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                ProtectedContentBadge(isProtected = true)
            }
            Text(
                "Solve high-yield Chemistry MCQs with immediate feedback, chemical equations, and Gemini AI explanations.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (currentQ == null) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Text("No practice questions found for this topic.", modifier = Modifier.padding(16.dp))
                }
            }
        } else {
            val options = JsonUtils.jsonToStringList(currentQ.optionsEnJson)
            val isHindi = preferredLang == LanguagePreference.HINDI

            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Question ${selectedQuestionIndex + 1} of ${questions.size}",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    currentQ.chapter,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        if (!currentQ.chemicalFormula.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            ChemicalFormulaBadge(formula = currentQ.chemicalFormula)
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (isHindi && currentQ.questionHi.isNotBlank()) currentQ.questionHi else currentQ.questionEn,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Options
                        options.forEachIndexed { idx, optText ->
                            val isSelected = selectedOptionIndex == idx
                            val isCorrect = idx == currentQ.correctOptionIndex

                            val backgroundColor = when {
                                showExplanation && isCorrect -> Color(0xFFC8E6C9)
                                showExplanation && isSelected && !isCorrect -> Color(0xFFFFCDD2)
                                isSelected -> MaterialTheme.colorScheme.primaryContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            }

                            Card(
                                onClick = {
                                    if (!showExplanation) {
                                        selectedOptionIndex = idx
                                    }
                                },
                                colors = CardDefaults.cardColors(containerColor = backgroundColor),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "${('A' + idx)})",
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.width(26.dp)
                                    )
                                    Text(
                                        optText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (showExplanation) {
                                        if (isCorrect) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32))
                                        } else if (isSelected) {
                                            Icon(Icons.Default.Cancel, contentDescription = null, tint = Color(0xFFC62828))
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (!showExplanation) {
                                Button(
                                    onClick = {
                                        if (selectedOptionIndex != null) {
                                            showExplanation = true
                                        } else {
                                            viewModel.showToast("Please choose an option first")
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Check Answer")
                                }
                            } else {
                                Button(
                                    onClick = {
                                        if (selectedQuestionIndex < questions.size - 1) {
                                            selectedQuestionIndex += 1
                                            selectedOptionIndex = null
                                            showExplanation = false
                                        } else {
                                            viewModel.showToast("All practice questions completed!")
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Next Question")
                                }
                            }
                        }

                        // Explanation & AI Section
                        if (showExplanation) {
                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(10.dp))

                            Text("Teacher Verified Explanation:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                if (isHindi && currentQ.explanationHi.isNotBlank()) currentQ.explanationHi else currentQ.explanationEn,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // AI Explanation Button
                            OutlinedButton(
                                onClick = {
                                    viewModel.requestAiExplanation(
                                        question = currentQ,
                                        selectedOption = selectedOptionIndex,
                                        targetLanguage = if (isHindi) "Hindi" else "English"
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Psychology, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Ask Gemini AI: Detailed Mechanism & Hints")
                            }

                            if (aiLoading) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Generating detailed chemical step-by-step reasoning...", style = MaterialTheme.typography.bodySmall)
                                }
                            }

                            if (aiText != null) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Gemini AI Conceptual Explanation", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(aiText ?: "", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }

                            if (aiError != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    aiError ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MockTestsTabContent(
    mockTests: List<MockTestEntity>,
    myAttempts: List<TestAttemptEntity>,
    viewModel: KarmaViewModel
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Timed Mock Examinations", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Text(
                "Simulated exam engine with countdown timer, negative marking, question palette, and instant detailed score analysis.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (mockTests.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Text("No mock tests available currently.", modifier = Modifier.padding(16.dp))
                }
            }
        } else {
            items(mockTests) { test ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
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
                                    test.targetClass,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                "Total Marks: ${test.totalMarks.toInt()}",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(test.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "⏱ Duration: ${test.durationMinutes} Mins | ❌ Negative Mark: -${test.negativeMarkingPerQuestion}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.startMockTest(test) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Start Timed Test Now")
                        }
                    }
                }
            }
        }

        // Past attempts
        if (myAttempts.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Text("My Test History & Scores", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
            items(myAttempts) { attempt ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(attempt.testTitle, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Text(
                                "Score: ${attempt.score} / ${attempt.totalMarks} | Accuracy: ${(attempt.correctCount * 100 / (attempt.attemptedCount.coerceAtLeast(1)))}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        TextButton(
                            onClick = { viewModel.navigateTo(AppScreen.StudentTestResult(attempt.id)) }
                        ) {
                            Text("Review")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotesTabContent(
    materials: List<StudyMaterialEntity>,
    viewModel: KarmaViewModel
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("PDF Notes & Formula Handbooks", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                ProtectedContentBadge(isProtected = true)
            }
            Text(
                "Authorized study materials and mind maps. Protected by Karma FLAG_SECURE window encryption.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (materials.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Text("No PDF materials published yet for this section.", modifier = Modifier.padding(16.dp))
                }
            }
        } else {
            items(materials) { mat ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.navigateTo(AppScreen.StudentPdfViewer(mat.id)) }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (mat.fileType == "PDF") Icons.Default.PictureAsPdf else Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(mat.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Text(
                                "Topic: ${mat.topic} | Pages: ${mat.pageCount}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = { viewModel.navigateTo(AppScreen.StudentPdfViewer(mat.id)) }
                        ) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "View PDF")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileTabContent(
    user: UserEntity?,
    viewModel: KarmaViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                user?.name?.take(1)?.uppercase() ?: "S",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(user?.name ?: "Student", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(user?.email ?: "", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Current Learning Profile", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Target Exam/Class:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(user?.targetClass ?: "CSIR-NET", fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Subject:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(user?.selectedSubject ?: "Chemical Sciences", fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Language:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(user?.languagePreference?.name ?: "Bilingual", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = { viewModel.navigateTo(AppScreen.ClassSelection) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.Edit, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Switch Class / Examination")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { viewModel.logout() },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.Logout, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sign Out")
        }
    }
}
