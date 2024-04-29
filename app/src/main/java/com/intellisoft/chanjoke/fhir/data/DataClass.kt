package com.intellisoft.chanjoke.fhir.data

import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.vaccine.validations.BasicVaccine
import java.util.Date

enum class UrlData(var message: Int) {
    BASE_URL(R.string.base_url),
}

data class DbVaccineAdmin(
    val dateAdministered: Date,
    val type: String
)

data class DbStatusColor(
    val keyTitle: String,
    val statusColor: String,
    val isStatusDue: Boolean = false,

    )

data class DbBatchNumbers(
    val vaccineName: String,
    val diseaseTargeted: String?
)

data class DbVaccineListData(
    val keyTitle: String,
    val statusColor: String,
)

enum class StatusColors {
    GREEN, //All administered
    AMBER, //Some administered
    RED, //All missed
    NORMAL, //Future
}

data class AdverseEventItem(
    val encounterId: String,
    val practitionerId: PractitionerDetails,
    val locationId: String,
    val locDisplay: String
)

data class PractitionerDetails(
    val name: String,
    val role: String,
)

data class DbVaccineDetailsData(
    val logicalId: String,
    val vaccineName: String,
    val dosesAdministered: String,
    val seriesDosesString: String,
    val series: String,
    val status: String,
)

data class Contraindication(
    val logicalId: String,
    val vaccineCode: String,
    val vaccineName: String,
    val nextDate: String,
    val contraDetail: String,
    val status: String,
)

data class DbVaccineData(
    val logicalId: String,
    val previousVaccineName: BasicVaccine? = null,
    val vaccineName: String,
    val doseNumber: String,
    var dateAdministered: String,
    val status: String,
)

data class AllergicReaction(
    val period: String,
    val vaccines: String,
    val reactions: List<DbVaccineData>
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
    val dateTime: String? = null
)

data class ObservationDateValue(
    val date: String,
    val value: String,
)

enum class NavigationDetails {
    CONTRAINDICATIONS,
    APPOINTMENT,
    ADMINISTER_VACCINE,
    NOT_ADMINISTER_VACCINE,
    UPDATE_CLIENT_HISTORY,
    LIST_VACCINE_DETAILS,
    CLIENT_LIST,
    EDIT_CLIENT,
    LIST_AEFI,
    ADD_AEFI,
    VACCINE_DETAILS,
    REFERRALS,
    UPDATE_VACCINE_DETAILS
}

data class DbAppointmentDataDetails(
    val id: String? = null,
    val vaccineName: String?,
    val dateScheduled: String,
)

data class DbAppointmentData(
    val id: String? = null,
    val title: String,
    val description: String,
    val vaccineName: String?,
    val dateScheduled: String,

    val recommendationList: ArrayList<DbAppointmentDetails>? = null,
    val status: String = ""
)

enum class Identifiers {
    SYSTEM_GENERATED
}

data class DbVaccineStockDetails(
    val name: String,
    val value: String
)

//This is the recommendation
data class DbAppointmentDetails(
    val appointmentId: String,
    val dateScheduled: String,
    val doseNumber: String?,
    val targetDisease: String,
    val vaccineName: String,
    val appointmentStatus: String
)

data class DbSignIn(
    val idNumber: String,
    val password: String
)
data class DbSetPasswordReq(
    val resetCode: String,
    val idNumber: String,
    val password: String
)

data class DbSignInResponse(
    val access_token: String,
    val expires_in: String,
    val refresh_expires_in: String,
    val refresh_token: String,
)
data class DbResetPassword(
    val status: String,
    val response: String,
)
data class DbResetPasswordData(
    val idNumber: String,
    val email: String
)

data class DbUserInfoResponse(
    val user: DbUser?,
)

data class DbUser(
    val fullNames: String,
    val idNumber: String,
    val practitionerRole: String,
    val fhirPractitionerId: String,
    val email: String,
    val phone: String?,
    val id: String,
    val facility: String,
    val facilityName: String,
)

data class DbVaccinationSchedule(
    val scheduleTime: String,
    val scheduleStatus: String,
    val scheduleVaccinationList: ArrayList<DbScheduleVaccination>
)

data class DbScheduleVaccination(
    val vaccineName: String,
    val vaccineDate: String,
    val vaccineStatus: String
)

data class DbVaccineSchedule(
    val scheduleTime: String,
    val scheduleStatus: String?,
    val scheduleVaccinationList: List<BasicVaccine>
)

data class CustomPatient(
    val firstname: String,
    val middlename: String,
    val lastname: String,
    val gender: String,
    val dateOfBirth: String,
    val age: String,
    val estimate: Boolean,
    val identification: String,
    val identificationNumber: String,
    val telephone: String
)

data class CareGiver(
    val type: String,
    val name: String,
    val phone: String
)

data class Administrative(
    val county: String,
    val subCounty: String,
    val ward: String,
    val trading: String,
    val estate: String
)

data class CompletePatient(
    val personal: CustomPatient,
    val caregivers: List<CareGiver>,
    val administrative: Administrative
)

data class County(
    val name: String,
    val sub_counties: List<SubCounty>
)

data class SubCounty(
    val name: String,
//
)

data class SubCountyWard(
    val subCounty: String,
    val wards: List<Ward>
//
)

data class Ward(
    val name: String,

    )

data class AEFIData(
    val type: String,
    val brief: String,
    val onset: String,
    val history: String,
    val specimen: String,
    val severity: String,
    val action: String,
    val outcome: String,
    val reporter: String,
    val phone: String
)

data class Parent(
    val type: String,
    val brief: String,
    val onset: String,
    val history: String,
)

data class Child(
    val specimen: String,
    val severity: String,
    val action: String,
    val outcome: String,
    val reporter: String,
    val phone: String

)

data class DbVaccineScheduleGroup(
    val vaccineSchedule: String,
    val colorCode: String,
    val aefiValue: String?,
    val dbVaccineScheduleChildList: ArrayList<DbVaccineScheduleChild>
)

data class DbVaccineScheduleChild(
    val vaccineName: String,
    val date: String,
    val status: String,
    var isVaccinated: Boolean,
    var canBeVaccinated: Boolean?,
)

data class DbRecycler(
    val recyclerView: RecyclerView,
    val vaccineSchedule: String
)

data class PatientIdentification(
    val document: String,
    val number: String

)