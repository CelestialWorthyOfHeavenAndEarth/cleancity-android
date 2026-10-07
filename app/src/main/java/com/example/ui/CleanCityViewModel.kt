package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.ChatMessage
import com.example.ai.GeminiService
import com.example.ai.MunicipalStats
import com.example.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class UserRole(val label: String, val badge: String, val subtitle: String) {
    CITIZEN("Citizen", "Citizen", "Household pickup, reporting, bills and assistant"),
    COLLECTOR("Collector", "Collector", "Daily door-to-door route and verification"),
    DRIVER("Driver", "Driver", "Bulk container pickup and weighbridge log"),
    SUPERVISOR("Supervisor", "Supervisor", "Live crew tracking and verification"),
    MUNICIPAL_ADMIN("Municipal Admin", "Admin", "Service benchmarks and municipal reports")
}

class CleanCityViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CleanCityRepository
    private val geminiService = GeminiService()

    val currentRole = MutableStateFlow(UserRole.CITIZEN)
    val selectedLanguage = MutableStateFlow("English")
    val selectedHouseholdId = MutableStateFlow(1)
    val isLoggedIn = MutableStateFlow(false)
    val registeredHousehold = MutableStateFlow<HouseholdEntity?>(null)

    // State flows from Room
    val households: StateFlow<List<HouseholdEntity>>
    val allPickupLogs: StateFlow<List<PickupLogEntity>>
    val allComplaints: StateFlow<List<ComplaintEntity>>
    val activeComplaints: StateFlow<List<ComplaintEntity>>
    val allPayments: StateFlow<List<PaymentEntity>>
    val weighbridgeLogs: StateFlow<List<WeighbridgeLogEntity>>
    val crewAttendance: StateFlow<List<CrewAttendanceEntity>>
    val violations: StateFlow<List<ViolationFineEntity>>
    val announcements: StateFlow<List<AnnouncementEntity>>

    // AI Chat state
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage("model", "Hello, I am CleanCity Assistant. You can ask me questions about door-to-door schedules, waste segregation guidelines, or how to report uncollected trash.")
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    // Municipal AI Executive Summary
    private val _aiSummary = MutableStateFlow<String?>(null)
    val aiSummary: StateFlow<String?> = _aiSummary.asStateFlow()

    private val _isSummaryLoading = MutableStateFlow(false)
    val isSummaryLoading: StateFlow<Boolean> = _isSummaryLoading.asStateFlow()

    // Active status message for user feedback
    private val _statusBanner = MutableStateFlow<String?>(null)
    val statusBanner: StateFlow<String?> = _statusBanner.asStateFlow()

    init {
        val database = CleanCityDatabase.getDatabase(application, viewModelScope)
        repository = CleanCityRepository(database.cleanCityDao())

        households = repository.allHouseholds.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        allPickupLogs = repository.allPickupLogs.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        allComplaints = repository.allComplaints.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        activeComplaints = repository.activeComplaints.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        allPayments = repository.allPayments.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        weighbridgeLogs = repository.allWeighbridgeLogs.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        crewAttendance = repository.crewAttendance.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        violations = repository.violations.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        announcements = repository.announcements.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
    }

    fun setRole(role: UserRole) {
        currentRole.value = role
    }

    fun setLanguage(lang: String) {
        selectedLanguage.value = lang
    }

    fun clearStatusBanner() {
        _statusBanner.value = null
    }

    // --- Authentication & Registration ---
    fun registerAndLogin(
        residentName: String,
        phone: String,
        ward: String,
        address: String,
        category: String,
        preferredLanguage: String
    ) {
        viewModelScope.launch {
            val qrCode = "QR-CC-${System.currentTimeMillis().toString().takeLast(6)}"
            val fee = when (category) {
                "Commercial Shop" -> 350.0
                "Hotel / Restaurant" -> 850.0
                else -> 90.0
            }
            val entity = HouseholdEntity(
                qrCode = qrCode,
                residentName = residentName,
                phone = phone,
                address = address,
                ward = ward,
                category = category,
                segregationScore = 95,
                streakDays = 1,
                monthlyFee = fee,
                feeStatus = "PAID",
                preferredLanguage = preferredLanguage
            )
            val insertedId = repository.registerOrUpdateHousehold(entity)
            val savedEntity = entity.copy(id = insertedId.toInt())
            registeredHousehold.value = savedEntity
            selectedHouseholdId.value = insertedId.toInt()
            selectedLanguage.value = preferredLanguage
            currentRole.value = UserRole.CITIZEN
            isLoggedIn.value = true
            _statusBanner.value = "Welcome to CleanCity, $residentName!"
        }
    }

    fun loginWithDemoCitizen() {
        isLoggedIn.value = true
        currentRole.value = UserRole.CITIZEN
        _statusBanner.value = "Logged in as Ward 42 resident"
    }

    fun loginExistingCitizen(phone: String) {
        viewModelScope.launch {
            val cleanPhone = phone.filter { it.isDigit() }.takeLast(10)
            val found = households.value.firstOrNull { h ->
                val hPhone = h.phone.filter { it.isDigit() }.takeLast(10)
                cleanPhone.isNotEmpty() && hPhone == cleanPhone
            } ?: households.value.firstOrNull()

            if (found != null) {
                registeredHousehold.value = found
                selectedHouseholdId.value = found.id
                selectedLanguage.value = found.preferredLanguage
            }
            currentRole.value = UserRole.CITIZEN
            isLoggedIn.value = true
            _statusBanner.value = "Welcome back, ${found?.residentName ?: "Citizen"}!"
        }
    }

    fun loginAsStaff(role: UserRole) {
        currentRole.value = role
        isLoggedIn.value = true
        _statusBanner.value = "Active duty console: ${role.label}"
    }

    fun logout() {
        isLoggedIn.value = false
        currentRole.value = UserRole.CITIZEN
        _statusBanner.value = "Signed out of CleanCity"
    }

    // --- Report Waste Feature ---
    fun submitWasteReport(
        title: String,
        category: String,
        description: String,
        address: String,
        latitude: Double,
        longitude: Double,
        photoPath: String?
    ) {
        viewModelScope.launch {
            repository.submitComplaint(
                title = title.ifBlank { "Uncollected waste at $address" },
                category = category,
                description = description.ifBlank { "Uncollected waste reported with photo and GPS location." },
                ward = "Ward 42 - Indiranagar",
                address = address,
                latitude = latitude,
                longitude = longitude,
                photoUri = photoPath
            )
            _statusBanner.value = "Waste report submitted successfully. Assigned to Ward Inspector."
        }
    }

    fun markWasteReportResolved(id: Int) {
        viewModelScope.launch {
            repository.resolveComplaint(id, "resolved_by_user")
            _statusBanner.value = "Report #$id marked as resolved."
        }
    }

    // --- AI Chatbot ---
    fun sendChatMessage(userText: String) {
        if (userText.isBlank()) return
        val currentList = _chatMessages.value.toMutableList()
        val userMsg = ChatMessage("user", userText)
        currentList.add(userMsg)
        _chatMessages.value = currentList

        viewModelScope.launch {
            _isChatLoading.value = true
            try {
                val replyText = geminiService.chatAssistant(
                    userMessage = userText,
                    language = selectedLanguage.value,
                    history = currentList
                )
                val updated = _chatMessages.value.toMutableList()
                updated.add(ChatMessage("model", replyText))
                _chatMessages.value = updated
            } finally {
                _isChatLoading.value = false
            }
        }
    }

    fun payMonthlyFee(householdId: Int, amount: Double) {
        viewModelScope.launch {
            val upiRef = "UPI/" + System.currentTimeMillis().toString().takeLast(10)
            repository.payMonthlyFee(
                householdId = householdId,
                monthYear = "October 2026",
                amount = amount,
                upiRef = upiRef
            )
            _statusBanner.value = "Payment of Rs ${amount.toInt()} via UPI successful. Receipt generated."
        }
    }

    // --- Collector Actions ---
    fun markCollection(
        household: HouseholdEntity,
        status: String,
        quality: String,
        reason: String? = null
    ) {
        viewModelScope.launch {
            repository.recordPickup(
                householdId = household.id,
                address = household.address,
                residentName = household.residentName,
                collectorName = "Ramesh Kumar (Route 4)",
                status = status,
                quality = quality,
                reasonNotCollected = reason
            )
            val msg = when (status) {
                "COLLECTED" -> "Recorded collection for ${household.residentName} (Segregation: $quality)"
                "MIXED_WASTE" -> "Flagged mixed waste for ${household.residentName}"
                else -> "Marked uncollected: $reason for ${household.residentName}"
            }
            _statusBanner.value = msg
        }
    }

    fun simulateQrScan(qrCode: String): HouseholdEntity? {
        val found = households.value.firstOrNull { it.qrCode.equals(qrCode.trim(), ignoreCase = true) }
        if (found != null) {
            _statusBanner.value = "Verified QR Tag: ${found.address} (${found.residentName})"
        } else {
            _statusBanner.value = "QR tag not recognized in Ward 42 database"
        }
        return found
    }

    // --- Driver Actions ---
    fun submitWeighbridgeEntry(
        vehicleNo: String,
        driver: String,
        wasteType: String,
        grossKg: Double,
        tareKg: Double,
        plant: String
    ) {
        viewModelScope.launch {
            repository.logWeighbridge(
                vehicleNumber = vehicleNo,
                driverName = driver,
                wasteType = wasteType,
                grossKg = grossKg,
                tareKg = tareKg,
                plant = plant
            )
            val netTon = (grossKg - tareKg) / 1000.0
            _statusBanner.value = "Weigh slip recorded: ${String.format("%.2f", netTon)} MT of $wasteType logged at $plant"
        }
    }

    // --- Supervisor Actions ---
    fun resolveComplaint(complaintId: Int, proofPhoto: String? = "inspector_field_photo_ok") {
        viewModelScope.launch {
            repository.resolveComplaint(complaintId, proofPhoto)
            _statusBanner.value = "Report #$complaintId verified and marked resolved."
        }
    }

    fun escalateComplaint(complaintId: Int) {
        viewModelScope.launch {
            repository.escalateComplaint(complaintId)
            _statusBanner.value = "Report #$complaintId escalated to Joint Commissioner."
        }
    }

    fun toggleCrewPpe(member: CrewAttendanceEntity) {
        viewModelScope.launch {
            repository.updateCrewMember(member.copy(ppeChecked = !member.ppeChecked))
        }
    }

    // --- Admin Actions ---
    fun generateExecutiveSummary() {
        viewModelScope.launch {
            _isSummaryLoading.value = true
            try {
                val totalH = households.value.size
                val pickups = allPickupLogs.value
                val todayCollected = pickups.count { it.status == "COLLECTED" }
                val goodSegregation = pickups.count { it.segregationQuality == "GOOD" }
                val segregationRate = if (pickups.isNotEmpty()) (goodSegregation.toDouble() / pickups.size) * 100.0 else 86.5
                val coverage = if (totalH > 0) (todayCollected.toDouble() / totalH) * 100.0 else 94.0

                val stats = MunicipalStats(
                    totalHouseholds = totalH.coerceAtLeast(1250),
                    collectedToday = todayCollected.coerceAtLeast(1180),
                    coveragePercent = coverage.coerceAtLeast(94.4),
                    segregationRate = segregationRate.coerceAtLeast(85.0),
                    wasteRecoveredTons = 48.6,
                    costRecoveryPercent = 88.4,
                    openComplaints = activeComplaints.value.size,
                    resolvedComplaints = allComplaints.value.count { it.status == "RESOLVED" }
                )
                val summary = geminiService.generateExecutiveSummary(stats)
                _aiSummary.value = summary
            } finally {
                _isSummaryLoading.value = false
            }
        }
    }

    fun issueSpotFine(violator: String, location: String, type: String, amount: Double) {
        viewModelScope.launch {
            repository.issueViolation(
                ward = "Ward 42",
                location = location,
                violatorName = violator,
                violationType = type,
                fineAmount = amount
            )
            _statusBanner.value = "Fine of Rs ${amount.toInt()} issued to $violator for $type."
        }
    }
}
