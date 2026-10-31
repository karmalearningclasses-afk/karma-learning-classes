package com.example

import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.data.local.KarmaDatabase
import com.example.data.repository.KarmaRepository
import com.example.ui.AppScreen
import com.example.ui.KarmaViewModel
import com.example.ui.KarmaViewModelFactory
import com.example.ui.auth.LoginScreen
import com.example.ui.auth.RegisterScreen
import com.example.ui.common.LocalSecureWindowController
import com.example.ui.common.SecureWindowController
import com.example.ui.selection.ClassSelectionScreen
import com.example.ui.student.*
import com.example.ui.teacher.*
import com.example.ui.theme.KarmaTheme

class MainActivity : ComponentActivity() {

    private val repository by lazy {
        val database = KarmaDatabase.getDatabase(this, lifecycleScope)
        KarmaRepository(database.karmaDao())
    }

    private val viewModel: KarmaViewModel by viewModels {
        KarmaViewModelFactory(repository)
    }

    private val secureController = object : SecureWindowController {
        override fun setProtection(isProtected: Boolean) {
            setNativeWindowSecurity(isProtected)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            KarmaTheme {
                val currentScreen by viewModel.currentScreen.collectAsState()
                val toastMsg by viewModel.toastMessage.collectAsState()
                val snackbarHostState = remember { SnackbarHostState() }

                // Reactively apply FLAG_SECURE if screen warrants it per teacher settings
                val screenProtected = viewModel.isCurrentScreenProtected()
                LaunchedEffect(screenProtected) {
                    setNativeWindowSecurity(screenProtected)
                }

                LaunchedEffect(toastMsg) {
                    toastMsg?.let {
                        Toast.makeText(this@MainActivity, it, Toast.LENGTH_SHORT).show()
                        viewModel.clearToast()
                    }
                }

                CompositionLocalProvider(
                    LocalSecureWindowController provides secureController
                ) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        snackbarHost = { SnackbarHost(snackbarHostState) }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (val screen = currentScreen) {
                                is AppScreen.Login -> LoginScreen(viewModel)
                                is AppScreen.Register -> RegisterScreen(viewModel)
                                is AppScreen.ClassSelection -> ClassSelectionScreen(viewModel)
                                is AppScreen.StudentMain -> StudentMainScreen(viewModel)
                                is AppScreen.StudentCourseDetail -> StudentCourseDetailScreen(screen.courseId, viewModel)
                                is AppScreen.StudentLessonViewer -> StudentLessonViewerScreen(screen.courseId, screen.lessonId, viewModel)
                                is AppScreen.StudentPdfViewer -> StudentPdfViewerScreen(screen.materialId, viewModel)
                                is AppScreen.StudentMockTestRunner -> StudentMockTestRunnerScreen(screen.mockTestId, viewModel)
                                is AppScreen.StudentTestResult -> StudentTestResultScreen(screen.attemptId, viewModel)

                                // Teacher Screens
                                is AppScreen.TeacherDashboard -> TeacherDashboardScreen(viewModel)
                                is AppScreen.TeacherCourses -> TeacherCoursesScreen(viewModel)
                                is AppScreen.TeacherLiveClasses -> TeacherLiveClassesScreen(viewModel)
                                is AppScreen.TeacherMaterials -> TeacherMaterialsScreen(viewModel)
                                is AppScreen.TeacherQuestionBank -> TeacherQuestionBankScreen(viewModel)
                                is AppScreen.TeacherMockTests -> TeacherMockTestsScreen(viewModel)
                                is AppScreen.TeacherAiHub -> TeacherAiHubScreen(viewModel)
                                is AppScreen.TeacherSecurity -> TeacherSecurityScreen(viewModel)
                                is AppScreen.TeacherStudents -> TeacherStudentsScreen(viewModel)
                                is AppScreen.TeacherAnnouncements -> TeacherAnnouncementsScreen(viewModel)

                                else -> LoginScreen(viewModel)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setNativeWindowSecurity(enableProtection: Boolean) {
        if (enableProtection) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    override fun onResume() {
        super.onResume()
        // Restore window protection state if returning from background
        if (viewModel.isCurrentScreenProtected()) {
            setNativeWindowSecurity(true)
        }
    }
}
