package com.example.data.repository

import com.example.data.local.JsonUtils
import com.example.data.local.KarmaDao
import com.example.data.local.KarmaDatabase
import com.example.data.model.*
import com.example.data.remote.GeminiAiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class KarmaRepository(private val dao: KarmaDao) {
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    fun setCurrentUser(user: UserEntity?) {
        _currentUser.value = user
    }

    // --- Auth & Role ---
    suspend fun login(email: String, passwordRaw: String): Result<UserEntity> {
        val trimmedEmail = email.trim()
        val user = dao.getUserByEmail(trimmedEmail)
            ?: return Result.failure(Exception("No account found with this email."))

        val hash = KarmaDatabase.hashPassword(passwordRaw)
        if (user.passwordHash != hash) {
            return Result.failure(Exception("Incorrect password. Please try again."))
        }

        _currentUser.value = user
        return Result.success(user)
    }

    suspend fun register(
        name: String,
        email: String,
        passwordRaw: String,
        role: UserRole = UserRole.STUDENT,
        educationLevel: EducationLevel = EducationLevel.COMPETITIVE_EXAM,
        targetClass: String = "CSIR-NET",
        subject: String = "Chemical Sciences"
    ): Result<UserEntity> {
        val trimmedEmail = email.trim()
        val existing = dao.getUserByEmail(trimmedEmail)
        if (existing != null) {
            return Result.failure(Exception("An account with this email already exists."))
        }

        val newUser = UserEntity(
            id = "user_${UUID.randomUUID().toString().take(8)}",
            name = name.trim(),
            email = trimmedEmail,
            passwordHash = KarmaDatabase.hashPassword(passwordRaw),
            role = role,
            educationLevel = educationLevel,
            targetClass = targetClass,
            selectedSubject = subject,
            languagePreference = LanguagePreference.BILINGUAL
        )
        dao.insertUser(newUser)
        _currentUser.value = newUser
        return Result.success(newUser)
    }

    suspend fun updateStudentPreferences(
        userId: String,
        level: EducationLevel,
        targetClass: String,
        subject: String,
        courseId: String?,
        lang: LanguagePreference
    ) {
        val user = dao.getUserById(userId) ?: return
        val updated = user.copy(
            educationLevel = level,
            targetClass = targetClass,
            selectedSubject = subject,
            selectedCourseId = courseId,
            languagePreference = lang
        )
        dao.updateUser(updated)
        if (_currentUser.value?.id == userId) {
            _currentUser.value = updated
        }
    }

    suspend fun enrollInCourse(userId: String, courseId: String) {
        val user = dao.getUserById(userId) ?: return
        val currentEnrolled = JsonUtils.jsonToStringList(user.enrolledCourseIdsJson).toMutableList()
        if (!currentEnrolled.contains(courseId)) {
            currentEnrolled.add(courseId)
        }
        val updated = user.copy(
            enrolledCourseIdsJson = JsonUtils.stringListToJson(currentEnrolled)
        )
        dao.updateUser(updated)
        if (_currentUser.value?.id == userId) {
            _currentUser.value = updated
        }
    }

    fun isEnrolled(courseId: String): Boolean {
        val user = _currentUser.value ?: return false
        val enrolled = JsonUtils.jsonToStringList(user.enrolledCourseIdsJson)
        return enrolled.contains(courseId)
    }

    // --- Courses ---
    fun getAllCoursesFlow(): Flow<List<CourseEntity>> = dao.getAllCoursesFlow()
    fun getPublishedCoursesByClassFlow(targetClass: String): Flow<List<CourseEntity>> =
        dao.getPublishedCoursesByClassFlow(targetClass)
    fun getCourseByIdFlow(id: String): Flow<CourseEntity?> = dao.getCourseByIdFlow(id)

    suspend fun insertCourse(course: CourseEntity) = dao.insertCourse(course)
    suspend fun updateCourse(course: CourseEntity) = dao.updateCourse(course)
    suspend fun deleteCourse(course: CourseEntity) = dao.deleteCourse(course)

    // --- Modules & Lessons ---
    fun getModulesForCourseFlow(courseId: String): Flow<List<CourseModuleEntity>> =
        dao.getModulesForCourseFlow(courseId)
    suspend fun insertModule(module: CourseModuleEntity) = dao.insertModule(module)
    suspend fun deleteModule(module: CourseModuleEntity) = dao.deleteModule(module)

    fun getLessonsForCourseFlow(courseId: String): Flow<List<LessonEntity>> =
        dao.getLessonsForCourseFlow(courseId)
    suspend fun insertLesson(lesson: LessonEntity) = dao.insertLesson(lesson)
    suspend fun updateLesson(lesson: LessonEntity) = dao.updateLesson(lesson)
    suspend fun deleteLesson(lesson: LessonEntity) = dao.deleteLesson(lesson)

    // --- Live Classes ---
    fun getAllLiveClassesFlow(): Flow<List<LiveClassEntity>> = dao.getAllLiveClassesFlow()
    fun getLiveClassesForClassFlow(targetClass: String): Flow<List<LiveClassEntity>> =
        dao.getLiveClassesForClassFlow(targetClass)
    suspend fun insertLiveClass(liveClass: LiveClassEntity) = dao.insertLiveClass(liveClass)
    suspend fun updateLiveClass(liveClass: LiveClassEntity) = dao.updateLiveClass(liveClass)
    suspend fun deleteLiveClass(liveClass: LiveClassEntity) = dao.deleteLiveClass(liveClass)

    // --- Study Materials ---
    fun getAllStudyMaterialsFlow(): Flow<List<StudyMaterialEntity>> = dao.getAllStudyMaterialsFlow()
    fun getPublishedMaterialsForClassFlow(targetClass: String): Flow<List<StudyMaterialEntity>> =
        dao.getPublishedMaterialsForClassFlow(targetClass)
    suspend fun insertStudyMaterial(material: StudyMaterialEntity) = dao.insertStudyMaterial(material)
    suspend fun updateStudyMaterial(material: StudyMaterialEntity) = dao.updateStudyMaterial(material)
    suspend fun deleteStudyMaterial(material: StudyMaterialEntity) = dao.deleteStudyMaterial(material)

    // --- MCQ Questions ---
    fun getAllQuestionsFlow(): Flow<List<McqQuestionEntity>> = dao.getAllQuestionsFlow()
    fun getApprovedQuestionsForClassFlow(targetClass: String): Flow<List<McqQuestionEntity>> =
        dao.getApprovedQuestionsForClassFlow(targetClass)
    suspend fun getQuestionsByIds(ids: List<String>): List<McqQuestionEntity> = dao.getQuestionsByIds(ids)
    suspend fun insertQuestion(question: McqQuestionEntity) = dao.insertQuestion(question)
    suspend fun updateQuestion(question: McqQuestionEntity) = dao.updateQuestion(question)
    suspend fun deleteQuestion(question: McqQuestionEntity) = dao.deleteQuestion(question)

    // --- Mock Tests ---
    fun getAllMockTestsFlow(): Flow<List<MockTestEntity>> = dao.getAllMockTestsFlow()
    fun getPublishedMockTestsForClassFlow(targetClass: String): Flow<List<MockTestEntity>> =
        dao.getPublishedMockTestsForClassFlow(targetClass)
    suspend fun getMockTestById(id: String): MockTestEntity? = dao.getMockTestById(id)
    suspend fun insertMockTest(test: MockTestEntity) = dao.insertMockTest(test)
    suspend fun updateMockTest(test: MockTestEntity) = dao.updateMockTest(test)
    suspend fun deleteMockTest(test: MockTestEntity) = dao.deleteMockTest(test)

    // --- Test Attempts ---
    fun getAttemptsForUserFlow(userId: String): Flow<List<TestAttemptEntity>> = dao.getAttemptsForUserFlow(userId)
    fun getAllAttemptsFlow(): Flow<List<TestAttemptEntity>> = dao.getAllAttemptsFlow()
    suspend fun insertTestAttempt(attempt: TestAttemptEntity) = dao.insertTestAttempt(attempt)

    // --- Announcements ---
    fun getAllAnnouncementsFlow(): Flow<List<AnnouncementEntity>> = dao.getAllAnnouncementsFlow()
    fun getAnnouncementsForClassFlow(targetClass: String): Flow<List<AnnouncementEntity>> =
        dao.getAnnouncementsForClassFlow(targetClass)
    suspend fun insertAnnouncement(announcement: AnnouncementEntity) = dao.insertAnnouncement(announcement)
    suspend fun deleteAnnouncement(announcement: AnnouncementEntity) = dao.deleteAnnouncement(announcement)

    // --- Students ---
    fun getAllStudentsFlow(): Flow<List<UserEntity>> = dao.getAllStudentsFlow()

    // --- Security Config ---
    fun getSecurityConfigFlow(): Flow<AppSecurityConfigEntity?> = dao.getSecurityConfigFlow()
    suspend fun getSecurityConfig(): AppSecurityConfigEntity =
        dao.getSecurityConfig() ?: AppSecurityConfigEntity()
    suspend fun saveSecurityConfig(config: AppSecurityConfigEntity) = dao.saveSecurityConfig(config)

    // --- Gemini AI Bridge ---
    suspend fun explainMcq(
        question: String,
        options: List<String>,
        correctOptionIndex: Int,
        userSelectedOptionIndex: Int?,
        chemicalFormula: String?,
        subject: String,
        targetLanguage: String
    ): Result<String> {
        return GeminiAiService.explainMcq(
            question,
            options,
            correctOptionIndex,
            userSelectedOptionIndex,
            chemicalFormula,
            subject,
            targetLanguage
        )
    }

    suspend fun generatePracticeQuestions(
        topic: String,
        targetClass: String,
        difficulty: String,
        language: String
    ): Result<String> {
        return GeminiAiService.generatePracticeQuestions(topic, targetClass, difficulty, language)
    }
}
