package com.zombiethumb.ui.debug

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zombiethumb.service.MonitoringService
import com.zombiethumb.ui.home.TranceGauge
import com.zombiethumb.ui.theme.*

@Composable
fun DebugScreen(
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val result by MonitoringService.latestResult.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = OnSurfaceHigh,
                )
            }
            Icon(
                Icons.Filled.BugReport,
                contentDescription = null,
                tint = WarmAmber,
            )
            Text(
                text = "Debug Panel",
                style = MaterialTheme.typography.headlineMedium,
                color = OnSurfaceHigh,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Live trance gauge
        TranceGauge(
            score = result.score,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Live stats
        val debugStats = listOf(
            "Flick Interval" to "${result.avgFlickIntervalMs.toInt()} ms",
            "Flick Rhythm" to "${(result.flickRhythm * 100).toInt()}%",
            "Interaction Ratio" to "${(result.interactionRatio * 100).toInt()}%",
            "Consumption Score" to "${(result.consumptionRatio * 100).toInt()}%",
            "Tilt Angle" to "${result.currentTiltDeg.toInt()}°",
            "Ambient Light" to "${result.currentLux.toInt()} lux",
            "Bedtime Score" to "${(result.bedtimeSlump * 100).toInt()}%",
            "Feed Mileage" to "${String.format("%.1f", result.totalMeters)} m",
            "Mileage Score" to "${(result.feedMileage * 100).toInt()}%",
            "Trance Score" to "${(result.score * 100).toInt()}%",
            "In Trance?" to if (result.isInTrance) "⚠️ YES" else "No",
            "Foreground App" to result.appName.ifBlank { "--" },
            "Session Duration" to "${result.sessionDurationMs / 1000}s",
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Live Telemetry",
                    style = MaterialTheme.typography.titleMedium,
                    color = WarmAmber,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(12.dp))
                debugStats.forEach { (label, value) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceMed,
                        )
                        Text(
                            text = value,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (label == "In Trance?" && result.isInTrance)
                                SoftCoral else MutedTeal,
                            fontFamily = FontFamily.Monospace,
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Biomarker weights info
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Score Formula",
                    style = MaterialTheme.typography.titleMedium,
                    color = OnSurfaceHigh,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Score = FlickRhythm × 0.35 + ConsumptionRatio × 0.30 + BedtimeSlump × 0.20 + FeedMileage × 0.15",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceMed,
                    fontFamily = FontFamily.Monospace,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Simulate trance button
        Button(
            onClick = { MonitoringService.simulateTrance(context) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SoftCoral,
                contentColor = DeepNavy,
            ),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text(
                text = "🧟 Simulate Trance",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Triggers a test notification with simulated telemetry",
            style = MaterialTheme.typography.bodySmall,
            color = OnSurfaceMed,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}
