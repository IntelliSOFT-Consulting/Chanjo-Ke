package com.intellisoft.chanjoke.vaccine.validations

import java.time.LocalDate

// Interface segregation principle
interface DbVaccine {
    val vaccineCode: String
    val vaccineName: String
    val administrativeMethod: String
    val administrativeWeeksSinceDOB: Int
    val administrativeWeeksSincePrevious: Int
    val dosage: String
}

data class BasicVaccine(
    override val vaccineCode: String,
    override val vaccineName: String,
    override val administrativeMethod: String,
    override val administrativeWeeksSinceDOB: Int,
    override val administrativeWeeksSincePrevious: Int,
    override val dosage: String
) : DbVaccine

data class SeriesVaccine(
    override val vaccineCode: String,
    override val vaccineName: String,
    val seriesDoses: Int,
    val vaccineList: List<BasicVaccine>,
    val related: List<String>? = null
) : DbVaccine {

    // Implementing properties of the DbVaccine interface
    override val administrativeMethod: String
        get() = vaccineList.firstOrNull()?.administrativeMethod ?: "N/A"

    override val administrativeWeeksSinceDOB: Int
        get() = vaccineList.firstOrNull()?.administrativeWeeksSinceDOB ?: 0

    override val administrativeWeeksSincePrevious: Int
        get() = vaccineList.firstOrNull()?.administrativeWeeksSincePrevious ?: 0

    override val dosage: String
        get() = vaccineList.firstOrNull()?.dosage ?: "N/A"
}


fun createVaccines(): List<SeriesVaccine> {
    // Polio
    val polio = "IMPO-"
    val polioSeries = SeriesVaccine(
        polio,
        "POLIO",
        4,
        listOf(
            BasicVaccine(polio+"OPV", "OPV", "Oral", 0, 0, "2 drops"),
            BasicVaccine(polio+"OPV-I", "OPV I", "Oral", 6, 0, "2 drops"),
            BasicVaccine(polio+"OPV-II", "OPV II", "Oral", 10, 0, "2 drops"),
            BasicVaccine(polio+"OPV-III", "OPV III", "Oral", 14, 0, "2 drops")
        )
    )

    //Yellow Fever
    val yellowFever = "IMYF-"
    val yellowFeverSeries = SeriesVaccine(
        yellowFever,
        "YELLOW FEVER",
        1,
        listOf(
            BasicVaccine(yellowFever+"I", "Yellow Fever", "Subcutaneous left upper arm", 40, 0, "0.5ml")
        )
    )

    //BCG
    /**
     * TODO: Check about the dosages
     */
    val bcg = "IMBCG-"
    val bcgSeries = SeriesVaccine(
        bcg,
        "BCG",
        1,
        listOf(
            BasicVaccine(bcg+"I", "BCG", "Intradermal", 0, 0, "0.5ml")
        )
    )

    //DPT-HepB+Hib
    val dpt = "IMDPT-"
    val dptSeries = SeriesVaccine(
        dpt,
        "DPT-HepB+Hib",
        1,
        listOf(
            BasicVaccine(dpt+"1", "DPT-HepB+Hib 1", "Intramuscular into the upper outer aspect of left thigh", 6, 0, "0.5ml"),
            BasicVaccine(dpt+"2", "DPT-HepB+Hib 2", "Intramuscular into the upper outer aspect of left thigh", 10, 0, "0.5ml"),
            BasicVaccine(dpt+"3", "DPT-HepB+Hib 3", "Intramuscular into the upper outer aspect of left thigh", 14, 0, "0.5ml")
        )
    )

    //PCV10
    val pcv = "IMPCV10-"
    val pcvSeries = SeriesVaccine(
        pcv,
        "PCV10",
        1,
        listOf(
            BasicVaccine(pcv+"1", "PCV10 1", "Intramuscular into the upper outer aspect of right thigh", 6, 0, "0.5ml"),
            BasicVaccine(pcv+"2", "PCV10 2", "Intramuscular into the upper outer aspect of right thigh", 10, 0, "0.5ml"),
            BasicVaccine(pcv+"3", "PCV10 3", "Intramuscular into the upper outer aspect of right thigh", 14, 0, "0.5ml")
        )
    )

    //Measles
    val measles = "IMMEAS-"
    val measlesSeries = SeriesVaccine(
        measles,
        "MEASLES",
        1,
        listOf(
            BasicVaccine(measles+"1", "Measles-Rubella 1st Dose", "Subcutaneous into the right upper arm (deltoid muscle)", 27, 0, "0.5ml"),
            BasicVaccine(measles+"2", "Measles-Rubella 2nd Dose", "Subcutaneous into the right upper arm (deltoid muscle)", 40, 0, "0.5ml")
        )
    )


    return listOf(
        polioSeries, yellowFeverSeries, bcgSeries, dptSeries, pcvSeries, measlesSeries

    )
}

class ImmunizationHandler() {

    val vaccines = createVaccines()
    // Open-closed principle
    fun getVaccineDetails(vaccineCode: String): Pair<String, Any?>  {
        val vaccine = vaccines.find { it.vaccineCode == vaccineCode }
        return vaccine?.let { getSeriesVaccineDetails(it) } ?: Pair("ERROR", null)
    }

    fun getVaccineDetailsByName(vaccineName: String): Pair<String, Any?> {
        val vaccine = vaccines.find { it.vaccineName == vaccineName }
        return vaccine?.let { getSeriesVaccineDetails(it) } ?: Pair("ERROR", null)
    }

    private fun getSeriesVaccineDetails(vaccine: DbVaccine): Pair<String, Any?> {

        return if (vaccine is SeriesVaccine){
            Pair("SERIES", vaccine)
        }else{
            Pair("BASE", vaccine)
        }



//        return if (vaccine is SeriesVaccine) {
//            buildString {
//                append("DbVaccine Code: ${vaccine.vaccineCode}\n")
//                append("DbVaccine Name: ${vaccine.vaccineName}\n")
//                append("Administrative Method: ${vaccine.administrativeMethod}\n")
//                append("Administrative Weeks Since DOB: ${vaccine.administrativeWeeksSinceDOB}\n")
//                append("Administrative Weeks Since Previous: ${vaccine.administrativeWeeksSincePrevious}\n")
//                append("Dosage: ${vaccine.dosage}\n")
//                append("DbVaccine List:\n")
//                vaccine.vaccineList.forEach { basicVaccine ->
//                    append(getBaseVaccineDetails(basicVaccine))
//                }
//                append("\n")
//            }
//        } else {
//            getBaseVaccineDetails(vaccine as BasicVaccine)
//        }
    }

    fun generateVaccineLists(): Pair<List<String>, Map<String, List<String>>> {
        val groupList = mutableListOf<String>()
        val childList = mutableMapOf<String, List<String>>()

        vaccines.forEach { vaccine ->
            groupList.add(vaccine.vaccineName)
            childList[vaccine.vaccineName] = vaccine.vaccineList.map { it.vaccineName }
        }

        return Pair(groupList, childList)
    }

    private fun getBaseVaccineDetails(basicVaccine: BasicVaccine): String {
        return buildString {
            append("DbVaccine Code: ${basicVaccine.vaccineCode}\n")
            append("DbVaccine Name: ${basicVaccine.vaccineName}\n")
            append("Administrative Method: ${basicVaccine.administrativeMethod}\n")
            append("Administrative Weeks Since DOB: ${basicVaccine.administrativeWeeksSinceDOB}\n")
            append("Administrative Weeks Since Previous: ${basicVaccine.administrativeWeeksSincePrevious}\n")
            append("Dosage: ${basicVaccine.dosage}\n")
            append("\n")
        }
    }

    // Liskov substitution principle
    fun getAvailableVaccines(dob: LocalDate = LocalDate.now()): List<AvailableVaccine?> {
        return vaccines.map { vaccine ->
            if (vaccine is SeriesVaccine) {
                val isEligible = checkEligibility(vaccine, dob)
                AvailableVaccine(vaccine, isEligible)
            } else {
                null
            }
        }
    }

    private fun checkEligibility(seriesVaccine: SeriesVaccine, dob: LocalDate): Boolean {
        val weeksSinceDOB = LocalDate.now().minusWeeks(seriesVaccine.administrativeWeeksSinceDOB.toLong())
        return weeksSinceDOB.isAfter(dob)
    }

    fun groupRelatedVaccines(): Map<String, List<DbVaccine>> {
        val groupedVaccines = mutableMapOf<String, MutableList<DbVaccine>>()

        vaccines.forEach { vaccine ->
            if (vaccine is SeriesVaccine && !vaccine.related.isNullOrEmpty()) {
                vaccine.related.forEach { relatedCode ->
                    val relatedVaccine = vaccines.find { it.vaccineCode == relatedCode }
                    if (relatedVaccine != null) {
                        groupedVaccines
                            .getOrPut(vaccine.vaccineCode) { mutableListOf() }
                            .add(vaccine)
                        groupedVaccines
                            .getOrPut(relatedCode) { mutableListOf() }
                            .add(relatedVaccine)
                    }
                }
            }
        }

        return groupedVaccines
    }

    data class AvailableVaccine(val vaccine: SeriesVaccine, val isEligible: Boolean)


}



//fun main() {
//    val vaccines = listOf(
//        SeriesVaccine(
//            "IMPO",
//            "Polio",
//            4,
//            listOf(
//                BasicVaccine("IMPO-OPV", "OPV", "Oral", 4, 0, "2 drops"),
//                BasicVaccine("IMPO-OPV-I", "OPV I", "Oral", 6, 0, "2 drops"),
//                BasicVaccine("IMPO-OPV-II", "OPV II", "Oral", 10, 0, "2 drops"),
//                BasicVaccine("IMPO-OPV-III", "OPV III", "Oral", 14, 0, "2 drops")
//            ),
//            related = listOf("IM-YF-I")
//        ),
//        SeriesVaccine(
//            "IM-YF",
//            "YELLOW FEVER",
//            1,
//            listOf(
//                BasicVaccine("IM-YF-I", "Yellow Fever", "Injection", 14, 0, "0.5ml")
//            )
//        )
//    )
//
//    val immunizationHandler = ImmunizationHandler(vaccines)
//
//    // Get available vaccines
//    val availableVaccines = immunizationHandler.getAvailableVaccines(LocalDate.now().minusWeeks(4))
//    availableVaccines.forEach {
//        println("DbVaccine: ${it?.vaccine?.vaccineName}, Eligible: ${it?.isEligible}")
//    }
//
//    // Group related vaccines
//    val groupedVaccines = immunizationHandler.groupRelatedVaccines()
//    groupedVaccines.forEach { (vaccineCode, relatedVaccines) ->
//        println("\nRelated Vaccines for $vaccineCode:")
//        relatedVaccines.forEach { relatedVaccine ->
//            println("  - ${relatedVaccine.vaccineName}")
//        }
//    }
//}
