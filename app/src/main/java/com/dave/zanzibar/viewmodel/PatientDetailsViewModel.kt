package com.dave.zanzibar.viewmodel


import android.app.Application
import android.content.res.Resources
import android.icu.text.DateFormat
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dave.zanzibar.R
import com.dave.zanzibar.fhir.data.DbVaccineData
import com.dave.zanzibar.fhir.data.EncounterItem
import com.dave.zanzibar.patient_list.PatientListViewModel
import com.dave.zanzibar.patient_list.toPatientItem
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.logicalId
import com.google.android.fhir.search.Order
import com.google.android.fhir.search.search
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.commons.lang3.StringUtils
import org.hl7.fhir.r4.model.Condition
import org.hl7.fhir.r4.model.Encounter
import org.hl7.fhir.r4.model.Observation
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.Resource
import org.hl7.fhir.r4.model.ResourceType
import org.hl7.fhir.r4.model.RiskAssessment
import org.hl7.fhir.r4.model.codesystems.RiskProbability

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
        searchResult.first().let {
            name = it.name[0].nameAsSingleString
            phone = it.telecom.first().value
            dob = LocalDate.parse(it.birthDateElement.valueAsString, DateTimeFormatter.ISO_DATE)
                .toString()
            gender = it.genderElement.valueAsString
        }

        return PatientData(name, phone, dob, gender)
    }


    data class PatientData(
        val name: String,
        val phone: String,
        val dob: String,
        val gender: String,
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


    fun getEncounterList()= runBlocking{
        getEncounterDetails()
    }
    private suspend fun getEncounterDetails():ArrayList<DbVaccineData>{

        val encounterList = ArrayList<DbVaccineData>()
        val encounter = mutableListOf<EncounterItem>()

        fhirEngine
            .search<Encounter> {
                filter(Encounter.SUBJECT, { value = "Patient/$patientId" })
                sort(Encounter.DATE, Order.DESCENDING)
            }
            .map { createEncounterItem(it, getApplication<Application>().resources) }
            .let { encounter.addAll(it) }

        encounter.forEach {

            val name = it.code
            val value = it.value

            val dbVaccineData = DbVaccineData(name, value)
            encounterList.add(dbVaccineData)

        }

        return encounterList
    }

    fun createEncounterItem(encounter: Encounter, resources: Resources): EncounterItem{

        val encounterDate =
            if (encounter.hasPeriod()) {
                if (encounter.period.hasStart()) {
                    encounter.period.start
                } else {
                    ""
                }
            } else {
                ""
            }

        var lastUpdatedValue = ""

        if (encounter.hasMeta()){
            if (encounter.meta.hasLastUpdated()){
                lastUpdatedValue = encounter.meta.lastUpdated.toString()
            }
        }

        val reasonCode = encounter.reasonCode.firstOrNull()?.text ?: ""

        var textValue = ""

        if(encounter.reasonCode.size > 0){

            val text = encounter.reasonCode[0].text
            val textString = encounter.reasonCode[0].text?.toString() ?: ""
            val textStringValue = encounter.reasonCode[0].coding[0].code ?: ""

            textValue = if (textString != "") {
                textString
            }else if (textStringValue != ""){
                textStringValue
            }else text ?: ""

        }

        val encounterDateStr = if (encounterDate != "") {
            encounterDate.toString()
        } else {
            lastUpdatedValue
        }

        return EncounterItem(
            encounter.logicalId,
            textValue,
            encounterDateStr,
            reasonCode
        )
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
