package com.zombiethumb.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.zombiethumb.ui.debug.DebugScreen
import com.zombiethumb.ui.home.HomeScreen
import com.zombiethumb.ui.insights.InsightsScreen
import com.zombiethumb.ui.onboarding.OnboardingScreen
import com.zombiethumb.ui.privacy.PrivacyScreen
import com.zombiethumb.ui.settings.SettingsScreen
import kotlinx.coroutines.launch

@Composable
fun ZombieNavHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val settings = androidx.compose.runtime.remember { com.zombiethumb.data.datastore.SettingsDataStore(context) }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = { fadeIn(animationSpec = tween(300)) },
        exitTransition = { fadeOut(animationSpec = tween(300)) },
    ) {
        composable(ONBOARDING_ROUTE) {
            OnboardingScreen(
                onComplete = {
                    scope.launch {
                        settings.setOnboardingComplete(true)
                    }
                    navController.navigate(Screen.Home.route) {
                        popUpTo(ONBOARDING_ROUTE) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToDebug = { navController.navigate(DEBUG_ROUTE) }
            )
        }

        composable(Screen.Insights.route) {
            InsightsScreen()
        }

        composable(Screen.Settings.route) {
            SettingsScreen()
        }

        composable(Screen.Privacy.route) {
            PrivacyScreen()
        }

        composable(DEBUG_ROUTE) {
            DebugScreen(onBack = { navController.popBackStack() })
        }
    }
}
