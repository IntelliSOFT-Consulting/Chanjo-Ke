package com.intellisoft.chanjoke.fhir.data

data class DbVaccineData(
    val vaccineName: String,
    val vaccineDosage: String
)

data class EncounterItem(
    val id: String,
    val code: String,
    val effective: String,
    val value: String
) {
    override fun toString(): String = code
}

data class DbCodeValue(
    val code: String,
    val value: String
)

enum class NavigationDetails {
    ADMINISTER_VACCINE,
    LIST_VACCINE_DETAILS
}

data class DbVaccineStockDetails(
    val name: String,
    val value: String
)
data class DbAppointmentDetails(
    val dateScheduled: String,
    val doseNumber: String?,
    val targetDisease: String
)