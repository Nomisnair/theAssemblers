package com.zombiethumb.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zombiethumb.ui.theme.*

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
) {
    val threshold by viewModel.threshold.collectAsState()
    val cooldownMinutes by viewModel.cooldownMinutes.collectAsState()
    val milestoneMeters by viewModel.milestoneMeters.collectAsState()
    val demoMode by viewModel.demoMode.collectAsState()
    val quietHoursEnabled by viewModel.quietHoursEnabled.collectAsState()
    val quietHoursStart by viewModel.quietHoursStart.collectAsState()
    val quietHoursEnd by viewModel.quietHoursEnd.collectAsState()
    val monitoredApps by viewModel.monitoredApps.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            color = OnSurfaceHigh,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Demo Mode
        SettingsCard(title = "Demo Mode") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Enable Demo Mode",
                        style = MaterialTheme.typography.bodyLarge,
                        color = OnSurfaceHigh,
                    )
                    Text(
                        text = if (demoMode) "Active — lower threshold, short cooldown"
                        else "Lower threshold & short cooldown for hackathon demos",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (demoMode) WarmAmber else OnSurfaceMed,
                    )
                }
                Switch(
                    checked = demoMode,
                    onCheckedChange = { viewModel.setDemoMode(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = WarmAmber,
                        checkedTrackColor = WarmAmber.copy(alpha = 0.3f),
                        uncheckedThumbColor = OnSurfaceLow,
                        uncheckedTrackColor = MidNavy,
                    ),
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Threshold
        SettingsCard(title = "Trance Threshold") {
            Text(
                text = "${(threshold * 100).toInt()}%",
                style = MaterialTheme.typography.titleLarge,
                color = MutedTeal,
            )
            Slider(
                value = threshold,
                onValueChange = { viewModel.setThreshold(it) },
                valueRange = 0.30f..1.0f,
                steps = 13,
                colors = SliderDefaults.colors(
                    thumbColor = MutedTeal,
                    activeTrackColor = MutedTeal,
                    inactiveTrackColor = MidNavy,
                ),
            )
            Text(
                text = "Notification triggers when trance score exceeds this",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceMed,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Cooldown
        SettingsCard(title = "Notification Cooldown") {
            Text(
                text = "$cooldownMinutes minutes",
                style = MaterialTheme.typography.titleLarge,
                color = MutedTeal,
            )
            Slider(
                value = cooldownMinutes.toFloat(),
                onValueChange = { viewModel.setCooldownMinutes(it.toInt()) },
                valueRange = 1f..30f,
                steps = 28,
                colors = SliderDefaults.colors(
                    thumbColor = MutedTeal,
                    activeTrackColor = MutedTeal,
                    inactiveTrackColor = MidNavy,
                ),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Milestone distance
        SettingsCard(title = "Milestone Distance") {
            Text(
                text = "$milestoneMeters meters",
                style = MaterialTheme.typography.titleLarge,
                color = MutedTeal,
            )
            Slider(
                value = milestoneMeters.toFloat(),
                onValueChange = { viewModel.setMilestoneMeters(it.toInt()) },
                valueRange = 10f..200f,
                steps = 18,
                colors = SliderDefaults.colors(
                    thumbColor = MutedTeal,
                    activeTrackColor = MutedTeal,
                    inactiveTrackColor = MidNavy,
                ),
            )
            Text(
                text = "Notify at each distance milestone while scrolling",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceMed,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quiet hours
        SettingsCard(title = "Quiet Hours") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Enable Quiet Hours",
                    style = MaterialTheme.typography.bodyLarge,
                    color = OnSurfaceHigh,
                )
                Switch(
                    checked = quietHoursEnabled,
                    onCheckedChange = { viewModel.setQuietHoursEnabled(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MutedTeal,
                        checkedTrackColor = MutedTeal.copy(alpha = 0.3f),
                        uncheckedThumbColor = OnSurfaceLow,
                        uncheckedTrackColor = MidNavy,
                    ),
                )
            }
            if (quietHoursEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No notifications from ${formatHour(quietHoursStart)} to ${formatHour(quietHoursEnd)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceMed,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Monitored apps
        SettingsCard(title = "Monitored Apps") {
            val apps = listOf(
                "Instagram" to "com.instagram.android",
                "TikTok" to "com.zhiliaoapp.musically",
                "YouTube" to "com.google.android.youtube",
                "X (Twitter)" to "com.twitter.android",
                "Facebook" to "com.facebook.katana",
                "Reddit" to "com.reddit.frontpage",
            )
            apps.forEach { (name, pkg) ->
                val enabled = pkg in monitoredApps
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = OnSurfaceHigh,
                    )
                    Switch(
                        checked = enabled,
                        onCheckedChange = { viewModel.toggleApp(pkg, it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MutedTeal,
                            checkedTrackColor = MutedTeal.copy(alpha = 0.3f),
                            uncheckedThumbColor = OnSurfaceLow,
                            uncheckedTrackColor = MidNavy,
                        ),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Delete all data
        SettingsCard(title = "Data Management") {
            Text(
                text = "All data is stored locally on this device only.",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceMed,
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = { showDeleteDialog = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = SoftCoral),
            ) {
                Text("Delete All Data")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete All Data?", color = OnSurfaceHigh) },
            text = {
                Text(
                    "This will permanently delete all scrolling stats, session history, and notification logs. This cannot be undone.",
                    color = OnSurfaceMed,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAllData()
                        showDeleteDialog = false
                    },
                ) {
                    Text("Delete", color = SoftCoral, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = OnSurfaceMed)
                }
            },
            containerColor = SurfaceCard,
        )
    }
}

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = OnSurfaceHigh,
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

private fun formatHour(hour: Int): String {
    return when {
        hour == 0 -> "12:00 AM"
        hour < 12 -> "$hour:00 AM"
        hour == 12 -> "12:00 PM"
        else -> "${hour - 12}:00 PM"
    }
}
