package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.data.ComplaintEntity
import com.example.ui.CleanCityViewModel
import com.example.ui.theme.*
import com.example.util.FileStorageHelper
import com.example.util.LocationHelper
import com.example.util.appStrings
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReportWasteScreen(
    viewModel: CleanCityViewModel,
    complaints: List<ComplaintEntity>,
    modifier: Modifier = Modifier
) {
    val strings = appStrings()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var photoPath by rememberSaveable { mutableStateOf<String?>(null) }
    var locationAddress by rememberSaveable { mutableStateOf("") }
    var latitude by rememberSaveable { mutableStateOf(12.9716) }
    var longitude by rememberSaveable { mutableStateOf(77.5946) }
    var isFetchingLocation by remember { mutableStateOf(false) }

    val categoryList = remember(strings) {
        listOf(
            strings.catOverflowingBin,
            strings.catStreetLitter,
            strings.catConstructionDebris,
            strings.catDrainClogged
        )
    }
    var selectedCategory by rememberSaveable { mutableStateOf("") }
    if (selectedCategory.isBlank() || selectedCategory !in categoryList) {
        selectedCategory = categoryList[0]
    }
    var descriptionText by rememberSaveable { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    var statusFilter by rememberSaveable { mutableStateOf("ALL") }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            val savedPath = FileStorageHelper.saveBitmap(context, bitmap)
            photoPath = savedPath
        }
    }

    // Photo picker launcher (zero-permission modern Android Photo Picker)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = FileStorageHelper.copyUriToInternal(context, uri)
            photoPath = savedPath
        }
    }

    // Camera runtime permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                cameraLauncher.launch(null)
            } catch (_: Exception) {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
        } else {
            // Graceful fallback to photo picker if camera permission is denied
            photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
    }

    // Location permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            coroutineScope.launch {
                isFetchingLocation = true
                try {
                    val result = LocationHelper.getCurrentLocation(context)
                    latitude = result.latitude
                    longitude = result.longitude
                    locationAddress = result.address
                } finally {
                    isFetchingLocation = false
                }
            }
        }
    }

    // Auto-fetch location if empty
    LaunchedEffect(Unit) {
        if (locationAddress.isNotBlank()) return@LaunchedEffect
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (hasFine || hasCoarse) {
            val result = LocationHelper.getCurrentLocation(context)
            latitude = result.latitude
            longitude = result.longitude
            locationAddress = result.address
        } else {
            locationAddress = "Ward 42, 2nd Cross, Indiranagar, Bengaluru"
        }
    }

    val filteredReports = remember(complaints, statusFilter) {
        when (statusFilter) {
            "PENDING" -> complaints.filter { it.status == "PENDING" || it.status == "OPEN" || it.status == "IN_PROGRESS" }
            "RESOLVED" -> complaints.filter { it.status == "RESOLVED" }
            else -> complaints
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Column(modifier = Modifier.padding(top = 4.dp)) {
                Text(
                    text = "${strings.reportWasteTitle}.",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MobbinInk
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${strings.reportWasteSubtitle}.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MobbinTextMuted
                    )
                )
            }
        }

        // Form Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MobbinCanvasSoft)
                    .border(1.dp, MobbinHairline, RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = strings.photoEvidenceTitle,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MobbinInk
                        )
                    )

                    // Photo Container / Preview
                    if (photoPath != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(190.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(18.dp))
                        ) {
                            AsyncImage(
                                model = File(photoPath!!),
                                contentDescription = "Captured uncollected trash photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Clear button
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(DarkBaseBackground.copy(alpha = 0.8f))
                                    .clickable { photoPath = null },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Remove photo", tint = PaletteWhite, modifier = Modifier.size(18.dp))
                            }
                        }
                    } else {
                        // Empty photo upload actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Camera Button
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(96.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(DarkSurfaceElevated)
                                    .border(1.dp, PaletteEmerald.copy(alpha = 0.3f), RoundedCornerShape(18.dp))
                                    .clickable {
                                        val hasCameraPerm = ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.CAMERA
                                        ) == PackageManager.PERMISSION_GRANTED

                                        if (hasCameraPerm) {
                                            try {
                                                cameraLauncher.launch(null)
                                            } catch (_: Exception) {
                                                photoPickerLauncher.launch(
                                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                )
                                            }
                                        } else {
                                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                        }
                                    }
                                    .testTag("btn_take_photo"),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = MobbinInk, modifier = Modifier.size(26.dp))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(strings.takePhoto, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MobbinInk)
                                }
                            }

                            // Gallery Button
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(96.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(MobbinCanvas)
                                    .border(1.dp, MobbinHairline, RoundedCornerShape(18.dp))
                                    .clickable {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    }
                                    .testTag("btn_pick_photo"),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = MobbinTextMuted, modifier = Modifier.size(26.dp))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(strings.uploadGallery, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MobbinInk)
                                }
                            }
                        }
                    }

                    // Location Section
                    Text(
                        text = strings.gpsLocationTitle,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MobbinInk
                        )
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MobbinCanvas)
                            .border(1.dp, MobbinHairline, RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = MobbinPrimary, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = locationAddress.ifBlank { strings.detectingGps },
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = MobbinInk),
                                        maxLines = 2
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Refresh GPS button
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(PaletteEmerald.copy(alpha = 0.15f))
                                        .clickable {
                                            locationPermissionLauncher.launch(
                                                arrayOf(
                                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                                )
                                            )
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                        .testTag("btn_get_location"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isFetchingLocation) {
                                        CircularProgressIndicator(color = PaletteEmerald, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                                    } else {
                                        Text(strings.refreshLocation, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PaletteEmerald)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Coordinates: ${String.format("%.4f", latitude)} N, ${String.format("%.4f", longitude)} E",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }

                    // Category Selection
                    Text(
                        text = strings.wasteCategoryTitle,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MobbinInk
                        )
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categoryList) { cat ->
                            val isSelected = selectedCategory == cat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(if (isSelected) MobbinPrimary else MobbinCanvas)
                                    .border(1.dp, if (isSelected) MobbinPrimary else MobbinHairline, RoundedCornerShape(50))
                                    .clickable { selectedCategory = cat }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MobbinOnPrimary else MobbinTextMuted
                                )
                            }
                        }
                    }

                    // Description Notes
                    OutlinedTextField(
                        value = descriptionText,
                        onValueChange = { descriptionText = it },
                        placeholder = { Text(strings.descriptionPlaceholder, color = MobbinTextFaint) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_report_description"),
                        minLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MobbinPrimary,
                            unfocusedBorderColor = MobbinHairline,
                            focusedTextColor = MobbinInk,
                            unfocusedTextColor = MobbinInk
                        )
                    )

                    // Submit Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(50))
                            .background(MobbinPrimary)
                            .clickable(enabled = !isSubmitting) {
                                isSubmitting = true
                                viewModel.submitWasteReport(
                                    title = "$selectedCategory - ${locationAddress.take(30)}",
                                    category = selectedCategory,
                                    description = descriptionText.ifBlank { strings.reportWasteSubtitle },
                                    address = locationAddress.ifBlank { "Ward 42, Indiranagar" },
                                    latitude = latitude,
                                    longitude = longitude,
                                    photoPath = photoPath
                                )
                                // Clear form
                                photoPath = null
                                descriptionText = ""
                                isSubmitting = false
                            }
                            .padding(vertical = 14.dp)
                            .testTag("btn_submit_waste_report"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = MobbinOnPrimary, modifier = Modifier.size(18.dp))
                            Text(
                                text = if (isSubmitting) strings.submittingReport else strings.submitReport,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MobbinOnPrimary
                                )
                            )
                        }
                    }
                }
            }
        }

        // Section: Past Reports & Status Tracking
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.myReportsTitle,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                // Filter pills
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        "ALL" to strings.filterAll,
                        "PENDING" to strings.filterPending,
                        "RESOLVED" to strings.filterResolved
                    ).forEach { (filterKey, filterLabel) ->
                        val isSelected = statusFilter == filterKey
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (isSelected) MobbinPrimary else MobbinCanvasSoft)
                                .border(1.dp, if (isSelected) MobbinPrimary else MobbinHairline, RoundedCornerShape(50))
                                .clickable { statusFilter = filterKey }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = filterLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MobbinOnPrimary else MobbinTextMuted
                            )
                        }
                    }
                }
            }
        }

        if (filteredReports.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(DarkSurfaceCard)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = strings.noReports,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                }
            }
        } else {
            items(filteredReports) { report ->
                WasteReportCard(
                    report = report,
                    onResolve = { viewModel.markWasteReportResolved(report.id) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
fun WasteReportCard(
    report: ComplaintEntity,
    onResolve: () -> Unit
) {
    val strings = appStrings()
    val statusColor = when (report.status) {
        "RESOLVED" -> TagWetGreen
        "IN_PROGRESS" -> TagAlertAmber
        else -> TagHazardRed
    }

    val displayStatus = when (report.status) {
        "RESOLVED" -> strings.statusResolved
        "IN_PROGRESS" -> strings.statusInProgress
        else -> strings.statusPending
    }

    val dateFormatter = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }
    val formattedDate = remember(report.createdAt) { dateFormatter.format(Date(report.createdAt)) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(DarkSurfaceCard)
            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(22.dp))
            .padding(16.dp)
            .testTag("report_item_${report.id}")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Text(
                        text = report.category,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MobbinInk)
                    )
                }

                // Status Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(statusColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = displayStatus,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            // Image Thumbnail if photo exists
            if (!report.photoUri.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(14.dp))
                ) {
                    val file = File(report.photoUri)
                    if (file.exists()) {
                        AsyncImage(
                            model = file,
                            contentDescription = "Uncollected trash photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(DarkSurfaceElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Trash Photo Attached", fontSize = 11.sp, color = TextMuted)
                        }
                    }
                }
            }

            Text(
                text = report.description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = PaletteEmerald, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${report.address} (${String.format("%.4f", report.latitude)}, ${String.format("%.4f", report.longitude)})",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    maxLines = 1
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Reported $formattedDate",
                    fontSize = 10.sp,
                    color = TextMuted
                )

                if (report.status != "RESOLVED") {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(DarkSurfaceElevated)
                            .border(1.dp, TagWetGreen.copy(alpha = 0.4f), RoundedCornerShape(50))
                            .clickable { onResolve() }
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(strings.markResolved, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TagWetGreen)
                    }
                }
            }
        }
    }
}
