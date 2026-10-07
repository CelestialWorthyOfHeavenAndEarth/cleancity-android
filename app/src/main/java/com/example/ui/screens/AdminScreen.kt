package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.data.ViolationFineEntity
import com.example.ui.CleanCityViewModel
import com.example.ui.theme.*

data class WardBenchmark(
    val wardName: String,
    val coverage: String,
    val segregation: String,
    val tonnageDaily: String,
    val complaintsOpen: Int,
    val collectionRate: String
)

@Composable
fun AdminScreen(
    viewModel: CleanCityViewModel,
    households: List<HouseholdEntity>,
    violations: List<ViolationFineEntity>,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val aiSummary by viewModel.aiSummary.collectAsState()
    val isSummaryLoading by viewModel.isSummaryLoading.collectAsState()
    var showFineDialog by remember { mutableStateOf(false) }

    val wardStats = listOf(
        WardBenchmark("Ward 42 (Indiranagar)", "96.4%", "86.5%", "14.2 MT", 2, "92.1%"),
        WardBenchmark("Ward 15 (Malleshwaram)", "94.8%", "82.0%", "12.8 MT", 4, "88.4%"),
        WardBenchmark("Ward 08 (Koramangala)", "98.1%", "89.2%", "16.4 MT", 1, "95.6%")
    )

    val tabLabels = listOf("SLB Metrics", "Ward Heatmap", "Finances", "Fines (${violations.size})")
    val tabIcons = listOf(Icons.Default.Speed, Icons.Default.TableChart, Icons.Default.AccountBalance, Icons.Default.Gavel)

    Scaffold(
        containerColor = DarkBaseBackground,
        floatingActionButton = {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(PaletteCreamGold)
                    .clickable { viewModel.generateExecutiveSummary() }
                    .padding(horizontal = 20.dp, vertical = 14.dp)
                    .testTag("admin_generate_ai_summary_fab"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isSummaryLoading) {
                        CircularProgressIndicator(color = PaletteBlack, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = PaletteBlack, modifier = Modifier.size(18.dp))
                    }
                    Text(
                        text = "AI Commissioner Brief",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = PaletteBlack
                        )
                    )
                }
            }
        },
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tabLabels.size) { index ->
                    val isSelected = selectedTab == index
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (isSelected) MobbinPrimary else MobbinCanvasSoft)
                            .border(1.dp, if (isSelected) MobbinPrimary else MobbinHairline, RoundedCornerShape(50))
                            .clickable { selectedTab = index }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                            .testTag("tab_admin_$index"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = tabIcons[index],
                                contentDescription = null,
                                tint = if (isSelected) MobbinOnPrimary else MobbinTextMuted,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = tabLabels[index],
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MobbinOnPrimary else MobbinTextMuted
                            )
                        }
                    }
                }
            }

            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn() + slideInHorizontally { width -> if (targetState > initialState) width / 3 else -width / 3 } togetherWith
                            fadeOut() + slideOutHorizontally { width -> if (targetState > initialState) -width / 3 else width / 3 }
                },
                label = "AdminTabContent"
            ) { targetIndex ->
                when (targetIndex) {
                    0 -> ServiceBenchmarksTab(
                        aiSummary = aiSummary,
                        isSummaryLoading = isSummaryLoading,
                        onRefreshSummary = { viewModel.generateExecutiveSummary() }
                    )
                    1 -> WardHeatmapTab(wardStats = wardStats)
                    2 -> FinanceDefaultersTab(
                        households = households,
                        onSendReminder = { /* simulated SMS reminder */ }
                    )
                    3 -> ViolationsFinesTab(
                        violations = violations,
                        onIssueFine = { showFineDialog = true }
                    )
                }
            }
        }
    }

    if (showFineDialog) {
        var violator by remember { mutableStateOf("") }
        var location by remember { mutableStateOf("100ft Road Junction, Ward 42") }
        var fineType by remember { mutableStateOf("Commercial Mixed Waste") }
        var amountText by remember { mutableStateOf("1500") }

        AlertDialog(
            onDismissRequest = { showFineDialog = false },
            containerColor = DarkSurfaceCard,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Gavel, contentDescription = null, tint = TagHazardRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Issue Municipal Penalty", style = MaterialTheme.typography.titleMedium.copy(color = MobbinInk))
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = violator,
                        onValueChange = { violator = it },
                        label = { Text("Violator Name / Establishment", color = MobbinTextMuted) },
                        modifier = Modifier.fillMaxWidth().testTag("violator_name_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MobbinPrimary,
                            unfocusedBorderColor = MobbinHairline,
                            focusedTextColor = MobbinInk,
                            unfocusedTextColor = MobbinInk
                        )
                    )
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Spot Location", color = MobbinTextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MobbinPrimary,
                            unfocusedBorderColor = MobbinHairline,
                            focusedTextColor = MobbinInk,
                            unfocusedTextColor = MobbinInk
                        )
                    )
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Penalty Amount (₹)", color = MobbinTextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MobbinPrimary,
                            unfocusedBorderColor = MobbinHairline,
                            focusedTextColor = MobbinInk,
                            unfocusedTextColor = MobbinInk
                        )
                    )
                }
            },
            confirmButton = {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(TagHazardRed)
                        .clickable(enabled = violator.isNotBlank()) {
                            val amt = amountText.toDoubleOrNull() ?: 1500.0
                            viewModel.issueSpotFine(violator, location, fineType, amt)
                            showFineDialog = false
                        }
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                ) {
                    Text("Issue Fine Notice", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MobbinOnPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showFineDialog = false }) { Text("Cancel", color = MobbinTextMuted) }
            }
        )
    }
}

@Composable
fun ServiceBenchmarksTab(
    aiSummary: String?,
    isSummaryLoading: Boolean,
    onRefreshSummary: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Header
        item {
            Column(modifier = Modifier.padding(top = 4.dp)) {
                Text("Municipal Corporation HQ", style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted))
                Text(
                    text = "Smart Analytics,\nCity Benchmarks",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.8).sp,
                        lineHeight = 36.sp,
                        color = MobbinInk
                    )
                )
            }
        }

        // AI Briefing Card if present or loading
        if (aiSummary != null || isSummaryLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(MobbinCanvasSoft)
                        .border(1.dp, MobbinHairline, RoundedCornerShape(24.dp))
                        .padding(20.dp)
                        .testTag("ai_briefing_card")
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MobbinPrimary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("AI Commissioner Briefing", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MobbinInk))
                            }
                            IconButton(onClick = onRefreshSummary, modifier = Modifier.size(26.dp)) {
                                Icon(Icons.Default.Refresh, contentDescription = "Regenerate", tint = MobbinPrimary, modifier = Modifier.size(18.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (isSummaryLoading) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(color = MobbinPrimary, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Synthesizing live IoT telemetry into executive briefing...", style = MaterialTheme.typography.bodySmall, color = MobbinTextMuted)
                            }
                        } else {
                            Text(
                                text = aiSummary ?: "",
                                fontSize = 12.sp,
                                lineHeight = 19.sp,
                                color = MobbinInk
                            )
                        }
                    }
                }
            }
        }

        item {
            Text("Service Level Benchmarks (SLBs)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    SlbLuxuryCard(
                        title = "Household Coverage",
                        value = "96.4%",
                        sub = "Target: 100%",
                        accent = MobbinPrimary,
                        icon = Icons.Default.Home,
                        modifier = Modifier.weight(1f)
                    )
                    SlbLuxuryCard(
                        title = "Collection Efficiency",
                        value = "98.2%",
                        sub = "Target: 100%",
                        accent = PaletteCreamGold,
                        icon = Icons.Default.LocalShipping,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    SlbLuxuryCard(
                        title = "Source Segregation",
                        value = "84.1%",
                        sub = "Target: 80%",
                        accent = TagWetGreen,
                        icon = Icons.Default.DeleteSweep,
                        modifier = Modifier.weight(1f)
                    )
                    SlbLuxuryCard(
                        title = "Waste Recovery",
                        value = "72.5%",
                        sub = "Target: 70%",
                        accent = MobbinPrimary,
                        icon = Icons.Default.Recycling,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    SlbLuxuryCard(
                        title = "Cost Recovery",
                        value = "88.6%",
                        sub = "Target: 100%",
                        accent = TagAlertAmber,
                        icon = Icons.Default.TrendingUp,
                        modifier = Modifier.weight(1f)
                    )
                    SlbLuxuryCard(
                        title = "Grievance Redressal",
                        value = "95.3%",
                        sub = "Target: 90%",
                        accent = TagWetGreen,
                        icon = Icons.Default.CheckCircle,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
fun SlbLuxuryCard(
    title: String,
    value: String,
    sub: String,
    accent: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(DarkSurfaceCard)
            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(22.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                value,
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold, color = accent)
            )
            Text(sub, fontSize = 10.sp, color = TextMuted)
        }
    }
}

@Composable
fun WardHeatmapTab(wardStats: List<WardBenchmark>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Inter-Ward Comparative Analysis", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }

        items(wardStats) { w ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(DarkSurfaceCard)
                    .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(22.dp))
                    .padding(18.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(w.wardName, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MobbinInk))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(DarkSurfaceElevated)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text("Daily: ${w.tonnageDaily}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PaletteCreamGold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Coverage", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            Text(w.coverage, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MobbinInk))
                        }
                        Column {
                            Text("Segregation", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            Text(w.segregation, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TagWetGreen))
                        }
                        Column {
                            Text("Fee Recovery", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            Text(w.collectionRate, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MobbinInk))
                        }
                        Column {
                            Text("Open Tickets", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            Text("${w.complaintsOpen}", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TagAlertAmber))
                        }
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
fun FinanceDefaultersTab(
    households: List<HouseholdEntity>,
    onSendReminder: (HouseholdEntity) -> Unit
) {
    val defaulters = households.filter { it.feeStatus != "PAID" }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MobbinCanvasSoft)
                    .border(1.dp, MobbinHairline, RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Text("Municipal Solid Waste Cost Recovery", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MobbinInk))
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Monthly O&M Budget:", style = MaterialTheme.typography.bodySmall, color = MobbinTextMuted)
                            Text("₹24.5 Lakhs", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MobbinInk))
                        }
                        Column {
                            Text("User Fees Collected:", style = MaterialTheme.typography.bodySmall, color = MobbinTextMuted)
                            Text("₹21.7 Lakhs (88.6%)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TagWetGreen))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Unit Processing Cost: ₹1,420 per metric tonne of municipal solid waste", style = MaterialTheme.typography.bodySmall, color = MobbinTextMuted)
                }
            }
        }

        item {
            Text("Pending Fee Accounts (${defaulters.size})", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }

        items(defaulters) { d ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkSurfaceCard)
                    .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(d.residentName, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MobbinInk))
                        Text("${d.address} • ${d.category}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        Text("Due: ₹${d.monthlyFee.toInt()} (${d.feeStatus})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TagAlertAmber)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(MobbinPrimary)
                            .clickable { onSendReminder(d) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, tint = MobbinOnPrimary, modifier = Modifier.size(12.dp))
                            Text("SMS Notice", fontSize = 11.sp, color = MobbinOnPrimary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
fun ViolationsFinesTab(
    violations: List<ViolationFineEntity>,
    onIssueFine: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Spot Penalties Register", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Text("Commercial mixed waste & night dumping", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(TagHazardRed)
                        .clickable { onIssueFine() }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("Issue Fine", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PaletteBlack)
                }
            }
        }

        items(violations) { v ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkSurfaceCard)
                    .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(v.violatorName, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MobbinInk))
                        Text(v.violationType, style = MaterialTheme.typography.bodySmall, color = TagHazardRed)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Place, contentDescription = null, tint = TextMuted, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(v.location, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("₹${v.fineAmount.toInt()}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MobbinInk))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (v.status == "PAID") TagWetGreenBg else TagAlertAmberBg)
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(v.status, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (v.status == "PAID") TagWetGreen else TagAlertAmber)
                        }
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}
