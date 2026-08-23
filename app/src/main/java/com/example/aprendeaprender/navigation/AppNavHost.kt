package com.example.aprendeaprender.navigation



import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.aprendeaprender.data.ai.ChallengeQuestionCache
import com.example.aprendeaprender.data.ai.GemmaModelManager
import com.example.aprendeaprender.data.auth.UtadeoCredentialsStore
import com.example.aprendeaprender.data.remote.FirebaseAuthService
import com.example.aprendeaprender.data.remote.FirestoreUserService
import com.example.aprendeaprender.data.remote.GemmaChallengeService
import com.example.aprendeaprender.data.remote.RealtimeChallengeService
import com.example.aprendeaprender.data.remote.RealtimeSubjectService
import com.example.aprendeaprender.data.remote.RealtimeTaskService
import com.example.aprendeaprender.data.repository.AuthRepository
import com.example.aprendeaprender.data.repository.ChallengeRepository
import com.example.aprendeaprender.data.repository.ChatRepository
import com.example.aprendeaprender.data.repository.ProfileRepository
import com.example.aprendeaprender.data.repository.SubjectRepository
import com.example.aprendeaprender.data.repository.TaskRepository
import com.example.aprendeaprender.data.repository.UtadeoRepository
import com.example.aprendeaprender.ui.components.BottomNavBar
import com.example.aprendeaprender.ui.components.BottomNavDestination
import com.example.aprendeaprender.ui.screens.ai.AiModelDownloadScreen
import com.example.aprendeaprender.ui.screens.auth.forgotpassword.ForgotPasswordScreen
import com.example.aprendeaprender.ui.screens.auth.login.LoginScreen
import com.example.aprendeaprender.ui.screens.auth.register.RegisterScreen
import com.example.aprendeaprender.ui.screens.auth.resetpassword.ResetPasswordEmailSentScreen
import com.example.aprendeaprender.ui.screens.auth.splash.SplashScreen
import com.example.aprendeaprender.ui.screens.auth.verifyemail.VerifyEmailScreen
import com.example.aprendeaprender.ui.screens.challenges.ChallengeDailyScreen
import com.example.aprendeaprender.ui.screens.challenges.ChallengeQuizScreen
import com.example.aprendeaprender.ui.screens.challenges.ChallengeSubjectScreen
import com.example.aprendeaprender.ui.screens.chat.ChatConversationScreen
import com.example.aprendeaprender.ui.screens.chat.ChatInboxScreen
import com.example.aprendeaprender.ui.screens.home.HomeRoute
import com.example.aprendeaprender.ui.screens.profile.ProfileRoute
import com.example.aprendeaprender.ui.screens.prueba.PruebaScreen
import com.example.aprendeaprender.ui.screens.subjects.CreateSubjectScreen
import com.example.aprendeaprender.ui.screens.subjects.SubjectDetailScreen
import com.example.aprendeaprender.ui.screens.subjects.SubjectListScreen
import com.example.aprendeaprender.ui.screens.subjects.SubjectSuccessScreen
import com.example.aprendeaprender.ui.screens.tasks.CreateTaskScreen
import com.example.aprendeaprender.ui.screens.tasks.TaskListScreen
import com.example.aprendeaprender.ui.theme.CyanAccent
import com.example.aprendeaprender.ui.theme.DarkBackground
import com.example.aprendeaprender.viewmodel.AuthEvent
import com.example.aprendeaprender.viewmodel.AuthViewModel
import com.example.aprendeaprender.viewmodel.ChallengeViewModel
import com.example.aprendeaprender.viewmodel.ChatViewModel
import com.example.aprendeaprender.viewmodel.HomeViewModel
import com.example.aprendeaprender.viewmodel.ProfileViewModel
import com.example.aprendeaprender.viewmodel.SubjectDetailViewModel
import com.example.aprendeaprender.viewmodel.SubjectViewModel
import com.example.aprendeaprender.viewmodel.TaskViewModel
import com.example.aprendeaprender.viewmodel.UtadeoViewModel
import kotlinx.coroutines.delay

private val bottomNavRoutes = setOf(
    Routes.HOME,
    Routes.SUBJECT_LIST,
    Routes.TASK_LIST,
    Routes.CHAT_INBOX,
    Routes.CHALLENGE_DAILY,
    Routes.CHALLENGE_SUBJECTS,
    Routes.CHALLENGE_QUIZ,
    Routes.PROFILE
)

private val fabRoutes = setOf(Routes.SUBJECT_LIST)

private fun NavHostController.navigateClearingStack(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { inclusive = true }
        launchSingleTop = true
    }
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomNav = currentRoute in bottomNavRoutes
    val showFab = currentRoute in fabRoutes

    val authService = remember { FirebaseAuthService() }
    val firestoreUserService = remember { FirestoreUserService() }
    val subjectService = remember { RealtimeSubjectService() }
    val taskService = remember { RealtimeTaskService() }
    val challengeService = remember { RealtimeChallengeService() }
    val gemmaModelManager = remember { GemmaModelManager(context.applicationContext) }
    val gemmaChallengeService = remember { GemmaChallengeService(gemmaModelManager) }
    val credentialsStore = remember { UtadeoCredentialsStore(context) }
    val questionCache = remember {
        ChallengeQuestionCache(context.applicationContext)
    }

    LaunchedEffect(gemmaModelManager) {
        gemmaModelManager.prepararEnSegundoPlano()
    }

    val authRepository = remember {
        AuthRepository(
            authService = authService,
            userService = firestoreUserService
        )
    }

    val profileRepository = remember {
        ProfileRepository(
            authService = authService,
            userService = firestoreUserService
        )
    }

    val subjectRepository = remember {
        SubjectRepository(
            authService = authService,
            subjectService = subjectService
        )
    }

    val taskRepository = remember {
        TaskRepository(
            authService = authService,
            taskService = taskService
        )
    }

    val chatRepository = remember { ChatRepository() }

    val challengeRepository = remember {
        ChallengeRepository(
            authService = authService,
            realtimeChallengeService = challengeService,
            subjectService = subjectService,
            taskService = taskService,
            gemmaChallengeService = gemmaChallengeService,
            questionCache = questionCache
        )
    }

    val authViewModel: AuthViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AuthViewModel(authRepository) as T
        }
    })

    val profileViewModel: ProfileViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProfileViewModel(profileRepository) as T
        }
    })

    val homeViewModel: HomeViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(
                profileRepository = profileRepository,
                taskRepository = taskRepository
            ) as T
        }
    })

    val subjectViewModel: SubjectViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SubjectViewModel(subjectRepository) as T
        }
    })

    val taskViewModel: TaskViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TaskViewModel(taskRepository, subjectRepository) as T
        }
    })

    val challengeViewModel: ChallengeViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ChallengeViewModel(challengeRepository) as T
        }
    })

    val utadeoViewModel: UtadeoViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return UtadeoViewModel(
                utadeoRepository = UtadeoRepository(),
                subjectRepository = subjectRepository,
                taskRepository = taskRepository,
                credentialsStore = credentialsStore
            ) as T
        }
    })

    val subjectDetailViewModel: SubjectDetailViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SubjectDetailViewModel(subjectRepository, taskRepository) as T
        }
    })

    val chatViewModel: ChatViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ChatViewModel(chatRepository, credentialsStore) as T
        }
    })

    val loginUiState by authViewModel.loginUiState.collectAsState()
    val registerUiState by authViewModel.registerUiState.collectAsState()
    val forgotPasswordUiState by authViewModel.forgotPasswordUiState.collectAsState()
    val verifyEmailUiState by authViewModel.verifyEmailUiState.collectAsState()
    val modelUiState by gemmaModelManager.uiState.collectAsState()

    var precargaRetosIniciada by remember { mutableStateOf(false) }

    LaunchedEffect(modelUiState.listo) {
        if (
            modelUiState.listo &&
            authService.currentUser() != null &&
            !precargaRetosIniciada
        ) {
            precargaRetosIniciada = true
            challengeViewModel.cargarRetoPorMaterias()
        }
    }


    LaunchedEffect(authViewModel) {
        authViewModel.authEvents.collect { event ->
            when (event) {
                AuthEvent.NavigateToHome -> {
                    navController.navigateClearingStack(Routes.HOME)
                }

                AuthEvent.NavigateToLogin -> {
                    precargaRetosIniciada = false
                    navController.navigateClearingStack(Routes.LOGIN)
                }

                AuthEvent.NavigateToVerifyEmail -> {
                    navController.navigateClearingStack(Routes.VERIFY_EMAIL)
                }

                AuthEvent.NavigateToResetPasswordEmailSent -> {
                    navController.navigate(Routes.RESET_PASSWORD_EMAIL_SENT) {
                        launchSingleTop = true
                    }
                }

                is AuthEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = context.getString(event.messageResId)
                    )
                }
            }
        }
    }

    val abrirModuloRetos = {
        gemmaModelManager.prepararEnSegundoPlano()

        navController.navigate(Routes.CHALLENGE_DAILY) {
            launchSingleTop = true
        }
    }

    val selectedBottomNav = when (currentRoute) {
        Routes.HOME -> BottomNavDestination.HOME
        Routes.SUBJECT_LIST -> BottomNavDestination.SUBJECTS
        Routes.TASK_LIST -> BottomNavDestination.TASKS
        Routes.CHAT_INBOX -> BottomNavDestination.CHAT
        Routes.CHALLENGE_DAILY,
        Routes.CHALLENGE_SUBJECTS,
        Routes.CHALLENGE_QUIZ -> BottomNavDestination.CHALLENGES
        Routes.PROFILE -> BottomNavDestination.PROFILE
        else -> BottomNavDestination.HOME
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = DarkBackground,
        bottomBar = {
            if (showBottomNav) {
                BottomNavBar(
                    selectedDestination = selectedBottomNav,
                    onDestinationSelected = { destination ->
                        when (destination) {
                            BottomNavDestination.HOME -> {
                                navController.navigate(Routes.HOME) {
                                    popUpTo(Routes.HOME) { inclusive = false }
                                    launchSingleTop = true
                                }
                            }

                            BottomNavDestination.SUBJECTS -> {
                                navController.navigate(Routes.SUBJECT_LIST) {
                                    launchSingleTop = true
                                }
                            }

                            BottomNavDestination.TASKS -> {
                                navController.navigate(Routes.TASK_LIST) {
                                    launchSingleTop = true
                                }
                            }

                            BottomNavDestination.CHAT -> {
                                navController.navigate(Routes.CHAT_INBOX) {
                                    launchSingleTop = true
                                }
                            }

                            BottomNavDestination.CHALLENGES -> {
                                abrirModuloRetos()
                            }

                            BottomNavDestination.PROFILE -> {
                                navController.navigate(Routes.PROFILE) {
                                    launchSingleTop = true
                                }
                            }
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (showFab) {
                FloatingActionButton(
                    onClick = {
                        when (currentRoute) {
                            Routes.TASK_LIST -> {
                                taskViewModel.resetCreateForm()
                                taskViewModel.cargarMaterias()
                                navController.navigate(Routes.CREATE_TASK) {
                                    launchSingleTop = true
                                }
                            }

                            Routes.SUBJECT_LIST -> {
                                subjectViewModel.resetCreateForm()
                                navController.navigate(Routes.CREATE_SUBJECT) {
                                    launchSingleTop = true
                                }
                            }
                        }
                    },
                    containerColor = CyanAccent,
                    contentColor = Color(0xFF0D1B2A),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Agregar"
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.SPLASH,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.SPLASH) {
                LaunchedEffect(Unit) {
                    delay(1200)
                    authViewModel.verificarSesion()
                }

                SplashScreen()
            }

            composable(Routes.LOGIN) {
                LoginScreen(
                    email = loginUiState.correo,
                    password = loginUiState.contrasena,
                    isLoading = loginUiState.cargando,
                    errorResId = loginUiState.mensajeErrorResId,
                    onEmailChange = authViewModel::onLoginCorreoChange,
                    onPasswordChange = authViewModel::onLoginContrasenaChange,
                    onLoginClick = authViewModel::iniciarSesion,
                    onForgotPasswordClick = {
                        navController.navigate(Routes.FORGOT_PASSWORD) {
                            launchSingleTop = true
                        }
                    },
                    onRegisterClick = {
                        navController.navigate(Routes.REGISTER) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Routes.REGISTER) {
                RegisterScreen(
                    nombre = registerUiState.nombre,
                    apellido = registerUiState.apellido,
                    edad = registerUiState.edad,
                    carrera = registerUiState.carrera,
                    email = registerUiState.correo,
                    password = registerUiState.contrasena,
                    confirmPassword = registerUiState.confirmarContrasena,
                    acceptedTerms = registerUiState.aceptaTerminos,
                    isLoading = registerUiState.cargando,
                    errorResId = registerUiState.mensajeErrorResId,
                    onNombreChange = authViewModel::onRegisterNombreChange,
                    onApellidoChange = authViewModel::onRegisterApellidoChange,
                    onEdadChange = authViewModel::onRegisterEdadChange,
                    onCarreraChange = authViewModel::onRegisterCarreraChange,
                    onEmailChange = authViewModel::onRegisterCorreoChange,
                    onPasswordChange = authViewModel::onRegisterContrasenaChange,
                    onConfirmPasswordChange = authViewModel::onRegisterConfirmarContrasenaChange,
                    onAcceptedTermsChange = authViewModel::onRegisterAceptaTerminosChange,
                    onRegisterClick = authViewModel::registrarUsuario,
                    onBackToLoginClick = { navController.popBackStack() }
                )
            }

            composable(Routes.FORGOT_PASSWORD) {
                ForgotPasswordScreen(
                    email = forgotPasswordUiState.correo,
                    isLoading = forgotPasswordUiState.cargando,
                    errorResId = forgotPasswordUiState.mensajeErrorResId,
                    successResId = forgotPasswordUiState.mensajeExitoResId,
                    onEmailChange = authViewModel::onForgotPasswordCorreoChange,
                    onSendLinkClick = authViewModel::enviarCorreoRecuperacion,
                    onBackToLoginClick = { navController.popBackStack() }
                )
            }

            composable(Routes.VERIFY_EMAIL) {
                VerifyEmailScreen(
                    uiState = verifyEmailUiState,
                    onReenviarCorreoClick = authViewModel::reenviarCorreoVerificacion,
                    onYaVerifiqueClick = authViewModel::revisarEstadoVerificacion,
                    onVolverLoginClick = authViewModel::cerrarSesion
                )
            }

            composable(Routes.RESET_PASSWORD_EMAIL_SENT) {
                ResetPasswordEmailSentScreen(
                    correo = forgotPasswordUiState.correoEnviadoA,
                    onVolverLoginClick = {
                        navController.navigateClearingStack(Routes.LOGIN)
                    }
                )
            }

            composable(Routes.HOME) {
                val homeUiState by homeViewModel.uiState.collectAsState()

                LaunchedEffect(Unit) {
                    homeViewModel.cargarDatosHome()
                }

                val lifecycleOwner = LocalLifecycleOwner.current

                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME) {
                            homeViewModel.cargarDatosHome()
                        }
                    }

                    lifecycleOwner.lifecycle.addObserver(observer)

                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }

                HomeRoute(
                    uiState = homeUiState,
                    onNavigateToProfile = {
                        navController.navigate(Routes.PROFILE) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToSubjects = {
                        navController.navigate(Routes.SUBJECT_LIST) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToTasks = {
                        navController.navigate(Routes.TASK_LIST) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToChallenges = abrirModuloRetos,
                    onEnrollSubjectClick = {
                        subjectViewModel.resetCreateForm()
                        navController.navigate(Routes.CREATE_SUBJECT) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Routes.CHALLENGE_DAILY) {
                val dailyUiState by challengeViewModel.dailyUiState.collectAsState()

                if (!modelUiState.listo) {
                    AiModelDownloadScreen(
                        modelManager = gemmaModelManager,
                        onModelReady = {
                            challengeViewModel.cargarRetoDiario()
                            challengeViewModel.iniciarPrecargaDePreguntas()
                        }
                    )
                } else {
                    LaunchedEffect(Unit) {
                        challengeViewModel.cargarRetoDiario()
                    }

                    ChallengeDailyScreen(
                        uiState = dailyUiState,
                        onOpenSubjects = {
                            challengeViewModel.cargarRetoPorMaterias()
                            navController.navigate(Routes.CHALLENGE_SUBJECTS) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }

            composable(Routes.CHALLENGE_SUBJECTS) {
                val subjectUiState by challengeViewModel.subjectUiState.collectAsState()

                LaunchedEffect(Unit) {
                    challengeViewModel.cargarRetoPorMaterias()
                }

                ChallengeSubjectScreen(
                    uiState = subjectUiState,
                    onSubjectClick = { subject ->
                        challengeViewModel.seleccionarMateria(subject)
                        navController.navigate(Routes.CHALLENGE_QUIZ) {
                            launchSingleTop = true
                        }
                    },
                    onBackClick = {
                        challengeViewModel.cargarRetoDiario()
                        navController.popBackStack()
                    }
                )
            }

            composable(Routes.CHALLENGE_QUIZ) {
                val subjectUiState by challengeViewModel.subjectUiState.collectAsState()

                ChallengeQuizScreen(
                    uiState = subjectUiState,
                    onAnswerSelected = challengeViewModel::seleccionarRespuesta,
                    onTryAgainClick = challengeViewModel::reintentarPregunta,
                    onNextClick = challengeViewModel::continuar,
                    onTimeExpired = challengeViewModel::tiempoAgotadoPreguntaActual,
                    onBackToSubjectsClick = {
                        challengeViewModel.cerrarActividad()
                        challengeViewModel.cargarRetoPorMaterias()
                        navController.popBackStack()
                    }
                )
            }

            composable(Routes.PROFILE) {
                ProfileRoute(
                    viewModel = profileViewModel,
                    onBackClick = { navController.popBackStack() },
                    onCerrarSesionClick = { authViewModel.cerrarSesion() }
                )
            }

            composable(Routes.CREATE_SUBJECT) {
                val createUiState by subjectViewModel.createUiState.collectAsState()

                LaunchedEffect(createUiState.materiaCreada) {
                    if (createUiState.materiaCreada) {
                        navController.navigate(Routes.SUBJECT_SUCCESS) {
                            popUpTo(Routes.CREATE_SUBJECT) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }

                CreateSubjectScreen(
                    uiState = createUiState,
                    onAsignaturaChange = subjectViewModel::onAsignaturaChange,
                    onInstructorChange = subjectViewModel::onInstructorChange,
                    onTemaChange = subjectViewModel::onTemaChange,
                    onAgregarTema = subjectViewModel::agregarTema,
                    onEliminarTema = subjectViewModel::eliminarTema,
                    onInscribirClick = subjectViewModel::inscribirMateria,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Routes.SUBJECT_SUCCESS) {
                SubjectSuccessScreen(
                    onAddTaskClick = {
                        subjectViewModel.resetCreateForm()
                        taskViewModel.resetCreateForm()
                        taskViewModel.cargarMaterias()
                        navController.navigate(Routes.CREATE_TASK) {
                            popUpTo(Routes.SUBJECT_SUCCESS) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onFinishClick = {
                        subjectViewModel.resetCreateForm()
                        navController.navigateClearingStack(Routes.HOME)
                    }
                )
            }

            composable(Routes.SUBJECT_LIST) {
                val listUiState by subjectViewModel.listUiState.collectAsState()

                LaunchedEffect(Unit) {
                    subjectViewModel.cargarMaterias()
                }

                SubjectListScreen(
                    uiState = listUiState,
                    onDeleteClick = subjectViewModel::eliminarMateria,
                    onSubjectClick = { subject ->
                        navController.navigate("subject_detail/${subject.id}")
                    },
                    onImportUtadeoClick = {
                        utadeoViewModel.resetear()
                        navController.navigate(Routes.UTADEO_SYNC) {
                            launchSingleTop = true
                        }
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(
                route = Routes.SUBJECT_DETAIL,
                arguments = listOf(
                    navArgument("subjectId") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val subjectId = backStackEntry.arguments?.getString("subjectId").orEmpty()
                val detailState by subjectDetailViewModel.uiState.collectAsState()

                LaunchedEffect(subjectId) {
                    subjectDetailViewModel.cargar(subjectId)
                }

                SubjectDetailScreen(
                    uiState = detailState,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Routes.TASK_LIST) {
                val taskListUiState by taskViewModel.listUiState.collectAsState()

                LaunchedEffect(Unit) {
                    taskViewModel.cargarTareas()
                }

                TaskListScreen(
                    uiState = taskListUiState,
                    onEstadoChange = { subjectId, taskId, estado ->
                        taskViewModel.cambiarEstado(
                            subjectId = subjectId,
                            taskId = taskId,
                            estado = estado
                        )
                    },
                    onDeleteClick = { subjectId, taskId ->
                        taskViewModel.eliminarTarea(
                            subjectId = subjectId,
                            taskId = taskId
                        )
                    },
                    onAddTaskClick = {
                        taskViewModel.resetCreateForm()
                        taskViewModel.cargarMaterias()
                        navController.navigate(Routes.CREATE_TASK) {
                            launchSingleTop = true
                        }
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Routes.CREATE_TASK) {
                val createUiState by taskViewModel.createUiState.collectAsState()

                LaunchedEffect(Unit) {
                    taskViewModel.cargarMaterias()
                }

                LaunchedEffect(createUiState.tareaCreada) {
                    if (createUiState.tareaCreada) {
                        navController.navigate(Routes.TASK_LIST) {
                            popUpTo(Routes.CREATE_TASK) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }

                CreateTaskScreen(
                    uiState = createUiState,
                    onTituloChange = taskViewModel::onTituloChange,
                    onDescripcionChange = taskViewModel::onDescripcionChange,
                    onFechaEntregaChange = taskViewModel::onFechaEntregaChange,
                    onPrioridadChange = taskViewModel::onPrioridadChange,
                    onEstadoChange = taskViewModel::onEstadoChange,
                    onSubjectChange = taskViewModel::onSubjectChange,
                    onCrearClick = taskViewModel::crearTarea,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Routes.UTADEO_SYNC) {
                val utadeoUiState by utadeoViewModel.uiState.collectAsState()

                PruebaScreen(
                    uiState = utadeoUiState,
                    onUsuarioChange = utadeoViewModel::onUsuarioChange,
                    onContrasenaChange = utadeoViewModel::onContrasenaChange,
                    onBuscarClick = utadeoViewModel::obtenerCursos,
                    onResetClick = utadeoViewModel::resetear,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Routes.CHAT_INBOX) {
                val state by chatViewModel.inboxState.collectAsState()
                val lifecycleOwner = LocalLifecycleOwner.current

                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME) {
                            chatViewModel.cargarBandeja()
                        }
                    }

                    lifecycleOwner.lifecycle.addObserver(observer)

                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }

                ChatInboxScreen(
                    uiState = state,
                    onConversationClick = { convId ->
                        navController.navigate("chat_conversation/$convId")
                    },
                    onSyncClick = {
                        navController.navigate(Routes.UTADEO_SYNC) {
                            launchSingleTop = true
                        }
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(
                route = Routes.CHAT_CONVERSATION,
                arguments = listOf(
                    navArgument("conversationId") {
                        type = NavType.LongType
                    }
                )
            ) { backStackEntry ->
                val convId = backStackEntry.arguments?.getLong("conversationId") ?: 0L
                val state by chatViewModel.conversationState.collectAsState()

                LaunchedEffect(convId) {
                    chatViewModel.abrirConversacion(convId)
                }

                ChatConversationScreen(
                    uiState = state,
                    onInputChange = chatViewModel::onInputChange,
                    onSendClick = chatViewModel::enviarMensaje,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}