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
import com.google.android.fhir.datacapture.mapping.ResourceMapper
import com.google.android.fhir.logicalId
import com.google.android.fhir.search.search
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.util.UUID
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.CodeableConcept
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.Condition
import org.hl7.fhir.r4.model.Encounter
import org.hl7.fhir.r4.model.Immunization
import org.hl7.fhir.r4.model.ImmunizationRecommendation
import org.hl7.fhir.r4.model.Observation
import org.hl7.fhir.r4.model.Questionnaire
import org.hl7.fhir.r4.model.QuestionnaireResponse
import org.hl7.fhir.r4.model.Reference
import org.hl7.fhir.r4.model.Resource
import org.hl7.fhir.r4.model.StringType

/** ViewModel for patient registration screen {@link AddPatientFragment}. */
class AdministerVaccineViewModel(application: Application, private val state: SavedStateHandle) :
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

      Log.e("-----","hhhhhhhh")

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
            saveResourceToDatabase(resource, "Obs "+uuid)
          }
        }
        is Condition -> {
          if (resource.hasCode()) {
            val uuid = generateUuid()
            resource.id = uuid
            resource.subject = subjectReference
            resource.encounter = encounterReference
            saveResourceToDatabase(resource, "cond "+uuid)
          }
        }
        is Encounter -> {
          resource.subject = subjectReference
          resource.id = encounterId
          saveResourceToDatabase(resource, "enc "+encounterId)
          createImmunisationRecord(encounterId, patientId)
        }
      }
    }
  }

  private fun createImmunisationRecord(encounterId: String, patientId: String) {

    CoroutineScope(Dispatchers.IO).launch {

      val uuid = generateUuid()
      val encounterReference = Reference("Encounter/$encounterId")
      val patientReference = Reference("Patient/$patientId")

      //Have a list of the observation codes

      val immunization = Immunization()

      immunization.encounter = encounterReference
      immunization.patient = patientReference
      immunization.id = uuid

      val protocolList = Immunization().protocolApplied
      val immunizationProtocolAppliedComponent = Immunization.ImmunizationProtocolAppliedComponent()

      //Disease target
      val diseaseTarget = observationFromCode(
        "8867-4",
        patientId,
        encounterId)
      val diseaseTargetCodeableConceptList = immunizationProtocolAppliedComponent.targetDisease
      val diseaseTargetCodeableConcept = CodeableConcept()
      diseaseTargetCodeableConcept.text = diseaseTarget.value
      diseaseTargetCodeableConceptList.add(diseaseTargetCodeableConcept)
      immunizationProtocolAppliedComponent.targetDisease = diseaseTargetCodeableConceptList

      //Dose number
      val doseNumber = observationFromCode(
        "408102007",
        patientId,
        encounterId)
      val stringTypeDoseNumber = StringType()
      stringTypeDoseNumber.value = doseNumber.value
      immunizationProtocolAppliedComponent.doseNumber = stringTypeDoseNumber

      //Combine Protocol list
      protocolList.add(immunizationProtocolAppliedComponent)
      immunization.protocolApplied = protocolList
      val immunizationId = generateUuid()
      immunization.id = immunizationId

      fhirEngine.create(immunization)

      //create ImmunisationRecommendation
//      createImmunisationRecommendation(immunizationId, patientId, encounterId)


    }



  }

  private suspend fun createImmunisationRecommendation(immunizationId: String, patientId: String, encounterId: String) {
    val encounterReference = Reference("Encounter/$encounterId")
    val patientReference = Reference("Patient/$patientId")

    //date of next visit
    val nextVisit = observationFromCode(
      "390840006",
      patientId,
      encounterId)
    val immunizationRecommendation = ImmunizationRecommendation()
    immunizationRecommendation.patient = patientReference
//      immunizationRecommendation.date = ""


    val immunizationRecommendationRecommendationComponent =
      ImmunizationRecommendation.
      ImmunizationRecommendationRecommendationComponent()

//    immunizationRecommendationRecommendationComponent.

//    immunizationRecommendation.setRecommendation()

  }

  private suspend fun observationFromCode(codeValue: String, patientId: String, encounterId: String):
          DbCodeValue{

    val observations = mutableListOf<PatientListViewModel.ObservationItem>()
    fhirEngine
      .search<Observation> {
        filter(Observation.CODE, {value = of(Coding().apply {
          code = codeValue
        })})
        filter(Observation.SUBJECT, {value = "Patient/$patientId"})
        filter(Observation.ENCOUNTER, {value = "Encounter/$encounterId"})
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

  fun createObservationItem(observation: Observation, resources: Resources): PatientListViewModel.ObservationItem {

    Log.e("*****","*****")
    println(observation)
    println(observation.value)


    // Show nothing if no values available for datetime and value quantity.
    var issuedDate = ""
    if (observation.hasIssued()){
      issuedDate = observation.issued.toString()
    }else{

      if (observation.hasMeta()){
        if (observation.meta.hasLastUpdated()){
          issuedDate = observation.meta.lastUpdated.toString()
        }else{
          ""
        }
      }else{
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
      }else if (observation.hasValueStringType()) {
        observation.valueStringType.asStringValue().toString() ?: ""
      }else {
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
      valueString)
  }


  private suspend fun saveResourceToDatabase(resource: Resource, type:String) {

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
    questionnaireJson = readFileFromAssets(state[AdministerVaccineFragment.QUESTIONNAIRE_FILE_PATH_KEY]!!)
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
