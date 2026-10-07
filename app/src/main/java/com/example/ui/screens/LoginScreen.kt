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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CleanCityViewModel
import com.example.ui.UserRole
import com.example.ui.theme.*
import com.example.util.appStrings

enum class AuthMode {
    LOGIN,
    SIGNUP,
    STAFF
}

@Composable
fun LoginScreen(
    viewModel: CleanCityViewModel,
    modifier: Modifier = Modifier
) {
    val strings = appStrings()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    var showLanguageDropdown by remember { mutableStateOf(false) }

    val langDisplayCode = when {
        selectedLanguage.contains("Telugu", ignoreCase = true) || selectedLanguage.contains("తెలుగు") -> "TE"
        selectedLanguage.contains("Hindi", ignoreCase = true) || selectedLanguage.contains("हिन्दी") -> "HI"
        selectedLanguage.contains("Kannada", ignoreCase = true) || selectedLanguage.contains("ಕನ್ನಡ") -> "KN"
        selectedLanguage.contains("Tamil", ignoreCase = true) || selectedLanguage.contains("தமிழ்") -> "TA"
        else -> "EN"
    }

    var authMode by rememberSaveable { mutableStateOf(AuthMode.LOGIN) }

    // Login Form State
    var loginPhone by rememberSaveable { mutableStateOf("") }
    var loginOtp by rememberSaveable { mutableStateOf("") }
    var isLoginOtpVerified by remember { mutableStateOf(false) }

    // Signup Form State
    var signupName by rememberSaveable { mutableStateOf("") }
    var signupPhone by rememberSaveable { mutableStateOf("") }
    var signupOtp by rememberSaveable { mutableStateOf("") }
    var isSignupOtpVerified by remember { mutableStateOf(false) }

    val wardOptions = remember {
        listOf(
            "Ward 42 - Indiranagar, Bengaluru",
            "Ward 104 - Gachibowli, Hyderabad",
            "Ward 56 - Benz Circle, Vijayawada",
            "Ward 15 - Koramangala, Bengaluru",
            "Ward 88 - Tirupati Central",
            "Ward 22 - Visakhapatnam Beach Road"
        )
    }
    var selectedWard by rememberSaveable { mutableStateOf(wardOptions[0]) }
    var showWardDropdown by remember { mutableStateOf(false) }

    var signupAddress by rememberSaveable { mutableStateOf("") }

    val categoryKeys = remember {
        listOf("residential", "commercial", "hotel")
    }
    var selectedCategoryKey by rememberSaveable { mutableStateOf("residential") }

    val languageChoices = remember {
        listOf(
            "తెలుగు (Telugu)" to "Telugu",
            "English" to "English",
            "हिन्दी (Hindi)" to "Hindi",
            "ಕನ್ನಡ (Kannada)" to "Kannada",
            "தமிழ் (Tamil)" to "Tamil"
        )
    }

    // Staff Form State
    var selectedStaffRole by rememberSaveable { mutableStateOf(UserRole.COLLECTOR) }
    var staffEmployeeId by rememberSaveable { mutableStateOf("EMP-4209") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBaseBackground)
            .statusBarsPadding().navigationBarsPadding().imePadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 540.dp)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Top Language Selector Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(MobbinCanvasSoft)
                                .border(1.dp, MobbinHairline, RoundedCornerShape(50))
                                .clickable { showLanguageDropdown = true }.heightIn(min = 48.dp)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("login_screen_language_chip"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    Icons.Default.Language,
                                    contentDescription = "Language",
                                    tint = MobbinInk,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "$langDisplayCode • ${strings.languageName}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MobbinInk
                                )
                                Icon(
                                    Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = MobbinTextMuted,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showLanguageDropdown,
                            onDismissRequest = { showLanguageDropdown = false },
                            modifier = Modifier
                                .background(MobbinSurface)
                                .border(1.dp, MobbinHairline, RoundedCornerShape(16.dp))
                        ) {
                            languageChoices.forEach { (display, key) ->
                                DropdownMenuItem(
                                    text = { Text(display, color = MobbinInk, fontSize = 13.sp) },
                                    onClick = {
                                        viewModel.setLanguage(key)
                                        showLanguageDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // CleanCity Branding Emblem (30% Squircle Mobbin Signature)
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MobbinPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Recycling,
                            contentDescription = "CleanCity Emblem",
                            tint = MobbinOnPrimary,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "CleanCity.",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MobbinInk
                        )
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Smart doorstep waste collection and municipal services.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Normal,
                            color = MobbinTextMuted
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Primary Auth Mode Switcher (Mobbin segmented-control: canvas-soft track, white active pill)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(50))
                        .background(MobbinCanvasSoft)
                        .padding(4.dp)
                ) {
                    // Log In Pill
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(50))
                            .background(if (authMode == AuthMode.LOGIN) MobbinCanvas else Color.Transparent)
                            .clickable { authMode = AuthMode.LOGIN }
                            .padding(vertical = 9.dp)
                            .testTag("tab_login_mode"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = strings.loginTab,
                            fontSize = 12.5.sp,
                            fontWeight = if (authMode == AuthMode.LOGIN) FontWeight.Bold else FontWeight.Medium,
                            color = if (authMode == AuthMode.LOGIN) MobbinInk else MobbinTextMuted
                        )
                    }

                    // Sign Up Pill
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(50))
                            .background(if (authMode == AuthMode.SIGNUP) MobbinCanvas else Color.Transparent)
                            .clickable { authMode = AuthMode.SIGNUP }
                            .padding(vertical = 9.dp)
                            .testTag("tab_signup_mode"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = strings.signupTab,
                            fontSize = 12.5.sp,
                            fontWeight = if (authMode == AuthMode.SIGNUP) FontWeight.Bold else FontWeight.Medium,
                            color = if (authMode == AuthMode.SIGNUP) MobbinInk else MobbinTextMuted
                        )
                    }

                    // Staff Pill
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(50))
                            .background(if (authMode == AuthMode.STAFF) MobbinCanvas else Color.Transparent)
                            .clickable { authMode = AuthMode.STAFF }
                            .padding(vertical = 9.dp)
                            .testTag("tab_staff_mode"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = strings.staffTab,
                            fontSize = 12.5.sp,
                            fontWeight = if (authMode == AuthMode.STAFF) FontWeight.Bold else FontWeight.Medium,
                            color = if (authMode == AuthMode.STAFF) MobbinInk else MobbinTextMuted
                        )
                    }
                }
            }

            // Body Card with Smooth Transition
            item {
                AnimatedContent(
                    targetState = authMode,
                    transitionSpec = {
                        fadeIn() + slideInHorizontally { width -> if (targetState.ordinal > initialState.ordinal) width / 4 else -width / 4 } togetherWith
                                fadeOut() + slideOutHorizontally { width -> if (targetState.ordinal > initialState.ordinal) -width / 4 else width / 4 }
                    },
                    label = "AuthModeTransition"
                ) { currentMode ->
                    when (currentMode) {
                        AuthMode.LOGIN -> {
                            // Dedicated Login Card (Mobbin pricing-card: 24px corners, white canvas, hairline border)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(MobbinSurface)
                                    .border(1.dp, MobbinHairline, RoundedCornerShape(24.dp))
                                    .padding(22.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Column {
                                        Text(
                                            text = strings.citizenLoginTitle,
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = MobbinInk
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = strings.citizenLoginSubtitle,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MobbinTextMuted
                                        )
                                    }

                                    // Mobile Number
                                    OutlinedTextField(
                                        value = loginPhone,
                                        onValueChange = { loginPhone = it },
                                        label = { Text("${strings.mobileNumberLabel} (+91)", color = MobbinTextMuted) },
                                        placeholder = { Text(strings.mobileNumberPlaceholder, color = MobbinTextFaint) },
                                        leadingIcon = {
                                            Icon(Icons.Default.Phone, contentDescription = null, tint = MobbinInk)
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("input_login_phone"),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MobbinInk,
                                            unfocusedBorderColor = MobbinHairline,
                                            focusedTextColor = MobbinInk,
                                            unfocusedTextColor = MobbinInk
                                        )
                                    )

                                    // OTP Code with Simulation Helper
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            value = loginOtp,
                                            onValueChange = {
                                                loginOtp = it
                                                isLoginOtpVerified = it == "482910" || it.length == 6
                                            },
                                            label = { Text(strings.enterOtpLabel, color = MobbinTextMuted) },
                                            placeholder = { Text("482910", color = MobbinTextFaint) },
                                            leadingIcon = {
                                                Icon(Icons.Default.Lock, contentDescription = null, tint = MobbinInk)
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("input_login_otp"),
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = if (isLoginOtpVerified) TagWetGreen else MobbinInk,
                                                unfocusedBorderColor = MobbinHairline,
                                                focusedTextColor = MobbinInk,
                                                unfocusedTextColor = MobbinInk
                                            )
                                        )

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (isLoginOtpVerified) TagWetGreenBg else MobbinCanvasSoft)
                                                .border(
                                                    1.dp,
                                                    if (isLoginOtpVerified) TagWetGreen else MobbinHairline,
                                                    RoundedCornerShape(12.dp)
                                                )
                                                .clickable {
                                                    loginOtp = "482910"
                                                    isLoginOtpVerified = true
                                                }
                                                .padding(horizontal = 12.dp, vertical = 14.dp)
                                                .testTag("btn_fill_login_otp"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (isLoginOtpVerified) "OTP OK" else "Auto-Fill",
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isLoginOtpVerified) TagWetGreen else MobbinInk
                                            )
                                        }
                                    }

                                    // Primary Sign In Button (button-primary: near-black stadium pill)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(50))
                                            .background(MobbinPrimary)
                                            .clickable {
                                                viewModel.loginExistingCitizen(loginPhone.ifBlank { "9876543210" })
                                            }
                                            .padding(vertical = 14.dp)
                                            .testTag("btn_submit_login"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = strings.verifyAndLogin,
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MobbinOnPrimary
                                        )
                                    }

                                    // Quick 1-Tap Demo Resident Login (button-outline: white canvas with hairline)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(50))
                                            .background(MobbinSurface)
                                            .border(1.dp, MobbinHairline, RoundedCornerShape(50))
                                            .clickable {
                                                viewModel.loginWithDemoCitizen()
                                            }
                                            .padding(vertical = 12.dp)
                                            .testTag("btn_quick_demo_login"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = strings.quickDemoCitizen,
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MobbinInk
                                        )
                                    }

                                    // Switch to Sign Up link
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { authMode = AuthMode.SIGNUP }
                                            .padding(vertical = 6.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${strings.signupTab}? ",
                                            fontSize = 12.sp,
                                            color = MobbinTextMuted
                                        )
                                        Text(
                                            text = strings.citizenSignupTitle,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MobbinAccent
                                        )
                                    }
                                }
                            }
                        }

                        AuthMode.SIGNUP -> {
                            // Dedicated Sign Up (Registration) Card
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(MobbinSurface)
                                    .border(1.dp, MobbinHairline, RoundedCornerShape(24.dp))
                                    .padding(22.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                    Column {
                                        Text(
                                            text = strings.citizenSignupTitle,
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = MobbinInk
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = strings.citizenSignupSubtitle,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MobbinTextMuted
                                        )
                                    }

                                    // Full Name
                                    OutlinedTextField(
                                        value = signupName,
                                        onValueChange = { signupName = it },
                                        label = { Text(strings.fullNameLabel, color = MobbinTextMuted) },
                                        placeholder = { Text(strings.fullNamePlaceholder, color = MobbinTextFaint) },
                                        leadingIcon = {
                                            Icon(Icons.Default.Person, contentDescription = null, tint = MobbinInk)
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("input_signup_name"),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MobbinInk,
                                            unfocusedBorderColor = MobbinHairline,
                                            focusedTextColor = MobbinInk,
                                            unfocusedTextColor = MobbinInk
                                        )
                                    )

                                    // Mobile Number
                                    OutlinedTextField(
                                        value = signupPhone,
                                        onValueChange = { signupPhone = it },
                                        label = { Text("${strings.mobileNumberLabel} (+91)", color = MobbinTextMuted) },
                                        placeholder = { Text(strings.mobileNumberPlaceholder, color = MobbinTextFaint) },
                                        leadingIcon = {
                                            Icon(Icons.Default.Phone, contentDescription = null, tint = MobbinInk)
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("input_signup_phone"),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MobbinInk,
                                            unfocusedBorderColor = MobbinHairline,
                                            focusedTextColor = MobbinInk,
                                            unfocusedTextColor = MobbinInk
                                        )
                                    )

                                    // OTP Verification Row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            value = signupOtp,
                                            onValueChange = {
                                                signupOtp = it
                                                isSignupOtpVerified = it == "482910" || it.length == 6
                                            },
                                            label = { Text(strings.enterOtpLabel, color = MobbinTextMuted) },
                                            placeholder = { Text("482910", color = MobbinTextFaint) },
                                            leadingIcon = {
                                                Icon(Icons.Default.Lock, contentDescription = null, tint = MobbinInk)
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("input_signup_otp"),
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = if (isSignupOtpVerified) TagWetGreen else MobbinInk,
                                                unfocusedBorderColor = MobbinHairline,
                                                focusedTextColor = MobbinInk,
                                                unfocusedTextColor = MobbinInk
                                            )
                                        )

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (isSignupOtpVerified) TagWetGreenBg else MobbinCanvasSoft)
                                                .border(
                                                    1.dp,
                                                    if (isSignupOtpVerified) TagWetGreen else MobbinHairline,
                                                    RoundedCornerShape(12.dp)
                                                )
                                                .clickable {
                                                    signupOtp = "482910"
                                                    isSignupOtpVerified = true
                                                }
                                                .padding(horizontal = 12.dp, vertical = 14.dp)
                                                .testTag("btn_fill_signup_otp"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (isSignupOtpVerified) "OTP OK" else "Auto-Fill",
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSignupOtpVerified) TagWetGreen else MobbinInk
                                            )
                                        }
                                    }

                                    // Ward Selector Dropdown
                                    Box {
                                        OutlinedTextField(
                                            value = selectedWard,
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text(strings.municipalWardLabel, color = TextMuted) },
                                            leadingIcon = {
                                                Icon(Icons.Default.LocationCity, contentDescription = null, tint = PaletteCreamGold)
                                            },
                                            trailingIcon = {
                                                IconButton(onClick = { showWardDropdown = true }) {
                                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Ward", tint = PaletteCreamGold)
                                                }
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { showWardDropdown = true }
                                                .testTag("input_signup_ward"),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = MobbinPrimary,
                                                unfocusedBorderColor = MobbinHairline,
                                                focusedTextColor = MobbinInk,
                                                unfocusedTextColor = MobbinInk
                                            )
                                        )

                                        DropdownMenu(
                                            expanded = showWardDropdown,
                                            onDismissRequest = { showWardDropdown = false },
                                            modifier = Modifier.background(DarkSurfaceCard)
                                        ) {
                                            wardOptions.forEach { ward ->
                                                DropdownMenuItem(
                                                    text = { Text(ward, color = MobbinInk, fontSize = 13.sp) },
                                                    onClick = {
                                                        selectedWard = ward
                                                        showWardDropdown = false
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    // House / Street Address
                                    OutlinedTextField(
                                        value = signupAddress,
                                        onValueChange = { signupAddress = it },
                                        label = { Text(strings.streetAddressLabel, color = TextMuted) },
                                        placeholder = { Text(strings.streetAddressPlaceholder, color = TextMuted) },
                                        leadingIcon = {
                                            Icon(Icons.Default.Home, contentDescription = null, tint = MobbinPrimary)
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("input_signup_address"),
                                        singleLine = false,
                                        maxLines = 2,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MobbinPrimary,
                                            unfocusedBorderColor = MobbinHairline,
                                            focusedTextColor = MobbinInk,
                                            unfocusedTextColor = MobbinInk
                                        )
                                    )

                                    // Property Category Selector
                                    Text(
                                        text = strings.propertyCategoryLabel,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextMuted
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        categoryKeys.forEach { catKey ->
                                            val label = when (catKey) {
                                                "residential" -> strings.catResidential
                                                "commercial" -> strings.catCommercial
                                                else -> strings.catHotel
                                            }
                                            val isSelected = selectedCategoryKey == catKey
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(if (isSelected) MobbinPrimary else MobbinCanvasSoft)
                                                    .border(
                                                        1.dp,
                                                        if (isSelected) MobbinPrimary else MobbinHairline,
                                                        RoundedCornerShape(12.dp)
                                                    )
                                                    .clickable { selectedCategoryKey = catKey }
                                                    .padding(vertical = 10.dp, horizontal = 6.dp)
                                                    .testTag("cat_choice_$catKey"),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = label,
                                                    fontSize = 10.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) MobbinOnPrimary else MobbinInk,
                                                    textAlign = TextAlign.Center,
                                                    maxLines = 2
                                                )
                                            }
                                        }
                                    }

                                    // Preferred Language (With Telugu prominently featured)
                                    Text(
                                        text = strings.preferredLanguageLabel,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextMuted
                                    )

                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        items(languageChoices) { (display, key) ->
                                            val isSelected = selectedLanguage.equals(key, ignoreCase = true)
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(50))
                                                    .background(if (isSelected) MobbinPrimary else MobbinCanvasSoft)
                                                    .border(1.dp, if (isSelected) MobbinPrimary else MobbinHairline, RoundedCornerShape(50))
                                                    .clickable {
                                                        viewModel.setLanguage(key)
                                                    }
                                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                                                    .testTag("signup_lang_${key.lowercase()}"),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = display,
                                                    fontSize = 12.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) MobbinOnPrimary else MobbinTextMuted
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Create Account Button (button-primary: near-black stadium pill)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(50))
                                            .background(MobbinPrimary)
                                            .clickable {
                                                val finalName = signupName.ifBlank { "Aswini Kumar" }
                                                val finalPhone = signupPhone.ifBlank { "+91 98765 43210" }
                                                val finalAddress = signupAddress.ifBlank { "#142, 5th Main, 2nd Cross" }
                                                val categoryValue = when (selectedCategoryKey) {
                                                    "commercial" -> "Commercial Shop"
                                                    "hotel" -> "Hotel / Restaurant"
                                                    else -> "Residential Household"
                                                }

                                                viewModel.registerAndLogin(
                                                    residentName = finalName,
                                                    phone = finalPhone,
                                                    ward = selectedWard,
                                                    address = finalAddress,
                                                    category = categoryValue,
                                                    preferredLanguage = selectedLanguage
                                                )
                                            }
                                            .padding(vertical = 14.dp)
                                            .testTag("btn_submit_signup"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MobbinOnPrimary, modifier = Modifier.size(18.dp))
                                            Text(
                                                text = strings.registerAndContinue,
                                                fontSize = 13.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MobbinOnPrimary
                                            )
                                        }
                                    }

                                    // Switch to Log In link
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { authMode = AuthMode.LOGIN }
                                            .padding(vertical = 6.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${strings.loginTab}? ",
                                            fontSize = 12.sp,
                                            color = TextMuted
                                        )
                                        Text(
                                            text = strings.citizenLoginTitle,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PaletteCreamGold
                                        )
                                    }
                                }
                            }
                        }

                        AuthMode.STAFF -> {
                            // Municipal Staff Portal Card (Mobbin 24px geometry)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(MobbinSurface)
                                    .border(1.dp, MobbinHairline, RoundedCornerShape(24.dp))
                                    .padding(22.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                    Column {
                                        Text(
                                            text = strings.staffLoginTitle,
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = MobbinInk
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = strings.staffLoginSubtitle,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MobbinTextMuted
                                        )
                                    }

                                    listOf(
                                        UserRole.COLLECTOR to strings.roleCollector,
                                        UserRole.DRIVER to strings.roleDriver,
                                        UserRole.SUPERVISOR to strings.roleSupervisor,
                                        UserRole.MUNICIPAL_ADMIN to strings.roleAdmin
                                    ).forEach { (role, label) ->
                                        val isSelected = selectedStaffRole == role
                                        val sub = when (role) {
                                            UserRole.COLLECTOR -> strings.roleCollectorSubtitle
                                            UserRole.DRIVER -> strings.roleDriverSubtitle
                                            UserRole.SUPERVISOR -> strings.roleSupervisorSubtitle
                                            UserRole.MUNICIPAL_ADMIN -> strings.roleAdminSubtitle
                                            else -> ""
                                        }
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(if (isSelected) MobbinCanvasSoft else MobbinCanvas)
                                                .border(
                                                    1.dp,
                                                    if (isSelected) MobbinPrimary else MobbinHairline,
                                                    RoundedCornerShape(16.dp)
                                                )
                                                .clickable { selectedStaffRole = role }
                                                .padding(12.dp)
                                                .testTag("staff_role_${role.name.lowercase()}")
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = label,
                                                        style = MaterialTheme.typography.titleSmall.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            color = MobbinInk
                                                        )
                                                    )
                                                    Text(sub, fontSize = 11.5.sp, color = MobbinTextMuted)
                                                }
                                                if (isSelected) {
                                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MobbinPrimary, modifier = Modifier.size(18.dp))
                                                }
                                            }
                                        }
                                    }

                                    OutlinedTextField(
                                        value = staffEmployeeId,
                                        onValueChange = { staffEmployeeId = it },
                                        label = { Text(strings.employeeIdLabel, color = MobbinTextMuted) },
                                        placeholder = { Text(strings.employeeIdPlaceholder, color = MobbinTextFaint) },
                                        leadingIcon = {
                                            Icon(Icons.Default.Badge, contentDescription = null, tint = MobbinInk)
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("input_staff_id"),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MobbinInk,
                                            unfocusedBorderColor = MobbinHairline,
                                            focusedTextColor = MobbinInk,
                                            unfocusedTextColor = MobbinInk
                                        )
                                    )

                                    // button-primary for Staff
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(50))
                                            .background(MobbinPrimary)
                                            .clickable {
                                                viewModel.loginAsStaff(selectedStaffRole)
                                            }
                                            .padding(vertical = 14.dp)
                                            .testTag("btn_staff_login"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = strings.enterDashboard,
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MobbinOnPrimary
                                        )
                                    }

                                    // Switch back to Login
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { authMode = AuthMode.LOGIN }
                                            .padding(vertical = 6.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${strings.roleCitizen}? ",
                                            fontSize = 12.sp,
                                            color = TextMuted
                                        )
                                        Text(
                                            text = strings.citizenLoginTitle,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PaletteCreamGold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}
