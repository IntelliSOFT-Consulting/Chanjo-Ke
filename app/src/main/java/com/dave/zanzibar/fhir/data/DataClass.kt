package com.dave.zanzibar.fhir.data

data class DbVaccineData(
    val vaccineName:String,
    val vaccineDosage:String
)
data class EncounterItem(
    val id: String,
    val code: String,
    val effective: String,
    val value: String
) {
    override fun toString(): String = code
}