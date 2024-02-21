package com.intellisoft.chanjoke.detail.ui.main.registration

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.search.Order
import com.google.android.fhir.search.StringFilterModifier
import com.google.android.fhir.search.search
import com.intellisoft.chanjoke.patient_list.PatientListViewModel
import com.intellisoft.chanjoke.patient_list.toPatientItem
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.Location
import org.hl7.fhir.r4.model.Patient
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class LocationViewModel(application: Application, private val fhirEngine: FhirEngine) :
    AndroidViewModel(application) {
    val liveSearchedPatients = MutableLiveData<List<LocationItem>>()

    init {
        loadCounties({ getSearchResults() })
    }

    private fun loadCounties(
        search: suspend () -> List<LocationItem>
    ) {
        viewModelScope.launch {
            liveSearchedPatients.value = search()
        }
    }

    private suspend fun getSearchResults(nameQuery: String = ""): List<LocationItem> {
        var patients: MutableList<LocationItem> = mutableListOf()
        fhirEngine
            .search<Location> {
                sort(Location.NAME, Order.DESCENDING)
                count = 100
                from = 0
            }
            .mapIndexed { index, fhirPatient -> fhirPatient.toPatientItem(index + 1) }
            .let { patients.addAll(it) }
        return patients
    }

    internal fun Location.toPatientItem(position: Int): LocationItem {
        // Show nothing if no values available for gender and date of birth.
        val patientId = if (hasIdElement()) idElement.idPart else ""
        val name = if (hasName()) name else ""

        return LocationItem(
            id = position.toString(),
            resourceId = patientId,
            name = name,
        )
    }

    data class LocationItem(
        val id: String,
        val resourceId: String,
        val name: String,
    ) {
        override fun toString(): String = name
    }
}