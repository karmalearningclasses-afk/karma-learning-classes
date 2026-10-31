package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID

@Database(
    entities = [
        UserEntity::class,
        CourseEntity::class,
        CourseModuleEntity::class,
        LessonEntity::class,
        LiveClassEntity::class,
        StudyMaterialEntity::class,
        McqQuestionEntity::class,
        MockTestEntity::class,
        TestAttemptEntity::class,
        AnnouncementEntity::class,
        AppSecurityConfigEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class KarmaDatabase : RoomDatabase() {
    abstract fun karmaDao(): KarmaDao

    companion object {
        @Volatile
        private var INSTANCE: KarmaDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): KarmaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KarmaDatabase::class.java,
                    "karma_learning_classes.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun hashPassword(password: String): String {
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest("karma_salt_$password".toByteArray())
            return digest.fold("") { str, it -> str + "%02x".format(it) }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.karmaDao())
                    }
                }
            }
        }

        suspend fun populateInitialData(dao: KarmaDao) {
            // Default Security Settings
            val securityConfig = AppSecurityConfigEntity(
                id = "singleton_config",
                antiScreenshotEnabled = true,
                protectPaidContent = true,
                protectPdfs = true,
                protectMockTests = true,
                protectAnswerKeys = true,
                showWatermarkNotice = true,
                aiExplanationsGlobalEnabled = true
            )
            dao.saveSecurityConfig(securityConfig)

            // 1. Initial Admin Teacher Account
            val adminUser = UserEntity(
                id = "user_admin_teacher_01",
                name = "Prof. Chandan Khandey (Admin)",
                email = "admin@karmaclasses.com",
                passwordHash = hashPassword("KarmaAdmin@2026"),
                role = UserRole.TEACHER,
                educationLevel = EducationLevel.COMPETITIVE_EXAM,
                targetClass = "CSIR-NET",
                selectedSubject = "Chemical Sciences",
                languagePreference = LanguagePreference.BILINGUAL
            )
            dao.insertUser(adminUser)

            // 2. Initial Sample Student Account
            val studentUser = UserEntity(
                id = "user_student_sample_01",
                name = "Aarav Sharma",
                email = "student@karmaclasses.com",
                passwordHash = hashPassword("Student@2026"),
                role = UserRole.STUDENT,
                educationLevel = EducationLevel.COMPETITIVE_EXAM,
                targetClass = "CSIR-NET",
                selectedSubject = "Chemical Sciences",
                selectedCourseId = "course_csir_chem_01",
                languagePreference = LanguagePreference.BILINGUAL,
                enrolledCourseIdsJson = "[\"course_csir_chem_01\", \"course_class12_chem_01\"]"
            )
            dao.insertUser(studentUser)

            // 3. Courses
            val courseCsir = CourseEntity(
                id = "course_csir_chem_01",
                title = "CSIR-NET & CGSET Chemistry Master Course",
                hindiTitle = "सीएसआईआर-नेट एवं सीजीसेट रसायन विज्ञान मास्टर कोर्स",
                description = "Complete mastery of Organic Reaction Mechanisms, Coordination Chemistry, Quantum Chemistry & Spectroscopy with PYQs & practice sets.",
                hindiDescription = "कार्बनिक अभिक्रिया क्रियाविधि, उपसहसंयोजन रसायन, क्वांटम रसायन एवं स्पेक्ट्रोस्कोपी का संपूर्ण अध्ययन पिछले वर्षों के प्रश्नों सहित।",
                instructorName = "Prof. Chandan Khandey",
                educationLevel = EducationLevel.COMPETITIVE_EXAM,
                targetClass = "CSIR-NET",
                subject = "Chemical Sciences",
                language = "Bilingual (Hindi + English)",
                durationText = "6 Months (350+ Hours)",
                syllabusText = "Part A: General Aptitude\nPart B: Core Chemistry (Inorganic, Organic, Physical)\nPart C: Advanced Specialization & Multi-step Mechanisms",
                learningObjectives = "1. Master chemical reaction mechanisms\n2. Solve 4-mark Part C questions under 3 minutes\n3. In-depth spectroscopic spectral analysis",
                thumbnailUrl = "",
                bannerUrl = "",
                isPaid = true,
                priceINR = 2499.0,
                discountPercent = 20,
                couponCode = "KARMA20",
                isPublished = true,
                orderIndex = 1
            )
            dao.insertCourse(courseCsir)

            val course12 = CourseEntity(
                id = "course_class12_chem_01",
                title = "Class 12 Board & NEET Chemistry Express",
                hindiTitle = "कक्षा 12 बोर्ड एवं नीट रसायन विज्ञान एक्सप्रेस",
                description = "Focused syllabus coverage for Class 12 Boards and competitive exams: Solutions, Electrochemistry, Chemical Kinetics, Coordination Compounds, and Organic Reactions.",
                hindiDescription = "कक्षा 12 बोर्ड परीक्षा और प्रवेश परीक्षाओं के लिए विशेष पाठ्यक्रम: विलयन, विद्युत रसायन, रासायनिक बलगतिकी, उपसहसंयोजन यौगिक एवं कार्बनिक रसायन।",
                instructorName = "Prof. Chandan Khandey",
                educationLevel = EducationLevel.SCHOOL,
                targetClass = "Class 12",
                subject = "Chemistry",
                language = "Bilingual (Hindi + English)",
                durationText = "Full Academic Year",
                syllabusText = "1. Solutions\n2. Electrochemistry\n3. Chemical Kinetics\n4. d and f Block Elements\n5. Coordination Compounds\n6. Haloalkanes & Haloarenes\n7. Biomolecules",
                learningObjectives = "1. 95+ score target in Board exams\n2. Complete numerical problem solving\n3. Step-by-step NCERT derivations",
                thumbnailUrl = "",
                bannerUrl = "",
                isPaid = false,
                priceINR = 0.0,
                isPublished = true,
                orderIndex = 2
            )
            dao.insertCourse(course12)

            val courseBsc = CourseEntity(
                id = "course_bsc_chem_01",
                title = "B.Sc. Chemistry Honours Foundation",
                hindiTitle = "बी.एससी. रसायन विज्ञान ऑनर्स फाउंडेशन",
                description = "Comprehensive physical, inorganic, and organic chemistry modules for university degrees and state level sets.",
                hindiDescription = "विश्वविद्यालय स्तर की परीक्षाओं के लिए भौतिक, अकार्बनिक और कार्बनिक रसायन विज्ञान के संपूर्ण मॉड्यूल।",
                instructorName = "Prof. Chandan Khandey",
                educationLevel = EducationLevel.COLLEGE,
                targetClass = "B.Sc.",
                subject = "Chemistry",
                language = "Bilingual (Hindi + English)",
                durationText = "4 Months",
                syllabusText = "Thermodynamics, Phase Equilibrium, Organometallics, Stereochemistry, Carbohydrates",
                learningObjectives = "University exam excellence and foundation for M.Sc. entrance",
                thumbnailUrl = "",
                bannerUrl = "",
                isPaid = true,
                priceINR = 1499.0,
                discountPercent = 15,
                couponCode = "COLLEGE15",
                isPublished = true,
                orderIndex = 3
            )
            dao.insertCourse(courseBsc)

            // 4. Course Modules & Lessons for CSIR-NET
            val mod1 = CourseModuleEntity(
                id = "mod_csir_01",
                courseId = "course_csir_chem_01",
                title = "Module 1: Advanced Organic Reaction Mechanisms",
                hindiTitle = "मॉड्यूल 1: उन्नत कार्बनिक अभिक्रिया क्रियाविधि",
                orderIndex = 1
            )
            val mod2 = CourseModuleEntity(
                id = "mod_csir_02",
                courseId = "course_csir_chem_01",
                title = "Module 2: Coordination Chemistry & Crystal Field Theory",
                hindiTitle = "मॉड्यूल 2: उपसहसंयोजन रसायन एवं क्रिस्टल क्षेत्र सिद्धांत",
                orderIndex = 2
            )
            dao.insertModule(mod1)
            dao.insertModule(mod2)

            dao.insertLesson(
                LessonEntity(
                    id = "les_csir_01",
                    courseId = "course_csir_chem_01",
                    moduleId = "mod_csir_01",
                    title = "Stereoselective Pericyclic Reactions - Woodward-Hoffmann Rules",
                    hindiTitle = "त्रिविमचयनात्मक पेरीसाइक्लिक अभिक्रियाएँ - वुडवर्ड-हॉफमैन नियम",
                    type = "VIDEO",
                    contentUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
                    durationMinutes = 55,
                    isPaidOnly = true,
                    orderIndex = 1
                )
            )
            dao.insertLesson(
                LessonEntity(
                    id = "les_csir_02",
                    courseId = "course_csir_chem_01",
                    moduleId = "mod_csir_01",
                    title = "Detailed Notes: Reactive Intermediates (Carbocations, Radicals, Carbenes)",
                    hindiTitle = "विस्तृत नोट्स: क्रियाशील मध्यवर्ती (कार्बोधनायन, मुक्त मूलक, कार्बीन)",
                    type = "PDF",
                    contentUrl = "https://example.com/notes/organic_intermediates.pdf",
                    durationMinutes = 30,
                    isPaidOnly = true,
                    orderIndex = 2
                )
            )
            dao.insertLesson(
                LessonEntity(
                    id = "les_csir_03",
                    courseId = "course_csir_chem_01",
                    moduleId = "mod_csir_02",
                    title = "Crystal Field Splitting in Octahedral & Tetrahedral Complexes",
                    hindiTitle = "अष्टफलकीय एवं चतुष्फलकीय संकुलों में क्रिस्टल क्षेत्र विपाटन",
                    type = "VIDEO",
                    contentUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
                    durationMinutes = 48,
                    isPaidOnly = false,
                    orderIndex = 1
                )
            )

            // 5. Live Classes
            dao.insertLiveClass(
                LiveClassEntity(
                    id = "live_01",
                    title = "Live Masterclass: CSIR-NET Part C Reaction Mechanisms & Spectral Identification",
                    hindiTitle = "लाइव मास्टरक्लास: सीएसआईआर-नेट पार्ट C अभिक्रिया क्रियाविधि एवं स्पेक्ट्रल पहचान",
                    courseId = "course_csir_chem_01",
                    targetClass = "CSIR-NET",
                    subject = "Chemical Sciences",
                    teacherName = "Prof. Chandan Khandey",
                    scheduledDate = "2026-10-27",
                    startTime = "18:00",
                    endTime = "19:30",
                    meetingUrl = "https://meet.google.com/zxy-karm-cls",
                    recordingUrl = null,
                    status = "UPCOMING",
                    description = "Join this high-yield interactive live session focusing on multi-step stereochemical synthesis and 13C-NMR correlation."
                )
            )

            dao.insertLiveClass(
                LiveClassEntity(
                    id = "live_02",
                    title = "Class 12 Electrochemistry: Nernst Equation & Cell Potential Numerical Drill",
                    hindiTitle = "कक्षा 12 विद्युत रसायन: नर्नस्ट समीकरण एवं सेल विभव आंकिक प्रश्न",
                    courseId = "course_class12_chem_01",
                    targetClass = "Class 12",
                    subject = "Chemistry",
                    teacherName = "Prof. Chandan Khandey",
                    scheduledDate = "2026-10-28",
                    startTime = "16:00",
                    endTime = "17:15",
                    meetingUrl = "https://meet.google.com/edu-chem-live",
                    recordingUrl = null,
                    status = "UPCOMING",
                    description = "Live practice of standard reduction potentials, Gibbs free energy calculation, and equilibrium constant derivations."
                )
            )

            // 6. Study Materials (PDFs / Notes)
            dao.insertStudyMaterial(
                StudyMaterialEntity(
                    id = "pdf_01",
                    title = "Comprehensive Chemical Sciences Formula Handbook & Reaction Tree",
                    hindiTitle = "रसायन विज्ञान सूत्र पुस्तिका एवं अभिक्रिया आरेख",
                    targetClass = "CSIR-NET",
                    subject = "Chemical Sciences",
                    topic = "Organic & Inorganic Chemistry",
                    fileType = "PDF",
                    fileUrl = "https://example.com/docs/chemistry_formula_handbook.pdf",
                    fileSizeBytes = 3450000,
                    pageCount = 38,
                    isPaidOnly = true,
                    isPublished = true
                )
            )
            dao.insertStudyMaterial(
                StudyMaterialEntity(
                    id = "pdf_02",
                    title = "Coordination Compounds Color, Magnetism & CFSE Cheat Sheet",
                    hindiTitle = "उपसहसंयोजन यौगिक रंग, चुंबकत्व एवं CFSE सारणी",
                    targetClass = "Class 12",
                    subject = "Chemistry",
                    topic = "Coordination Chemistry",
                    fileType = "FORMULA_SHEET",
                    fileUrl = "https://example.com/docs/coordination_compounds.pdf",
                    fileSizeBytes = 1250000,
                    pageCount = 8,
                    isPaidOnly = false,
                    isPublished = true
                )
            )
            dao.insertStudyMaterial(
                StudyMaterialEntity(
                    id = "pdf_03",
                    title = "Electrochemical Series & Thermodynamics Master Chart",
                    hindiTitle = "विद्युत रासायनिक श्रेणी एवं ऊष्मागतिकी मास्टर चार्ट",
                    targetClass = "Class 12",
                    subject = "Chemistry",
                    topic = "Electrochemistry",
                    fileType = "PDF",
                    fileUrl = "https://example.com/docs/electrochemistry_chart.pdf",
                    fileSizeBytes = 1850000,
                    pageCount = 14,
                    isPaidOnly = false,
                    isPublished = true
                )
            )

            // 7. MCQ Question Bank
            val q1 = McqQuestionEntity(
                id = "mcq_chem_01",
                targetClass = "CSIR-NET",
                subject = "Chemical Sciences",
                chapter = "Coordination Chemistry",
                topic = "Crystal Field Theory",
                difficulty = "HARD",
                questionEn = "For an octahedral high-spin [Fe(H2O)6]3+ complex (d5), what is the Crystal Field Stabilization Energy (CFSE)?",
                questionHi = "एक अष्टफलकीय उच्च-चक्रण [Fe(H2O)6]3+ संकुल (d5) के लिए क्रिस्टल क्षेत्र स्थायित्व ऊर्जा (CFSE) क्या होगी?",
                optionsEnJson = "[\"0 Δo\", \"-0.4 Δo + P\", \"-2.0 Δo + 2P\", \"-0.6 Δo\"]",
                optionsHiJson = "[\"0 Δo\", \"-0.4 Δo + P\", \"-2.0 Δo + 2P\", \"-0.6 Δo\"]",
                correctOptionIndex = 0,
                explanationEn = "Fe3+ is a d5 system. Since H2O is a weak-field ligand, it forms a high-spin complex with configuration t2g^3 eg^2. CFSE = (3 × -0.4 Δo) + (2 × +0.6 Δo) = -1.2 Δo + 1.2 Δo = 0 Δo.",
                explanationHi = "Fe3+ एक d5 तंत्र है। जल (H2O) एक दुर्बल क्षेत्र लिगैंड होने के कारण उच्च-चक्रण विन्यास t2g^3 eg^2 बनाता है। CFSE = (3 × -0.4 Δo) + (2 × +0.6 Δo) = 0 Δo प्राप्त होता है।",
                chemicalFormula = "[Fe(H2O)6]3+ -> t2g^3 eg^2 (S=5/2)",
                marks = 4.0f,
                negativeMarks = 1.0f,
                isApproved = true,
                examCategory = "CSIR-NET"
            )

            val q2 = McqQuestionEntity(
                id = "mcq_chem_02",
                targetClass = "CSIR-NET",
                subject = "Chemical Sciences",
                chapter = "Pericyclic Reactions",
                topic = "Electrocyclic Ring Closure",
                difficulty = "HARD",
                questionEn = "Under thermal conditions (Δ), the electrocyclic ring closure of (2E,4Z,6E)-octa-2,4,6-triene (6π electrons) proceeds via which stereochemical mode?",
                questionHi = "तापीय परिस्थितियों (Δ) में (2E,4Z,6E)-ऑक्टा-2,4,6-ट्राईन (6π इलेक्ट्रॉन) का इलेक्ट्रोसाइक्लिक रिंग क्लोजर किस त्रिविम रसायन मार्ग से होता है?",
                optionsEnJson = "[\"Disrotatory giving cis-dimethylcyclohexadiene\", \"Conrotatory giving trans-dimethylcyclohexadiene\", \"Disrotatory giving trans-dimethylcyclohexadiene\", \"Conrotatory giving cis-dimethylcyclohexadiene\"]",
                optionsHiJson = "[\"डिसरोटेटरी जिससे cis-डाईमेथिलसाइक्लोहेक्साडाईन बनता है\", \"कॉनरोटेटरी जिससे trans-डाईमेथिलसाइक्लोहेक्साडाईन बनता है\", \"डिसरोटेटरी जिससे trans-डाईमेथिलसाइक्लोहेक्साडाईन बनता है\", \"कॉनरोटेटरी जिससे cis-डाईमेथिलसाइक्लोहेक्साडाईन बनता है\"]",
                correctOptionIndex = 0,
                explanationEn = "By Woodward-Hoffmann rules, thermal reactions of 4n+2 π electrons (6π) are Disrotatory in the ground state (HOMO = Ψ3). The (E,Z,E) geometry under disrotation yields the cis-isomer.",
                explanationHi = "वुडवर्ड-हॉफमैन नियमानुसार, 4n+2 π इलेक्ट्रॉनों (6π) की तापीय अभिक्रियाएं मूल अवस्था में डिसरोटेटरी (Disrotatory) होती हैं। इससे cis-उत्पाद प्राप्त होता है।",
                chemicalFormula = "6π e- (thermal) -> Disrotatory -> cis",
                marks = 4.0f,
                negativeMarks = 1.0f,
                isApproved = true,
                examCategory = "CSIR-NET"
            )

            val q3 = McqQuestionEntity(
                id = "mcq_chem_03",
                targetClass = "Class 12",
                subject = "Chemistry",
                chapter = "Electrochemistry",
                topic = "Nernst Equation",
                difficulty = "MEDIUM",
                questionEn = "What is the cell potential (E_cell) of a Daniell cell: Zn(s) | Zn2+(0.01M) || Cu2+(0.1M) | Cu(s) given E°_cell = 1.10 V at 298 K? (2.303 RT/F = 0.059 V)",
                questionHi = "डेनियल सेल: Zn(s) | Zn2+(0.01M) || Cu2+(0.1M) | Cu(s) के लिए 298 K पर सेल विभव (E_cell) क्या होगा यदि E°_cell = 1.10 V है? (2.303 RT/F = 0.059 V)",
                optionsEnJson = "[\"1.1295 V\", \"1.0705 V\", \"1.1000 V\", \"1.1590 V\"]",
                optionsHiJson = "[\"1.1295 V\", \"1.0705 V\", \"1.1000 V\", \"1.1590 V\"]",
                correctOptionIndex = 0,
                explanationEn = "Nernst equation: E_cell = E°_cell - (0.059/n) log([Zn2+]/[Cu2+]). Here n = 2, [Zn2+]/[Cu2+] = 0.01/0.1 = 10^-1. Thus E_cell = 1.10 - (0.059/2)(-1) = 1.10 + 0.0295 = 1.1295 V.",
                explanationHi = "नर्नस्ट समीकरण: E_cell = E°_cell - (0.059/n) log([Zn2+]/[Cu2+]). यहाँ n = 2 तथा [Zn2+]/[Cu2+] = 0.01/0.1 = 10^-1. इसलिए E_cell = 1.10 + 0.0295 = 1.1295 V होगा।",
                chemicalFormula = "Zn + Cu2+ -> Zn2+ + Cu (n=2)",
                marks = 4.0f,
                negativeMarks = 1.0f,
                isApproved = true,
                examCategory = "Board & NEET"
            )

            val q4 = McqQuestionEntity(
                id = "mcq_chem_04",
                targetClass = "Class 12",
                subject = "Chemistry",
                chapter = "Chemical Kinetics",
                topic = "Rate Law & Order",
                difficulty = "MEDIUM",
                questionEn = "For a first order reaction, the half-life period (t_1/2) is 69.3 minutes. What is the rate constant (k) of this reaction?",
                questionHi = "एक प्रथम कोटि की अभिक्रिया के लिए अर्द्ध-आयु काल (t_1/2) 69.3 मिनट है। इस अभिक्रिया का वेग स्थिरांक (k) क्या होगा?",
                optionsEnJson = "[\"0.01 min⁻¹\", \"0.1 min⁻¹\", \"0.001 min⁻¹\", \"1.0 min⁻¹\"]",
                optionsHiJson = "[\"0.01 min⁻¹\", \"0.1 min⁻¹\", \"0.001 min⁻¹\", \"1.0 min⁻¹\"]",
                correctOptionIndex = 0,
                explanationEn = "For 1st order reaction: k = 0.693 / t_1/2 = 0.693 / 69.3 = 0.01 min⁻¹.",
                explanationHi = "प्रथम कोटि की अभिक्रिया हेतु: k = 0.693 / t_1/2 = 0.693 / 69.3 = 0.01 min⁻¹.",
                chemicalFormula = "k = ln(2) / t_1/2 = 0.693 / t_1/2",
                marks = 4.0f,
                negativeMarks = 1.0f,
                isApproved = true,
                examCategory = "Board & NEET"
            )

            dao.insertQuestions(listOf(q1, q2, q3, q4))

            // 8. Mock Tests
            val mockCsir = MockTestEntity(
                id = "mock_csir_01",
                title = "CSIR-NET Chemical Sciences Part B & C All India Test 1",
                hindiTitle = "सीएसआईआर-नेट रसायन विज्ञान भाग B एवं C अखिल भारतीय टेस्ट 1",
                targetClass = "CSIR-NET",
                subject = "Chemical Sciences",
                durationMinutes = 60,
                totalMarks = 50f,
                negativeMarkingPerQuestion = 1.0f,
                questionIdsJson = "[\"mcq_chem_01\", \"mcq_chem_02\"]",
                randomizeQuestions = true,
                immediateReviewAllowed = true,
                isPublished = true,
                isPaidOnly = true
            )
            dao.insertMockTest(mockCsir)

            val mock12 = MockTestEntity(
                id = "mock_class12_01",
                title = "Class 12 Chemistry: Electrochemistry & Kinetics Rapid Test",
                hindiTitle = "कक्षा 12 रसायन: विद्युत रसायन एवं बलगतिकी त्वरित टेस्ट",
                targetClass = "Class 12",
                subject = "Chemistry",
                durationMinutes = 30,
                totalMarks = 20f,
                negativeMarkingPerQuestion = 0.5f,
                questionIdsJson = "[\"mcq_chem_03\", \"mcq_chem_04\"]",
                randomizeQuestions = true,
                immediateReviewAllowed = true,
                isPublished = true,
                isPaidOnly = false
            )
            dao.insertMockTest(mock12)

            // 9. Announcements
            dao.insertAnnouncement(
                AnnouncementEntity(
                    id = "ann_01",
                    title = "Welcome to Karma Learning Classes!",
                    hindiTitle = "कर्मा लर्निंग क्लासेस में आपका हार्दिक स्वागत है!",
                    message = "Explore interactive courses, live classes with Prof. Chandan Khandey, PDF notes, question banks, and AI-powered step-by-step solutions.",
                    targetClass = "ALL",
                    teacherName = "Prof. Chandan Khandey",
                    priority = "HIGH"
                )
            )
            dao.insertAnnouncement(
                AnnouncementEntity(
                    id = "ann_02",
                    title = "CSIR-NET Chemistry Live Class Tomorrow at 6:00 PM",
                    hindiTitle = "सीएसआईआर-नेट रसायन विज्ञान लाइव क्लास कल शाम 6:00 बजे",
                    message = "Don't miss our deep dive on Pericyclic Mechanisms & Woodward-Hoffmann rules. Keep your notes ready!",
                    targetClass = "CSIR-NET",
                    teacherName = "Prof. Chandan Khandey",
                    priority = "URGENT"
                )
            )
        }
    }
}
