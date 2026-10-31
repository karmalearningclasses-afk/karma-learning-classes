package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.JsonUtils
import com.example.data.model.*
import com.example.data.repository.KarmaRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

sealed class AppScreen {
    object Login : AppScreen()
    object Register : AppScreen()
    object ClassSelection : AppScreen()
    object StudentMain : AppScreen()
    object TeacherDashboard : AppScreen()

    // Sub-screens
    data class StudentCourseDetail(val courseId: String) : AppScreen()
    data class StudentLessonViewer(val courseId: String, val lessonId: String) : AppScreen()
    data class StudentPdfViewer(val materialId: String) : AppScreen()
    data class StudentLiveClassDetail(val liveClassId: String) : AppScreen()
    data class StudentPracticeSession(val targetClass: String, val chapter: String?) : AppScreen()
    data class StudentMockTestRunner(val mockTestId: String) : AppScreen()
    data class StudentTestResult(val attemptId: String) : AppScreen()

    // Teacher sub-screens
    object TeacherCourses : AppScreen()
    object TeacherLiveClasses : AppScreen()
    object TeacherMaterials : AppScreen()
    object TeacherQuestionBank : AppScreen()
    object TeacherMockTests : AppScreen()
    object TeacherAiHub : AppScreen()
    object TeacherStudents : AppScreen()
    object TeacherSecurity : AppScreen()
    object TeacherAnnouncements : AppScreen()
}

class KarmaViewModel(private val repository: KarmaRepository) : ViewModel() {

    private val _screenStack = MutableStateFlow<List<AppScreen>>(listOf(AppScreen.Login))
    val currentScreen: StateFlow<AppScreen> = _screenStack.map { it.lastOrNull() ?: AppScreen.Login }
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppScreen.Login)

    val currentUser = repository.currentUser

    val securityConfig = repository.getSecurityConfigFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppSecurityConfigEntity())

    val allCourses = repository.getAllCoursesFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val allLiveClasses = repository.getAllLiveClassesFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val allMaterials = repository.getAllStudyMaterialsFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val allQuestions = repository.getAllQuestionsFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val allMockTests = repository.getAllMockTestsFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val allAnnouncements = repository.getAllAnnouncementsFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val allStudents = repository.getAllStudentsFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val allAttempts = repository.getAllAttemptsFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // UI Feedback
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // Active Mock Test Run State
    val mockTestTimerSeconds = MutableStateFlow(0)
    val mockTestAnswers = MutableStateFlow<Map<String, Int>>(emptyMap())
    val mockTestReviewMarked = MutableStateFlow<Set<String>>(emptySet())
    val mockTestCurrentIndex = MutableStateFlow(0)
    private var testTimerJob: Job? = null

    // AI Explanation State
    val aiExplanationLoading = MutableStateFlow(false)
    val aiExplanationText = MutableStateFlow<String?>(null)
    val aiExplanationError = MutableStateFlow<String?>(null)

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun navigateTo(screen: AppScreen) {
        val current = _screenStack.value.toMutableList()
        current.add(screen)
        _screenStack.value = current
    }

    fun navigateBack(): Boolean {
        val current = _screenStack.value.toMutableList()
        if (current.size > 1) {
            current.removeAt(current.size - 1)
            _screenStack.value = current
            return true
        }
        return false
    }

    fun logout() {
        repository.setCurrentUser(null)
        _screenStack.value = listOf(AppScreen.Login)
    }

    // --- Authentication ---
    fun login(email: String, pass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val res = repository.login(email, pass)
            res.onSuccess { user ->
                if (user.role == UserRole.TEACHER) {
                    _screenStack.value = listOf(AppScreen.TeacherDashboard)
                } else {
                    if (user.selectedCourseId.isNullOrBlank()) {
                        _screenStack.value = listOf(AppScreen.ClassSelection)
                    } else {
                        _screenStack.value = listOf(AppScreen.StudentMain)
                    }
                }
                onSuccess()
            }.onFailure {
                onError(it.message ?: "Login failed")
            }
        }
    }

    fun register(
        name: String,
        email: String,
        pass: String,
        role: UserRole = UserRole.STUDENT,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val res = repository.register(name, email, pass, role)
            res.onSuccess { user ->
                if (user.role == UserRole.TEACHER) {
                    _screenStack.value = listOf(AppScreen.TeacherDashboard)
                } else {
                    _screenStack.value = listOf(AppScreen.ClassSelection)
                }
                onSuccess()
            }.onFailure {
                onError(it.message ?: "Registration failed")
            }
        }
    }

    fun updateStudentSelection(
        level: EducationLevel,
        targetClass: String,
        subject: String,
        courseId: String?,
        lang: LanguagePreference
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.updateStudentPreferences(user.id, level, targetClass, subject, courseId, lang)
            _screenStack.value = listOf(AppScreen.StudentMain)
        }
    }

    fun enrollInCourse(courseId: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.enrollInCourse(user.id, courseId)
            showToast("Successfully enrolled in course!")
        }
    }

    fun isUserEnrolled(courseId: String): Boolean {
        return repository.isEnrolled(courseId)
    }

    // --- Teacher Content Management ---
    fun saveCourse(course: CourseEntity) {
        viewModelScope.launch {
            repository.insertCourse(course)
            showToast("Course saved successfully")
        }
    }

    fun deleteCourse(course: CourseEntity) {
        viewModelScope.launch {
            repository.deleteCourse(course)
            showToast("Course removed")
        }
    }

    fun saveLesson(lesson: LessonEntity) {
        viewModelScope.launch {
            repository.insertLesson(lesson)
            showToast("Lesson updated")
        }
    }

    fun deleteLesson(lesson: LessonEntity) {
        viewModelScope.launch {
            repository.deleteLesson(lesson)
            showToast("Lesson removed")
        }
    }

    fun saveLiveClass(liveClass: LiveClassEntity) {
        viewModelScope.launch {
            repository.insertLiveClass(liveClass)
            showToast("Live class updated")
        }
    }

    fun deleteLiveClass(liveClass: LiveClassEntity) {
        viewModelScope.launch {
            repository.deleteLiveClass(liveClass)
            showToast("Live class deleted")
        }
    }

    fun saveMaterial(mat: StudyMaterialEntity) {
        viewModelScope.launch {
            repository.insertStudyMaterial(mat)
            showToast("Material saved")
        }
    }

    fun deleteMaterial(mat: StudyMaterialEntity) {
        viewModelScope.launch {
            repository.deleteStudyMaterial(mat)
            showToast("Material deleted")
        }
    }

    fun saveQuestion(q: McqQuestionEntity) {
        viewModelScope.launch {
            repository.insertQuestion(q)
            showToast("Question saved in bank")
        }
    }

    fun deleteQuestion(q: McqQuestionEntity) {
        viewModelScope.launch {
            repository.deleteQuestion(q)
            showToast("Question deleted")
        }
    }

    fun saveMockTest(test: MockTestEntity) {
        viewModelScope.launch {
            repository.insertMockTest(test)
            showToast("Mock test configured")
        }
    }

    fun deleteMockTest(test: MockTestEntity) {
        viewModelScope.launch {
            repository.deleteMockTest(test)
            showToast("Mock test deleted")
        }
    }

    fun saveAnnouncement(ann: AnnouncementEntity) {
        viewModelScope.launch {
            repository.insertAnnouncement(ann)
            showToast("Announcement published")
        }
    }

    fun deleteAnnouncement(ann: AnnouncementEntity) {
        viewModelScope.launch {
            repository.deleteAnnouncement(ann)
            showToast("Announcement removed")
        }
    }

    fun saveSecurityConfig(cfg: AppSecurityConfigEntity) {
        viewModelScope.launch {
            repository.saveSecurityConfig(cfg)
            showToast("Security settings updated")
        }
    }

    // --- Mock Test Execution ---
    fun startMockTest(test: MockTestEntity) {
        testTimerJob?.cancel()
        mockTestAnswers.value = emptyMap()
        mockTestReviewMarked.value = emptySet()
        mockTestCurrentIndex.value = 0
        mockTestTimerSeconds.value = test.durationMinutes * 60

        testTimerJob = viewModelScope.launch {
            while (mockTestTimerSeconds.value > 0) {
                delay(1000L)
                mockTestTimerSeconds.value -= 1
            }
            // Auto submit when time runs out
            submitMockTest(test)
        }
        navigateTo(AppScreen.StudentMockTestRunner(test.id))
    }

    fun selectAnswer(questionId: String, optionIndex: Int) {
        val current = mockTestAnswers.value.toMutableMap()
        current[questionId] = optionIndex
        mockTestAnswers.value = current
    }

    fun toggleMarkForReview(questionId: String) {
        val current = mockTestReviewMarked.value.toMutableSet()
        if (current.contains(questionId)) current.remove(questionId) else current.add(questionId)
        mockTestReviewMarked.value = current
    }

    fun submitMockTest(test: MockTestEntity, onSubmitted: ((String) -> Unit)? = null) {
        testTimerJob?.cancel()
        val user = currentUser.value ?: return

        viewModelScope.launch {
            val qIds = JsonUtils.jsonToStringList(test.questionIdsJson)
            val questions = repository.getQuestionsByIds(qIds)
            val answers = mockTestAnswers.value

            var correct = 0
            var incorrect = 0
            var score = 0f

            questions.forEach { q ->
                val selected = answers[q.id]
                if (selected != null) {
                    if (selected == q.correctOptionIndex) {
                        correct += 1
                        score += q.marks
                    } else {
                        incorrect += 1
                        score -= q.negativeMarks
                    }
                }
            }

            val attempt = TestAttemptEntity(
                id = "att_${UUID.randomUUID().toString().take(8)}",
                userId = user.id,
                mockTestId = test.id,
                testTitle = test.title,
                totalQuestions = questions.size,
                attemptedCount = answers.size,
                correctCount = correct,
                incorrectCount = incorrect,
                score = score.coerceAtLeast(0f),
                totalMarks = test.totalMarks,
                timeTakenSeconds = (test.durationMinutes * 60) - mockTestTimerSeconds.value,
                answersMapJson = JsonUtils.intMapToJson(answers)
            )

            repository.insertTestAttempt(attempt)
            showToast("Test submitted successfully!")
            if (onSubmitted != null) {
                onSubmitted(attempt.id)
            } else {
                navigateTo(AppScreen.StudentTestResult(attempt.id))
            }
        }
    }

    // --- AI Explanations ---
    fun requestAiExplanation(
        question: McqQuestionEntity,
        selectedOption: Int?,
        targetLanguage: String
    ) {
        aiExplanationLoading.value = true
        aiExplanationText.value = null
        aiExplanationError.value = null

        viewModelScope.launch {
            val options = JsonUtils.jsonToStringList(question.optionsEnJson)
            val res = repository.explainMcq(
                question = question.questionEn,
                options = options,
                correctOptionIndex = question.correctOptionIndex,
                userSelectedOptionIndex = selectedOption,
                chemicalFormula = question.chemicalFormula,
                subject = question.subject,
                targetLanguage = targetLanguage
            )

            aiExplanationLoading.value = false
            res.onSuccess {
                aiExplanationText.value = it
            }.onFailure {
                aiExplanationError.value = it.message ?: "Unable to fetch AI explanation"
            }
        }
    }

    fun isCurrentScreenProtected(): Boolean {
        val cfg = securityConfig.value ?: return false
        if (!cfg.antiScreenshotEnabled) return false

        return when (val screen = currentScreen.value) {
            is AppScreen.StudentLessonViewer -> cfg.protectPaidContent
            is AppScreen.StudentPdfViewer -> cfg.protectPdfs
            is AppScreen.StudentMockTestRunner -> cfg.protectMockTests
            is AppScreen.StudentTestResult -> cfg.protectAnswerKeys
            is AppScreen.StudentPracticeSession -> cfg.protectAnswerKeys
            else -> false
        }
    }

    fun getCourseModules(courseId: String): Flow<List<CourseModuleEntity>> =
        repository.getModulesForCourseFlow(courseId)

    fun getCourseLessons(courseId: String): Flow<List<LessonEntity>> =
        repository.getLessonsForCourseFlow(courseId)
}

class KarmaViewModelFactory(private val repository: KarmaRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(KarmaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return KarmaViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
