package com.example.ui.student

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.JsonUtils
import com.example.data.model.*
import com.example.ui.AppScreen
import com.example.ui.KarmaViewModel
import com.example.ui.common.ChemicalFormulaBadge
import com.example.ui.common.LocalSecureWindowController
import com.example.ui.common.ProtectedContentBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentCourseDetailScreen(courseId: String, viewModel: KarmaViewModel) {
    val allCourses by viewModel.allCourses.collectAsState()
    val course = allCourses.firstOrNull { it.id == courseId }
    val modules by viewModel.getCourseModules(courseId).collectAsState(initial = emptyList())
    val lessons by viewModel.getCourseLessons(courseId).collectAsState(initial = emptyList())
    val isEnrolled = viewModel.isUserEnrolled(courseId)

    BackHandler { viewModel.navigateBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(course?.title ?: "Course Details", maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    ProtectedContentBadge(isProtected = course?.isPaid == true)
                }
            )
        },
        bottomBar = {
            if (!isEnrolled && course != null) {
                Surface(shadowElevation = 8.dp) {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Button(
                            onClick = { viewModel.enrollInCourse(course.id) },
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) {
                            Text(
                                if (course.isPaid) "Enroll for ₹${course.priceINR.toInt()}" else "Enroll for Free",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (course == null) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("Course not found.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(course.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Faculty: ${course.instructorName}", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(course.description, style = MaterialTheme.typography.bodyMedium)
                }

                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Syllabus & Details", fontWeight = FontWeight.Bold)
                            Text("⏱ Duration: ${course.durationText}", style = MaterialTheme.typography.bodySmall)
                            Text("🗣 Language: ${course.language}", style = MaterialTheme.typography.bodySmall)
                            if (course.syllabusText.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Curriculum Outline:\n${course.syllabusText}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                item {
                    Text("Course Modules & Lessons", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }

                if (modules.isEmpty() && lessons.isEmpty()) {
                    item {
                        Text("No lessons uploaded for this course yet.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                } else {
                    items(modules) { module ->
                        val modLessons = lessons.filter { it.moduleId == module.id }
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(module.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                Spacer(modifier = Modifier.height(8.dp))
                                modLessons.forEach { lesson ->
                                    val isLocked = lesson.isPaidOnly && !isEnrolled
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                if (isLocked) {
                                                    viewModel.showToast("Please enroll in course to unlock this lesson.")
                                                } else {
                                                    viewModel.navigateTo(AppScreen.StudentLessonViewer(course.id, lesson.id))
                                                }
                                            }
                                            .padding(vertical = 8.dp, horizontal = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (lesson.type == "VIDEO") Icons.Default.PlayCircle else Icons.Default.Description,
                                            contentDescription = null,
                                            tint = if (isLocked) Color.Gray else MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(lesson.title, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
                                            Text("⏱ ${lesson.durationMinutes} mins • ${lesson.type}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                        }
                                        if (isLocked) {
                                            Icon(Icons.Default.Lock, contentDescription = "Locked", tint = Color.Gray, modifier = Modifier.size(18.dp))
                                        } else {
                                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentLessonViewerScreen(courseId: String, lessonId: String, viewModel: KarmaViewModel) {
    val securityController = LocalSecureWindowController.current
    val lessons by viewModel.getCourseLessons(courseId).collectAsState(initial = emptyList())
    val lesson = lessons.firstOrNull { it.id == lessonId }
    val context = LocalContext.current

    // Trigger FLAG_SECURE window protection
    DisposableEffect(Unit) {
        securityController.setProtection(true)
        onDispose {
            securityController.setProtection(false)
        }
    }

    BackHandler { viewModel.navigateBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(lesson?.title ?: "Lesson Viewer", maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    ProtectedContentBadge(isProtected = true)
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Video / Player representation
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Black),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.PlayCircleFilled,
                            contentDescription = "Play Lecture",
                            tint = Color.White,
                            modifier = Modifier
                                .size(64.dp)
                                .clickable {
                                    val url = lesson?.contentUrl
                                    if (!url.isNullOrBlank() && url.startsWith("http")) {
                                        runCatching {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                            context.startActivity(intent)
                                        }
                                    } else {
                                        viewModel.showToast("Streaming lecture player started")
                                    }
                                }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Karma Protected Video Player", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Screen Capture Disabled by FLAG_SECURE", color = Color(0xFFFFD54F), fontSize = 11.sp)
                    }
                }
            }

            Text(
                lesson?.title ?: "",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Lecture Notes & Summary", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "In this lecture, Prof. Chandan Khandey covers core reaction pathways, electron flow diagrams, orbital symmetry, and competitive examination shortcuts.\n\n" +
                        "Note: Taking screenshots or recording this window will result in a blank protected screen.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Button(
                onClick = {
                    val url = lesson?.contentUrl
                    if (!url.isNullOrBlank() && url.startsWith("http")) {
                        runCatching {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        }
                    } else {
                        viewModel.showToast("Opening lesson content...")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.OpenInNew, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open Video Link / Stream")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentPdfViewerScreen(materialId: String, viewModel: KarmaViewModel) {
    val securityController = LocalSecureWindowController.current
    val allMaterials by viewModel.allMaterials.collectAsState()
    val material = allMaterials.firstOrNull { it.id == materialId }
    var currentPage by remember { mutableStateOf(1) }

    // Trigger FLAG_SECURE window protection
    DisposableEffect(Unit) {
        securityController.setProtection(true)
        onDispose {
            securityController.setProtection(false)
        }
    }

    BackHandler { viewModel.navigateBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(material?.title ?: "Protected PDF Viewer", maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    ProtectedContentBadge(isProtected = true)
                }
            )
        },
        bottomBar = {
            Surface(shadowElevation = 6.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { if (currentPage > 1) currentPage -= 1 },
                        enabled = currentPage > 1
                    ) {
                        Text("Previous")
                    }
                    Text("Page $currentPage of ${material?.pageCount ?: 12}", fontWeight = FontWeight.Bold)
                    Button(
                        onClick = { if (currentPage < (material?.pageCount ?: 12)) currentPage += 1 },
                        enabled = currentPage < (material?.pageCount ?: 12)
                    ) {
                        Text("Next")
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "KARMA LEARNING CLASSES",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp
                            )
                            Text("TOPIC: ${material?.topic}", style = MaterialTheme.typography.labelSmall)
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        Text(
                            material?.title ?: "Chemistry Handbook",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            "Section $currentPage: Reaction Mechanisms & Energetics\n\n" +
                            "• Standard cell EMF calculation: E°cell = E°cathode - E°anode\n" +
                            "• Nernst modification for non-standard concentration.\n" +
                            "• Stereochemical retention in nucleophilic substitutions.\n" +
                            "• In-depth crystal field splitting parameters (10 Dq = Δo).\n\n" +
                            "[Encrypted study document page $currentPage rendered in high fidelity]",
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp
                        )
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "🛡️ Anti-Screenshot Active: Native Window FLAG_SECURE",
                            modifier = Modifier.padding(8.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentMockTestRunnerScreen(mockTestId: String, viewModel: KarmaViewModel) {
    val securityController = LocalSecureWindowController.current
    val allMockTests by viewModel.allMockTests.collectAsState()
    val allQuestions by viewModel.allQuestions.collectAsState()
    val test = allMockTests.firstOrNull { it.id == mockTestId }

    val testQuestions = remember(test, allQuestions) {
        val qIds = JsonUtils.jsonToStringList(test?.questionIdsJson)
        allQuestions.filter { qIds.contains(it.id) }
    }

    val timerSeconds by viewModel.mockTestTimerSeconds.collectAsState()
    val userAnswers by viewModel.mockTestAnswers.collectAsState()
    val reviewMarked by viewModel.mockTestReviewMarked.collectAsState()
    val currentIndex by viewModel.mockTestCurrentIndex.collectAsState()

    var showSubmitDialog by remember { mutableStateOf(false) }
    var showPaletteSheet by remember { mutableStateOf(false) }

    // Trigger FLAG_SECURE window protection during active test
    DisposableEffect(Unit) {
        securityController.setProtection(true)
        onDispose {
            securityController.setProtection(false)
        }
    }

    BackHandler {
        showSubmitDialog = true
    }

    val currentQ = testQuestions.getOrNull(currentIndex)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(test?.title ?: "Mock Examination", maxLines = 1, style = MaterialTheme.typography.titleSmall)
                        val mins = timerSeconds / 60
                        val secs = timerSeconds % 60
                        Text(
                            "⏱ Time Left: %02d:%02d".format(mins, secs),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (timerSeconds < 300) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showPaletteSheet = true }) {
                        Icon(Icons.Default.GridOn, contentDescription = "Question Palette")
                    }
                    TextButton(
                        onClick = { showSubmitDialog = true },
                        modifier = Modifier.testTag("submit_mock_test_btn")
                    ) {
                        Text("Submit", fontWeight = FontWeight.Bold)
                    }
                }
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            if (currentIndex > 0) viewModel.mockTestCurrentIndex.value = currentIndex - 1
                        },
                        enabled = currentIndex > 0
                    ) {
                        Text("Prev")
                    }

                    if (currentQ != null) {
                        val isMarked = reviewMarked.contains(currentQ.id)
                        FilledTonalButton(
                            onClick = { viewModel.toggleMarkForReview(currentQ.id) }
                        ) {
                            Icon(
                                if (isMarked) Icons.Default.BookmarkAdded else Icons.Default.BookmarkBorder,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isMarked) "Marked" else "Review")
                        }
                    }

                    Button(
                        onClick = {
                            if (currentIndex < testQuestions.size - 1) {
                                viewModel.mockTestCurrentIndex.value = currentIndex + 1
                            } else {
                                showSubmitDialog = true
                            }
                        }
                    ) {
                        Text(if (currentIndex < testQuestions.size - 1) "Next" else "Review & Submit")
                    }
                }
            }
        }
    ) { innerPadding ->
        if (currentQ == null) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("Loading mock test questions...")
            }
        } else {
            val options = JsonUtils.jsonToStringList(currentQ.optionsEnJson)
            val selectedOption = userAnswers[currentQ.id]

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Question ${currentIndex + 1} of ${testQuestions.size}",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("+${currentQ.marks} / -${currentQ.negativeMarks}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                if (!currentQ.chemicalFormula.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    ChemicalFormulaBadge(formula = currentQ.chemicalFormula)
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    currentQ.questionEn,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(16.dp))

                options.forEachIndexed { idx, optText ->
                    val isSelected = selectedOption == idx
                    Card(
                        onClick = { viewModel.selectAnswer(currentQ.id, idx) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { viewModel.selectAnswer(currentQ.id, idx) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(optText, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }

    // Submit confirmation dialog
    if (showSubmitDialog) {
        AlertDialog(
            onDismissRequest = { showSubmitDialog = false },
            title = { Text("Submit Mock Test?") },
            text = {
                Text(
                    "You have answered ${userAnswers.size} of ${testQuestions.size} questions.\n\n" +
                    "Are you sure you want to finalize and submit?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSubmitDialog = false
                        if (test != null) {
                            viewModel.submitMockTest(test)
                        }
                    }
                ) {
                    Text("Submit Test")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitDialog = false }) {
                    Text("Continue Test")
                }
            }
        )
    }

    // Question Palette BottomSheet
    if (showPaletteSheet) {
        ModalBottomSheet(onDismissRequest = { showPaletteSheet = false }) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Question Navigation Palette", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(12.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(testQuestions.size) { idx ->
                        val q = testQuestions[idx]
                        val isAnswered = userAnswers.containsKey(q.id)
                        val isMarked = reviewMarked.contains(q.id)

                        val bg = when {
                            isMarked -> Color(0xFFFFF176) // Yellow
                            isAnswered -> Color(0xFFA5D6A7) // Green
                            else -> Color(0xFFEEEEEE)
                        }

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(bg)
                                .clickable {
                                    viewModel.mockTestCurrentIndex.value = idx
                                    showPaletteSheet = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${idx + 1}", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentTestResultScreen(attemptId: String, viewModel: KarmaViewModel) {
    val securityController = LocalSecureWindowController.current
    val allAttempts by viewModel.allAttempts.collectAsState()
    val allQuestions by viewModel.allQuestions.collectAsState()
    val attempt = allAttempts.firstOrNull { it.id == attemptId }

    // Protect answer key screen with FLAG_SECURE
    DisposableEffect(Unit) {
        securityController.setProtection(true)
        onDispose {
            securityController.setProtection(false)
        }
    }

    BackHandler {
        viewModel.navigateTo(AppScreen.StudentMain)
    }

    val userAnswers = remember(attempt) {
        JsonUtils.jsonToIntMap(attempt?.answersMapJson)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mock Test Scorecard") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(AppScreen.StudentMain) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    ProtectedContentBadge(isProtected = true)
                }
            )
        }
    ) { innerPadding ->
        if (attempt == null) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("Result not found.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Score card summary
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("YOUR TOTAL SCORE", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Text(
                                "%.1f / %.0f".format(attempt.score, attempt.totalMarks),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Correct", style = MaterialTheme.typography.labelSmall)
                                    Text("✓ ${attempt.correctCount}", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Incorrect", style = MaterialTheme.typography.labelSmall)
                                    Text("✗ ${attempt.incorrectCount}", color = Color(0xFFC62828), fontWeight = FontWeight.Bold)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Time", style = MaterialTheme.typography.labelSmall)
                                    Text("${attempt.timeTakenSeconds / 60}m ${attempt.timeTakenSeconds % 60}s", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                item {
                    Text("Question-by-Question Solution Review", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }

                items(allQuestions.filter { userAnswers.containsKey(it.id) }) { q ->
                    val userPick = userAnswers[q.id]
                    val isCorrect = userPick == q.correctOptionIndex
                    val options = JsonUtils.jsonToStringList(q.optionsEnJson)

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
                                Text(q.chapter, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                Text(
                                    if (isCorrect) "✓ Correct (+${q.marks})" else "✗ Incorrect (-${q.negativeMarks})",
                                    color = if (isCorrect) Color(0xFF2E7D32) else Color(0xFFC62828),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(q.questionEn, fontWeight = FontWeight.SemiBold)

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Your Answer: Option ${userPick?.let { ('A' + it) } ?: "Unattempted"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isCorrect) Color(0xFF2E7D32) else Color(0xFFC62828)
                            )
                            Text(
                                "Correct Key: Option ${('A' + q.correctOptionIndex)} (${options.getOrNull(q.correctOptionIndex)})",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            if (q.explanationEn.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    "Explanation: ${q.explanationEn}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = {
                                    viewModel.requestAiExplanation(
                                        question = q,
                                        selectedOption = userPick,
                                        targetLanguage = "English"
                                    )
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Ask Gemini AI Step-by-Step", fontSize = 12.sp)
                            }
                        }
                    }
                }

                item {
                    Button(
                        onClick = { viewModel.navigateTo(AppScreen.StudentMain) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Return to Student Dashboard")
                    }
                }
            }
        }
    }
}
