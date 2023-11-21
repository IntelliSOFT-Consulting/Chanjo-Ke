package com.intellisoft.chanjoke.vaccine.validations

import java.time.LocalDate

// Interface for the Vaccine
interface Vaccine {
    fun isEligible(dob: LocalDate): Boolean
    fun details(): VaccineDetails
    fun administer()
}

// Data class to represent vaccine details
data class VaccineDetails(
    val name: String,
    val dosage: String,
    val administrationMethod: String,
    val timeToAdminister: String)

// Base class for vaccines with common properties and methods
abstract class BaseVaccine(
    private val weeksAfterDob: Long,
    private val dosage: String,
    private val administrationMethod: String) : Vaccine {
    override fun isEligible(dob: LocalDate): Boolean {
        return LocalDate.now() >= dob.plusWeeks(weeksAfterDob)
    }

    override fun details(): VaccineDetails {
        return VaccineDetails(javaClass.simpleName, dosage, administrationMethod, "Given at ${weeksAfterDob} weeks after birth")
    }

    override fun administer() {
        val details = details()
        println("Administering ${details.dosage} drops ${details.administrationMethod} - ${details.timeToAdminister}")
    }
}

// Polio vaccines
class bOPV : BaseVaccine(0, "2 dosages", "Oral")
class OPV1 : BaseVaccine(6, "2 dosages", "Oral")
class OPV2 : BaseVaccine(10, "2 dosages", "Oral")
class OPV3 : BaseVaccine(14, "2 dosages", "Oral")

// Measles vaccines
class Measles1 : BaseVaccine(24, "0.5ml", "Subcutaneous right upper arm")
class Measles2 : BaseVaccine(36, "0.5ml", "Subcutaneous right upper arm")
class Measles3 : BaseVaccine(78, "0.5ml", "Subcutaneous right upper arm")

// Yellow Fever vaccine
class YellowFever : BaseVaccine(36, "0.5ml", "Subcutaneous left upper arm")

// Vaccination Manager
class VaccinationManager {
    private val vaccines = mapOf(
        "bOPV" to bOPV(),
        "OPV1" to OPV1(),
        "OPV2" to OPV2(),
        "OPV3" to OPV3(),
        "Measles1" to Measles1(),
        "Measles2" to Measles2(),
        "Measles3" to Measles3(),
        "YellowFever" to YellowFever()
    )

    fun getEligibleVaccines(dob: LocalDate): List<VaccineDetails> {
        return vaccines.values.filter { it.isEligible(dob) }.map { it.details() }
    }

    private fun convertToVaccineCode(input: String): String {
        return when (input.trim().toUpperCase()) {
            "OPV I" -> "OPV1"
            "OPV II" -> "OPV2"
            "OPV III" -> "OPV3"
            "YELLOW FEVER" -> "YellowFever"
            "MEASLES" -> "Measles1"
            else -> input.trim()
        }
    }

    fun getVaccineDetails(vaccineName: String): VaccineDetails? {
        val newVaccine = convertToVaccineCode(vaccineName.capitalize())
        return vaccines[newVaccine]?.details()
    }
}

//fun main() {
//    val dob = LocalDate.of(2022, 1, 1) // Replace with the child's date of birth
//
//    val vaccinationManager = VaccinationManager()
//    val eligibleVaccineDetails = vaccinationManager.getEligibleVaccines(dob)
//
//    if (eligibleVaccineDetails.isNotEmpty()) {
//        println("Eligible Vaccines:")
//        eligibleVaccineDetails.forEach { details ->
//            println("- ${details.name}")
//            println("  - Dosage: ${details.dosage}")
//            println("  - Administration Method: ${details.administrationMethod}")
//            println("  - Time to Administer: ${details.timeToAdminister}")
//            println("  ------------------------")
//        }
//    } else {
//        println("No vaccines are currently eligible for the child.")
//    }

    // Example of getting details for a specific vaccine
//    val vaccineName = "OPV1"
//    val vaccineDetails = vaccinationManager.getVaccineDetails(vaccineName)
//
//    if (vaccineDetails != null) {
//        println("\nDetails for $vaccineName:")
//        println("- Dosage: ${vaccineDetails.dosage}")
//        println("- Administration Method: ${vaccineDetails.administrationMethod}")
//        println("- Time to Administer: ${vaccineDetails.timeToAdminister}")
//    } else {
//        println("\nNo details found for $vaccineName.")
//    }
//}