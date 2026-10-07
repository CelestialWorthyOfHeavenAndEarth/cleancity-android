package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.WeighbridgeLogEntity
import com.example.ui.CleanCityViewModel
import com.example.ui.theme.*

data class SecondaryBinStop(
    val id: Int,
    val location: String,
    val capacity: String,
    val isEmptied: Boolean = false,
    val time: String? = null
)

@Composable
fun DriverScreen(
    viewModel: CleanCityViewModel,
    weighbridgeLogs: List<WeighbridgeLogEntity>,
    modifier: Modifier = Modifier
) {
    var showWeighbridgeDialog by remember { mutableStateOf(false) }
    var showBreakdownDialog by remember { mutableStateOf(false) }

    var secondaryBins by remember {
        mutableStateOf(
            listOf(
                SecondaryBinStop(1, "BDA Complex Secondary Bin #1, 2nd Cross", "1100 Litres (Wet)", true, "07:15 AM"),
                SecondaryBinStop(2, "Market Square Bulk Container #2", "2400 Litres (Mixed Dry)", true, "07:45 AM"),
                SecondaryBinStop(3, "Park Corner Community Bin #3", "1100 Litres (Wet)", false, null),
                SecondaryBinStop(4, "Metro Station Bulk Bin #4", "1100 Litres (Dry)", false, null)
            )
        )
    }

    Scaffold(
        containerColor = DarkBaseBackground,
        floatingActionButton = {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(PaletteCreamGold)
                    .clickable { showWeighbridgeDialog = true }
                    .padding(horizontal = 20.dp, vertical = 14.dp)
                    .testTag("driver_weighbridge_fab"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Scale, contentDescription = null, tint = PaletteBlack, modifier = Modifier.size(20.dp))
                    Text(
                        text = "Log Weighbridge Slip",
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            item {
                Column(modifier = Modifier.padding(top = 4.dp)) {
                    Text(
                        text = "Secondary Fleet • Heavy Tipper",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted)
                    )
                    Text(
                        text = "Vehicle Duty,\nManjunath (Truck #2041)",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.8).sp,
                            lineHeight = 36.sp,
                            color = MobbinInk
                        )
                    )
                }
            }

            // Vehicle Telemetry Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(DarkSurfaceCard)
                        .border(1.dp, PaletteEmerald.copy(alpha = 0.3f), RoundedCornerShape(22.dp))
                        .padding(18.dp)
                        .testTag("driver_vehicle_card")
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("KA-04-EA-2041", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MobbinInk))
                                Text("Hydraulic Compactor Truck • GPS Live", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(TagAlertAmberBg)
                                    .clickable { showBreakdownDialog = true }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                                    .testTag("driver_sos_button")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = TagAlertAmber, modifier = Modifier.size(13.dp))
                                    Text("SOS Alert", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TagAlertAmber)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(DarkSurfaceElevated)
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Navigation, contentDescription = null, tint = PaletteCreamGold, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Route: Ward 42 Secondary Points to Plant #4", style = MaterialTheme.typography.titleSmall.copy(color = MobbinInk))
                                    Text("Biomethanation & RDF Power Plant (14.2 km)", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                }
                            }
                        }
                    }
                }
            }

            // Bulk Bins Checklist Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("1100L Bulk Bin Pickups", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Text("2 / 4 Emptied", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TagWetGreen)
                }
            }

            items(secondaryBins) { bin ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(DarkSurfaceCard)
                        .border(
                            1.dp,
                            if (bin.isEmptied) TagWetGreen.copy(alpha = 0.3f) else DarkSurfaceBorder,
                            RoundedCornerShape(22.dp)
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(bin.location, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MobbinInk))
                            Text(bin.capacity, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            if (bin.isEmptied) {
                                Text("Emptied at ${bin.time ?: "Morning"}", fontSize = 11.sp, color = TagWetGreen, fontWeight = FontWeight.Bold)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (bin.isEmptied) TagWetGreen else MobbinPrimary)
                                .clickable {
                                    secondaryBins = secondaryBins.map {
                                        if (it.id == bin.id) it.copy(isEmptied = !it.isEmptied, time = "08:10 AM") else it
                                    }
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                if (bin.isEmptied) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = MobbinOnPrimary, modifier = Modifier.size(13.dp))
                                }
                                Text(
                                    text = if (bin.isEmptied) "Emptied" else "Empty Bin",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MobbinOnPrimary
                                )
                            }
                        }
                    }
                }
            }

            // Weighbridge Logs Header
            item {
                Text("Weighbridge Tonnage History", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }

            items(weighbridgeLogs) { log ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(DarkSurfaceCard)
                        .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(log.vehicleNumber, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = PaletteCreamGold))
                            Text("Net: ${String.format("%.2f", log.netWeightKg / 1000.0)} MT", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MobbinInk))
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Stream: ${log.wasteType}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text("Plant: ${log.treatmentPlant}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Gross: ${log.grossWeightKg.toInt()} kg | Tare: ${log.tareWeightKg.toInt()} kg", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            Text("#WS-${log.id}", fontSize = 10.sp, color = PaletteCreamGold)
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    // Weighbridge Slip Dialog
    if (showWeighbridgeDialog) {
        var vehicleNo by remember { mutableStateOf("KA-04-EA-2041") }
        var grossText by remember { mutableStateOf("8650") }
        var tareText by remember { mutableStateOf("4200") }
        var selectedType by remember { mutableStateOf("Wet Waste (Organic)") }
        var plant by remember { mutableStateOf("Biomethanation & Composting Plant #4") }

        AlertDialog(
            onDismissRequest = { showWeighbridgeDialog = false },
            containerColor = DarkSurfaceCard,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Scale, contentDescription = null, tint = PaletteCreamGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Treatment Plant Weigh Slip", style = MaterialTheme.typography.titleMedium.copy(color = MobbinInk))
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = vehicleNo,
                        onValueChange = { vehicleNo = it },
                        label = { Text("Vehicle Registration", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PaletteCreamGold,
                            unfocusedBorderColor = DarkSurfaceBorder,
                            focusedTextColor = MobbinInk,
                            unfocusedTextColor = MobbinInk
                        )
                    )

                    OutlinedTextField(
                        value = grossText,
                        onValueChange = { grossText = it },
                        label = { Text("Gross Loaded Weight (kg)", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PaletteCreamGold,
                            unfocusedBorderColor = DarkSurfaceBorder,
                            focusedTextColor = MobbinInk,
                            unfocusedTextColor = MobbinInk
                        )
                    )

                    OutlinedTextField(
                        value = tareText,
                        onValueChange = { tareText = it },
                        label = { Text("Tare Empty Weight (kg)", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PaletteCreamGold,
                            unfocusedBorderColor = DarkSurfaceBorder,
                            focusedTextColor = MobbinInk,
                            unfocusedTextColor = MobbinInk
                        )
                    )

                    val net = ((grossText.toDoubleOrNull() ?: 0.0) - (tareText.toDoubleOrNull() ?: 0.0)).coerceAtLeast(0.0)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurfaceElevated)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "Net Waste Tonnage: ${net.toInt()} kg (${String.format("%.2f", net / 1000.0)} MT)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = PaletteCreamGold
                        )
                    }
                }
            },
            confirmButton = {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(PaletteCreamGold)
                        .clickable {
                            val g = grossText.toDoubleOrNull() ?: 8000.0
                            val t = tareText.toDoubleOrNull() ?: 4000.0
                            viewModel.submitWeighbridgeEntry(vehicleNo, "Manjunath", selectedType, g, t, plant)
                            showWeighbridgeDialog = false
                        }
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                ) {
                    Text("Submit Slip", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PaletteBlack)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWeighbridgeDialog = false }) { Text("Cancel", color = TextMuted) }
            }
        )
    }

    // Breakdown SOS Dialog
    if (showBreakdownDialog) {
        AlertDialog(
            onDismissRequest = { showBreakdownDialog = false },
            containerColor = DarkSurfaceCard,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = TagAlertAmber)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Fleet SOS Malfunction", style = MaterialTheme.typography.titleMedium.copy(color = MobbinInk))
                }
            },
            text = {
                Text("Alerting Ward 42 Fleet Control with live vehicle GPS coordinates (12.9785° N, 77.6410° E) for immediate mechanical dispatch.", color = TextSecondary, fontSize = 13.sp)
            },
            confirmButton = {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(TagAlertAmber)
                        .clickable { showBreakdownDialog = false }
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                ) {
                    Text("Send SOS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PaletteBlack)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBreakdownDialog = false }) { Text("Cancel", color = TextMuted) }
            }
        )
    }
}
