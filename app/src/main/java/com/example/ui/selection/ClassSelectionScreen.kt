package com.example.ui.selection

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EducationLevel
import com.example.data.model.LanguagePreference
import com.example.ui.KarmaViewModel
import com.example.ui.common.LanguageToggleChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassSelectionScreen(viewModel: KarmaViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allCourses by viewModel.allCourses.collectAsState()

    var selectedLevel by remember(currentUser) {
        mutableStateOf(currentUser?.educationLevel ?: EducationLevel.COMPETITIVE_EXAM)
    }

    var selectedClass by remember(currentUser) {
        mutableStateOf(currentUser?.targetClass ?: "CSIR-NET")
    }

    var selectedSubject by remember(currentUser) {
        mutableStateOf(currentUser?.selectedSubject ?: "Chemical Sciences")
    }

    var selectedCourseId by remember(currentUser) {
        mutableStateOf(currentUser?.selectedCourseId)
    }

    var selectedLanguage by remember(currentUser) {
        mutableStateOf(currentUser?.languagePreference ?: LanguagePreference.BILINGUAL)
    }

    val classOptions = remember(selectedLevel) {
        when (selectedLevel) {
            EducationLevel.SCHOOL -> listOf("Class 6", "Class 7", "Class 8", "Class 9", "Class 10", "Class 11", "Class 12")
            EducationLevel.COLLEGE -> listOf("B.Sc.", "M.Sc.")
            EducationLevel.COMPETITIVE_EXAM -> listOf("CSIR-NET", "CGSET", "NEET / JEE Prep")
        }
    }

    val subjectOptions = remember(selectedLevel, selectedClass) {
        when {
            selectedClass.contains("CSIR") || selectedClass.contains("SET") -> listOf("Chemical Sciences", "Life Sciences", "General Aptitude")
            selectedLevel == EducationLevel.COLLEGE -> listOf("Chemistry", "Physics", "Mathematics")
            selectedClass in listOf("Class 11", "Class 12") -> listOf("Chemistry", "Physics", "Mathematics", "Biology")
            else -> listOf("Science", "Mathematics", "General Studies")
        }
    }

    // Filter matching courses
    val matchingCourses = remember(selectedClass, allCourses) {
        allCourses.filter { it.isPublished && (it.targetClass == selectedClass || it.targetClass == "ALL") }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Select Your Goals", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.updateStudentSelection(
                                level = selectedLevel,
                                targetClass = selectedClass,
                                subject = selectedSubject,
                                courseId = selectedCourseId ?: matchingCourses.firstOrNull()?.id,
                                lang = selectedLanguage
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("confirm_class_selection_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Save & Go to My Dashboard",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Welcome, ${currentUser?.name ?: "Student"}!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Customize your curriculum, study materials, and mock test syllabus.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 1. Education Level
            item {
                Text("1. Select Education Level", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        EducationLevel.COMPETITIVE_EXAM to "Competitive",
                        EducationLevel.COLLEGE to "College",
                        EducationLevel.SCHOOL to "School"
                    ).forEach { (level, title) ->
                        val isSelected = selectedLevel == level
                        OutlinedCard(
                            onClick = {
                                selectedLevel = level
                                selectedClass = when (level) {
                                    EducationLevel.SCHOOL -> "Class 12"
                                    EducationLevel.COLLEGE -> "B.Sc."
                                    EducationLevel.COMPETITIVE_EXAM -> "CSIR-NET"
                                }
                            },
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(
                                1.5.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.School,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            // 2. Class / Exam
            item {
                Text("2. Target Class / Examination", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    classOptions.chunked(3).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowItems.forEach { option ->
                                val isSelected = selectedClass == option
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedClass = option
                                        selectedSubject = subjectOptions.firstOrNull() ?: "Chemistry"
                                    },
                                    label = { Text(option, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            // Fill remaining space if last row has fewer items
                            for (i in 0 until (3 - rowItems.size)) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // 3. Subject
            item {
                Text("3. Primary Subject", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    subjectOptions.forEach { subj ->
                        val isSelected = selectedSubject == subj
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedSubject = subj },
                            label = { Text(subj) },
                            leadingIcon = {
                                Icon(Icons.Default.Science, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        )
                    }
                }
            }

            // 4. Preferred Language
            item {
                Text("4. Language Preference", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                LanguageToggleChip(
                    current = selectedLanguage,
                    onChanged = { selectedLanguage = it }
                )
            }

            // 5. Recommended Course Matching
            item {
                Text("5. Available Courses for $selectedClass", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                if (matchingCourses.isEmpty()) {
                    Text(
                        "No published course yet for $selectedClass. You can still access general chemistry notes and question banks.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        matchingCourses.forEach { course ->
                            val isSelected = selectedCourseId == course.id || (selectedCourseId == null && matchingCourses.first() == course)
                            Card(
                                onClick = { selectedCourseId = course.id },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedCourseId = course.id }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(course.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                        Text(
                                            if (course.isPaid) "Paid Course (₹${course.priceINR.toInt()})" else "Free Course",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (course.isPaid) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
