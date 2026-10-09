package com.zombiethumb.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.zombiethumb.ui.navigation.ONBOARDING_ROUTE
import com.zombiethumb.ui.navigation.Screen
import com.zombiethumb.ui.navigation.ZombieNavHost
import com.zombiethumb.ui.theme.DeepNavy
import com.zombiethumb.ui.theme.DarkNavy
import com.zombiethumb.ui.theme.MutedTeal
import com.zombiethumb.ui.theme.OnSurfaceHigh
import com.zombiethumb.ui.theme.OnSurfaceLow
import com.zombiethumb.ui.theme.ZombieThumbTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ZombieThumbTheme {
                ZombieThumbApp()
            }
        }
    }
}

@Composable
fun ZombieThumbApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val settings = androidx.compose.runtime.remember { com.zombiethumb.data.datastore.SettingsDataStore(context) }
    val onboardingComplete by settings.onboardingComplete.collectAsState(initial = null)

    if (onboardingComplete == null) {
        // Still loading from DataStore, show empty screen
        return
    }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Hide bottom bar on onboarding and debug screens
    val showBottomBar = currentRoute != null
            && currentRoute != ONBOARDING_ROUTE
            && currentRoute != "debug"

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DeepNavy,
        bottomBar = {
            if (showBottomBar) {
                ZombieBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { screen ->
                        navController.navigate(screen.route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        ZombieNavHost(
            navController = navController,
            startDestination = if (onboardingComplete == true) Screen.Home.route else ONBOARDING_ROUTE,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Composable
private fun ZombieBottomBar(
    currentRoute: String?,
    onNavigate: (Screen) -> Unit,
) {
    NavigationBar(
        containerColor = DarkNavy,
        contentColor = OnSurfaceHigh,
    ) {
        Screen.bottomNavItems.forEach { screen ->
            val selected = currentRoute == screen.route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(screen) },
                icon = {
                    Icon(
                        imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                        contentDescription = screen.label,
                    )
                },
                label = { Text(screen.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MutedTeal,
                    selectedTextColor = MutedTeal,
                    unselectedIconColor = OnSurfaceLow,
                    unselectedTextColor = OnSurfaceLow,
                    indicatorColor = MutedTeal.copy(alpha = 0.12f),
                ),
            )
        }
    }
}
