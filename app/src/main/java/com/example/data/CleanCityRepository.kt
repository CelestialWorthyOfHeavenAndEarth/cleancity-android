package com.example.data

import kotlinx.coroutines.flow.Flow
import java.util.UUID

class CleanCityRepository(private val dao: CleanCityDao) {

    val allHouseholds: Flow<List<HouseholdEntity>> = dao.getAllHouseholds()
    val allPickupLogs: Flow<List<PickupLogEntity>> = dao.getAllPickupLogs()
    val allComplaints: Flow<List<ComplaintEntity>> = dao.getAllComplaints()
    val activeComplaints: Flow<List<ComplaintEntity>> = dao.getActiveComplaints()
    val allPayments: Flow<List<PaymentEntity>> = dao.getAllPayments()
    val allWeighbridgeLogs: Flow<List<WeighbridgeLogEntity>> = dao.getAllWeighbridgeLogs()
    val crewAttendance: Flow<List<CrewAttendanceEntity>> = dao.getCrewAttendance()
    val violations: Flow<List<ViolationFineEntity>> = dao.getAllViolations()
    val announcements: Flow<List<AnnouncementEntity>> = dao.getAllAnnouncements()

    fun getHousehold(id: Int): Flow<HouseholdEntity?> = dao.getHouseholdById(id)
    fun getPayments(householdId: Int): Flow<List<PaymentEntity>> = dao.getPaymentsForHousehold(householdId)
    fun getPickupLogs(householdId: Int): Flow<List<PickupLogEntity>> = dao.getLogsForHousehold(householdId)

    suspend fun registerOrUpdateHousehold(household: HouseholdEntity): Long {
        return dao.insertHousehold(household)
    }

    suspend fun findHouseholdByQr(qrCode: String): HouseholdEntity? = dao.getHouseholdByQrCode(qrCode)

    suspend fun recordPickup(
        householdId: Int,
        address: String,
        residentName: String,
        collectorName: String,
        status: String,
        quality: String,
        reasonNotCollected: String? = null,
        notes: String? = null
    ): Long {
        val log = PickupLogEntity(
            householdId = householdId,
            householdAddress = address,
            residentName = residentName,
            collectorName = collectorName,
            status = status,
            segregationQuality = quality,
            reasonNotCollected = reasonNotCollected,
            notes = notes,
            timestamp = System.currentTimeMillis()
        )
        val id = dao.insertPickupLog(log)

        if (status == "COLLECTED") {
            if (quality == "GOOD") {
                dao.recordGoodSegregation(householdId)
            } else if (quality == "MIXED") {
                dao.recordMixedSegregation(householdId)
            }
        }
        return id
    }

    suspend fun submitComplaint(
        title: String,
        category: String,
        description: String,
        ward: String,
        address: String,
        latitude: Double,
        longitude: Double,
        photoUri: String? = null,
        citizenPhone: String = "+91 98765 43210"
    ): Long {
        val complaint = ComplaintEntity(
            title = title,
            category = category,
            description = description,
            ward = ward,
            address = address,
            latitude = latitude,
            longitude = longitude,
            photoUri = photoUri,
            status = "PENDING",
            createdAt = System.currentTimeMillis(),
            slaDeadlineMillis = System.currentTimeMillis() + 86400000L, // 24 hour SLA
            assignedInspector = "Inspector R. Sharma (Ward 42)",
            citizenPhone = citizenPhone
        )
        return dao.insertComplaint(complaint)
    }

    suspend fun updateWasteReportStatus(id: Int, status: String) {
        dao.updateStatus(id, status)
    }

    suspend fun resolveComplaint(complaintId: Int, proofPhoto: String? = null) {
        dao.resolveComplaint(complaintId, System.currentTimeMillis(), proofPhoto)
    }

    suspend fun escalateComplaint(complaintId: Int) {
        dao.escalateComplaint(complaintId)
    }

    suspend fun payMonthlyFee(
        householdId: Int,
        monthYear: String,
        amount: Double,
        upiRef: String
    ): Long {
        val receiptNumber = "CC-REC-" + UUID.randomUUID().toString().take(8).uppercase()
        val payment = PaymentEntity(
            householdId = householdId,
            monthYear = monthYear,
            amount = amount,
            status = "PAID",
            upiRef = upiRef,
            paidAt = System.currentTimeMillis(),
            receiptNumber = receiptNumber
        )
        val id = dao.insertPayment(payment)
        dao.markFeePaid(householdId)
        return id
    }

    suspend fun logWeighbridge(
        vehicleNumber: String,
        driverName: String,
        wasteType: String,
        grossKg: Double,
        tareKg: Double,
        plant: String
    ): Long {
        val netKg = (grossKg - tareKg).coerceAtLeast(0.0)
        val log = WeighbridgeLogEntity(
            vehicleNumber = vehicleNumber,
            driverName = driverName,
            wasteType = wasteType,
            grossWeightKg = grossKg,
            tareWeightKg = tareKg,
            netWeightKg = netKg,
            treatmentPlant = plant,
            timestamp = System.currentTimeMillis()
        )
        return dao.insertWeighbridgeLog(log)
    }

    suspend fun updateCrewMember(member: CrewAttendanceEntity) {
        dao.updateCrewMember(member)
    }

    suspend fun issueViolation(
        ward: String,
        location: String,
        violatorName: String,
        violationType: String,
        fineAmount: Double
    ): Long {
        val violation = ViolationFineEntity(
            ward = ward,
            location = location,
            violatorName = violatorName,
            violationType = violationType,
            fineAmount = fineAmount,
            status = "ISSUED",
            timestamp = System.currentTimeMillis()
        )
        return dao.insertViolation(violation)
    }

    suspend fun updateHousehold(household: HouseholdEntity) {
        dao.updateHousehold(household)
    }
}
