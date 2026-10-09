package com.zombiethumb.ui.onboarding

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zombiethumb.ui.theme.*

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Snapout",
            style = MaterialTheme.typography.headlineLarge,
            color = OnSurfaceHigh,
            fontWeight = FontWeight.Bold,
        )

        Text(
            text = "Break free from the endless scroll",
            style = MaterialTheme.typography.bodyLarge,
            color = OnSurfaceMed,
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Privacy promise
        OnboardingCard(
            icon = Icons.Filled.Shield,
            iconColor = PaleGreen,
            title = "100% Private",
            description = "Snapout only measures how you scroll — speed, distance, tilt. " +
                    "It NEVER reads your screen, messages, or any content. " +
                    "No internet. No cloud. Everything stays on your device.",
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Accessibility permission
        OnboardingCard(
            icon = Icons.Filled.Accessibility,
            iconColor = MutedTeal,
            title = "Accessibility Service",
            description = "Snapout needs accessibility access to detect scroll speed and tap patterns " +
                    "in social media apps. It cannot read text (canRetrieveWindowContent is disabled).",
        ) {
            OutlinedButton(
                onClick = {
                    context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MutedTeal),
            ) {
                Text("Open Accessibility Settings")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Notification permission
        OnboardingCard(
            icon = Icons.Filled.Notifications,
            iconColor = WarmAmber,
            title = "Notifications",
            description = "Snapout sends gentle break reminders when it detects doomscrolling. " +
                    "On Android 13+, please grant notification permission.",
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Restricted settings note
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(
                text = "⚠️ Sideloaded APK? On Android 13+, go to Settings → Apps → Snapout → " +
                        "⋮ menu → \"Allow restricted settings\" to enable the accessibility service.",
                style = MaterialTheme.typography.bodySmall,
                color = WarmAmber,
                modifier = Modifier.padding(16.dp),
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onComplete,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MutedTeal,
                contentColor = DeepNavy,
            ),
            shape = RoundedCornerShape(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Get Started",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Icon(Icons.Filled.ArrowForward, contentDescription = null)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun OnboardingCard(
    icon: ImageVector,
    iconColor: androidx.compose.ui.graphics.Color,
    title: String,
    description: String,
    action: @Composable (() -> Unit)? = null,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(28.dp),
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = OnSurfaceHigh,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceMed,
            )
            if (action != null) {
                Spacer(modifier = Modifier.height(12.dp))
                action()
            }
        }
    }
}
