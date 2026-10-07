package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        HouseholdEntity::class,
        PickupLogEntity::class,
        ComplaintEntity::class,
        PaymentEntity::class,
        WeighbridgeLogEntity::class,
        CrewAttendanceEntity::class,
        ViolationFineEntity::class,
        AnnouncementEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class CleanCityDatabase : RoomDatabase() {
    abstract fun cleanCityDao(): CleanCityDao

    companion object {
        @Volatile
        private var INSTANCE: CleanCityDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): CleanCityDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CleanCityDatabase::class.java,
                    "clean_city_database"
                )
                    .addCallback(CleanCityDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class CleanCityDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.cleanCityDao())
                    }
                }
            }
        }

        suspend fun populateInitialData(dao: CleanCityDao) {
            // Seed sample households in Ward 42
            val sampleHouseholds = listOf(
                HouseholdEntity(
                    id = 1,
                    qrCode = "BIN-W42-101",
                    residentName = "Ananya & Rohan Sharma",
                    phone = "+91 98765 10101",
                    address = "#14, 2nd Cross, 12th Main, Ward 42",
                    ward = "Ward 42 - Indiranagar",
                    category = "Household",
                    segregationScore = 96,
                    streakDays = 14,
                    monthlyFee = 90.0,
                    feeStatus = "DUE",
                    preferredLanguage = "English"
                ),
                HouseholdEntity(
                    id = 2,
                    qrCode = "BIN-W42-102",
                    residentName = "Dr. K. S. Venkatesh",
                    phone = "+91 98765 10102",
                    address = "#16, 2nd Cross, 12th Main, Ward 42",
                    ward = "Ward 42 - Indiranagar",
                    category = "Household",
                    segregationScore = 92,
                    streakDays = 9,
                    monthlyFee = 90.0,
                    feeStatus = "PAID",
                    preferredLanguage = "Kannada"
                ),
                HouseholdEntity(
                    id = 3,
                    qrCode = "BIN-W42-103",
                    residentName = "Green Leaf Organic Cafe",
                    phone = "+91 98765 10103",
                    address = "#22, 100ft Road Corner, Ward 42",
                    ward = "Ward 42 - Indiranagar",
                    category = "Hotel / Restaurant",
                    segregationScore = 88,
                    streakDays = 5,
                    monthlyFee = 850.0,
                    feeStatus = "PAID",
                    preferredLanguage = "English"
                ),
                HouseholdEntity(
                    id = 4,
                    qrCode = "BIN-W42-104",
                    residentName = "Gupta Provision Stores",
                    phone = "+91 98765 10104",
                    address = "#25, 2nd Cross, Ward 42",
                    ward = "Ward 42 - Indiranagar",
                    category = "Commercial Shop",
                    segregationScore = 74,
                    streakDays = 2,
                    monthlyFee = 350.0,
                    feeStatus = "OVERDUE",
                    preferredLanguage = "Hindi"
                ),
                HouseholdEntity(
                    id = 5,
                    qrCode = "BIN-W42-105",
                    residentName = "Meera & Siddharth Rao",
                    phone = "+91 98765 10105",
                    address = "#29, 3rd Cross, Ward 42",
                    ward = "Ward 42 - Indiranagar",
                    category = "Household",
                    segregationScore = 98,
                    streakDays = 21,
                    monthlyFee = 90.0,
                    feeStatus = "PAID",
                    preferredLanguage = "English"
                ),
                HouseholdEntity(
                    id = 6,
                    qrCode = "BIN-W42-106",
                    residentName = "Preeti Kulkarni",
                    phone = "+91 98765 10106",
                    address = "#34, 3rd Cross, Ward 42",
                    ward = "Ward 42 - Indiranagar",
                    category = "Household",
                    segregationScore = 82,
                    streakDays = 4,
                    monthlyFee = 90.0,
                    feeStatus = "PAID",
                    preferredLanguage = "Marathi"
                ),
                HouseholdEntity(
                    id = 7,
                    qrCode = "BIN-W42-107",
                    residentName = "Sai Krupa Bakery",
                    phone = "+91 98765 10107",
                    address = "#41, 12th Main Road, Ward 42",
                    ward = "Ward 42 - Indiranagar",
                    category = "Commercial Shop",
                    segregationScore = 90,
                    streakDays = 12,
                    monthlyFee = 350.0,
                    feeStatus = "PAID",
                    preferredLanguage = "Kannada"
                )
            )
            dao.insertHouseholds(sampleHouseholds)

            // Seed sample past payments
            val samplePayments = listOf(
                PaymentEntity(
                    householdId = 1,
                    monthYear = "August 2026",
                    amount = 90.0,
                    status = "PAID",
                    upiRef = "UPI/260814982103",
                    paidAt = System.currentTimeMillis() - 2592000000L,
                    receiptNumber = "CC-REC-2026-0814"
                ),
                PaymentEntity(
                    householdId = 1,
                    monthYear = "July 2026",
                    amount = 90.0,
                    status = "PAID",
                    upiRef = "UPI/260714981123",
                    paidAt = System.currentTimeMillis() - (2592000000L * 2),
                    receiptNumber = "CC-REC-2026-0714"
                )
            )
            samplePayments.forEach { dao.insertPayment(it) }

            // Seed sample active complaints
            val now = System.currentTimeMillis()
            val sampleComplaints = listOf(
                ComplaintEntity(
                    id = 1,
                    title = "Secondary Bin Overflowing near 2nd Cross Park",
                    category = "Overflowing Bin",
                    description = "Community green bin is overflowing and stray animals are scattering wet waste onto the footpath.",
                    ward = "Ward 42 - Indiranagar",
                    address = "Near BDA Park Gate, 2nd Cross, Ward 42",
                    latitude = 12.9784,
                    longitude = 77.6408,
                    status = "PENDING",
                    createdAt = now - (6 * 3600000L), // 6 hours ago
                    slaDeadlineMillis = now + (18 * 3600000L), // 18 hours remaining
                    assignedInspector = "Inspector R. Sharma (Ward 42)"
                ),
                ComplaintEntity(
                    id = 2,
                    title = "Construction Debris Dumped Overnight",
                    category = "Illegal Dumping",
                    description = "A mini truck dumped concrete rubble and broken tiles on the vacant plot beside #45.",
                    ward = "Ward 42 - Indiranagar",
                    address = "Plot 45B, 10th Main, Ward 42",
                    latitude = 12.9792,
                    longitude = 77.6425,
                    status = "IN_PROGRESS",
                    createdAt = now - (19 * 3600000L),
                    slaDeadlineMillis = now + (5 * 3600000L), // 5 hours remaining (urgent)
                    assignedInspector = "Insp. R. Sharma (Ward 42)"
                ),
                ComplaintEntity(
                    id = 3,
                    title = "Missed Morning Door-to-Door Pickup on Monday",
                    category = "Missed Pickup",
                    description = "Collector cart did not arrive at odd numbered houses between 7 AM and 9 AM.",
                    ward = "Ward 42 - Indiranagar",
                    address = "3rd Cross Odd Sides, Ward 42",
                    latitude = 12.9770,
                    longitude = 77.6390,
                    status = "RESOLVED",
                    createdAt = now - (48 * 3600000L),
                    slaDeadlineMillis = now - (24 * 3600000L),
                    resolvedAt = now - (22 * 3600000L),
                    assignedInspector = "Insp. R. Sharma (Ward 42)",
                    resolutionProofPhoto = "proof_resolved_pickup"
                )
            )
            sampleComplaints.forEach { dao.insertComplaint(it) }

            // Seed crew attendance
            val sampleCrew = listOf(
                CrewAttendanceEntity(
                    id = 1,
                    workerName = "Ramesh Kumar",
                    role = "Collector",
                    ward = "Ward 42",
                    date = "Today",
                    status = "ON_ROUTE",
                    checkInTime = "06:30 AM",
                    ppeChecked = true
                ),
                CrewAttendanceEntity(
                    id = 2,
                    workerName = "Suresh M.",
                    role = "Collector",
                    ward = "Ward 42",
                    date = "Today",
                    status = "ON_ROUTE",
                    checkInTime = "06:35 AM",
                    ppeChecked = true
                ),
                CrewAttendanceEntity(
                    id = 3,
                    workerName = "Kavitha Bai",
                    role = "Helper / Sorter",
                    ward = "Ward 42",
                    date = "Today",
                    status = "PRESENT",
                    checkInTime = "06:45 AM",
                    ppeChecked = true
                ),
                CrewAttendanceEntity(
                    id = 4,
                    workerName = "Manjunath (Truck Driver)",
                    role = "Driver",
                    ward = "Ward 42",
                    date = "Today",
                    status = "ON_ROUTE",
                    checkInTime = "06:15 AM",
                    ppeChecked = true
                )
            )
            dao.insertCrewAttendance(sampleCrew)

            // Seed weighbridge logs
            val sampleWeighbridge = listOf(
                WeighbridgeLogEntity(
                    id = 1,
                    vehicleNumber = "KA-04-EA-2041",
                    driverName = "Manjunath",
                    wasteType = "Wet Waste (Organic)",
                    grossWeightKg = 8450.0,
                    tareWeightKg = 4200.0,
                    netWeightKg = 4250.0,
                    treatmentPlant = "Biomethanation & Composting Plant #4",
                    timestamp = now - (2 * 3600000L)
                ),
                WeighbridgeLogEntity(
                    id = 2,
                    vehicleNumber = "KA-04-EA-1988",
                    driverName = "Gopal Yadav",
                    wasteType = "Dry Recyclable",
                    grossWeightKg = 6100.0,
                    tareWeightKg = 3900.0,
                    netWeightKg = 2200.0,
                    treatmentPlant = "Dry Waste Collection Centre (DWCC) East",
                    timestamp = now - (5 * 3600000L)
                )
            )
            sampleWeighbridge.forEach { dao.insertWeighbridgeLog(it) }

            // Seed announcements
            val sampleAnnouncements = listOf(
                AnnouncementEntity(
                    id = 1,
                    title = "Plastic Ban & 100% Segregation Drive",
                    content = "Strict enforcement of 2-bin segregation begins this week. Wet waste in Green bins, Clean dry recyclables in Blue bags. Commercial violators face spot fines.",
                    category = "Compost Campaign",
                    date = "Sep 28, 2026",
                    isUrgent = false
                ),
                AnnouncementEntity(
                    id = 2,
                    title = "Monsoon Waste Collection Alert",
                    content = "Due to heavy morning rain forecasts, collection timing may extend up to 10:30 AM in Low-lying lanes. Keep waste covered to prevent soaking.",
                    category = "Weather Alert",
                    date = "Today",
                    isUrgent = true
                ),
                AnnouncementEntity(
                    id = 3,
                    title = "Free Home Composting Kit Workshop",
                    content = "Join the BBMP Ward 42 resident welfare workshop this Saturday 10 AM at Community Hall. Learn aerated pipe composting and get 50% subsidized bins.",
                    category = "Advisory",
                    date = "Upcoming Saturday",
                    isUrgent = false
                )
            )
            dao.insertAnnouncements(sampleAnnouncements)

            // Seed initial violations
            val sampleViolations = listOf(
                ViolationFineEntity(
                    id = 1,
                    ward = "Ward 42",
                    location = "100ft Road Market",
                    violatorName = "Star Fast Food",
                    violationType = "Commercial Mixed Waste",
                    fineAmount = 1500.0,
                    status = "PAID",
                    timestamp = now - (24 * 3600000L)
                ),
                ViolationFineEntity(
                    id = 2,
                    ward = "Ward 42",
                    location = "80ft Road Junction",
                    violatorName = "Anonymous Truck #KA03-7712",
                    violationType = "Illegal Night Dumping",
                    fineAmount = 5000.0,
                    status = "ISSUED",
                    timestamp = now - (12 * 3600000L)
                )
            )
            sampleViolations.forEach { dao.insertViolation(it) }
        }
    }
}
