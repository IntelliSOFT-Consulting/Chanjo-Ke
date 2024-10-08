package com.intellisoft.chanjoke.detail.ui.main

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.fhir.data.Reasons
import com.intellisoft.chanjoke.fhir.data.ReusableListItem
import com.intellisoft.chanjoke.fhir.data.Status
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModel

class ReusableViewModel(
    private val patientDetailsViewModel: PatientDetailsViewModel) : ViewModel() {
    private val _items = MutableLiveData<List<ReusableListItem>>()
    val items: LiveData<List<ReusableListItem>> get() = _items

    init {
        _items.value = listOf() // Initialize with empty or default list
    }

    fun fetchItems(type: String, context: Context) {

        val formatterClass = FormatterClass()

        val notAdministeredList = ArrayList(
            listOf(
                "371900001",
                // Add other IDs as necessary
            )
        )

        // Call the method from PatientDetailsViewModel
        val clientObjectionList = patientDetailsViewModel.getRecommendationStatus(notAdministeredList)
        val reusableListItemList = ArrayList<ReusableListItem>()

        clientObjectionList.forEach {
            val statusValue = it.statusValue
            val vaccineName = it.vaccineName

            if (statusValue != null ){
                if(statusValue.contains("Client objection ") ||
                    statusValue.contains("Religion/Culture ")){

                    /**
                     * Logic for Routine / Non-routine is required
                     */

                    val message = "$vaccineName was not administered due to $statusValue"
                    val reusableListItem = ReusableListItem(message, Status.NOT_ADMINISTERED)

                    val exists = reusableListItemList.any {
                        reusable -> reusable.name == message &&
                            reusable.status == Status.NOT_ADMINISTERED
                    }
                    // If it doesn't exist, add the item to the list
                    if (!exists) {
                        reusableListItemList.add(reusableListItem)
                    }

                }
                if (statusValue.contains("Contraindicate")){
                    formatterClass.saveSharedPref("${Reasons.CONTRAINDICATE.name} VALUES",
                        vaccineName, context)
                }
            }

        }




        _items.value = reusableListItemList

//        _items.value = if (type == "Routine") {
//            listOf(
//                ReusableListItem("Item 1", Status.ADMINISTERED),
//                ReusableListItem("Item 2", Status.NOT_ADMINISTERED),
//                ReusableListItem("Item 3", Status.RESCHEDULED)
//            )
//        } else {
//            listOf(
//                ReusableListItem("Item A", Status.NOT_ADMINISTERED),
//                ReusableListItem("Item B", Status.ADMINISTERED)
//            )
//        }
    }

    fun removeItem(item: ReusableListItem) {
        _items.value = _items.value?.filter { it != item }
    }
}

class ReusableViewModelFactory(
    private val patientDetailsViewModel: PatientDetailsViewModel
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReusableViewModel::class.java)) {
            return ReusableViewModel(patientDetailsViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

