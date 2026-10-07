package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.data.ComplaintEntity
import com.example.data.CrewAttendanceEntity
import com.example.ui.CleanCityViewModel
import com.example.ui.theme.*

@Composable
fun SupervisorScreen(
    viewModel: CleanCityViewModel,
    complaints: List<ComplaintEntity>,
    crew: List<CrewAttendanceEntity>,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    var verifyingComplaint by remember { mutableStateOf<ComplaintEntity?>(null) }
    val activeTickets = complaints.filter { it.status != "RESOLVED" }

    val tabLabels = listOf("Ward Radar", "Grievance SLA (${activeTickets.size})", "Crew Attendance")
    val tabIcons = listOf(Icons.Default.Radar, Icons.Default.HourglassTop, Icons.Default.Badge)

    Scaffold(
        containerColor = DarkBaseBackground,
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Horizontal Pill Navigation
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
                            .testTag("tab_supervisor_$index"),
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
                label = "SupervisorTabContent"
            ) { targetIndex ->
                when (targetIndex) {
                    0 -> WardLiveMapTab(crew = crew)
                    1 -> ComplaintSlaQueueTab(
                        activeTickets = activeTickets,
                        onVerifyResolution = { verifyingComplaint = it },
                        onEscalate = { viewModel.escalateComplaint(it.id) }
                    )
                    2 -> CrewAttendanceTab(
                        crew = crew,
                        onTogglePpe = { viewModel.toggleCrewPpe(it) }
                    )
                }
            }
        }
    }

    if (verifyingComplaint != null) {
        val target = verifyingComplaint!!
        AlertDialog(
            onDismissRequest = { verifyingComplaint = null },
            containerColor = DarkSurfaceCard,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Verified, contentDescription = null, tint = TagWetGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Field Inspection & Closure", style = MaterialTheme.typography.titleMedium.copy(color = MobbinInk))
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Ticket #${target.id}: ${target.title}", fontWeight = FontWeight.Bold, color = MobbinInk)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Place, contentDescription = null, tint = TextMuted, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(target.address, fontSize = 12.sp, color = TextMuted)
                    }
                    HorizontalDivider(color = DarkSurfaceBorder)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurfaceElevated)
                            .padding(10.dp)
                    ) {
                        Column {
                            Text("Inspector Geotag Proof:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PaletteCreamGold)
                            Text("Site cleared photographic proof recorded at 12.9785° N, 77.6410° E", fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                }
            },
            confirmButton = {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(TagWetGreen)
                        .clickable {
                            viewModel.resolveComplaint(target.id, "proof_field_verified_by_inspector")
                            verifyingComplaint = null
                        }
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                        .testTag("confirm_resolve_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = PaletteBlack, modifier = Modifier.size(14.dp))
                        Text("Mark Resolved", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PaletteBlack)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { verifyingComplaint = null }) { Text("Cancel", color = TextMuted) }
            }
        )
    }
}

@Composable
fun WardLiveMapTab(crew: List<CrewAttendanceEntity>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Header
        item {
            Column(modifier = Modifier.padding(top = 4.dp)) {
                Text("Ward 42 Inspectorate", style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted))
                Text(
                    text = "Field Operations,\nLive Fleet Radar",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.8).sp,
                        lineHeight = 36.sp,
                        color = MobbinInk
                    )
                )
            }
        }

        // Radar Map Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(DarkSurfaceCard)
                    .border(1.dp, PaletteCreamGold.copy(alpha = 0.35f), RoundedCornerShape(26.dp))
                    .padding(20.dp)
                    .testTag("ward_map_card")
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Active Telemetry Grid", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MobbinInk))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(TagWetGreenBg)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text("RADAR LIVE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TagWetGreen)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Clean radar viewport
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(MobbinCanvasSoft)
                            .border(1.dp, MobbinHairline, RoundedCornerShape(20.dp))
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxSize()) {
                            HorizontalDivider(color = MobbinHairline)
                            HorizontalDivider(color = MobbinHairline)
                            HorizontalDivider(color = MobbinHairline)
                        }

                        // Truck pin
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .clip(RoundedCornerShape(50))
                                .background(PaletteWhite)
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.LocalShipping, contentDescription = null, tint = PaletteBlack, modifier = Modifier.size(12.dp))
                                Text("Truck #2041 (22 km/h)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PaletteBlack)
                            }
                        }

                        // Collector Ramesh pin
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .clip(RoundedCornerShape(50))
                                .background(TagWetGreen)
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Engineering, contentDescription = null, tint = PaletteBlack, modifier = Modifier.size(12.dp))
                                Text("Cart #4: Ramesh (Active)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PaletteBlack)
                            }
                        }

                        // Hotspot bin
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .clip(RoundedCornerShape(50))
                                .background(TagAlertAmber)
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = PaletteBlack, modifier = Modifier.size(12.dp))
                                Text("Secondary Bin #1 (85%)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PaletteBlack)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Telemetry: 2 Trucks en route • 4 Doorstep carts active • Zero SOS breakdowns",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }
        }

        // Active Crew Header
        item {
            Text("Sanitation Crew on Route", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }

        items(crew) { member ->
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
                    Column {
                        Text(member.workerName, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MobbinInk))
                        Text("Role: ${member.role} • Check-in: ${member.checkInTime}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(TagWetGreenBg)
                            .padding(horizontal = 9.dp, vertical = 4.dp)
                    ) {
                        Text(member.status, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TagWetGreen)
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
fun ComplaintSlaQueueTab(
    activeTickets: List<ComplaintEntity>,
    onVerifyResolution: (ComplaintEntity) -> Unit,
    onEscalate: (ComplaintEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(activeTickets) { ticket ->
            val remainingHours = ((ticket.slaDeadlineMillis - System.currentTimeMillis()) / 3600000L).coerceAtLeast(0)
            val isCritical = remainingHours < 6

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(DarkSurfaceCard)
                    .border(
                        1.dp,
                        if (isCritical) TagHazardRed.copy(alpha = 0.5f) else DarkSurfaceBorder,
                        RoundedCornerShape(24.dp)
                    )
                    .padding(18.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "#TKT-${ticket.id} • ${ticket.category}",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isCritical) TagHazardRed else PaletteCreamGold
                            )
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (isCritical) TagHazardRedBg else TagAlertAmberBg)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (isCritical) "SLA CRITICAL" else "PENDING",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCritical) TagHazardRed else TagAlertAmber
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(ticket.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MobbinInk))
                    Text(ticket.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Place, contentDescription = null, tint = TextMuted, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(ticket.address, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurfaceElevated)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "$remainingHours Hours remaining before auto-escalation to Commissioner",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCritical) TagHazardRed else PaletteCreamGold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1.5f)
                                .clip(RoundedCornerShape(50))
                                .background(MobbinPrimary)
                                .clickable { onVerifyResolution(ticket) }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Inspect & Resolve", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MobbinOnPrimary)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(50))
                                .background(DarkSurfaceElevated)
                                .border(1.dp, TagHazardRed.copy(alpha = 0.4f), RoundedCornerShape(50))
                                .clickable { onEscalate(ticket) }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Escalate", fontSize = 12.sp, color = TagHazardRed)
                        }
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
fun CrewAttendanceTab(
    crew: List<CrewAttendanceEntity>,
    onTogglePpe: (CrewAttendanceEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(crew) { member ->
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
                        Text(member.workerName, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MobbinInk))
                        Text("Role: ${member.role} • ${member.checkInTime}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(
                                if (member.ppeChecked) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (member.ppeChecked) TagWetGreen else TagHazardRed,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = if (member.ppeChecked) "Mandatory PPE Verified" else "PPE Equipment Audit Required",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (member.ppeChecked) TagWetGreen else TagHazardRed
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (member.ppeChecked) MobbinCanvasSoft else MobbinPrimary)
                            .border(1.dp, if (member.ppeChecked) MobbinHairline else MobbinPrimary, RoundedCornerShape(50))
                            .clickable { onTogglePpe(member) }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (member.ppeChecked) "PPE OK" else "Verify PPE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (member.ppeChecked) MobbinInk else MobbinOnPrimary
                        )
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}
