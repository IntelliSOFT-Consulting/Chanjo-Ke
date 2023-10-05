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

package com.dave.zanzibar.vaccine

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import com.dave.zanzibar.fhir.FhirApplication
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.datacapture.mapping.ResourceMapper
import com.google.android.fhir.datacapture.validation.Invalid
import com.google.android.fhir.datacapture.validation.QuestionnaireResponseValidator
import java.util.UUID
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Condition
import org.hl7.fhir.r4.model.Encounter
import org.hl7.fhir.r4.model.Observation
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.Questionnaire
import org.hl7.fhir.r4.model.QuestionnaireResponse
import org.hl7.fhir.r4.model.Reference
import org.hl7.fhir.r4.model.Resource

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

      saveResources(bundle, subjectReference, encounterId)

      isResourcesSaved.value = true
    }
  }

  private suspend fun saveResources(
    bundle: Bundle,
    subjectReference: Reference,
    encounterId: String,
  ) {
    val uuid = generateUuid()
    val encounterReference = Reference("Encounter/$encounterId")
    bundle.entry.forEach {



      when (val resource = it.resource) {
        is Observation -> {
          if (resource.hasCode()) {
            resource.id = uuid
            resource.subject = subjectReference
            resource.encounter = encounterReference
            saveResourceToDatabase(resource, "Obs "+uuid)
          }
        }
        is Condition -> {
          if (resource.hasCode()) {
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
        }
      }
    }
  }

  private suspend fun saveResourceToDatabase(resource: Resource, type:String) {
    Log.e("-----","------")
    println(type)
    println(resource)
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
