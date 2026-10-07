package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HouseholdEntity
import com.example.data.PickupLogEntity
import com.example.ui.CleanCityViewModel
import com.example.ui.theme.*
import com.example.util.appStrings

@Composable
fun CollectorScreen(
    viewModel: CleanCityViewModel,
    households: List<HouseholdEntity>,
    pickupLogs: List<PickupLogEntity>,
    modifier: Modifier = Modifier
) {
    val strings = appStrings()
    var selectedHouseholdForStatus by remember { mutableStateOf<HouseholdEntity?>(null) }
    var showQrScannerDialog by remember { mutableStateOf(false) }
    var showNotCollectedDialog by remember { mutableStateOf<HouseholdEntity?>(null) }
    var showEarningsDialog by remember { mutableStateOf(false) }
    var voicePromptText by remember { mutableStateOf<String?>(null) }

    val collectedIds = pickupLogs.map { it.householdId }.toSet()
    val completedCount = households.count { it.id in collectedIds }
    val totalCount = households.size.coerceAtLeast(1)
    val progressFraction = completedCount.toFloat() / totalCount.toFloat()

    Scaffold(
        containerColor = DarkBaseBackground,
        floatingActionButton = {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(MobbinPrimary)
                    .clickable { showQrScannerDialog = true }
                    .padding(horizontal = 22.dp, vertical = 14.dp)
                    .testTag("collector_scan_qr_fab"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = MobbinOnPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = strings.householdQrCode,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MobbinOnPrimary
                        )
                    )
                }
            }
        },
        modifier = modifier
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(top = 4.dp)) {
                    Text(
                        text = "Doorstep Route • Ward 42.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = MobbinTextMuted)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Collector Duty,\nRamesh Kumar.",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MobbinInk
                        )
                    )
                }
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(MobbinCanvasSoft)
                        .border(1.dp, MobbinHairline, RoundedCornerShape(24.dp))
                        .padding(20.dp)
                        .testTag("collector_progress_card")
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Ward 42 Doorstep Route.",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MobbinInk
                                    )
                                )
                                Text(
                                    text = "2nd & 3rd Cross Corridor",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MobbinTextMuted
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(TagWetGreenBg)
                                    .border(1.dp, TagWetGreen.copy(alpha = 0.3f), RoundedCornerShape(50))
                                    .clickable { showEarningsDialog = true }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                    .testTag("collector_earnings_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.AccountBalanceWallet,
                                        contentDescription = null,
                                        tint = TagWetGreen,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "₹730 Today",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TagWetGreen
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        LinearProgressIndicator(
                            progress = { progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(50)),
                            color = TagWetGreen,
                            trackColor = MobbinHairline
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "$completedCount of $totalCount Doorsteps Cleared",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MobbinInk
                            )
                            Text(
                                text = "${(progressFraction * 100).toInt()}% Done",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TagWetGreen
                            )
                        }
                    }
                }
            }

            item {
                AnimatedVisibility(visible = voicePromptText != null) {
                    if (voicePromptText != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(DarkSurfaceElevated)
                                .border(1.dp, PaletteCreamGold.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                                .padding(14.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = PaletteEmerald, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(voicePromptText!!, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MobbinInk)
                                }
                                IconButton(onClick = { voicePromptText = null }, modifier = Modifier.size(22.dp)) {
                                    Icon(Icons.Default.Close, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Assigned Stops ($totalCount)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text("Tap to mark status", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                }
            }

            items(households) { h ->
                val log = pickupLogs.firstOrNull { it.householdId == h.id }

                CollectorHouseholdCard(
                    household = h,
                    pickupLog = log,
                    onCollected = { quality ->
                        viewModel.markCollection(h, "COLLECTED", quality)
                        voicePromptText = "Waste collection recorded for ${h.residentName}"
                    },
                    onNotCollected = {
                        showNotCollectedDialog = h
                    },
                    onMixedWaste = {
                        viewModel.markCollection(h, "MIXED_WASTE", "MIXED", "Waste unsegregated")
                        voicePromptText = "Warning: Mixed waste flagged for ${h.residentName}"
                    }
                )
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    if (showQrScannerDialog) {
        QrScannerDialog(
            households = households,
            onScanned = { qr ->
                val found = viewModel.simulateQrScan(qr)
                showQrScannerDialog = false
                if (found != null) {
                    selectedHouseholdForStatus = found
                    voicePromptText = "QR verified for ${found.residentName}"
                }
            },
            onDismiss = { showQrScannerDialog = false }
        )
    }

    if (showNotCollectedDialog != null) {
        val target = showNotCollectedDialog!!
        val reasons = listOf("Door Locked / Resident Absent", "No Waste Generated", "Refused to Hand Over", "Bin Not Kept Outside")
        var selectedReason by remember { mutableStateOf(reasons.first()) }

        AlertDialog(
            onDismissRequest = { showNotCollectedDialog = null },
            containerColor = DarkSurfaceCard,
            title = { Text("Reason: Not Collected", style = MaterialTheme.typography.titleMedium.copy(color = MobbinInk)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(target.residentName, fontWeight = FontWeight.Bold, color = MobbinInk)
                    Text(target.address, fontSize = 12.sp, color = TextMuted)
                    HorizontalDivider(color = DarkSurfaceBorder)
                    reasons.forEach { r ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedReason = r }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedReason == r,
                                onClick = { selectedReason = r },
                                colors = RadioButtonDefaults.colors(selectedColor = PaletteEmerald)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(r, fontSize = 13.sp, color = MobbinInk)
                        }
                    }
                }
            },
            confirmButton = {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(TagHazardRed)
                        .clickable {
                            viewModel.markCollection(target, "NOT_COLLECTED", "MIXED", selectedReason)
                            voicePromptText = "Recorded: $selectedReason"
                            showNotCollectedDialog = null
                        }
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                ) {
                    Text("Save Status", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PaletteBlack)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNotCollectedDialog = null }) { Text("Cancel", color = TextMuted) }
            }
        )
    }

    if (showEarningsDialog) {
        AlertDialog(
            onDismissRequest = { showEarningsDialog = false },
            containerColor = DarkSurfaceCard,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = PaletteCreamGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Collector Earnings", style = MaterialTheme.typography.titleMedium.copy(color = MobbinInk))
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(DarkSurfaceElevated)
                            .padding(14.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text("TODAY'S PAYOUT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                            Text("Rs 730.00", style = MaterialTheme.typography.displayMedium.copy(color = PaletteEmeraldDark, fontWeight = FontWeight.Bold))
                            Text("Direct DBT transfer to bank account", fontSize = 10.sp, color = TextMuted)
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Base Daily Wage (8h):", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        Text("Rs 450", fontWeight = FontWeight.Bold, color = MobbinInk, fontSize = 13.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Segregation Bonus (36 homes x Rs 5):", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        Text("+Rs 180", fontWeight = FontWeight.Bold, color = TagWetGreen, fontSize = 13.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("On-Time Route Finish:", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        Text("+Rs 100", fontWeight = FontWeight.Bold, color = TagWetGreen, fontSize = 13.sp)
                    }
                }
            },
            confirmButton = {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(PaletteCreamGold)
                        .clickable { showEarningsDialog = false }
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text("Close", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PaletteBlack)
                }
            }
        )
    }
}

@Composable
fun CollectorHouseholdCard(
    household: HouseholdEntity,
    pickupLog: PickupLogEntity?,
    onCollected: (quality: String) -> Unit,
    onNotCollected: () -> Unit,
    onMixedWaste: () -> Unit
) {
    val isDone = pickupLog != null
    var showQualitySelector by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(DarkSurfaceCard)
            .border(
                1.dp,
                if (isDone) TagWetGreen.copy(alpha = 0.3f) else DarkSurfaceBorder,
                RoundedCornerShape(22.dp)
            )
            .padding(18.dp)
            .testTag("collector_stop_${household.id}")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = household.address,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MobbinInk)
                    )
                    Text(
                        text = "${household.residentName} - ${household.category}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(MobbinCanvasSoft)
                        .border(1.dp, MobbinHairline, RoundedCornerShape(50))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = household.qrCode,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MobbinTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isDone) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (pickupLog?.status == "COLLECTED") TagWetGreenBg else TagHazardRedBg
                        )
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (pickupLog?.status == "COLLECTED") Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (pickupLog?.status == "COLLECTED") TagWetGreen else TagHazardRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Status: ${pickupLog?.status} (${pickupLog?.segregationQuality ?: ""}) - ${pickupLog?.reasonNotCollected ?: "Done"}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (pickupLog?.status == "COLLECTED") TagWetGreen else TagHazardRed
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1.3f)
                            .clip(RoundedCornerShape(50))
                            .background(TagWetGreenBg)
                            .border(1.dp, TagWetGreen.copy(alpha = 0.4f), RoundedCornerShape(50))
                            .clickable { showQualitySelector = !showQualitySelector }
                            .padding(vertical = 12.dp)
                            .testTag("btn_collected_${household.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = TagWetGreen, modifier = Modifier.size(15.dp))
                            Text("Collected", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TagWetGreen)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1.1f)
                            .clip(RoundedCornerShape(50))
                            .background(TagAlertAmberBg)
                            .border(1.dp, TagAlertAmber.copy(alpha = 0.4f), RoundedCornerShape(50))
                            .clickable { onMixedWaste() }
                            .padding(vertical = 12.dp)
                            .testTag("btn_mixed_${household.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Mixed Waste", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TagAlertAmber)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(50))
                            .background(MobbinCanvasSoft)
                            .border(1.dp, MobbinHairline, RoundedCornerShape(50))
                            .clickable { onNotCollected() }
                            .padding(vertical = 12.dp)
                            .testTag("btn_not_collected_${household.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Skip", fontSize = 12.sp, color = MobbinTextMuted)
                    }
                }

                AnimatedVisibility(visible = showQualitySelector) {
                    Column(modifier = Modifier.padding(top = 10.dp)) {
                        Text("Rate Segregation Purity:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MobbinInk)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(50))
                                    .background(TagWetGreen)
                                    .clickable {
                                        onCollected("GOOD")
                                        showQualitySelector = false
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Good 100%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MobbinOnPrimary)
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(50))
                                    .background(MobbinCanvasSoft)
                                    .border(1.dp, MobbinHairline, RoundedCornerShape(50))
                                    .clickable {
                                        onCollected("PARTIAL")
                                        showQualitySelector = false
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Partial", fontSize = 11.sp, color = MobbinInk)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QrScannerDialog(
    households: List<HouseholdEntity>,
    onScanned: (qr: String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurfaceCard,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = MobbinPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Household Bin QR Tag Scanner", style = MaterialTheme.typography.titleMedium.copy(color = MobbinInk))
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MobbinPrimary)
                        .border(2.dp, MobbinHairline, RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CenterFocusWeak, contentDescription = null, tint = MobbinOnPrimary, modifier = Modifier.size(54.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Align Bin QR in frame", fontSize = 11.sp, color = MobbinOnPrimary)
                    }
                }

                Text("Simulate Scanning Ward 42 Bin Tags:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MobbinInk)

                LazyColumn(
                    modifier = Modifier.fillMaxWidth().height(140.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(households) { h ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MobbinCanvasSoft)
                                .border(1.dp, MobbinHairline, RoundedCornerShape(12.dp))
                                .clickable { onScanned(h.qrCode) }
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(h.qrCode, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MobbinInk)
                                    Text(h.address, fontSize = 10.sp, color = MobbinTextMuted)
                                }
                                Text("Scan QR", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TagWetGreen)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextMuted) }
        }
    )
}
