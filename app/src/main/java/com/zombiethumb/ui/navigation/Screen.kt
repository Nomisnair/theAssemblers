package com.zombiethumb.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.ui.graphics.vector.ImageVector

enum class Screen(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    Home("home", "Home", Icons.Filled.Home, Icons.Outlined.Home),
    Insights("insights", "Insights", Icons.Filled.Insights, Icons.Outlined.Insights),
    Settings("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings),
    Privacy("privacy", "Privacy", Icons.Filled.Shield, Icons.Outlined.Shield);

    companion object {
        val bottomNavItems = listOf(Home, Insights, Settings, Privacy)
    }
}

const val ONBOARDING_ROUTE = "onboarding"
const val DEBUG_ROUTE = "debug"
