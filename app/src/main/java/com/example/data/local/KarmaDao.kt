package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface KarmaDao {
    // --- Users ---
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserByIdFlow(id: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE role = 'STUDENT' ORDER BY createdAt DESC")
    fun getAllStudentsFlow(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    // --- Courses ---
    @Query("SELECT * FROM courses WHERE isArchived = 0 ORDER BY orderIndex ASC, createdAt DESC")
    fun getAllCoursesFlow(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE isPublished = 1 AND isArchived = 0 AND targetClass = :targetClass ORDER BY orderIndex ASC")
    fun getPublishedCoursesByClassFlow(targetClass: String): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE id = :id LIMIT 1")
    suspend fun getCourseById(id: String): CourseEntity?

    @Query("SELECT * FROM courses WHERE id = :id LIMIT 1")
    fun getCourseByIdFlow(id: String): Flow<CourseEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: CourseEntity)

    @Update
    suspend fun updateCourse(course: CourseEntity)

    @Delete
    suspend fun deleteCourse(course: CourseEntity)

    // --- Modules & Lessons ---
    @Query("SELECT * FROM course_modules WHERE courseId = :courseId ORDER BY orderIndex ASC")
    fun getModulesForCourseFlow(courseId: String): Flow<List<CourseModuleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModule(module: CourseModuleEntity)

    @Delete
    suspend fun deleteModule(module: CourseModuleEntity)

    @Query("SELECT * FROM lessons WHERE courseId = :courseId ORDER BY orderIndex ASC")
    fun getLessonsForCourseFlow(courseId: String): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE id = :id LIMIT 1")
    suspend fun getLessonById(id: String): LessonEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLesson(lesson: LessonEntity)

    @Update
    suspend fun updateLesson(lesson: LessonEntity)

    @Delete
    suspend fun deleteLesson(lesson: LessonEntity)

    // --- Live Classes ---
    @Query("SELECT * FROM live_classes ORDER BY scheduledDate ASC, startTime ASC")
    fun getAllLiveClassesFlow(): Flow<List<LiveClassEntity>>

    @Query("SELECT * FROM live_classes WHERE targetClass = :targetClass OR targetClass = 'ALL' ORDER BY scheduledDate ASC, startTime ASC")
    fun getLiveClassesForClassFlow(targetClass: String): Flow<List<LiveClassEntity>>

    @Query("SELECT * FROM live_classes WHERE id = :id LIMIT 1")
    suspend fun getLiveClassById(id: String): LiveClassEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLiveClass(liveClass: LiveClassEntity)

    @Update
    suspend fun updateLiveClass(liveClass: LiveClassEntity)

    @Delete
    suspend fun deleteLiveClass(liveClass: LiveClassEntity)

    // --- Study Materials ---
    @Query("SELECT * FROM study_materials ORDER BY createdAt DESC")
    fun getAllStudyMaterialsFlow(): Flow<List<StudyMaterialEntity>>

    @Query("SELECT * FROM study_materials WHERE isPublished = 1 AND (targetClass = :targetClass OR targetClass = 'ALL') ORDER BY createdAt DESC")
    fun getPublishedMaterialsForClassFlow(targetClass: String): Flow<List<StudyMaterialEntity>>

    @Query("SELECT * FROM study_materials WHERE id = :id LIMIT 1")
    suspend fun getMaterialById(id: String): StudyMaterialEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyMaterial(material: StudyMaterialEntity)

    @Update
    suspend fun updateStudyMaterial(material: StudyMaterialEntity)

    @Delete
    suspend fun deleteStudyMaterial(material: StudyMaterialEntity)

    // --- MCQ Questions ---
    @Query("SELECT * FROM mcq_questions ORDER BY chapter ASC, topic ASC")
    fun getAllQuestionsFlow(): Flow<List<McqQuestionEntity>>

    @Query("SELECT * FROM mcq_questions WHERE isApproved = 1 AND (targetClass = :targetClass OR targetClass = 'ALL')")
    fun getApprovedQuestionsForClassFlow(targetClass: String): Flow<List<McqQuestionEntity>>

    @Query("SELECT * FROM mcq_questions WHERE id IN (:ids)")
    suspend fun getQuestionsByIds(ids: List<String>): List<McqQuestionEntity>

    @Query("SELECT * FROM mcq_questions WHERE id = :id LIMIT 1")
    suspend fun getQuestionById(id: String): McqQuestionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: McqQuestionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<McqQuestionEntity>)

    @Update
    suspend fun updateQuestion(question: McqQuestionEntity)

    @Delete
    suspend fun deleteQuestion(question: McqQuestionEntity)

    // --- Mock Tests ---
    @Query("SELECT * FROM mock_tests ORDER BY createdAt DESC")
    fun getAllMockTestsFlow(): Flow<List<MockTestEntity>>

    @Query("SELECT * FROM mock_tests WHERE isPublished = 1 AND (targetClass = :targetClass OR targetClass = 'ALL') ORDER BY createdAt DESC")
    fun getPublishedMockTestsForClassFlow(targetClass: String): Flow<List<MockTestEntity>>

    @Query("SELECT * FROM mock_tests WHERE id = :id LIMIT 1")
    suspend fun getMockTestById(id: String): MockTestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMockTest(test: MockTestEntity)

    @Update
    suspend fun updateMockTest(test: MockTestEntity)

    @Delete
    suspend fun deleteMockTest(test: MockTestEntity)

    // --- Test Attempts ---
    @Query("SELECT * FROM test_attempts WHERE userId = :userId ORDER BY submittedAt DESC")
    fun getAttemptsForUserFlow(userId: String): Flow<List<TestAttemptEntity>>

    @Query("SELECT * FROM test_attempts ORDER BY submittedAt DESC")
    fun getAllAttemptsFlow(): Flow<List<TestAttemptEntity>>

    @Query("SELECT * FROM test_attempts WHERE id = :id LIMIT 1")
    suspend fun getAttemptById(id: String): TestAttemptEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTestAttempt(attempt: TestAttemptEntity)

    // --- Announcements ---
    @Query("SELECT * FROM announcements ORDER BY createdAt DESC")
    fun getAllAnnouncementsFlow(): Flow<List<AnnouncementEntity>>

    @Query("SELECT * FROM announcements WHERE targetClass = 'ALL' OR targetClass = :targetClass ORDER BY createdAt DESC")
    fun getAnnouncementsForClassFlow(targetClass: String): Flow<List<AnnouncementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: AnnouncementEntity)

    @Delete
    suspend fun deleteAnnouncement(announcement: AnnouncementEntity)

    // --- Security Config ---
    @Query("SELECT * FROM app_security_config WHERE id = 'singleton_config' LIMIT 1")
    fun getSecurityConfigFlow(): Flow<AppSecurityConfigEntity?>

    @Query("SELECT * FROM app_security_config WHERE id = 'singleton_config' LIMIT 1")
    suspend fun getSecurityConfig(): AppSecurityConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSecurityConfig(config: AppSecurityConfigEntity)
}
