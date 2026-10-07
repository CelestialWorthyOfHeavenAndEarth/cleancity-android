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
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AnnouncementEntity
import com.example.data.ComplaintEntity
import com.example.data.HouseholdEntity
import com.example.data.PaymentEntity
import com.example.ui.CleanCityViewModel
import com.example.ui.theme.*
import com.example.util.appStrings

@Composable
fun CitizenScreen(
    viewModel: CleanCityViewModel,
    household: HouseholdEntity?,
    announcements: List<AnnouncementEntity>,
    complaints: List<ComplaintEntity>,
    payments: List<PaymentEntity>,
    modifier: Modifier = Modifier
) {
    val strings = appStrings()
    val tabStateHolder = rememberSaveableStateHolder()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var showQrDialog by remember { mutableStateOf(false) }
    var showPaymentSuccessDialog by remember { mutableStateOf<PaymentEntity?>(null) }

    data class CitizenNavItem(
        val label: String,
        val testTag: String,
        val icon: androidx.compose.ui.graphics.vector.ImageVector
    )

    val navItems = remember(strings) {
        listOf(
            CitizenNavItem(strings.tabPickup, "tab_daily_pickup", Icons.Default.Schedule),
            CitizenNavItem(strings.tabReportWaste, "tab_report_waste", Icons.Default.ReportProblem),
            CitizenNavItem(strings.tab2BinGuide, "tab_2_bin_guide", Icons.Default.DeleteSweep),
            CitizenNavItem(strings.tabAiHelp, "tab_ai_assistant", Icons.Default.SmartToy),
            CitizenNavItem(strings.tabBillsUpi, "tab_bills_&_upi", Icons.AutoMirrored.Filled.ReceiptLong)
        )
    }

    Scaffold(
        containerColor = MobbinCanvas,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            // Mobbin Bottom Navigation: Soft canvas bar with hairline border and stadium active indicator
            Column(modifier = Modifier.fillMaxWidth().background(MobbinCanvas)) {
                HorizontalDivider(color = MobbinHairline, thickness = 1.dp)
                NavigationBar(
                    containerColor = MobbinCanvas,
                    tonalElevation = 0.dp,
                    windowInsets = WindowInsets.navigationBars,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    navItems.forEachIndexed { index, item ->
                        val isSelected = selectedTab == index
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedTab = index },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = item.label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MobbinPrimary,
                                selectedTextColor = MobbinInk,
                                indicatorColor = TagWetGreenBg,
                                unselectedIconColor = MobbinTextMuted,
                                unselectedTextColor = MobbinTextMuted
                            ),
                            modifier = Modifier.testTag(item.testTag)
                        )
                    }
                }
            }
        },
        modifier = modifier
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding())
                .background(MobbinCanvas)
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn() + slideInHorizontally { width -> if (targetState > initialState) width / 4 else -width / 4 } togetherWith
                            fadeOut() + slideOutHorizontally { width -> if (targetState > initialState) -width / 4 else width / 4 }
                },
                label = "CitizenTabContent"
            ) { targetIndex ->
                tabStateHolder.SaveableStateProvider(targetIndex) {
                    when (targetIndex) {
                        0 -> DailyPickupTab(
                            household = household,
                            announcements = announcements,
                            onOpenQrTag = { showQrDialog = true }
                        )
                        1 -> ReportWasteScreen(
                            viewModel = viewModel,
                            complaints = complaints
                        )
                        2 -> SegregationGuideTab(
                            household = household
                        )
                        3 -> AiChatbotScreen(
                            viewModel = viewModel
                        )
                        4 -> BillsPaymentTab(
                            household = household,
                            payments = payments,
                            onPayFee = { hId, amt ->
                                viewModel.payMonthlyFee(hId, amt)
                            },
                            onViewReceipt = { payment ->
                                showPaymentSuccessDialog = payment
                            }
                        )
                    }
                }
            }
        }
    }

    if (showQrDialog && household != null) {
        HouseholdQrTagDialog(
            household = household,
            onDismiss = { showQrDialog = false }
        )
    }

    if (showPaymentSuccessDialog != null) {
        PaymentReceiptDialog(
            payment = showPaymentSuccessDialog!!,
            household = household,
            onDismiss = { showPaymentSuccessDialog = null }
        )
    }
}

@Composable
fun DailyPickupTab(
    household: HouseholdEntity?,
    announcements: List<AnnouncementEntity>,
    onOpenQrTag: () -> Unit
) {
    val strings = appStrings()
    var showAllAnnouncements by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MobbinCanvas),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = household?.residentName?.takeIf { it.isNotBlank() } ?: strings.roleCitizen,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MobbinTextMuted
                )
                Text(
                    text = strings.dailyPickupStatus,
                    style = MaterialTheme.typography.displayMedium,
                    color = MobbinInk
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = MobbinPrimary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = household?.address ?: strings.wardLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MobbinTextMuted
                    )
                }
            }
        }

        item {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MobbinPrimary,
                contentColor = MobbinOnPrimary
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Icon(
                        Icons.Default.LocalShipping, null,
                        modifier = Modifier.size(36.dp),
                        tint = MobbinOnPrimary
                    )
                    // A schedule is available; there is no live vehicle feed.
                    Text(
                        text = strings.estArrival,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MobbinOnPrimary
                    )
                    HorizontalDivider(color = MobbinOnPrimary.copy(alpha = 0.24f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        listOf(
                            strings.wetWaste to Icons.Default.Eco,
                            strings.dryWaste to Icons.Default.Recycling
                        ).forEach { (label, icon) ->
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(icon, null, tint = MobbinOnPrimary, modifier = Modifier.size(24.dp))
                                Text(label, style = MaterialTheme.typography.labelLarge, color = MobbinOnPrimary)
                            }
                        }
                    }
                    Button(
                        onClick = onOpenQrTag,
                        enabled = household != null,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("view_bin_qr_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MobbinSurface,
                            contentColor = MobbinPrimary,
                            disabledContainerColor = MobbinOnPrimary.copy(alpha = 0.16f),
                            disabledContentColor = MobbinOnPrimary.copy(alpha = 0.7f)
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Icon(Icons.Default.QrCode2, null, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(strings.householdQrCode, style = MaterialTheme.typography.labelLarge, color = LocalContentColor.current)
                    }
                }
            }
        }
        // Eco Score & Streak Card (Mobbin pricing-card: white canvas, 1px hairline border, 24px corners)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MobbinSurface)
                    .border(1.dp, MobbinHairline, RoundedCornerShape(24.dp))
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        // 30% squircle green icon tile
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(TagWetGreenBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = TagWetGreen,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${household?.segregationScore ?: 0}%",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MobbinInk
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "• ${strings.goodSegregationBadge}",
                                    fontSize = 12.sp,
                                    color = TagWetGreen,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Text(
                                text = strings.segregationScore,
                                style = MaterialTheme.typography.bodySmall.copy(color = MobbinTextMuted)
                            )
                        }
                    }

                    // button-pill-soft streak chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(MobbinCanvasSoft)
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = TagAlertAmber,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${household?.streakDays ?: 0} ${strings.streakDays}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MobbinInk
                            )
                        }
                    }
                }
            }
        }

        // Municipal Updates / Notices (Mobbin faq-row style: canvas-soft bars with 16px corners)
        if (announcements.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.announcementsTitle,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MobbinInk
                        )
                    )
                    if (announcements.size > 1) {
                        Text(
                            text = if (showAllAnnouncements) "Show Less" else "View All (${announcements.size})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MobbinAccent,
                            modifier = Modifier.clickable { showAllAnnouncements = !showAllAnnouncements }
                        )
                    }
                }
            }

            val displayedNotices = if (showAllAnnouncements) announcements else announcements.take(1)
            items(displayedNotices) { notice ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MobbinCanvasSoft)
                        .border(1.dp, MobbinHairlineSoft, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Stadium pill category badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(if (notice.isUrgent) TagAlertAmberBg else MobbinCanvas)
                                    .padding(horizontal = 9.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = notice.category.uppercase(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (notice.isUrgent) TagAlertAmber else MobbinInk,
                                    letterSpacing = 0.4.sp
                                )
                            }
                            Text(
                                text = notice.date,
                                style = MaterialTheme.typography.bodySmall,
                                color = MobbinTextMuted
                            )
                        }
                        Text(
                            text = notice.title,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MobbinInk
                            )
                        )
                        Text(
                            text = notice.content,
                            style = MaterialTheme.typography.bodySmall.copy(color = MobbinTextMuted),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
fun SegregationGuideTab(
    household: HouseholdEntity?
) {
    val strings = appStrings()
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MobbinCanvas),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(top = 4.dp)) {
                Text(
                    text = strings.segregationGuideTitle,
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MobbinInk
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = strings.segregationGuideSubtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(color = MobbinTextMuted)
                )
            }
        }

        item {
            MobbinBinCard(
                accentColor = TagWetGreen,
                bgTint = TagWetGreenBg,
                binName = strings.greenBinTitle,
                subHeading = strings.wetWaste,
                itemsList = listOf(strings.greenBinDesc),
                footerNote = "Do NOT put plastic bags inside."
            )
        }

        item {
            MobbinBinCard(
                accentColor = TagDryBlue,
                bgTint = TagDryBlueBg,
                binName = strings.blueBinTitle,
                subHeading = strings.dryWaste,
                itemsList = listOf(strings.blueBinDesc),
                footerNote = "Keep clean and dry before collection."
            )
        }

        item {
            MobbinBinCard(
                accentColor = TagHazardRed,
                bgTint = TagHazardRedBg,
                binName = strings.redBagTitle,
                subHeading = strings.sanitaryWaste,
                itemsList = listOf(strings.redBagDesc),
                footerNote = "Wrap securely and hand over separately."
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
fun MobbinBinCard(
    accentColor: Color,
    bgTint: Color,
    binName: String,
    subHeading: String,
    itemsList: List<String>,
    footerNote: String
) {
    // Mobbin pricing-card style: 24px corners, white canvas, hairline border
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MobbinSurface)
            .border(1.dp, MobbinHairline, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 30% squircle color badge
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(bgTint),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                    )
                }
                Column {
                    Text(
                        text = binName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MobbinInk
                        )
                    )
                    Text(
                        text = subHeading,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MobbinTextMuted
                    )
                }
            }

            itemsList.forEach { item ->
                Text(
                    text = item,
                    style = MaterialTheme.typography.bodyMedium.copy(color = MobbinInk),
                    lineHeight = 20.sp
                )
            }

            // Soft canvas tip row
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MobbinCanvasSoft)
                    .padding(horizontal = 12.dp, vertical = 9.dp)
            ) {
                Text(
                    text = "Tip: $footerNote",
                    fontSize = 12.sp,
                    color = MobbinInk,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun BillsPaymentTab(
    household: HouseholdEntity?,
    payments: List<PaymentEntity>,
    onPayFee: (Int, Double) -> Unit,
    onViewReceipt: (PaymentEntity) -> Unit
) {
    val strings = appStrings()
    val fee = household?.monthlyFee ?: 90.0
    val isDue = household?.feeStatus != "PAID"

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MobbinCanvas),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Bill Header Card (Mobbin featured card: 24px corners, canvas-soft)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MobbinCanvasSoft)
                    .border(1.dp, MobbinHairlineSoft, RoundedCornerShape(24.dp))
                    .padding(22.dp)
                    .testTag("current_bill_card")
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.billsTitle,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MobbinInk
                            )
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (isDue) TagAlertAmberBg else TagWetGreenBg)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isDue) strings.due else strings.paid,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDue) TagAlertAmber else TagWetGreen,
                                letterSpacing = 0.4.sp
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "${strings.propertyCategoryLabel}: ${household?.category ?: strings.catResidential}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MobbinTextMuted
                            )
                            Text(
                                text = strings.billsSubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MobbinTextMuted
                            )
                        }
                        Text(
                            text = "₹${fee.toInt()}",
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MobbinInk
                            )
                        )
                    }

                    if (isDue) {
                        // button-primary: Near-black ink stadium pill
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(50))
                                .background(MobbinPrimary)
                                .clickable { household?.id?.let { onPayFee(it, fee) } }
                                .padding(vertical = 14.dp)
                                .testTag("pay_upi_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = MobbinOnPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "${strings.payNow} (₹${fee.toInt()})",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MobbinOnPrimary
                                    )
                                )
                            }
                        }
                    } else {
                        // button-outline: White canvas, hairline border
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(50))
                                .background(MobbinSurface)
                                .border(1.dp, MobbinHairline, RoundedCornerShape(50))
                                .clickable { payments.firstOrNull()?.let { onViewReceipt(it) } }
                                .padding(vertical = 12.dp)
                                .testTag("download_receipt_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.Receipt,
                                    contentDescription = null,
                                    tint = MobbinInk,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = strings.downloadReceipt,
                                    style = MaterialTheme.typography.labelLarge.copy(color = MobbinInk)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = strings.transactionHistory,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MobbinInk
                )
            )
        }

        items(payments) { p ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MobbinSurface)
                    .border(1.dp, MobbinHairline, RoundedCornerShape(16.dp))
                    .clickable { onViewReceipt(p) }
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = p.monthYear,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MobbinInk
                            )
                        )
                        Text(
                            text = "${strings.upiReference}: ${p.upiRef}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MobbinTextMuted
                        )
                        Text(
                            text = p.receiptNumber,
                            style = MaterialTheme.typography.bodySmall.copy(color = MobbinAccent),
                            fontSize = 11.5.sp
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "₹${p.amount.toInt()}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MobbinInk
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(TagWetGreenBg)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = strings.paid,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TagWetGreen
                            )
                        }
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
fun HouseholdQrTagDialog(
    household: HouseholdEntity,
    onDismiss: () -> Unit
) {
    val strings = appStrings()
    // Mobbin ex-modal-card: 24px corners, white canvas, shadow-free
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MobbinCanvas,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.QrCode2, contentDescription = null, tint = MobbinInk)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    strings.householdQrCode,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MobbinInk
                    )
                )
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
                        .clip(RoundedCornerShape(16.dp))
                        .background(MobbinCanvasSoft)
                        .border(1.dp, MobbinHairline, RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.QrCode,
                            contentDescription = null,
                            modifier = Modifier.size(100.dp),
                            tint = MobbinInk
                        )
                        Text(household.qrCode, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MobbinInk)
                    }
                }
                Text(household.residentName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MobbinInk)
                Text(household.address, fontSize = 13.sp, color = MobbinTextMuted, textAlign = TextAlign.Center)
                Text(
                    text = strings.qrInstructions,
                    fontSize = 12.sp,
                    color = MobbinTextMuted,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(MobbinPrimary)
                    .clickable { onDismiss() }
                    .padding(horizontal = 24.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(strings.close, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MobbinOnPrimary)
            }
        }
    )
}

@Composable
fun PaymentReceiptDialog(
    payment: PaymentEntity,
    household: HouseholdEntity?,
    onDismiss: () -> Unit
) {
    val strings = appStrings()
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MobbinCanvas,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, tint = MobbinInk)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    strings.paymentSuccessTitle,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MobbinInk
                    )
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MobbinCanvasSoft)
                        .padding(14.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("CLEANCITY MUNICIPAL RECEIPT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MobbinTextMuted, letterSpacing = 0.5.sp)
                        Text(payment.receiptNumber, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MobbinInk)
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${strings.roleCitizen}:", style = MaterialTheme.typography.bodySmall, color = MobbinTextMuted)
                    Text(household?.residentName ?: strings.roleCitizen, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = MobbinInk))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Billing Month:", style = MaterialTheme.typography.bodySmall, color = MobbinTextMuted)
                    Text(payment.monthYear, style = MaterialTheme.typography.bodySmall, color = MobbinInk)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${strings.upiReference}:", style = MaterialTheme.typography.bodySmall, color = MobbinTextMuted)
                    Text(payment.upiRef, style = MaterialTheme.typography.bodySmall, color = MobbinAccent)
                }
                HorizontalDivider(color = MobbinHairline)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Amount Paid:", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MobbinInk))
                    Text("₹${payment.amount.toInt()}.00", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MobbinInk))
                }
            }
        },
        confirmButton = {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(MobbinPrimary)
                    .clickable { onDismiss() }
                    .padding(horizontal = 24.dp, vertical = 10.dp)
            ) {
                Text(strings.close, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MobbinOnPrimary)
            }
        }
    )
}
