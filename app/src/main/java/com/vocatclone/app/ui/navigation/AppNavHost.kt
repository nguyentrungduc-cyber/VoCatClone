package com.vocatclone.app.ui.navigation

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.vocatclone.app.MainApp
import com.vocatclone.app.ui.auth.AuthViewModel
import com.vocatclone.app.ui.auth.LoginScreen
import com.vocatclone.app.ui.auth.RegisterScreen
import com.vocatclone.app.ui.bookcase.BookcaseDetailRoute
import com.vocatclone.app.ui.bookcase.BookcaseFormRoute
import com.vocatclone.app.ui.home.HomeRoute
import com.vocatclone.app.ui.splash.SplashScreen
import com.vocatclone.app.ui.welcome.WelcomeScreen
import kotlinx.coroutines.delay

/** Thời gian hiện màn Splash (mili giây). */
private const val SPLASH_DURATION_MS = 1200L
private const val PREFS_NAME = "vocat_prefs"
private const val KEY_SEEN_WELCOME = "seen_welcome"

/**
 * Khung điều hướng của cả app (thay cho việc MainActivity tự chọn màn bằng `when`).
 *
 * - Chưa đăng nhập thì sau Splash/Welcome vào LOGIN; đăng nhập hoặc đăng ký xong vào MAIN (Home)
 *   và xóa các màn đăng nhập khỏi back stack, nên bấm Back ở Home không quay lại màn Login.
 * - Đăng xuất thì về LOGIN và xóa toàn bộ back stack.
 * - Các route có tham số xem ở [Routes]. Route nào chưa có màn nối dữ liệu thì dùng [PlaceholderScreen].
 */
@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }

    // Email lấy từ AuthStateListener để UI tự cập nhật khi đăng nhập/đăng xuất.
    var userEmail by remember { mutableStateOf(auth.currentUser?.email) }
    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { userEmail = it.currentUser?.email }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    /** Màn đầu tiên sau phần giới thiệu (Splash/Welcome). */
    fun routeAfterIntro() = if (auth.currentUser == null) Routes.LOGIN else Routes.MAIN

    NavHost(navController = navController, startDestination = Routes.SPLASH, modifier = modifier) {

        composable(Routes.SPLASH) {
            SplashScreen()
            LaunchedEffect(Unit) {
                delay(SPLASH_DURATION_MS)
                val next = if (prefs.getBoolean(KEY_SEEN_WELCOME, false)) routeAfterIntro() else Routes.WELCOME
                navController.navigate(next) { popUpTo(Routes.SPLASH) { inclusive = true } }
            }
        }

        composable(Routes.WELCOME) {
            WelcomeScreen(
                onStartClick = {
                    prefs.edit().putBoolean(KEY_SEEN_WELCOME, true).apply()
                    navController.navigate(routeAfterIntro()) { popUpTo(Routes.WELCOME) { inclusive = true } }
                }
            )
        }

        composable(Routes.LOGIN) {
            val viewModel: AuthViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()
            LaunchedEffect(uiState.isSuccess) { if (uiState.isSuccess) navController.goHome() }
            LoginScreen(
                uiState = uiState,
                onBack = { navController.backOrFinish(context) },
                onLogin = viewModel::signIn,
                onRegisterClick = {
                    viewModel.clearMessages()
                    navController.navigate(Routes.REGISTER) { launchSingleTop = true }
                },
                onSendPasswordReset = viewModel::sendPasswordReset,
                onDismissMessage = viewModel::clearMessages
            )
        }

        composable(Routes.REGISTER) {
            val viewModel: AuthViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()
            LaunchedEffect(uiState.isSuccess) { if (uiState.isSuccess) navController.goHome() }
            RegisterScreen(
                uiState = uiState,
                onBack = { navController.backOrFinish(context) },
                onRegister = viewModel::register,
                onLoginClick = {
                    // Đến từ LOGIN thì chỉ cần quay lại; mở thẳng REGISTER thì thay nó bằng LOGIN.
                    if (!navController.popBackStack(Routes.LOGIN, inclusive = false)) {
                        navController.navigate(Routes.LOGIN) { popUpTo(Routes.REGISTER) { inclusive = true } }
                    }
                },
                onDismissMessage = viewModel::clearMessages
            )
        }

        composable(Routes.MAIN) {
            MainApp(
                userEmail = userEmail,
                onSignOut = {
                    auth.signOut()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onAccountClick = {
                    if (userEmail == null) navController.navigate(Routes.LOGIN) { launchSingleTop = true }
                },
                onRegisterClick = { navController.navigate(Routes.REGISTER) { launchSingleTop = true } },
                vocabulary = { m ->
                    HomeRoute(
                        modifier = m,
                        onAddBookcase = { navController.navigate(Routes.bookcaseForm()) },
                        onOpenBookcase = { id -> navController.navigate(Routes.bookcaseDetail(id)) },
                        onEditBookcase = { id -> navController.navigate(Routes.bookcaseForm(id)) }
                    )
                }
            )
        }

        // ---- Các route có tham số: chưa nối dữ liệu, dùng màn tạm ----

        composable(
            route = Routes.BOOKCASE_DETAIL,
            arguments = listOf(navArgument(Routes.ARG_BOOKCASE_ID) { type = NavType.LongType })
        ) { entry ->
            val id = entry.arguments?.getLong(Routes.ARG_BOOKCASE_ID) ?: NO_ID
            BookcaseDetailRoute(
                bookcaseId = id,
                onBack = { navController.popBackStack() },
                onAddWord = { bookcaseId -> navController.navigate(Routes.wordForm(bookcaseId)) },
                onOpenWord = { wordId -> navController.navigate(Routes.wordDetail(wordId)) }
            )
        }

        composable(
            route = Routes.BOOKCASE_FORM,
            arguments = listOf(
                navArgument(Routes.ARG_BOOKCASE_ID) { type = NavType.LongType; defaultValue = NO_ID }
            )
        ) { entry ->
            val id = entry.arguments?.getLong(Routes.ARG_BOOKCASE_ID) ?: NO_ID
            BookcaseFormRoute(
                bookcaseId = id.takeIf { it != NO_ID },
                onDone = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.WORD_DETAIL,
            arguments = listOf(navArgument(Routes.ARG_WORD_ID) { type = NavType.LongType })
        ) { entry ->
            val id = entry.arguments?.getLong(Routes.ARG_WORD_ID) ?: NO_ID
            PlaceholderScreen("Chi tiết từ vựng", "wordId = $id", onBack = { navController.popBackStack() })
        }

        composable(
            route = Routes.WORD_FORM,
            arguments = listOf(
                navArgument(Routes.ARG_BOOKCASE_ID) { type = NavType.LongType },
                navArgument(Routes.ARG_WORD_ID) { type = NavType.LongType; defaultValue = NO_ID }
            )
        ) { entry ->
            val bookcaseId = entry.arguments?.getLong(Routes.ARG_BOOKCASE_ID) ?: NO_ID
            val wordId = entry.arguments?.getLong(Routes.ARG_WORD_ID) ?: NO_ID
            val mode = if (wordId == NO_ID) "Thêm từ vào bộ $bookcaseId" else "Sửa từ $wordId (bộ $bookcaseId)"
            PlaceholderScreen("Form từ vựng", mode, onBack = { navController.popBackStack() })
        }
    }
}

/** Vào Home (MAIN) và xóa toàn bộ back stack cũ (Splash/Login/Register) để Back không quay lại màn đăng nhập. */
private fun NavHostController.goHome() {
    navigate(Routes.MAIN) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}

/** Back: quay về màn trước; nếu đây là màn duy nhất trong back stack thì thoát app. */
private fun NavHostController.backOrFinish(context: Context) {
    if (!popBackStack()) (context as? Activity)?.finish()
}
