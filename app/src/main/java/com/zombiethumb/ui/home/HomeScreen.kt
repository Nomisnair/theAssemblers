package com.zombiethumb.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zombiethumb.ui.theme.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onNavigateToDebug: () -> Unit = {},
    viewModel: HomeViewModel = viewModel(),
) {
    val monitoringEnabled by viewModel.monitoringEnabled.collectAsState()
    val tranceResult by viewModel.tranceResult.collectAsState()
    val todayStats by viewModel.todayStats.collectAsState()
    val notifCount by viewModel.notificationCount.collectAsState()

    val meters = todayStats?.totalMeters?.toInt() ?: tranceResult.totalMeters.toInt()
    val doomMinutes = todayStats?.totalDoomscrollMinutes ?: (tranceResult.sessionDurationMs / 60_000).toInt()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Title — long-press for debug panel
        Text(
            text = "ZombieThumb",
            style = MaterialTheme.typography.headlineMedium,
            color = OnSurfaceHigh,
            modifier = Modifier.combinedClickable(
                onClick = {},
                onLongClick = onNavigateToDebug,
            ),
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Doomscroll Detector",
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceMed,
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Trance Score Gauge
        TranceGauge(score = tranceResult.score)

        Spacer(modifier = Modifier.height(32.dp))

        // Monitoring toggle
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            shape = RoundedCornerShape(16.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        text = "Monitoring",
                        style = MaterialTheme.typography.titleMedium,
                        color = OnSurfaceHigh,
                    )
                    Text(
                        text = if (monitoringEnabled) "Active — watching for patterns"
                        else "Tap to start monitoring",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (monitoringEnabled) MutedTeal else OnSurfaceMed,
                    )
                }
                Switch(
                    checked = monitoringEnabled,
                    onCheckedChange = { viewModel.setMonitoringEnabled(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MutedTeal,
                        checkedTrackColor = MutedTeal.copy(alpha = 0.3f),
                        uncheckedThumbColor = OnSurfaceLow,
                        uncheckedTrackColor = MidNavy,
                    ),
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Stats row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.DirectionsWalk,
                value = "$meters m",
                label = "Feed mileage",
                color = MutedTeal,
            )
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.Timer,
                value = "$doomMinutes min",
                label = "Doomscroll",
                color = WarmAmber,
            )
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.Notifications,
                value = "$notifCount",
                label = "Nudges sent",
                color = PaleGreen,
            )
        }

        // Live app indicator
        if (monitoringEnabled && tranceResult.appName.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(16.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Currently monitoring",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceMed,
                    )
                    Text(
                        text = tranceResult.appName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MutedTeal,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
fun TranceGauge(
    score: Float, // 0f..1f
    modifier: Modifier = Modifier,
) {
    val animatedScore by animateFloatAsState(
        targetValue = score,
        animationSpec = tween(durationMillis = 800),
        label = "tranceScore",
    )

    val scoreColor = when {
        animatedScore < 0.5f -> ScoreSafe
        animatedScore < 0.85f -> ScoreWarning
        else -> ScoreDanger
    }

    Box(
        modifier = modifier.size(220.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 16.dp.toPx()
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

            // Background arc
            drawArc(
                color = MidNavy,
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )

            // Score arc
            if (animatedScore > 0.001f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(ScoreSafe, ScoreWarning, ScoreDanger),
                    ),
                    startAngle = 135f,
                    sweepAngle = 270f * animatedScore,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${(animatedScore * 100).toInt()}%",
                style = MaterialTheme.typography.displaySmall,
                color = scoreColor,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Trance Score",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceMed,
            )
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String,
    color: Color,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = OnSurfaceHigh,
                textAlign = TextAlign.Center,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceMed,
                textAlign = TextAlign.Center,
            )
        }
    }
}
