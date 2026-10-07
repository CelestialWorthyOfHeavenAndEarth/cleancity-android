package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.UserRole
import com.example.ui.theme.*
import com.example.util.appStrings

@Composable
fun RoleHeaderBar(
    currentRole: UserRole,
    selectedLanguage: String,
    onRoleSelected: (UserRole) -> Unit,
    onLanguageSelected: (String) -> Unit,
    statusBanner: String?,
    onDismissBanner: () -> Unit,
    residentName: String? = null,
    onLogout: (() -> Unit)? = null
) {
    val strings = appStrings()
    var showRoleMenu by remember { mutableStateOf(false) }
    var showLanguageMenu by remember { mutableStateOf(false) }

    val langDisplayCode = when {
        selectedLanguage.contains("Telugu", ignoreCase = true) || selectedLanguage.contains("తెలుగు") -> "TE"
        selectedLanguage.contains("Hindi", ignoreCase = true) || selectedLanguage.contains("हिन्दी") -> "HI"
        selectedLanguage.contains("Kannada", ignoreCase = true) || selectedLanguage.contains("ಕನ್ನಡ") -> "KN"
        selectedLanguage.contains("Tamil", ignoreCase = true) || selectedLanguage.contains("தமிழ்") -> "TA"
        else -> "EN"
    }

    val currentRoleName = when (currentRole) {
        UserRole.CITIZEN -> strings.roleCitizen
        UserRole.COLLECTOR -> strings.roleCollector
        UserRole.DRIVER -> strings.roleDriver
        UserRole.SUPERVISOR -> strings.roleSupervisor
        UserRole.MUNICIPAL_ADMIN -> strings.roleAdmin
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MobbinCanvas)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mobbin Branding: 30% squircle icon tile + Saans bold wordmark with terminal period
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                // 30% Squircle Icon Tile (Mobbin Signature)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(MobbinPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Recycling,
                        contentDescription = "CleanCity Emblem",
                        tint = MobbinOnPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(modifier = Modifier.widthIn(max = 160.dp)) {
                    Text(
                        text = "CleanCity.",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MobbinInk
                        ),
                        maxLines = 1
                    )
                    Text(
                        text = "Ward 42 • Indiranagar.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MobbinTextMuted
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Right Controls: Stadium-pill controls on canvas-soft tint
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Language Pill (button-pill-soft)
                Box {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(MobbinCanvasSoft)
                            .clickable { showLanguageMenu = true }.heightIn(min = 48.dp)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("language_selector_chip"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Language,
                                contentDescription = "Language",
                                tint = MobbinInk,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = langDisplayCode,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MobbinInk
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showLanguageMenu,
                        onDismissRequest = { showLanguageMenu = false },
                        modifier = Modifier
                            .background(MobbinCanvas)
                            .border(1.dp, MobbinHairline, RoundedCornerShape(16.dp))
                    ) {
                        listOf(
                            "English" to "English",
                            "తెలుగు (Telugu)" to "Telugu",
                            "हिन्दी (Hindi)" to "Hindi",
                            "ಕನ್ನಡ (Kannada)" to "Kannada",
                            "தமிழ் (Tamil)" to "Tamil"
                        ).forEach { (display, key) ->
                            DropdownMenuItem(
                                text = { Text(display, color = MobbinInk, fontSize = 13.sp) },
                                onClick = {
                                    onLanguageSelected(key)
                                    showLanguageMenu = false
                                }
                            )
                        }
                    }
                }

                // Role Switcher Pill (button-pill-soft)
                Box {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(MobbinCanvasSoft)
                            .clickable { showRoleMenu = true }.heightIn(min = 48.dp)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("role_switcher_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val roleIcon = when (currentRole) {
                                UserRole.CITIZEN -> Icons.Default.Person
                                UserRole.COLLECTOR -> Icons.Default.Recycling
                                UserRole.DRIVER -> Icons.Default.LocalShipping
                                UserRole.SUPERVISOR -> Icons.Default.SupervisorAccount
                                UserRole.MUNICIPAL_ADMIN -> Icons.Default.AdminPanelSettings
                            }
                            Icon(
                                roleIcon,
                                contentDescription = null,
                                tint = MobbinInk,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = currentRoleName,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MobbinInk,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.widthIn(max = 76.dp)
                            )
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                contentDescription = "Switch",
                                tint = MobbinTextMuted,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showRoleMenu,
                        onDismissRequest = { showRoleMenu = false },
                        modifier = Modifier
                            .background(MobbinCanvas)
                            .border(1.dp, MobbinHairline, RoundedCornerShape(16.dp))
                    ) {
                        Text(
                            text = strings.switchPerspective,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MobbinTextMuted,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        UserRole.values().forEach { role ->
                            val rName = when (role) {
                                UserRole.CITIZEN -> strings.roleCitizen
                                UserRole.COLLECTOR -> strings.roleCollector
                                UserRole.DRIVER -> strings.roleDriver
                                UserRole.SUPERVISOR -> strings.roleSupervisor
                                UserRole.MUNICIPAL_ADMIN -> strings.roleAdmin
                            }
                            val rSub = when (role) {
                                UserRole.CITIZEN -> strings.roleCitizenSubtitle
                                UserRole.COLLECTOR -> strings.roleCollectorSubtitle
                                UserRole.DRIVER -> strings.roleDriverSubtitle
                                UserRole.SUPERVISOR -> strings.roleSupervisorSubtitle
                                UserRole.MUNICIPAL_ADMIN -> strings.roleAdminSubtitle
                            }
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = rName,
                                            fontWeight = if (role == currentRole) FontWeight.Bold else FontWeight.Medium,
                                            color = if (role == currentRole) MobbinAccent else MobbinInk,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = rSub,
                                            fontSize = 11.sp,
                                            color = MobbinTextMuted
                                        )
                                    }
                                },
                                leadingIcon = {
                                    if (role == currentRole) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = MobbinAccent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                },
                                onClick = {
                                    onRoleSelected(role)
                                    showRoleMenu = false
                                }
                            )
                        }
                    }
                }

                // Profile / Logout Button (button-outline)
                if (onLogout != null) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MobbinCanvas)
                            .border(1.dp, MobbinHairline, CircleShape)
                            .clickable { onLogout() }
                            .padding(7.dp)
                            .testTag("btn_logout"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = strings.logout,
                            tint = MobbinInk,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }

        // 1px Hairline divider
        HorizontalDivider(
            color = MobbinHairlineSoft,
            thickness = 1.dp
        )

        // Animated Status / Alert Banner
        AnimatedVisibility(
            visible = statusBanner != null,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            if (statusBanner != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MobbinCanvasSoft)
                        .border(1.dp, MobbinHairline, RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(MobbinAccent)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = statusBanner,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MobbinInk,
                                    fontWeight = FontWeight.Medium
                                ),
                                fontSize = 12.5.sp
                            )
                        }
                        IconButton(
                            onClick = onDismissBanner,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = MobbinTextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
