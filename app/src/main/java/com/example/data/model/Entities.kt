package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole {
    TEACHER,
    STUDENT
}

enum class EducationLevel {
    SCHOOL,
    COLLEGE,
    COMPETITIVE_EXAM
}

enum class LanguagePreference {
    BILINGUAL,
    HINDI,
    ENGLISH
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val passwordHash: String,
    val role: UserRole,
    val educationLevel: EducationLevel = EducationLevel.COMPETITIVE_EXAM,
    val targetClass: String = "CSIR-NET",
    val selectedSubject: String = "Chemical Sciences",
    val selectedCourseId: String? = null,
    val languagePreference: LanguagePreference = LanguagePreference.BILINGUAL,
    val enrolledCourseIdsJson: String = "[]",
    val bookmarkedMaterialIdsJson: String = "[]",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey val id: String,
    val title: String,
    val hindiTitle: String = "",
    val description: String,
    val hindiDescription: String = "",
    val instructorName: String = "Prof. Chandan Khandey",
    val educationLevel: EducationLevel,
    val targetClass: String,
    val subject: String,
    val language: String = "Bilingual (Hindi + English)",
    val durationText: String = "6 Months",
    val syllabusText: String = "",
    val learningObjectives: String = "",
    val thumbnailUrl: String = "",
    val bannerUrl: String = "",
    val isPaid: Boolean = false,
    val priceINR: Double = 0.0,
    val discountPercent: Int = 0,
    val couponCode: String = "",
    val isPublished: Boolean = true,
    val isArchived: Boolean = false,
    val orderIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "course_modules")
data class CourseModuleEntity(
    @PrimaryKey val id: String,
    val courseId: String,
    val title: String,
    val hindiTitle: String = "",
    val orderIndex: Int = 0
)

@Entity(tableName = "lessons")
data class LessonEntity(
    @PrimaryKey val id: String,
    val courseId: String,
    val moduleId: String,
    val title: String,
    val hindiTitle: String = "",
    val type: String = "VIDEO", // VIDEO, PDF, NOTE, PRACTICE
    val contentUrl: String = "", // YouTube URL, PDF link, or text body
    val durationMinutes: Int = 45,
    val isPaidOnly: Boolean = false,
    val orderIndex: Int = 0,
    val isPublished: Boolean = true
)

@Entity(tableName = "live_classes")
data class LiveClassEntity(
    @PrimaryKey val id: String,
    val title: String,
    val hindiTitle: String = "",
    val courseId: String? = null,
    val targetClass: String,
    val subject: String,
    val teacherName: String = "Prof. Chandan Khandey",
    val scheduledDate: String, // YYYY-MM-DD
    val startTime: String,     // HH:mm (24hr)
    val endTime: String,       // HH:mm (24hr)
    val meetingUrl: String,    // Real Google Meet / Zoom link
    val recordingUrl: String? = null,
    val status: String = "UPCOMING", // UPCOMING, LIVE, COMPLETED, CANCELLED
    val description: String = "",
    val isPaidOnly: Boolean = false
)

@Entity(tableName = "study_materials")
data class StudyMaterialEntity(
    @PrimaryKey val id: String,
    val title: String,
    val hindiTitle: String = "",
    val targetClass: String,
    val subject: String,
    val topic: String,
    val fileType: String = "PDF", // PDF, IMAGE, FORMULA_SHEET
    val fileUrl: String = "",
    val fileSizeBytes: Long = 1024 * 1024 * 2,
    val pageCount: Int = 12,
    val isPaidOnly: Boolean = false,
    val isPublished: Boolean = true,
    val downloadAllowed: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "mcq_questions")
data class McqQuestionEntity(
    @PrimaryKey val id: String,
    val targetClass: String,
    val subject: String,
    val chapter: String,
    val topic: String,
    val difficulty: String = "MEDIUM", // EASY, MEDIUM, HARD
    val questionEn: String,
    val questionHi: String = "",
    val optionsEnJson: String, // List<String> JSON
    val optionsHiJson: String = "[]",
    val correctOptionIndex: Int,
    val explanationEn: String = "",
    val explanationHi: String = "",
    val chemicalFormula: String? = null,
    val marks: Float = 4.0f,
    val negativeMarks: Float = 1.0f,
    val isApproved: Boolean = true,
    val examCategory: String = "General"
)

@Entity(tableName = "mock_tests")
data class MockTestEntity(
    @PrimaryKey val id: String,
    val title: String,
    val hindiTitle: String = "",
    val targetClass: String,
    val subject: String,
    val durationMinutes: Int = 60,
    val totalMarks: Float = 100f,
    val negativeMarkingPerQuestion: Float = 0.5f,
    val questionIdsJson: String = "[]", // List<String>
    val randomizeQuestions: Boolean = true,
    val immediateReviewAllowed: Boolean = true,
    val isPublished: Boolean = true,
    val isPaidOnly: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "test_attempts")
data class TestAttemptEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val mockTestId: String,
    val testTitle: String,
    val totalQuestions: Int,
    val attemptedCount: Int,
    val correctCount: Int,
    val incorrectCount: Int,
    val score: Float,
    val totalMarks: Float,
    val timeTakenSeconds: Int,
    val submittedAt: Long = System.currentTimeMillis(),
    val answersMapJson: String = "{}" // Map<String, Int> QuestionId -> SelectedOption
)

@Entity(tableName = "announcements")
data class AnnouncementEntity(
    @PrimaryKey val id: String,
    val title: String,
    val hindiTitle: String = "",
    val message: String,
    val targetClass: String = "ALL",
    val courseId: String? = null,
    val teacherName: String = "Karma Learning Admin",
    val priority: String = "NORMAL", // NORMAL, HIGH, URGENT
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_security_config")
data class AppSecurityConfigEntity(
    @PrimaryKey val id: String = "singleton_config",
    val antiScreenshotEnabled: Boolean = true,
    val protectPaidContent: Boolean = true,
    val protectPdfs: Boolean = true,
    val protectMockTests: Boolean = true,
    val protectAnswerKeys: Boolean = true,
    val showWatermarkNotice: Boolean = true,
    val aiExplanationsGlobalEnabled: Boolean = true,
    val lastUpdated: Long = System.currentTimeMillis()
)
