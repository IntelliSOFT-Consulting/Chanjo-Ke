package com.intellisoft.chanjoke.viewmodel


import android.app.Application
import android.content.res.Resources
import android.icu.text.DateFormat
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.fhir.data.DbVaccineData
import com.intellisoft.chanjoke.utils.AppUtils
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.datacapture.common.datatype.asStringValue
import com.google.android.fhir.logicalId
import com.google.android.fhir.search.Order
import com.google.android.fhir.search.search
import com.intellisoft.chanjoke.fhir.data.AdverseEventData
import com.intellisoft.chanjoke.fhir.data.DbAppointmentDetails
import com.intellisoft.chanjoke.fhir.data.EncounterItem
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.patient_list.PatientListViewModel
import com.intellisoft.chanjoke.vaccine.validations.VaccinationManager
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.hl7.fhir.r4.model.AdverseEvent
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.Encounter
import org.hl7.fhir.r4.model.Immunization
import org.hl7.fhir.r4.model.ImmunizationRecommendation
import org.hl7.fhir.r4.model.Observation
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.Resource
import org.hl7.fhir.r4.model.RiskAssessment
import timber.log.Timber
import java.sql.Time
import java.time.ZonedDateTime
import java.util.Calendar
import java.util.TimeZone

/**
 * The ViewModel helper class for PatientItemRecyclerViewAdapter, that is responsible for preparing
 * data for UI.
 */
class PatientDetailsViewModel(
    application: Application,
    private val fhirEngine: FhirEngine,
    private val patientId: String,
) : AndroidViewModel(application) {
    val livePatientData = MutableLiveData<PatientData>()

    /** Emits list of [PatientDetailData]. */
    fun getPatientDetailData() {
        viewModelScope.launch { livePatientData.value = getPatientDetailDataModel() }
    }

    private suspend fun getPatientDetailDataModel(): PatientData {
        val searchResult =
            fhirEngine.search<Patient> {
                filter(Resource.RES_ID, { value = of(patientId) })
            }
        var name = ""
        var phone = ""
        var dob = ""
        var gender = ""
        var contact_name = ""
        var contact_phone = ""
        var contact_gender = ""
        searchResult.first().let {
            name = it.name[0].nameAsSingleString
            phone = it.telecom.first().value
            dob = LocalDate.parse(it.birthDateElement.valueAsString, DateTimeFormatter.ISO_DATE)
                .toString()
            gender = it.genderElement.valueAsString
            contact_name = if (it.hasContact()) it.contactFirstRep.name.nameAsSingleString else ""
            contact_phone = if (it.hasContact()) it.contactFirstRep.telecomFirstRep.value else ""
            contact_gender =
                if (it.hasContact()) AppUtils().capitalizeFirstLetter(it.contactFirstRep.genderElement.valueAsString) else ""
        }

        return PatientData(
            name,
            phone,
            dob,
            gender,
            contact_name = contact_name,
            contact_phone = contact_phone,
            contact_gender = contact_gender
        )
    }


    data class PatientData(
        val name: String,
        val phone: String,
        val dob: String,
        val gender: String,
        val contact_name: String?,
        val contact_phone: String?,
        val contact_gender: String?
    ) {
        override fun toString(): String = name
    }


    private val LocalDate.localizedString: String
        get() {
            val date = Date.from(atStartOfDay(ZoneId.systemDefault())?.toInstant())
            return if (isAndroidIcuSupported()) {
                DateFormat.getDateInstance(DateFormat.DEFAULT).format(date)
            } else {
                SimpleDateFormat.getDateInstance(DateFormat.DEFAULT, Locale.getDefault())
                    .format(date)
            }
        }

    // Android ICU is supported API level 24 onwards.
    private fun isAndroidIcuSupported() = true

    private fun getString(resId: Int) = getApplication<Application>().resources.getString(resId)

    private fun getLastContactedDate(riskAssessment: RiskAssessment?): String {
        riskAssessment?.let {
            if (it.hasOccurrence()) {
                return LocalDate.parse(
                    it.occurrenceDateTimeType.valueAsString,
                    DateTimeFormatter.ISO_DATE_TIME,
                )
                    .localizedString
            }
        }
        return getString(R.string.none)
    }

    fun recommendationList() = runBlocking {
        getRecommendationList()
    }


    private suspend fun getRecommendationList(): ArrayList<DbAppointmentDetails> {
        val recommendationList = ArrayList<DbAppointmentDetails>()


        fhirEngine
            .search<ImmunizationRecommendation> {
                filter(ImmunizationRecommendation.PATIENT, { value = "Patient/$patientId" })
                sort(Encounter.DATE, Order.DESCENDING)
            }
            .map { createRecommendation(it) }
            .let { recommendationList.addAll(it) }




        return recommendationList
    }


    private fun createRecommendation(it: ImmunizationRecommendation): DbAppointmentDetails {


        val vaccinationManager = VaccinationManager()
        val date = if (it.date != null) it.date.toString() else ""
        var targetDisease = ""
        var doseNumber: String? = ""


        if (it.hasRecommendation()) {
            val recommendation = it.recommendation
            if (recommendation.isNotEmpty()) {
                val codeableConceptTargetDisease = recommendation[0].targetDisease
                if (codeableConceptTargetDisease.hasText()) {
                    targetDisease = codeableConceptTargetDisease.text
                }
            }
        }
        if (targetDisease != "") {
            doseNumber = vaccinationManager.getVaccineDetails(targetDisease)?.dosage
        }


        return DbAppointmentDetails(date, doseNumber, targetDisease)


    }


    fun getEncounterList() = runBlocking {
        getEncounterDetails()
    }

    private suspend fun getEncounterDetails(): ArrayList<DbVaccineData> {

        val encounterList = ArrayList<DbVaccineData>()

        fhirEngine
            .search<Immunization> {
                filter(Immunization.PATIENT, { value = "Patient/$patientId" })
                sort(Immunization.DATE, Order.DESCENDING)
            }
            .map { createEncounterItem(it) }
            .let { encounterList.addAll(it) }

        return encounterList
    }

    private fun createEncounterItem(immunization: Immunization): DbVaccineData {

        var targetDisease = ""
        var doseNumberValue = ""
        var logicalId = immunization.encounter.reference
        var dateScheduled = ""

        val ref = logicalId.toString().replace("Encounter/", "")

        val protocolList = immunization.protocolApplied
        protocolList.forEach {

            //Target Disease

            val targetDiseaseList = it.targetDisease
            if (targetDiseaseList.isNotEmpty()) targetDisease = targetDiseaseList[0].text

            //Dose number
            val doseNumber = it.doseNumber
            if (doseNumber != null) doseNumberValue = doseNumber.asStringValue()

        }
        if (immunization.hasOccurrenceDateTimeType()) {
            val fhirDate = immunization.occurrenceDateTimeType.valueAsString
            val convertedDate = FormatterClass().convertDateFormat(fhirDate)
            if (convertedDate != null){
                dateScheduled = convertedDate
            }
        }


        return DbVaccineData(
            ref,
            targetDisease,
            doseNumberValue,
            dateScheduled
        )
    }

    private suspend fun createEncounterAefiItem(
        encounter: Encounter,
        resources: Resources
    ): AdverseEventData {

        val type = generateObservationByCode(encounter.logicalId, "882-22") ?: ""
        val date = generateObservationByCode(encounter.logicalId, "833-23") ?: ""
        return AdverseEventData(
            encounter.logicalId,
            type,
            date,
        )
    }

    private suspend fun generateObservationByCode(encounterId: String, codeValue: String): String? {
        var data = ""
        fhirEngine
            .search<Observation> {
                filter(Observation.SUBJECT, { value = "Patient/$patientId" })
                filter(Observation.ENCOUNTER, { value = "Encounter/$encounterId" })
                filter(
                    Observation.CODE,
                    {
                        value = of(Coding().apply {
                            system = "http://loinc.org"
                            code = codeValue
                        })
                    })
                sort(Observation.DATE, Order.ASCENDING)
            }
            .map { createObservationItem(it, getApplication<Application>().resources) }
            .firstOrNull()?.let {
                data = it.value
            }
        return data
    }

    fun loadImmunizationAefis(logicalId: String) = runBlocking {
        loadInternalImmunizationAefis(logicalId)
    }

    private suspend fun loadInternalImmunizationAefis(logicalId: String): List<AdverseEventData> {

        val encounterList = ArrayList<AdverseEventData>()
        fhirEngine
            .search<Encounter> {
                filter(
                    Encounter.SUBJECT,
                    { value = "Patient/$patientId" })
                filter(
                    Encounter.PART_OF,
                    { value = "Encounter/$logicalId" })
                sort(Encounter.DATE, Order.DESCENDING)
            }
            .map {
                createEncounterAefiItem(
                    it,
                    getApplication<Application>().resources
                )
            }
            .let { encounterList.addAll(it) }
        return encounterList.reversed()
    }

    fun getObservationByCode(
        patientId: String,
        encounterId: String,
        code: String
    ) = runBlocking {
        getObservationDataByCode(patientId, encounterId, code)
    }


    private suspend fun getObservationDataByCode(
        patientId: String,
        encounterId: String,
        codeValue: String
    ): String {
        var data = ""
        fhirEngine
            .search<Observation> {
                filter(Observation.SUBJECT, { value = "Patient/$patientId" })
                filter(Observation.ENCOUNTER, { value = "Encounter/$encounterId" })
                filter(
                    Observation.CODE,
                    {
                        value = of(Coding().apply {
                            system = "http://loinc.org"
                            code = codeValue
                        })
                    })
                sort(Observation.DATE, Order.ASCENDING)
            }
            .map { createObservationItem(it, getApplication<Application>().resources) }
            .firstOrNull()?.let {
                data = it.value
            }

        return data

    }

    private fun createObservationItem(
        observation: Observation,
        resources: Resources
    ): PatientListViewModel.ObservationItem {
        val observationCode = observation.code.codingFirstRep.code ?: ""


        // Show nothing if no values available for datetime and value quantity.
        val value =
            when {
                observation.hasValueQuantity() -> {
                    observation.valueQuantity.value.toString()
                }

                observation.hasValueCodeableConcept() -> {
                    observation.valueCodeableConcept.coding.firstOrNull()?.display ?: ""
                }

                observation.hasNote() -> {
                    observation.note.firstOrNull()?.author
                }

                observation.hasValueDateTimeType() -> {
                    formatDateToHumanReadable(observation.valueDateTimeType.value.toString())

                }

                observation.hasValueStringType() -> {
                    observation.valueStringType.value.toString()
                }

                else -> {
                    observation.code.text ?: observation.code.codingFirstRep.display
                }
            }
        val valueUnit =
            if (observation.hasValueQuantity()) {
                observation.valueQuantity.unit ?: observation.valueQuantity.code
            } else {
                ""
            }
        val valueString = "$value $valueUnit"

        return PatientListViewModel.ObservationItem(
            observation.logicalId,
            observationCode,
            "$value",
            "$valueString",
        )
    }

    private fun formatDateToHumanReadable(date: String): String? {
        // Create a Calendar instance and set the time zone
        /*   val sourceFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH)
           val destFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
           val convertedDate = sourceFormat.parse(date)
           val data = destFormat.parse(convertedDate?.let { destFormat.format(it) }.toString())*/
        return "$date"
    }
}

class PatientDetailsViewModelFactory(
    private val application: Application,
    private val fhirEngine: FhirEngine,
    private val patientId: String,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(PatientDetailsViewModel::class.java)) {
            "Unknown ViewModel class"
        }
        return PatientDetailsViewModel(application, fhirEngine, patientId) as T
    }
}
