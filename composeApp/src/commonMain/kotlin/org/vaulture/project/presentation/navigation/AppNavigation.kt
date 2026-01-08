package org.vaulture.project.presentation.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.navigation.toRoute
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.storage.storage
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.vaulture.project.presentation.utils.AuthServiceImpl
import org.vaulture.project.presentation.viewmodels.LoginViewModel
import org.vaulture.project.data.local.OnboardingManager
import org.vaulture.project.presentation.theme.AppTheme
import org.vaulture.project.presentation.theme.AppThemeMode
import org.vaulture.project.presentation.theme.ThemePalette
import org.vaulture.project.presentation.ui.components.MiniPlayer
import org.vaulture.project.presentation.ui.screens.home.AnalyticsScreen
import org.vaulture.project.presentation.ui.screens.home.CBTScreen
import org.vaulture.project.presentation.ui.screens.home.HomeScreen
import org.vaulture.project.presentation.ui.screens.home.RhythmHomeScreen
import org.vaulture.project.presentation.ui.screens.home.RhythmPlayerScreen
import org.vaulture.project.presentation.ui.screens.home.WellnessTimerScreen
import org.vaulture.project.presentation.ui.screens.onboarding.InsightScreen
import org.vaulture.project.presentation.ui.screens.onboarding.ConnectScreen
import org.vaulture.project.presentation.ui.screens.onboarding.LoginScreen
import org.vaulture.project.presentation.ui.screens.onboarding.OnboardingScreenOne
import org.vaulture.project.presentation.ui.screens.onboarding.OnboardingScreenTwo
import org.vaulture.project.presentation.ui.screens.onboarding.SplashScreen
import org.vaulture.project.presentation.ui.screens.onboarding.WelcomeScreen
import org.vaulture.project.presentation.ui.screens.profile.ProfileScreen
import org.vaulture.project.presentation.ui.screens.profile.SettingsScreen
import org.vaulture.project.presentation.ui.screens.space.AddStoryScreen
import org.vaulture.project.presentation.ui.screens.space.CommentSheetContent
import org.vaulture.project.presentation.ui.screens.space.CreateSpaceScreen
import org.vaulture.project.presentation.ui.screens.space.SpaceFilter
import org.vaulture.project.presentation.ui.screens.space.SpacesHomeScreen
import org.vaulture.project.presentation.ui.screens.space.SpacesScreen
import org.vaulture.project.presentation.utils.rememberKmpAudioPlayer
import org.vaulture.project.presentation.viewmodels.AnalyticsViewModel
import org.vaulture.project.presentation.viewmodels.CBTViewModel
import org.vaulture.project.presentation.viewmodels.ProfileFilter
import org.vaulture.project.presentation.viewmodels.RhythmViewModel
import org.vaulture.project.presentation.viewmodels.SettingsViewModel
import org.vaulture.project.presentation.viewmodels.SpaceViewModel
import org.vaulture.project.presentation.viewmodels.WellnessViewModel
import org.vaulture.project.ui.screens.SpaceDetailScreen

@Serializable
sealed interface NavDestination {
    val routePattern: String
}

@Serializable
object Routes {
    @Serializable data object SPLASH : NavDestination { override val routePattern: String = "SPLASH" }
    @Serializable data object WELCOME : NavDestination { override val routePattern: String = "WELCOME" }
    @Serializable data object ONBOARDING_ONE : NavDestination { override val routePattern: String = "ONBOARDING_ONE" }
    @Serializable data object ONBOARDING_TWO : NavDestination { override val routePattern: String = "ONBOARDING_TWO" }
    @Serializable data object BEST_DEALS : NavDestination { override val routePattern: String = "BEST_DEALS" }
    @Serializable data object CONNECT : NavDestination { override val routePattern: String = "CONNECT" }
    @Serializable data object LOGIN : NavDestination { override val routePattern: String = "LOGIN" }
    @Serializable data object SIGN_UP : NavDestination { override val routePattern: String = "SIGN_UP" }

    // Main App Routes
    @Serializable data object HOME : NavDestination { override val routePattern: String = "HOME" }
    @Serializable data object SPACES : NavDestination { override val routePattern: String = "SPACES" }
    @Serializable data object PROFILE : NavDestination { override val routePattern: String = "PROFILE" }

    @Serializable data class SPACE_DETAIL(val spaceId: String) : NavDestination { override val routePattern: String = "SPACE_DETAIL" }
    @Serializable data object ADD_STORY : NavDestination { override val routePattern: String = "ADD_STORY" }
    @Serializable data object CHECK_IN: NavDestination { override val routePattern: String = "CHECK_IN" }
    @Serializable data object ANALYTICS: NavDestination { override val routePattern: String = "ANALYTICS" }
    @Serializable data object RHYTHM_HOME: NavDestination { override val routePattern: String = "AUDIO" }
    @Serializable data class RHYTHM_PLAYER(val trackId: String) : NavDestination { override val routePattern: String = "RHYTHM_PLAYER" }
    @Serializable data object WELLNESS_TIMER: NavDestination { override val routePattern: String = "WELLNESS_TIMER" }
    @Serializable data object SPACES_HOME: NavDestination { override val routePattern: String = "COMMUNITY" }
    @Serializable data object SETTINGS : NavDestination { override val routePattern: String = "SETTINGS" }

    @Serializable data object CREATE_SPACE : NavDestination { override val routePattern: String = "CREATE_SPACE" }
}

data class BottomNavItem(
    val label: String,
    val unselectedIcon: ImageVector,
    val selectedIcon: ImageVector,
    val destination: NavDestination
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    onGoogleSignInRequest: (() -> Unit)? = null
) {
    val navController: NavHostController = rememberNavController()
    val loginViewModel = remember { LoginViewModel(authService = AuthServiceImpl(auth = Firebase.auth)) }
    val spaceViewModel = remember {
        SpaceViewModel(
            firestore = Firebase.firestore,
            storage = Firebase.storage,
            auth = Firebase.auth
        )
    }
    val kmpAudioPlayer = rememberKmpAudioPlayer()
    val rhythmViewModel = remember { RhythmViewModel(kmpAudioPlayer) }
    val wellnessViewModel = remember { WellnessViewModel() }
    val cbtViewModel: CBTViewModel = remember { CBTViewModel() }
    val settingsViewModel = remember { SettingsViewModel() }

    val authState by loginViewModel.isAuthenticated.collectAsState(initial = null)
    val isFirstRun = remember { !OnboardingManager.hasCompletedOnboarding }
    val playerState by rhythmViewModel.uiState.collectAsState()
    val settingsState by settingsViewModel.uiState.collectAsState()

    val startDestination: NavDestination? = when (authState) {
        true -> Routes.HOME
        false -> if (isFirstRun) Routes.SPLASH else Routes.LOGIN
        null -> null // Loading state
    }

    val bottomNavItems = listOf(
        BottomNavItem("Home", Icons.Outlined.Home, Icons.Filled.Home, Routes.HOME),
        BottomNavItem("Space", Icons.Outlined.People, Icons.Filled.People, Routes.SPACES),
        BottomNavItem("Profile", Icons.Outlined.Person, Icons.Filled.Person, Routes.PROFILE)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomBarRoutePatterns = remember { bottomNavItems.map { it.destination.routePattern }.toSet() }
    val isBottomBarVisible = currentDestination?.hierarchy?.any {
        val routeName = it.route ?: ""
        bottomBarRoutePatterns.any { pattern -> routeName.contains(pattern, ignoreCase = true) }
    } == true

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedStoryIdForComments by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    var selectedFilter by rememberSaveable { mutableStateOf(ProfileFilter.MY_POSTS) }



    AppTheme(
        content = {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Transparent)
                    ) {
                        val currentRoute = currentDestination?.route
                        val isPlayerScreen = currentRoute?.contains("RHYTHM_PLAYER") == true

                        AnimatedVisibility(
                            visible = playerState.currentTrack != null && !isPlayerScreen,
                            enter = slideInVertically { it } + fadeIn(),
                            exit = slideOutVertically { it } + fadeOut()
                        ) {
                            playerState.currentTrack?.let { track ->
                                Box(
                                    modifier = Modifier.padding(
                                        bottom = 8.dp,
                                        start = 8.dp,
                                        end = 8.dp
                                    )
                                ) {
                                    MiniPlayer(
                                        track = track,
                                        isPlaying = playerState.isPlaying,
                                        onTogglePlay = { rhythmViewModel.togglePlayback() },
                                        onNext = { rhythmViewModel.playNext() },
                                        onClose = { rhythmViewModel.closePlayer() },
                                        onClick = {
                                            navController.navigate(Routes.RHYTHM_PLAYER(track.id))
                                        }
                                    )
                                }
                            }
                        }

                        AnimatedVisibility(
                            visible = isBottomBarVisible,
                            enter = slideInVertically { it },
                            exit = slideOutVertically { it }
                        ) {
                            NavigationBar(
                                modifier = Modifier.fillMaxWidth().height(64.dp),
                                containerColor = MaterialTheme.colorScheme.background,
                                tonalElevation = 8.dp
                            ) {
                                bottomNavItems.forEach { item ->
                                    val isSelected = currentDestination?.hierarchy?.any {
                                        it.route?.contains(
                                            item.destination.routePattern,
                                            ignoreCase = true
                                        ) == true
                                    } == true

                                    NavigationBarItem(
                                        selected = isSelected,
                                        onClick = {
                                            navController.navigate(item.destination) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        icon = {
                                            Icon(
                                                if (isSelected) item.selectedIcon else item.unselectedIcon,
                                                item.label
                                            )
                                        },
                                        label = {
                                            Text(
                                                item.label,
                                                fontSize = 11.sp,
                                                modifier = Modifier.padding(top = 0.dp)
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.primary,
                                            selectedTextColor = MaterialTheme.colorScheme.primary,
                                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            indicatorColor = Color.Transparent
                                        ),
                                    )
                                }
                            }
                        }
                    }
                }
            ) { paddingValues ->
                if (selectedStoryIdForComments != null) {
                    ModalBottomSheet(
                        onDismissRequest = { selectedStoryIdForComments = null },
                        sheetState = sheetState,
                        containerColor = MaterialTheme.colorScheme.surface,
                        dragHandle = { BottomSheetDefaults.DragHandle() },
                    ) {
                        CommentSheetContent(
                            storyId = selectedStoryIdForComments!!,
                            viewModel = spaceViewModel,
                            onClose = {
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    if (!sheetState.isVisible) selectedStoryIdForComments = null
                                }
                            }
                        )
                    }
                }

                if (startDestination == null) {
                    Box(
                        modifier = Modifier.fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    NavHost(
                        navController = navController,
                        startDestination = startDestination,
                        modifier = Modifier.fillMaxSize().padding(paddingValues)
                            .consumeWindowInsets(paddingValues)
                    ) {
                        composable<Routes.SPLASH> {
                            SplashScreen(
                                onTimeout = {
                                    navController.navigate(Routes.WELCOME) {
                                        popUpTo<Routes.SPLASH> {
                                            inclusive = true
                                        }
                                    }
                                })
                        }
                        composable<Routes.WELCOME> {
                            WelcomeScreen(
                                onGetStarted = { navController.navigate(Routes.ONBOARDING_ONE) },
                                onLoginClicked = {
                                    OnboardingManager.completeOnboarding()
                                    navController.navigate(Routes.LOGIN) {
                                        popUpTo<Routes.WELCOME> {
                                            inclusive = true
                                        }
                                    }
                                }
                            )
                        }
                        composable<Routes.ONBOARDING_ONE> {
                            OnboardingScreenOne(
                                onNext = { navController.navigate(Routes.ONBOARDING_TWO) },
                                onSkip = {
                                    OnboardingManager.completeOnboarding()
                                    navController.navigate(Routes.LOGIN) {
                                        popUpTo<Routes.WELCOME> {
                                            inclusive = true
                                        }
                                    }
                                }
                            )
                        }
                        composable<Routes.ONBOARDING_TWO> {
                            OnboardingScreenTwo(
                                onNext = { navController.navigate(Routes.BEST_DEALS) },
                                onSkip = {
                                    OnboardingManager.completeOnboarding()
                                    navController.navigate(Routes.LOGIN) {
                                        popUpTo<Routes.WELCOME> {
                                            inclusive = true
                                        }
                                    }
                                }
                            )
                        }
                        composable<Routes.BEST_DEALS> {
                            InsightScreen(
                                onGetStarted = { navController.navigate(Routes.CONNECT) })
                        }
                        composable<Routes.CONNECT> {
                            ConnectScreen(
                                onNavigateToLogin = {
                                    OnboardingManager.completeOnboarding()
                                    navController.navigate(Routes.LOGIN) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            inclusive = true
                                        }
                                    }
                                },
                                onNavigateToSignUp = {
                                    OnboardingManager.completeOnboarding()
                                    // Navigate to SIGN_UP route
                                    navController.navigate(Routes.SIGN_UP) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            inclusive = true
                                        }
                                    }
                                })
                        }

                        composable<Routes.LOGIN> {
                            LaunchedEffect(authState, navController) {
                                if (authState == true) {
                                    navController.navigate(Routes.HOME) {
                                        popUpTo(navController.graph.id) {
                                            inclusive = true
                                        }
                                    }
                                }
                            }
                            LoginScreen(
                                viewModel = loginViewModel,
                                onGoogleSignInRequest = {
                                    onGoogleSignInRequest?.invoke()
                                },
                                initialSignUpMode = false
                            )
                        }
                        composable<Routes.SIGN_UP> {
                            LaunchedEffect(authState, navController) {
                                if (authState == true) {
                                    navController.navigate(Routes.HOME) {
                                        popUpTo(navController.graph.id) {
                                            inclusive = true
                                        }
                                    }
                                }
                            }
                            LoginScreen(
                                viewModel = loginViewModel,
                                onGoogleSignInRequest = { onGoogleSignInRequest?.invoke() },
                                initialSignUpMode = true
                            )
                        }

                        composable<Routes.HOME> {
                            val authService = loginViewModel.authService
                            HomeScreen(
                                onNavigateToCheckIn = { navController.navigate(Routes.CHECK_IN) },
                                authService = authService,
                                navController = navController,
                                wellnessViewModel = wellnessViewModel,
                                onSignOut = {
                                    scope.launch {
                                        loginViewModel.authService.signOut()
                                        navController.navigate(Routes.LOGIN) {
                                            popUpTo(0)
                                        }
                                    }
                                }
                            )
                        }
                        composable<Routes.CHECK_IN> {
                            CBTScreen(
                                navController = navController,
                                viewModel = cbtViewModel
                            )
                        }
                        composable<Routes.SETTINGS> {
                            SettingsScreen(
                                viewModel = settingsViewModel,
                                onBack = { navController.popBackStack() },
                                onSignOut = {
                                    scope.launch {
                                        loginViewModel.authService.signOut()
                                        navController.navigate(Routes.LOGIN) {
                                            popUpTo(0)
                                        }
                                    }
                                }
                            )
                        }

                        composable<Routes.RHYTHM_HOME> {
                            RhythmHomeScreen(
                                navController = navController,
                                viewModel = rhythmViewModel
                            )
                        }

                        composable<Routes.RHYTHM_PLAYER> { backStackEntry ->
                            val route: Routes.RHYTHM_PLAYER = backStackEntry.toRoute()
                            RhythmPlayerScreen(
                                trackId = route.trackId,
                                viewModel = rhythmViewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable<Routes.WELLNESS_TIMER> {
                            WellnessTimerScreen(
                                viewModel = wellnessViewModel,
                                onBack = navController::popBackStack,
                            )
                        }


                        composable<Routes.ANALYTICS> {
                            val analyticsViewModel: AnalyticsViewModel =
                                remember { AnalyticsViewModel() }
                            AnalyticsScreen(
                                navController = navController,
                                viewModel = analyticsViewModel
                            )
                        }
                        composable<Routes.SPACES> {
                            var selectedFilter by rememberSaveable { mutableStateOf(SpaceFilter.Spaces) }
                            val authService = loginViewModel.authService
                            SpacesScreen(
                                selectedFilter = selectedFilter,
                                onFilterSelected = { filter -> selectedFilter = filter },
                                onSpaceClick = { spaceId ->
                                    navController.navigate(Routes.SPACE_DETAIL(spaceId = spaceId))
                                },
                                onCreateSpaceClick = {
                                    navController.navigate(Routes.CREATE_SPACE)
                                },
                                onAddStoryClick = {
                                    navController.navigate(Routes.ADD_STORY)
                                },
                                viewModel = spaceViewModel,
                                onCommentClick = { storyId ->
                                    selectedStoryIdForComments =
                                        storyId
                                },
                                wellnessViewModel = wellnessViewModel,
                                navController = navController,
                                onSignOut = {
                                    scope.launch {
                                        loginViewModel.authService.signOut()
                                        navController.navigate(Routes.LOGIN) {
                                            popUpTo(0)
                                        }
                                    }
                                },
                                authService = authService,
                            )

                        }
                        composable<Routes.SPACES_HOME> {
                            var selectedFilter by rememberSaveable { mutableStateOf(SpaceFilter.Spaces) }
                            val authService = loginViewModel.authService
                            SpacesHomeScreen(
                                selectedFilter = selectedFilter,
                                onFilterSelected = { filter -> selectedFilter = filter },
                                onSpaceClick = { spaceId ->
                                    navController.navigate(Routes.SPACE_DETAIL(spaceId = spaceId))
                                },
                                onCreateSpaceClick = {
                                    navController.navigate(Routes.CREATE_SPACE)
                                },
                                onAddStoryClick = {
                                    navController.navigate(Routes.ADD_STORY)
                                },
                                viewModel = spaceViewModel,
                                onCommentClick = { storyId ->
                                    selectedStoryIdForComments =
                                        storyId
                                },
                                wellnessViewModel = wellnessViewModel,
                                navController = navController,
                                onSignOut = {
                                    scope.launch {
                                        loginViewModel.authService.signOut()
                                        navController.navigate(Routes.LOGIN) {
                                            popUpTo(0)
                                        }
                                    }
                                },
                                authService = authService,
                            )

                        }
                        composable<Routes.CREATE_SPACE> {
                            CreateSpaceScreen(
                                onNavigateBack = { navController.popBackStack() },
                                viewModel = spaceViewModel,
                                onSpaceCreated = { navController.popBackStack() },
                            )
                        }

                        composable<Routes.SPACE_DETAIL> { backStackEntry ->
                            val spaceId = backStackEntry.toRoute<Routes.SPACE_DETAIL>().spaceId
                            SpaceDetailScreen(
                                spaceId = spaceId,
                                onNavigateBack = { navController.popBackStack() },
                                viewModel = spaceViewModel,
                                onSpaceSelected = { newSpaceId ->
                                    navController.navigate(Routes.SPACE_DETAIL(spaceId = newSpaceId)) {
                                        popUpTo(navController.graph.id) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable<Routes.ADD_STORY> {
                            val spaceViewModel = remember {
                                SpaceViewModel(
                                    firestore = Firebase.firestore,
                                    storage = Firebase.storage,
                                    auth = Firebase.auth
                                )
                            }
                            AddStoryScreen(
                                viewModel = spaceViewModel,
                                onStoryAdded = { navController.popBackStack() },
                                onCancel = { navController.popBackStack() }
                            )
                        }
                        composable<Routes.PROFILE> {
                            ProfileScreen(
                                authService = loginViewModel.authService,
                                wellnessViewModel = wellnessViewModel,
                                onSignOut = {
                                    scope.launch {
                                        loginViewModel.authService.signOut()
                                        navController.navigate(Routes.LOGIN) {
                                            popUpTo(0)
                                        }
                                    }
                                },
                                navController = navController,
                                spaceViewModel = spaceViewModel,
                                onFilterSelected = { selectedFilter = it },
                                onCommentClick = { storyId ->
                                    selectedStoryIdForComments =
                                        storyId
                                },
                                selectedFilter = selectedFilter,
                                onNavigateToSettings = {navController.navigate(Routes.SETTINGS)}
                            )
                        }

                        composable<Routes.ADD_STORY> {
                            AddStoryScreen(
                                viewModel = spaceViewModel,
                                onStoryAdded = { navController.popBackStack() },
                                onCancel = { navController.popBackStack() })
                        }
                    }
                }
            }
        },
        themeMode = settingsState.themeMode,
        themePalette = settingsState.themePalette
    )
}