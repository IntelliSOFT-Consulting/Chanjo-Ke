/*
 * Copyright 2022-2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intellisoft.chanjoke.vaccine

import android.app.Application
import android.content.res.Resources
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import com.intellisoft.chanjoke.fhir.FhirApplication
import com.intellisoft.chanjoke.fhir.data.DbCodeValue
import com.intellisoft.chanjoke.patient_list.PatientListViewModel
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.datacapture.common.datatype.asStringValue
import com.google.android.fhir.datacapture.mapping.ResourceMapper
import com.google.android.fhir.logicalId
import com.google.android.fhir.search.search
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.util.UUID
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.CodeableConcept
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.Condition
import org.hl7.fhir.r4.model.Encounter
import org.hl7.fhir.r4.model.Immunization
import org.hl7.fhir.r4.model.Immunization.ImmunizationStatus
import org.hl7.fhir.r4.model.ImmunizationRecommendation
import org.hl7.fhir.r4.model.Observation
import org.hl7.fhir.r4.model.PositiveIntType
import org.hl7.fhir.r4.model.Questionnaire
import org.hl7.fhir.r4.model.QuestionnaireResponse
import org.hl7.fhir.r4.model.Reference
import org.hl7.fhir.r4.model.Resource
import org.hl7.fhir.r4.model.SimpleQuantity
import org.hl7.fhir.r4.model.Type
import java.math.BigDecimal
import java.util.Date

/** ViewModel for patient registration screen {@link AddPatientFragment}. */
class AdministerVaccineViewModel(
    application: Application,
    private val state: SavedStateHandle
) :
    AndroidViewModel(application) {

    val questionnaire: String
        get() = getQuestionnaireJson()

    val isResourcesSaved = MutableLiveData<Boolean>()

    private val questionnaireResource: Questionnaire
        get() =
            FhirContext.forCached(FhirVersionEnum.R4).newJsonParser().parseResource(questionnaire)
                    as Questionnaire

    private var questionnaireJson: String? = null
    private var fhirEngine: FhirEngine = FhirApplication.fhirEngine(application.applicationContext)

    fun saveScreenerEncounter(questionnaireResponse: QuestionnaireResponse, patientId: String) {
        viewModelScope.launch {
            val bundle = ResourceMapper.extract(questionnaireResource, questionnaireResponse)
            val subjectReference = Reference("Patient/$patientId")
            val encounterId = generateUuid()
//      if (isRequiredFieldMissing(bundle)) {
//        isResourcesSaved.value = false
//        return@launch
//      }

            Log.e("-----", "hhhhhhhh")

            val context = FhirContext.forR4()
            val questionnaire =
                context.newJsonParser().encodeResourceToString(questionnaireResponse)

            println(questionnaire)

            saveResources(bundle, subjectReference, encounterId, patientId)

            isResourcesSaved.value = true
        }
    }

    private suspend fun saveResources(
        bundle: Bundle,
        subjectReference: Reference,
        encounterId: String,
        patientId: String,
    ) {

        val encounterReference = Reference("Encounter/$encounterId")
        bundle.entry.forEach {

            when (val resource = it.resource) {
                is Observation -> {
                    if (resource.hasCode()) {
                        val uuid = generateUuid()
                        resource.id = uuid
                        resource.subject = subjectReference
                        resource.encounter = encounterReference
                        saveResourceToDatabase(resource, "Obs " + uuid)
                    }
                }

                is Condition -> {
                    if (resource.hasCode()) {
                        val uuid = generateUuid()
                        resource.id = uuid
                        resource.subject = subjectReference
                        resource.encounter = encounterReference
                        saveResourceToDatabase(resource, "cond " + uuid)
                    }
                }

                is Encounter -> {
                    resource.subject = subjectReference
                    resource.id = encounterId
                    /**
                     * Check for AEFIs should be partOf
                     * */
                    if (FormatterClass().getSharedPref("vaccinationFlow",getApplication<Application>().applicationContext)=="addAefi"){
                        val ref=FormatterClass().getSharedPref(
                            "encounter_logical_id",
                            getApplication<Application>().applicationContext
                        )
                        val parentReference = Reference("Encounter/$ref")
                        resource.partOf=parentReference
                    }

                    saveResourceToDatabase(resource, "enc " + encounterId)
                    val vaccinationFlow = FormatterClass().getSharedPref("vaccinationFlow", getApplication<Application>().applicationContext)
                    if (vaccinationFlow == "createVaccineDetails" || vaccinationFlow == "updateVaccineDetails"){
                        createImmunisationRecord(encounterId, patientId)
                    }
                }
            }
        }
    }

    private fun createImmunisationRecord(
        encounterId: String,
        patientId: String) {


        CoroutineScope(Dispatchers.IO).launch {


            val immunizationId = generateUuid()
            val encounterReference = Reference("Encounter/$encounterId")
            val patientReference = Reference("Patient/$patientId")


            //Have a list of the observation codes

            var immunization = Immunization()

            immunization.encounter = encounterReference
            immunization.patient = patientReference
            immunization.id = immunizationId
            val immunisationStatus: ImmunizationStatus


            val vaccinationFlow = FormatterClass().getSharedPref("vaccinationFlow", getApplication<Application>().applicationContext)
            if (vaccinationFlow == "createVaccineDetails"){
                //Get more details about the immunisation
                val job = Job()
                CoroutineScope(Dispatchers.IO + job).launch {
                    immunization = generateImmunisation(immunization)
                }.join()


                //Status and status reason
                val status = observationFromCode(
                    "11-1122",
                    patientId,
                    encounterId)
                val statusReason = observationFromCode(
                    "72029-2",
                    patientId,
                    encounterId)


                if (status.value.replace(" ","") == "Yes"){
                    /**
                     *  This means the immunisation was successful and there were not contraindications
                     * The only recommendation to be done here is for the next dose according to DAK
                     */
                    immunisationStatus = ImmunizationStatus.COMPLETED
                    //Immunisation flow
//        val vaccinationFlow = FormatterClass().getSharedPref(
//          "vaccinationFlow",
//          getApplication<Application>().applicationContext)
//        if (vaccinationFlow != null && vaccinationFlow == "createVaccineDetails"){
//          /**
//           * This is used to distinguish between update vaccine history and create vaccine history
//           * For update, we should not create a recommendation, otherwise create according to DAK
//           * TODO: Create recommendation
//           */
//
//        }


                }else{
                    /**
                     * This means there was a contraindication and Vaccination was not done.
                     * Create an ImmunisationRecommendation for this, according to the nextDate provided
                     */
                    immunisationStatus = ImmunizationStatus.NOTDONE


                    //add CodeableConcept for status
                    val codeableConcept = CodeableConcept()
                    //Add coding
                    val codingList = ArrayList<Coding>()
                    val coding = Coding()
                    coding.code = statusReason.code
                    codingList.add(coding)


                    codeableConcept.coding = codingList
                    codeableConcept.text = statusReason.value


                    immunization.statusReason = codeableConcept


                    // Get the provided date after having a contraindication
                    val nextVisit = observationFromCode(
                        "date-next-dose-value",
                        patientId,
                        encounterId)
                    val dateNext = nextVisit.value


                    Log.e("-----****","------$dateNext")




                    val nextDate = FormatterClass().convertStringToDate(dateNext, "YYYY-MM-DD")
                    createImmunisationRecommendation(nextDate, immunization, patientId, encounterId)


                }
            }else{
                //This is an update
                immunisationStatus = ImmunizationStatus.COMPLETED


                // Get the type of vaccine
                val vaccineTypeObs = observationFromCode(
                    "type-of-vaccine-group",
                    patientId,
                    encounterId)
                val vaccineType = vaccineTypeObs.value


                //Date of last dose
                val lastDoseDateObs = observationFromCode(
                    "date-next-dose-value",
                    patientId,
                    encounterId)
                val lastDoseDate = lastDoseDateObs.value


                val date = FormatterClass().convertStringToDate(lastDoseDate, "")
                if (date != null) immunization.occurrenceDateTimeType.value = date


                //Target Disease


                val protocolList = Immunization().protocolApplied
                val immunizationProtocolAppliedComponent = Immunization.ImmunizationProtocolAppliedComponent()
                val diseaseTargetCodeableConceptList = immunizationProtocolAppliedComponent.targetDisease
                val diseaseTargetCodeableConcept = CodeableConcept()
                diseaseTargetCodeableConcept.text = vaccineType
                diseaseTargetCodeableConceptList.add(diseaseTargetCodeableConcept)
                immunizationProtocolAppliedComponent.targetDisease = diseaseTargetCodeableConceptList
                protocolList.add(immunizationProtocolAppliedComponent)


                immunization.protocolApplied = protocolList


            }

            //Immunisation status
            immunization.status = immunisationStatus


            FormatterClass().deleteSharedPref("vaccinationFlow",
                getApplication<Application>().applicationContext)


            saveResourceToDatabase(immunization, "Imm "+immunizationId)
        }

    }


    private fun generateImmunisation(immunization: Immunization): Immunization {

        /**
         * TODO: Administered By = Performer.actor. There needs to be a Practitioner
         */

        /**
         * Create immunisation resource
         * diseaseTargeted = sharedPref.
         * status & status reason = vaccineAdministered & reason(if no)
         * location  = site_administered
         * dateAdministered = system date
         * administeredBy = performer.actor
         * vaccineBatchNo = Iot number
         * expirationDate = expirationDate
         * doseQty = Dose Qty
         * nextVaccinationDate = **Create ImmunisationRequest
         */
        val stockList = ArrayList<String>()
        stockList.addAll(
            listOf(
                "vaccinationTargetDisease",
                "vaccinationDosage",
                "vaccinationAdministrationMethod",
                "vaccinationBatchNumber",
                "vaccinationExpirationDate",
                "vaccinationBrand",
                "vaccinationManufacturer"
            )
        )
        //Date administered
        immunization.occurrenceDateTimeType.value = Date()


        //Target Disease
        val targetDisease = FormatterClass().getSharedPref(
            "vaccinationTargetDisease",
            getApplication<Application>().applicationContext
        )

        val protocolList = Immunization().protocolApplied
        val immunizationProtocolAppliedComponent =
            Immunization.ImmunizationProtocolAppliedComponent()
        val diseaseTargetCodeableConceptList = immunizationProtocolAppliedComponent.targetDisease
        val diseaseTargetCodeableConcept = CodeableConcept()
        diseaseTargetCodeableConcept.text = targetDisease
        diseaseTargetCodeableConceptList.add(diseaseTargetCodeableConcept)
        immunizationProtocolAppliedComponent.targetDisease = diseaseTargetCodeableConceptList
        protocolList.add(immunizationProtocolAppliedComponent)

        immunization.protocolApplied = protocolList

        //Dosage
        val dosage = FormatterClass().getSharedPref("vaccinationDosage",
            getApplication<Application>().applicationContext)
        if (dosage != null){
            val nonDosage = FormatterClass().removeNonNumeric(dosage)
            val bigDecimalValue = BigDecimal(nonDosage)
            val simpleQuantity = SimpleQuantity()
            simpleQuantity.value = bigDecimalValue
            immunization.doseQuantity = simpleQuantity
        }


        //Administration Method
        val vaccinationAdministrationMethod = FormatterClass().getSharedPref(
            "vaccinationAdministrationMethod",
            getApplication<Application>().applicationContext
        )
        if (vaccinationAdministrationMethod != null) {
            val codeableConcept = CodeableConcept()
            codeableConcept.text = vaccinationAdministrationMethod
            codeableConcept.id = generateUuid()

            immunization.site = codeableConcept
        }

        //Batch number
        val vaccinationBatchNumber = FormatterClass().getSharedPref(
            "vaccinationBatchNumber",
            getApplication<Application>().applicationContext
        )
        if (vaccinationBatchNumber != null) {
            immunization.lotNumber = vaccinationBatchNumber
        }

        //Expiration date
        val vaccinationExpirationDate = FormatterClass().getSharedPref(
            "vaccinationExpirationDate",
            getApplication<Application>().applicationContext
        )
        if (vaccinationExpirationDate != null) {
            val dateExp = FormatterClass().convertStringToDate(
                vaccinationExpirationDate, "YYYY-MM-DD"
            )
            if (dateExp != null) {
                immunization.expirationDate = dateExp
            }

        }

        return immunization

    }

    private suspend fun createImmunisationRecommendation(
        recommendedDate: Date?,
        immunization: Immunization,
        patientId: String,
        encounterId: String) {


        val immunizationRecommendation = ImmunizationRecommendation()

        val encounterReference = Reference("Encounter/$encounterId")
        val patientReference = Reference("Patient/$patientId")

        val id = generateUuid()


        immunizationRecommendation.patient = patientReference
        immunizationRecommendation.id = id


        if (recommendedDate != null) immunizationRecommendation.date = recommendedDate

        //Recommendation
        val recommendationList = ArrayList<ImmunizationRecommendation.ImmunizationRecommendationRecommendationComponent>()
        val immunizationRequest = ImmunizationRecommendation.ImmunizationRecommendationRecommendationComponent()


        //Target Disease
        val codeableConceptTargetDisease = CodeableConcept()
        val protocolApplied = immunization.protocolApplied


        protocolApplied.forEach { appliedComponent ->
            val appliedTargetDisease = appliedComponent.targetDisease
            appliedTargetDisease.forEach {


                if (it.hasText()) codeableConceptTargetDisease.text = it.text
                if (it.hasCoding()) codeableConceptTargetDisease.coding = it.coding
            }
        }
//    codeableConceptTargetDisease.id = generateUuid()

    
        immunizationRequest.targetDisease = codeableConceptTargetDisease


        //Dose number
        val doseNumber = immunization.doseQuantity.value
        val intValue = doseNumber.toInt()
        val positiveIntType = PositiveIntType(intValue)
        immunizationRequest.doseNumber = positiveIntType


        //Supporting immunisation
        val immunizationReferenceList = ArrayList<Reference>()
        val immunizationReference = Reference()
        immunizationReference.reference = "Immunization/${immunization.id}"
        immunizationReference.display = "Immunization"
        immunizationReferenceList.add(immunizationReference)


        immunizationRequest.supportingImmunization = immunizationReferenceList


        recommendationList.add(immunizationRequest)
        immunizationRecommendation.recommendation = recommendationList


        saveResourceToDatabase(immunizationRecommendation, "ImmReccomend "+id)


    }


    private suspend fun observationFromCode(
        codeValue: String,
        patientId: String,
        encounterId: String
    ):
            DbCodeValue {

        val observations = mutableListOf<PatientListViewModel.ObservationItem>()
        fhirEngine
            .search<Observation> {
                filter(Observation.CODE, {
                    value = of(Coding().apply {
                        code = codeValue
                    })
                })
                filter(Observation.SUBJECT, { value = "Patient/$patientId" })
                filter(Observation.ENCOUNTER, { value = "Encounter/$encounterId" })
            }
            .take(1)
            .map { createObservationItem(it, getApplication<Application>().resources) }
            .let { observations.addAll(it) }

        //Return limited results
        var code = ""
        var value = ""
        observations.forEach {
            code = it.code
            value = it.value
        }


        return DbCodeValue(code, value)

    }

    fun createObservationItem(
        observation: Observation,
        resources: Resources
    ): PatientListViewModel.ObservationItem {

        Log.e("*****", "*****")
        println(observation)
        println(observation.value)


        // Show nothing if no values available for datetime and value quantity.
        var issuedDate = ""
        if (observation.hasIssued()) {
            issuedDate = observation.issued.toString()
        } else {

            if (observation.hasMeta()) {
                if (observation.meta.hasLastUpdated()) {
                    issuedDate = observation.meta.lastUpdated.toString()
                } else {
                    ""
                }
            } else {
                ""
            }

        }


        val id = observation.logicalId
        val text = observation.code.text ?: observation.code.codingFirstRep.display
        val code = observation.code.coding[0].code
        val value =
            if (observation.hasValueQuantity()) {
                observation.valueQuantity.value.toString()
            } else if (observation.hasValueCodeableConcept()) {
                observation.valueCodeableConcept.coding.firstOrNull()?.display ?: ""
            } else if (observation.hasValueStringType()) {
                observation.valueStringType.asStringValue().toString() ?: ""
            } else {
                ""
            }
        val valueUnit =
            if (observation.hasValueQuantity()) {
                observation.valueQuantity.unit ?: observation.valueQuantity.code
            } else {
                ""
            }
        val valueString = "$value $valueUnit"

        //Get Date
//    var newDate = ""
//    if (issuedDate != ""){
//      val convertedDate = FormatterClass().convertFhirDate(issuedDate)
//      if (convertedDate != null){
//        newDate = convertedDate
//      }
//    }

        //Get Time
//    var newTime = ""
//    if (issuedDate != ""){
//      val convertedDate = FormatterClass().convertFhirTime(issuedDate)
//      if (convertedDate != null){
//        newTime = convertedDate
//      }
//    }

        return PatientListViewModel.ObservationItem(
            id,
            code,
            text,
            valueString
        )
    }


    private suspend fun saveResourceToDatabase(resource: Resource, type: String) {

        Log.e("----", "----$type")
        fhirEngine.create(resource)

    }


    //  private fun isRequiredFieldMissing(bundle: Bundle): Boolean {
//    bundle.entry.forEach {
//      val resource = it.resource
//      when (resource) {
//        is Observation -> {
//          if (resource.hasValueQuantity() && !resource.valueQuantity.hasValueElement()) {
//            return true
//          }
//        }
//        // TODO check other resources inputs
//      }
//    }
//    return false
//  }
    private fun getQuestionnaireJson(): String {
        questionnaireJson?.let {
            return it!!
        }
        questionnaireJson =
            readFileFromAssets(state[AdministerVaccineFragment.QUESTIONNAIRE_FILE_PATH_KEY]!!)
        return questionnaireJson!!
    }

    private fun readFileFromAssets(filename: String): String {
        return getApplication<Application>().assets.open(filename).bufferedReader().use {
            it.readText()
        }
    }

    private fun generateUuid(): String {
        return UUID.randomUUID().toString()
    }
}
