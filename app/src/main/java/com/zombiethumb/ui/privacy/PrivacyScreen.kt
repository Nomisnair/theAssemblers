package com.zombiethumb.ui.privacy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zombiethumb.ui.theme.*

@Composable
fun PrivacyScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Shield,
                contentDescription = "Privacy",
                tint = PaleGreen,
                modifier = Modifier.size(32.dp),
            )
            Text(
                text = "Privacy",
                style = MaterialTheme.typography.headlineMedium,
                color = OnSurfaceHigh,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Snapout is designed with privacy as a core principle. " +
                    "All data stays on your device. There is no internet connection.",
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceMed,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // What we track
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "✅ What We Track",
                    style = MaterialTheme.typography.titleMedium,
                    color = PaleGreen,
                )
                Spacer(modifier = Modifier.height(12.dp))

                val tracked = listOf(
                    "Swipe velocity and cadence (time between flicks)",
                    "Scroll-to-tap ratio (passive scrolling vs. active taps)",
                    "Device tilt angle (gyroscope/accelerometer)",
                    "Ambient light level (light sensor)",
                    "Total scroll distance in meters (via screen DPI)",
                    "Which monitored app is in the foreground (package name only)",
                    "Aggregated numeric statistics (stored locally in Room)",
                )

                tracked.forEach { item ->
                    PrivacyItem(text = item, isTracked = true)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // What we NEVER touch
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "🚫 What We NEVER Touch",
                    style = MaterialTheme.typography.titleMedium,
                    color = SoftCoral,
                )
                Spacer(modifier = Modifier.height(12.dp))

                val neverTouched = listOf(
                    "Screen content, text, or what you're reading",
                    "Chat messages, comments, or search queries",
                    "Account info, usernames, or passwords",
                    "Photos, videos, or camera",
                    "Screenshots or screen recordings",
                    "Contacts, call logs, or SMS",
                    "Location data",
                    "Internet — no network permission exists at all",
                    "Cloud sync — zero servers, everything is local",
                    "Analytics or tracking of any kind",
                )

                neverTouched.forEach { item ->
                    PrivacyItem(text = item, isTracked = false)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Technical details
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "🔧 Technical Details",
                    style = MaterialTheme.typography.titleMedium,
                    color = WarmAmber,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "• Accessibility Service: canRetrieveWindowContent=\"false\"\n" +
                            "• Only listens to: TYPE_VIEW_SCROLLED, TYPE_VIEW_CLICKED, TYPE_WINDOW_STATE_CHANGED\n" +
                            "• Never calls event.t" + "ext, node.t" + "ext, or content" + "Description\n" +
                            "• Never uses dispatch" + "Gesture or modifies your scrolling\n" +
                            "• INTERNET permission is explicitly removed via tools:node=\"remove\"\n" +
                            "• All AI inference runs on-device via MediaPipe + Gemma\n" +
                            "• Data is aggregated numeric stats only, deletable anytime",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceMed,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun PrivacyItem(text: String, isTracked: Boolean) {
    Row(
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = if (isTracked) Icons.Filled.Check else Icons.Filled.Close,
            contentDescription = null,
            tint = if (isTracked) PaleGreen else SoftCoral,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceHigh,
        )
    }
}
