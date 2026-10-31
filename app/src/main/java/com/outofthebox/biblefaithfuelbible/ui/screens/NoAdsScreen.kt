package com.outofthebox.biblefaithfuelbible.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.outofthebox.biblefaithfuelbible.ui.viewmodel.BibleViewModel

private fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

@Composable
fun NoAdsScreen(
    viewModel: BibleViewModel,
    onOpenDrawer: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header Row with Drawer Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onOpenDrawer) {
                    Text(
                        text = "☰",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = "NO ADS",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Scrollable Content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status Feedback Banner (if message available)
            if (uiState.billingStatusMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = uiState.billingStatusMessage!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.clearBillingMessage() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Text("✕", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Hero Header Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (uiState.isAdFree) "✨" else "🚀",
                            fontSize = 32.sp
                        )
                    }

                    Text(
                        text = if (uiState.isAdFree) "Ad-Free Activated!" else "Remove Ads Forever",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = if (uiState.isAdFree)
                            "You have full lifetime ad-free access. Thank you for supporting Bible Faith Fuel!"
                        else
                            "Enjoy a pure, distraction-free Holy Bible experience with a simple one-time purchase.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Feature List Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "💎 What's Included:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    FeatureRow(
                        icon = "🚫",
                        title = "Zero Advertisements",
                        description = "Removes banner ads permanently across all screens."
                    )

                    HorizontalDivider()

                    FeatureRow(
                        icon = "⚡",
                        title = "Faster & Smoother",
                        description = "Saves bandwidth and speeds up scripture loading times."
                    )

                    HorizontalDivider()

                    FeatureRow(
                        icon = "🔒",
                        title = "One-Time Purchase",
                        description = "Pay once, keep forever! No monthly or yearly subscriptions."
                    )

                    HorizontalDivider()

                    FeatureRow(
                        icon = "💖",
                        title = "Support Development",
                        description = "Helps us create and maintain offline Bible tools for everyone."
                    )
                }
            }

            // Purchase / Action Section Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.isAdFree)
                        MaterialTheme.colorScheme.secondaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(16.dp),
                border = if (uiState.isAdFree)
                    BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                else null
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (uiState.isAdFree) {
                        Text(
                            text = "✅ Premium Status Active",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = "Ads are permanently disabled on your app. Enjoy reading God's Word!",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )

                        OutlinedButton(
                            onClick = { viewModel.restorePurchases() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Refresh Purchase Status")
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = uiState.adFreePrice ?: "One-Time Purchase",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Non-Consumable • Pay Once, Ad-Free Forever",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = {
                                activity?.let { viewModel.purchaseNoAds(it) }
                                    ?: viewModel.setAdFreeStatusForTesting(true)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            enabled = !uiState.isBillingLoading,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (uiState.isBillingLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "🚀 Remove Ads Now",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = { viewModel.restorePurchases() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isBillingLoading,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Restore Previous Purchase")
                        }

                        Text(
                            text = "Purchases are securely processed by Google Play and linked to your Google Account.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureRow(
    icon: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = icon, fontSize = 24.sp)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
