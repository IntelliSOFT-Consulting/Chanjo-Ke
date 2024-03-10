package com.intellisoft.chanjoke.viewmodel


import android.app.Application
import android.content.Context
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
import com.google.android.fhir.search.StringFilterModifier
import com.google.android.fhir.search.count
import com.google.android.fhir.search.search
import com.intellisoft.chanjoke.fhir.data.AdverseEventData
import com.intellisoft.chanjoke.fhir.data.Contraindication
import com.intellisoft.chanjoke.fhir.data.DbAppointmentData
import com.intellisoft.chanjoke.fhir.data.DbAppointmentDetails
import com.intellisoft.chanjoke.fhir.data.DbVaccineDetailsData
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.fhir.data.Identifiers
import com.intellisoft.chanjoke.fhir.data.ObservationDateValue
import com.intellisoft.chanjoke.patient_list.PatientListViewModel
import com.intellisoft.chanjoke.utils.Constants.AEFI_DATE
import com.intellisoft.chanjoke.utils.Constants.AEFI_TYPE
import com.intellisoft.chanjoke.vaccine.validations.BasicVaccine
import com.intellisoft.chanjoke.vaccine.validations.ImmunizationHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.hl7.fhir.r4.model.AllergyIntolerance
import org.hl7.fhir.r4.model.Appointment
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.Encounter
import org.hl7.fhir.r4.model.Immunization
import org.hl7.fhir.r4.model.ImmunizationRecommendation
import org.hl7.fhir.r4.model.Location
import org.hl7.fhir.r4.model.Observation
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.Reference
import org.hl7.fhir.r4.model.Resource
import org.hl7.fhir.r4.model.RiskAssessment
import timber.log.Timber

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


    fun getPatientInfo() = runBlocking {
        getPatientDetailDataModel()
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
        var contact_type = ""
        var type = ""
        var systemId = ""
        var county = ""
        var subCounty = ""
        var ward = ""
        var trading = ""
        var estate = ""
        var logicalId = ""
        searchResult.first().let {
            logicalId = it.logicalId
            name = if (it.hasName()) {
                // display name in order as fname, then others
                "${it.name[0].family} ${it.name[0].givenAsSingleString}"
            } else ""

            phone = ""
            if (it.hasTelecom()) {
                if (it.telecom.isNotEmpty()) {
                    if (it.telecom.first().hasValue()) {
                        phone = it.telecom.first().value
                    }
                }
            }

            if (it.hasBirthDateElement()) {
                if (it.birthDateElement.hasValue()) dob =
                    LocalDate.parse(it.birthDateElement.valueAsString, DateTimeFormatter.ISO_DATE)
                        .toString()
            }

            if (it.hasContact()) {
                if (it.contactFirstRep.hasName()) contact_name =
                    if (it.hasContact()) {
                        if (it.contactFirstRep.hasName()) {
                            it.contactFirstRep.name.nameAsSingleString
                        } else ""
                    } else ""
                if (it.contactFirstRep.hasTelecom()) contact_phone =
                    if (it.hasContact()) {
                        if (it.contactFirstRep.hasTelecom()) {
                            if (it.contactFirstRep.telecomFirstRep.hasValue()) {
                                it.contactFirstRep.telecomFirstRep.value
                            } else ""
                        } else ""
                    } else ""
                if (it.contactFirstRep.hasGenderElement()) contact_gender =
                    if (it.hasContact()) AppUtils().capitalizeFirstLetter(it.contactFirstRep.genderElement.valueAsString) else ""
                if (it.contactFirstRep.hasRelationship()) {
                    if (it.contactFirstRep.relationshipFirstRep.hasCoding()) {
                        contact_type = it.contactFirstRep.relationshipFirstRep.text
                    }
                }
            }

            if (it.hasGenderElement()) gender = it.genderElement.valueAsString

            if (it.hasIdentifier()) {
                it.identifier.forEach { identifier ->

                    try {
                        if (identifier.system.toString() != "system-creation") {
                            systemId = identifier.value
                            type = identifier.system
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    val codeableConceptType = identifier.type
                    if (codeableConceptType.hasText() && codeableConceptType.text.contains(
                            Identifiers.SYSTEM_GENERATED.name
                        )
                    ) {


                    }
                }
            }


            if (it.hasAddress()) {
                if (it.addressFirstRep.hasCity()) county = it.addressFirstRep.city
                if (it.addressFirstRep.hasDistrict()) subCounty = it.addressFirstRep.district
                if (it.addressFirstRep.hasState()) ward = it.addressFirstRep.state
                if (it.addressFirstRep.hasLine()) {
                    if (it.addressFirstRep.line.size >= 2) {
                        trading = it.addressFirstRep.line[0].value
                        estate = it.addressFirstRep.line[1].value
                    }
                }
            }


        }

        FormatterClass().saveSharedPref(
            "patientDob",
            dob,
            getApplication<Application>().applicationContext
        )
        FormatterClass().saveSharedPref(
            "patientId",
            patientId,
            getApplication<Application>().applicationContext
        )

        return PatientData(
            logicalId = logicalId,
            name,
            phone,
            dob,
            gender,
            contact_name = contact_name,
            contact_phone = contact_phone,
            contact_gender = contact_type,
            systemId = systemId,
            county = county,
            type = type,
            subCounty = subCounty,
            ward = ward,
            trading = trading,
            estate = estate
        )
    }

    data class PatientData(
        val logicalId: String,
        val name: String,
        val phone: String,
        val dob: String,
        val gender: String,
        val contact_name: String?,
        val contact_phone: String?,
        val contact_gender: String?,
        val systemId: String?,
        val type: String?,
        val county: String?,
        val subCounty: String?,
        val ward: String?,
        val trading: String?,
        val estate: String?,

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

    fun recommendationList(status:String?) = runBlocking {
        getRecommendationList(status)
    }


    private suspend fun getRecommendationList(status:String?): ArrayList<DbAppointmentDetails> {
        val recommendationList = ArrayList<DbAppointmentDetails>()
        fhirEngine
            .search<ImmunizationRecommendation> {
                filter(ImmunizationRecommendation.PATIENT, { value = "Patient/$patientId" })
                sort(Encounter.DATE, Order.DESCENDING)
            }
            .map { createRecommendation(it) }
            .let { recommendationList.addAll(it) }


        var newRecommendationList = ArrayList<DbAppointmentDetails>()
        if (status != null){
            val newVaccineList = recommendationList.filter {
                it.appointmentStatus == status
            }
            newRecommendationList = ArrayList(newVaccineList)
        }else{
            newRecommendationList = recommendationList
        }


        return newRecommendationList
    }


    private fun createRecommendation(it: ImmunizationRecommendation): DbAppointmentDetails {

        var appointmentId = ""

        if (it.hasId()) appointmentId = it.id.replace("ImmunizationRecommendation/", "")

        var date = ""

        if (it.hasRecommendation() && it.recommendation.isNotEmpty()) {
            if (it.recommendation[0].hasDateCriterion() &&
                it.recommendation[0].dateCriterion.isNotEmpty() &&
                it.recommendation[0].dateCriterion[0].hasValue()
            ) {
                val dateCriterion = it.recommendation[0].dateCriterion[0].value.toString()
                val dobFormat = FormatterClass().convertDateFormat(dateCriterion)
                if (dobFormat != null) {
                    date = dobFormat.toString()
                }


            }

        }
        var targetDisease = ""
        var doseNumber: String? = ""
        var appointmentStatus = ""
        var vaccineName = ""


        if (it.hasRecommendation()) {
            val recommendation = it.recommendation
            if (recommendation.isNotEmpty()) {
                //targetDisease
                val codeableConceptTargetDisease = recommendation[0].targetDisease
                if (codeableConceptTargetDisease.hasText()) {
                    targetDisease = codeableConceptTargetDisease.text
                }

                //appointment status
                val codeableConceptTargetStatus = recommendation[0].forecastStatus
                if (codeableConceptTargetStatus.hasText()) {
                    appointmentStatus = codeableConceptTargetStatus.text
                }

                //Dose number
                if (recommendation[0].hasDoseNumber()) {
                    doseNumber = recommendation[0].doseNumber.asStringValue()
                }

                //Contraindicated vaccine code
                if (recommendation[0].hasContraindicatedVaccineCode()) {
                    vaccineName = recommendation[0].contraindicatedVaccineCode[0].text
                }

            }
        }





        return DbAppointmentDetails(
            appointmentId,
            date,
            doseNumber,
            targetDisease,
            vaccineName,
            appointmentStatus
        )


    }


    fun getAppointmentList() = runBlocking {
        getAppointmentDetails()
    }

    private suspend fun getAppointmentDetails(): ArrayList<DbAppointmentData> {

        val appointmentList = ArrayList<DbAppointmentData>()

        fhirEngine
            .search<Appointment> {
                filter(Appointment.SUPPORTING_INFO, { value = "Patient/$patientId" })
                sort(Appointment.DATE, Order.DESCENDING)
            }
            .map { createAppointment(it) }
            .let { appointmentList.addAll(it) }

        return appointmentList
    }

    private suspend fun createAppointment(it: Appointment): DbAppointmentData {

        val recommendationList = getRecommendationList(null)

        val id = if (it.hasId()) it.id else ""
        val status = if (it.hasStatus()) it.status else ""
        val title = if (it.hasDescription()) it.description else ""
        val start = if (it.hasStart()) it.start else ""
        var dateScheduled = ""

        val startDate = FormatterClass().convertDateFormat(start.toString())
        if (startDate != null) {
            dateScheduled = startDate
        }

        Log.e(">>>>>>>>>","<<<<<<<<")
        println("title $title")
        Log.e(">>>>>>>>>","<<<<<<<<")

        var recommendationSavedList = ArrayList<DbAppointmentDetails>()
        val basedOnImmunizationRecommendationList = if (it.hasBasedOn()) {
            it.basedOn
        } else {
            emptyList()
        }
        basedOnImmunizationRecommendationList.forEach { ref ->
            val immunizationRecommendation = ref.reference
            val recommendationId =
                immunizationRecommendation.replace("ImmunizationRecommendation/", "")
            val selectedRecommendation =
                recommendationList.find { it.appointmentId == recommendationId }
            if (selectedRecommendation != null) {
                recommendationSavedList.add(selectedRecommendation)
            }
        }

        return DbAppointmentData(
            id,
            title,
            "",
            null,
            dateScheduled,
            recommendationSavedList,
            status.toString()
        )


    }

    fun getVaccineListWithAefis() = runBlocking {
        getVaccineListDetails()
    }

    fun getVaccineList() = runBlocking {
        getVaccineListDetailsOld()
    }

    private suspend fun getVaccineListDetails(): ArrayList<DbVaccineData> {

        val vaccineList = ArrayList<DbVaccineData>()

        fhirEngine
            .search<AllergyIntolerance> {
                filter(AllergyIntolerance.PATIENT, { value = "Patient/$patientId" })
                sort(AllergyIntolerance.DATE, Order.DESCENDING)
            }
            .map { createAllergyIntoleranceItem(it) }
            .let { vaccineList.addAll(it) }


        return ArrayList(vaccineList)
    }

    fun getImmunizationDataDetails(codeValue: String) =
        runBlocking { getImmunizationDetails(codeValue) }

    fun getAllImmunizationDetails() =
        runBlocking { getAllImmunizationDetailsData() }

    fun loadContraindications(codeValue: String) =
        runBlocking { loadContraindicationsInner(codeValue) }

    private suspend fun getImmunizationDetails(codeValue: String): ArrayList<DbVaccineDetailsData> {
        val vaccineList = ArrayList<DbVaccineDetailsData>()

        fhirEngine
            .search<Immunization> {
                filter(Immunization.PATIENT, { value = "Patient/$patientId" })
                filter(
                    Immunization.VACCINE_CODE,
                    {
                        value = of(Coding().apply { code = codeValue })
                    })
                sort(Immunization.DATE, Order.DESCENDING)
            }
            .map { createVaccineItemDetails(it) }
            .let { vaccineList.addAll(it) }

        return vaccineList
    }

    private suspend fun getAllImmunizationDetailsData(): ArrayList<DbVaccineDetailsData> {
        val vaccineList = ArrayList<DbVaccineDetailsData>()

        fhirEngine
            .search<Immunization> {
                filter(Immunization.PATIENT, { value = "Patient/$patientId" })
                sort(Immunization.DATE, Order.DESCENDING)
            }
            .map { createVaccineItemDetails(it) }
            .let { q ->
                q.forEach {
                    if (it.status == "COMPLETED") {
                        vaccineList.add(it)
                    }
                }
            }


        return vaccineList
    }

    private suspend fun loadContraindicationsInner(codeValue: String): ArrayList<Contraindication> {
        val vaccineList = ArrayList<Contraindication>()

        fhirEngine
            .search<ImmunizationRecommendation> {
                filter(ImmunizationRecommendation.PATIENT, { value = "Patient/$patientId" })
                sort(ImmunizationRecommendation.DATE, Order.DESCENDING)
            }
            .map { createContraItemDetails(it) }
            .let { q ->
                q.forEach {
                    if (it.vaccineCode.contains(codeValue) && it.status.contains("Contraindicated")) {
                        vaccineList.add(it)
                    }
                }
            }

        return vaccineList
    }

    private fun createVaccineItemDetails(immunization: Immunization): DbVaccineDetailsData {

        var logicalId = ""
        var vaccineName = ""
        var dosesAdministered = ""
        var seriesDosesString = ""
        var series = ""
        var status = ""

        if (immunization.hasId()) {
            logicalId = immunization.id
        }
        if (immunization.hasVaccineCode()) {
            if (immunization.vaccineCode.hasText()) vaccineName = immunization.vaccineCode.text
        }
        if (immunization.hasOccurrenceDateTimeType()) {
            val fhirDate = immunization.occurrenceDateTimeType.valueAsString
            val convertedDate = FormatterClass().convertDateFormat(fhirDate)
            if (convertedDate != null) {
                dosesAdministered = convertedDate
            }
        }
        if (immunization.hasProtocolApplied()) {
            if (immunization.protocolApplied.isNotEmpty() && immunization.protocolApplied[0].hasSeriesDoses()) {
                seriesDosesString = immunization.protocolApplied[0].seriesDoses.asStringValue()

                series = immunization.protocolApplied[0].series
            }
        }


        if (immunization.hasStatus()) {
            status = immunization.statusElement.value.name
        }


        return DbVaccineDetailsData(
            logicalId, vaccineName, dosesAdministered, seriesDosesString, series, status
        )
    }

    private fun createContraItemDetails(data: ImmunizationRecommendation): Contraindication {

        var logicalId = ""
        var vaccineName = ""
        var vaccineCode = ""
        var nextDate = ""
        var contraDetail = ""
        var status = ""

        if (data.hasId()) {
            logicalId = data.id
        }
        if (data.hasRecommendation()) {
            vaccineCode = data.recommendationFirstRep.contraindicatedVaccineCodeFirstRep.text
            vaccineName = data.recommendationFirstRep.targetDisease.text
            nextDate = data.recommendationFirstRep.dateCriterionFirstRep.value.toString()
            contraDetail = data.recommendationFirstRep.forecastReasonFirstRep.text
            status = data.recommendationFirstRep.forecastStatus.text
        }


        return Contraindication(
            logicalId, vaccineCode, vaccineName, nextDate, contraDetail, status
        )
    }

    private suspend fun getVaccineListDetailsOld(): ArrayList<DbVaccineData> {

        val vaccineList = ArrayList<DbVaccineData>()

        fhirEngine
            .search<Immunization> {
                filter(Immunization.PATIENT, { value = "Patient/$patientId" })
                sort(Immunization.DATE, Order.DESCENDING)
            }
            .map { createVaccineItem(it) }
            .let { vaccineList.addAll(it) }

        val newVaccineList = vaccineList.filterNot {
            it.status == "NOTDONE"
        }

        return ArrayList(newVaccineList)
    }

    private fun createAllergyIntoleranceItem(data: AllergyIntolerance): DbVaccineData {

        var vaccineName = ""
        var doseNumberValue = ""
        val logicalId = if (data.hasEncounter()) data.encounter.reference else ""
        var dateScheduled = ""
        var status = ""

        val ref = logicalId.toString().replace("Encounter/", "")

        if (data.hasNote()) {
            status = if (data.noteFirstRep.hasText()) data.noteFirstRep.text else ""
        }

        return DbVaccineData(
            ref,
            null,
            vaccineName,
            doseNumberValue,
            dateScheduled,
            status
        )
    }

    private fun createVaccineItem(immunization: Immunization): DbVaccineData {

        val immunizationHandler = ImmunizationHandler()

        var vaccineName = ""
        var doseNumberValue = ""
        val logicalId = if (immunization.hasEncounter()) immunization.encounter.reference else ""
        var dateScheduled = ""
        var status = ""

        val ref = logicalId.toString().replace("Encounter/", "")

        if (immunization.hasVaccineCode()) {
            if (immunization.vaccineCode.hasText()) vaccineName = immunization.vaccineCode.text
        }

        if (immunization.hasOccurrenceDateTimeType()) {
            val fhirDate = immunization.occurrenceDateTimeType.valueAsString
            val convertedDate = FormatterClass().convertDateFormat(fhirDate)
            if (convertedDate != null) {
                dateScheduled = convertedDate
            }
        }
        if (immunization.hasProtocolApplied()) {
            if (immunization.protocolApplied.isNotEmpty() && immunization.protocolApplied[0].hasSeriesDoses()) doseNumberValue =
                immunization.protocolApplied[0].seriesDoses.asStringValue()
        }
        if (immunization.hasStatus()) {
            status = immunization.statusElement.value.name
        }

        /**
         * 1. Get the vaccine name, get series number, get the previous series number, get the previous Basic Vaccine
         * 2. From the Previous Basic Vaccine, get the date administered
         * 3. Calculate the next vaccination date and display it
         */
        var previousBasicVaccine: BasicVaccine? = null
        val basicVaccine = immunizationHandler.getVaccineDetailsByBasicVaccineName(vaccineName)
        if (basicVaccine != null) {
            val doseNumber = basicVaccine.doseNumber
            val seriesVaccine = immunizationHandler.getRoutineSeriesByBasicVaccine(basicVaccine)
            if (seriesVaccine != null) {
                previousBasicVaccine =
                    immunizationHandler.getPreviousBasicVaccineInSeries(seriesVaccine, doseNumber)
            }
        }

        return DbVaccineData(
            ref,
            previousBasicVaccine,
            vaccineName,
            doseNumberValue,
            dateScheduled,
            status
        )
    }

    private suspend fun createEncounterAefiItem(
        encounter: Encounter,
        resources: Resources
    ): AdverseEventData {

        val type = generateObservationByCode(encounter.logicalId, AEFI_TYPE) ?: ""
        val date = generateObservationByCode(encounter.logicalId, AEFI_DATE) ?: ""
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

    fun loadImmunizationAefis(logicalId: List<DbVaccineData>) = runBlocking {
        loadInternalImmunizationAefis(logicalId)
    }

    private suspend fun loadInternalImmunizationAefis(list: List<DbVaccineData>): List<AdverseEventData> {

        val encounterList = ArrayList<AdverseEventData>()
        fhirEngine
            .search<Encounter> {
                filter(
                    Encounter.SUBJECT,
                    { value = "Patient/$patientId" })
                sort(Encounter.DATE, Order.DESCENDING)
            }
            .map {
                createEncounterAefiItem(
                    it,
                    getApplication<Application>().resources
                )
            }
            .forEach { j ->
                if (list.any { it.logicalId == j.logicalId }) {
                    encounterList.add(j)
                }
            }

        return encounterList.reversed()
    }

    fun getObservationByCode(
        patientId: String,
        encounterId: String?,
        code: String
    ) = runBlocking {
        getObservationDataByCode(patientId, encounterId, code)
    }


    private suspend fun getObservationDataByCode(
        patientId: String,
        encounterId: String?,
        codeValue: String
    ): ObservationDateValue {
        var date = ""
        var dataValue = ""
        fhirEngine
            .search<Observation> {
                filter(Observation.SUBJECT, { value = "Patient/$patientId" })
                if (encounterId != null) filter(
                    Observation.ENCOUNTER,
                    { value = "Encounter/$encounterId" })
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
                date = it.effective
                dataValue = it.value
            }

        return ObservationDateValue(
            date,
            dataValue,
        )

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
        val dateTimeString = if (observation.hasIssued()) observation.issued.toString() else ""


        return PatientListViewModel.ObservationItem(
            observation.logicalId,
            observationCode,
            "$dateTimeString",
            "$valueString",
        )
    }

    private fun formatDateToHumanReadable(date: String): String? {
        return FormatterClass().convertDateFormat(date)

    }

    fun createAefiEncounter(context: Context, patientId: String, currentAge: String) {

        viewModelScope.launch {
            recordData(context, patientId, currentAge)
        }
    }

    suspend fun recordData(context: Context, patientId: String, currentAge: String) {
        try {
            val modifiedString = currentAge.toLowerCase().replace(' ', '_')
            val subjectReference = Reference("Patient/$patientId")
            val resource = Encounter();
            resource.subject = subjectReference
            resource.id = modifiedString
            fhirEngine.create(resource)
            Timber.tag("TAG").e("Created an Encounter with ID %s", currentAge)
        } catch (e: Exception) {
            e.printStackTrace()
            Timber.tag("TAG").e("Created an Encounter with Exception %s", e.message)
        }
    }

    fun generateCurrentCount(weekNo: String, patientId: String) = runBlocking {
        counterAllergies(weekNo, patientId)
    }


    private suspend fun counterAllergies(weekNo: String, patientId: String): String {
        var counter = 0
        fhirEngine
            .search<AllergyIntolerance> {
                filter(AllergyIntolerance.PATIENT, { value = "Patient/$patientId" })
                sort(AllergyIntolerance.DATE, Order.DESCENDING)
            }
            .map { createAllergyIntoleranceItem(it) }
            .forEach { q ->
                if (q.status.contains(weekNo)) {
                    counter++
                }
            }

        return "$counter"
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
