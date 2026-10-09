package com.zombiethumb.ui.insights

import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zombiethumb.ui.theme.*

@Composable
fun InsightsScreen(
    viewModel: InsightsViewModel = viewModel(),
) {
    val weeklyStats by viewModel.weeklyStats.collectAsState()
    val bedtimeStreak by viewModel.bedtimeStreak.collectAsState()

    // Prepare chart data — last 7 days
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val values = if (weeklyStats.isNotEmpty()) {
        // Fill from latest to oldest, padded with zeros
        val filled = MutableList(7) { 0f }
        weeklyStats.take(7).forEachIndexed { index, stat ->
            if (index < 7) filled[6 - index] = stat.totalMeters
        }
        filled
    } else {
        List(7) { 0f }
    }

    val peakHour = weeklyStats.maxByOrNull { it.peakTranceScore }?.peakHour

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Text(
            text = "Insights",
            style = MaterialTheme.typography.headlineMedium,
            color = OnSurfaceHigh,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your scrolling patterns this week",
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceMed,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Weekly meters chart
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Feed Mileage (meters/day)",
                    style = MaterialTheme.typography.titleMedium,
                    color = OnSurfaceHigh,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                ) {
                    val barWidth = size.width / (days.size * 2f)
                    val maxVal = values.maxOrNull()?.takeIf { it > 0 } ?: 100f

                    values.forEachIndexed { index, value ->
                        val barHeight = if (maxVal > 0) (value / maxVal) * size.height * 0.85f else 0f
                        val x = barWidth * (index * 2 + 0.5f)

                        // Bar background
                        drawRoundRect(
                            color = MidNavy,
                            topLeft = Offset(x, 0f),
                            size = Size(barWidth, size.height),
                            cornerRadius = CornerRadius(8f, 8f),
                        )

                        // Bar value
                        if (barHeight > 2f) {
                            val barColor = when {
                                value > maxVal * 0.8f -> ScoreDanger.copy(alpha = 0.8f)
                                value > maxVal * 0.5f -> ScoreWarning.copy(alpha = 0.8f)
                                else -> MutedTeal.copy(alpha = 0.8f)
                            }
                            drawRoundRect(
                                color = barColor,
                                topLeft = Offset(x, size.height - barHeight),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(8f, 8f),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    days.forEach { day ->
                        Text(
                            text = day,
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceLow,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                if (values.any { it > 0 }) {
                    Spacer(modifier = Modifier.height(12.dp))
                    val totalWeekMeters = values.sum().toInt()
                    val avgPerDay = (totalWeekMeters / 7f).toInt()
                    Text(
                        text = "Total: ${totalWeekMeters}m this week · Avg: ${avgPerDay}m/day",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceMed,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Peak hours card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Peak Doomscroll Hours",
                    style = MaterialTheme.typography.titleMedium,
                    color = OnSurfaceHigh,
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (peakHour != null && peakHour >= 0) {
                    val hour12 = if (peakHour == 0) "12 AM"
                    else if (peakHour < 12) "$peakHour AM"
                    else if (peakHour == 12) "12 PM"
                    else "${peakHour - 12} PM"
                    Text(
                        text = "Your peak scrolling tends to be around $hour12",
                        style = MaterialTheme.typography.bodyMedium,
                        color = WarmAmber,
                    )
                } else {
                    Text(
                        text = "Start monitoring to see your patterns",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceMed,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bedtime streak card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Bedtime Scrolling Streak",
                    style = MaterialTheme.typography.titleMedium,
                    color = OnSurfaceHigh,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "$bedtimeStreak",
                        style = MaterialTheme.typography.displaySmall,
                        color = if (bedtimeStreak > 0) PaleGreen else OnSurfaceMed,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "nights without late-night scrolling",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceMed,
                        modifier = Modifier.padding(bottom = 6.dp),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
