package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.widthIn
import com.example.ui.CleanCityViewModel
import com.example.ui.UserRole
import com.example.ui.components.RoleHeaderBar
import com.example.ui.screens.*
import com.example.ui.theme.CleanCityTheme
import com.example.ui.theme.DarkBaseBackground
import com.example.util.LocalAppStrings
import com.example.util.getAppStrings

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CleanCityTheme {
                CleanCityApp()
            }
        }
    }
}

@Composable
fun CleanCityApp(viewModel: CleanCityViewModel = viewModel()) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val registeredHousehold by viewModel.registeredHousehold.collectAsState()
    val currentRole by viewModel.currentRole.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val statusBanner by viewModel.statusBanner.collectAsState()

    val currentStrings = remember(selectedLanguage) { getAppStrings(selectedLanguage) }

    val households by viewModel.households.collectAsState()
    val pickupLogs by viewModel.allPickupLogs.collectAsState()
    val complaints by viewModel.allComplaints.collectAsState()
    val payments by viewModel.allPayments.collectAsState()
    val weighbridgeLogs by viewModel.weighbridgeLogs.collectAsState()
    val crew by viewModel.crewAttendance.collectAsState()
    val violations by viewModel.violations.collectAsState()
    val announcements by viewModel.announcements.collectAsState()

    val currentHousehold = registeredHousehold ?: households.firstOrNull()

    // Handle back button to return to Citizen perspective if in another role
    if (isLoggedIn && currentRole != UserRole.CITIZEN) {
        BackHandler {
            viewModel.setRole(UserRole.CITIZEN)
        }
    }

    CompositionLocalProvider(LocalAppStrings provides currentStrings) {
        AnimatedContent(
            targetState = isLoggedIn,
            transitionSpec = {
                fadeIn() + slideInVertically { height -> height / 6 } togetherWith
                        fadeOut() + slideOutVertically { height -> -height / 6 }
            },
            label = "AuthSessionTransition"
        ) { loggedInState ->
            if (!loggedInState) {
                LoginScreen(viewModel = viewModel)
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DarkBaseBackground)
                ) {
                    RoleHeaderBar(
                        currentRole = currentRole,
                        selectedLanguage = selectedLanguage,
                        onRoleSelected = { viewModel.setRole(it) },
                        onLanguageSelected = { viewModel.setLanguage(it) },
                        statusBanner = statusBanner,
                        onDismissBanner = { viewModel.clearStatusBanner() },
                        residentName = currentHousehold?.residentName,
                        onLogout = { viewModel.logout() }
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .widthIn(max = 640.dp)
                        ) {
                            AnimatedContent(
                                targetState = currentRole,
                                transitionSpec = {
                                    fadeIn() + slideInVertically { height -> height / 6 } togetherWith
                                            fadeOut() + slideOutVertically { height -> -height / 6 }
                                },
                                label = "RoleScreenTransition"
                            ) { targetRole ->
                                when (targetRole) {
                                    UserRole.CITIZEN -> CitizenScreen(
                                        viewModel = viewModel,
                                        household = currentHousehold,
                                        announcements = announcements,
                                        complaints = complaints,
                                        payments = payments
                                    )
                                    UserRole.COLLECTOR -> CollectorScreen(
                                        viewModel = viewModel,
                                        households = households,
                                        pickupLogs = pickupLogs
                                    )
                                    UserRole.DRIVER -> DriverScreen(
                                        viewModel = viewModel,
                                        weighbridgeLogs = weighbridgeLogs
                                    )
                                    UserRole.SUPERVISOR -> SupervisorScreen(
                                        viewModel = viewModel,
                                        complaints = complaints,
                                        crew = crew
                                    )
                                    UserRole.MUNICIPAL_ADMIN -> AdminScreen(
                                        viewModel = viewModel,
                                        households = households,
                                        violations = violations
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
