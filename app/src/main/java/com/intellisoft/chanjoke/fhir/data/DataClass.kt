package com.intellisoft.chanjoke.fhir.data

import com.intellisoft.chanjoke.vaccine.validations.BasicVaccine

data class DbVaccineData(
    val logicalId: String,
    val vaccineName: String,
    val doseNumber: String,
    val dateAdministered: String
)

data class AdverseEventData(
    val logicalId: String,
    val type: String,
    val date: String,

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
    val value: String,
    val dateTime:String? = null
)
data class ObservationDateValue(
    val date: String,
    val value: String,
)

enum class NavigationDetails {
    ADMINISTER_VACCINE,
    LIST_VACCINE_DETAILS,
    CLIENT_LIST
}

enum class Identifiers{
    SYSTEM_GENERATED
}

data class DbVaccineStockDetails(
    val name: String,
    val value: String
)
data class DbAppointmentDetails(
    val appointmentId:String,
    val dateScheduled: String,
    val doseNumber: String?,
    val targetDisease: String,
    val vaccineName: String,
    val appointmentStatus: String
)