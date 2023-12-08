package com.intellisoft.chanjoke.vaccine.validations

import android.app.ProgressDialog
import android.content.Context
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.abs

// Interface segregation principle
interface DbVaccine {
    val vaccineCode: String
    val vaccineName: String
    val administrativeMethod: String
    val administrativeWeeksSinceDOB: Int
    val administrativeWeeksSincePrevious: Int
    val doseQuantity: String
    val doseNumber: String //Dose number within series
}

data class BasicVaccine(
    override val vaccineCode: String,
    override val vaccineName: String,
    override val administrativeMethod: String,
    override val administrativeWeeksSinceDOB: Int,
    override val administrativeWeeksSincePrevious: Int,
    override val doseQuantity: String,
    override val doseNumber: String, //Dose number within series
) : DbVaccine

data class SeriesVaccine(
    val diseaseCode: String,
    val targetDisease: String,
    val seriesDoses: Int, // Recommended number of doses for immunity
    val vaccineList: List<BasicVaccine>,
    val related: List<SeriesVaccine>? = null,
    val isRoutine: Boolean = true
) : DbVaccine {
    override val vaccineCode: String
        get() = vaccineList.firstOrNull()?.vaccineCode ?: ""
    override val vaccineName: String
        get() = vaccineList.firstOrNull()?.vaccineName ?: ""

    // Implementing properties of the DbVaccine interface
    override val administrativeMethod: String
        get() = vaccineList.firstOrNull()?.administrativeMethod ?: "N/A"

    override val administrativeWeeksSinceDOB: Int
        get() = vaccineList.firstOrNull()?.administrativeWeeksSinceDOB ?: 0

    override val administrativeWeeksSincePrevious: Int
        get() = vaccineList.firstOrNull()?.administrativeWeeksSincePrevious ?: 0

    override val doseQuantity: String
        get() = vaccineList.firstOrNull()?.doseQuantity ?: "0"
    override val doseNumber: String
        get() = vaccineList.firstOrNull()?.doseNumber ?: "0"
}


fun createVaccines(): List<SeriesVaccine> {
    // Polio
    val polio = "IMPO-"
    val polioSeries = SeriesVaccine(
        polio,
        "Polio",
        5,
        listOf(
            BasicVaccine(polio+"bOPV", "bOPV", "Oral", 0, 0, "2 drops","1"),
            BasicVaccine(polio+"OPV-I", "OPV I", "Oral", 6, 0, "2 drops","2"),
            BasicVaccine(polio+"OPV-II", "OPV II", "Oral", 10, 0, "2 drops","3"),
            BasicVaccine(polio+"OPV-III", "OPV III", "Oral", 14, 0, "2 drops","4"),
            BasicVaccine(polio+"IPV I", "IPV I", "Oral", 14, 0, "2 drops","5")
        )
    )

    //Yellow Fever
    val yellowFever = "IMYF-"
    val yellowFeverSeries = SeriesVaccine(
        yellowFever,
        "Yellow Fever",
        1,
        listOf(
            BasicVaccine(yellowFever+"I", "Yellow Fever", "Subcutaneous left upper arm", 40, 0, "0.5ml","1")
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
            BasicVaccine(bcg+"I", "BCG", "Intradermal", 0, 0, "0.5ml","1")
        )
    )

    //DPT-HepB+Hib
    val dpt = "IMDPT-"
    val dptSeries = SeriesVaccine(
        dpt,
        "DPT-HepB+Hib",
        3,
        listOf(
            BasicVaccine(dpt+"1", "DPT-HepB+Hib 1", "Intramuscular into the upper outer aspect of left thigh", 6, 0, "0.5ml","1"),
            BasicVaccine(dpt+"2", "DPT-HepB+Hib 2", "Intramuscular into the upper outer aspect of left thigh", 10, 0, "0.5ml","2"),
            BasicVaccine(dpt+"3", "DPT-HepB+Hib 3", "Intramuscular into the upper outer aspect of left thigh", 14, 0, "0.5ml","3")
        )
    )

    //PCV10
    val pcv = "IMPCV10-"
    val pcvSeries = SeriesVaccine(
        pcv,
        "PCV10",
        3,
        listOf(
            BasicVaccine(pcv+"1", "PCV10 1", "Intramuscular into the upper outer aspect of right thigh", 6, 0, "0.5ml","1"),
            BasicVaccine(pcv+"2", "PCV10 2", "Intramuscular into the upper outer aspect of right thigh", 10, 0, "0.5ml","2"),
            BasicVaccine(pcv+"3", "PCV10 3", "Intramuscular into the upper outer aspect of right thigh", 14, 0, "0.5ml","3")
        )
    )

    //Measles
    val measles = "IMMEAS-"
    val measlesSeries = SeriesVaccine(
        measles,
        "Measles",
        2,
        listOf(
            BasicVaccine(measles+"0", "Measles-Rubella", "Subcutaneous into the right upper arm (deltoid muscle)", 27, 0, "0.5ml","0"),
            BasicVaccine(measles+"1", "Measles-Rubella 1st Dose", "Subcutaneous into the right upper arm (deltoid muscle)", 40, 0, "0.5ml","1"),
            BasicVaccine(measles+"2", "Measles-Rubella 2nd Dose", "Subcutaneous into the right upper arm (deltoid muscle)", 79, 0, "0.5ml","2")
        )
    )

    //Rota Virus
    val rotaVirus = "IMROTA-"
    val rotaSeries = SeriesVaccine(
        rotaVirus,
        "Rota Virus",
        3,
        listOf(
           BasicVaccine(rotaVirus+"1", "Rota Virus 1st Dose", "Oral", 6, 0, "0.5ml","1"),
           BasicVaccine(rotaVirus+"2", "Rota Virus 2nd Dose", "Oral", 10, 0, "0.5ml","2"),
           BasicVaccine(rotaVirus+"3", "Rota Virus 3rd Dose", "Oral", 14, 0, "0.5ml","3"),
        )
    )

    //Vitamin A
    val vitaminA = "IMVIT-"
    val vitaminASeries = SeriesVaccine(
        vitaminA,
        "Vitamin A",
        10,
        listOf(
           BasicVaccine(vitaminA+"1", "Vitamin A 1st Dose", "Oral", 26, 0, "100,000OUI","1"),
           BasicVaccine(vitaminA+"2", "Vitamin A 2nd Dose", "Oral", 52, 0, "200,000OUI","2"),
           BasicVaccine(vitaminA+"3", "Vitamin A 3rd Dose", "Oral", 78, 0, "1Capsule","3"),
           BasicVaccine(vitaminA+"3", "Vitamin A 4th Dose", "Oral", 104, 0, "1Capsule","4"),
           BasicVaccine(vitaminA+"3", "Vitamin A 5th Dose", "Oral", 130, 0, "1Capsule","5"),
           BasicVaccine(vitaminA+"3", "Vitamin A 6th Dose", "Oral", 156, 0, "1Capsule","6"),
           BasicVaccine(vitaminA+"3", "Vitamin A 7th Dose", "Oral", 182, 0, "1Capsule","7"),
           BasicVaccine(vitaminA+"3", "Vitamin A 8th Dose", "Oral", 208, 0, "1Capsule","8"),
           BasicVaccine(vitaminA+"3", "Vitamin A 9th Dose", "Oral", 234, 0, "1Capsule","9"),
           BasicVaccine(vitaminA+"3", "Vitamin A 10th Dose", "Oral", 260, 0, "1Capsule","10"),
        )
    )

    //Vitamin A
    val malaria = "IMMALA-"
    val malariaSeries = SeriesVaccine(
        malaria,
        "RTS/AS01 (Malaria)",
        4,
        listOf(
            BasicVaccine(malaria+"1", "RTS/AS01 (Malaria Vaccine - 1)", "Intramuscular left deltoid muscle", 26, 0, "0.5ml","1"),
            BasicVaccine(malaria+"2", "RTS/AS01 (Malaria Vaccine - 2)", "Intramuscular left deltoid muscle", 30, 0, "0.5ml","2"),
            BasicVaccine(malaria+"3", "RTS/AS01 (Malaria Vaccine - 3)", "Intramuscular left deltoid muscle", 39, 0, "0.5ml","3"),
            BasicVaccine(malaria+"4", "RTS/AS01 (Malaria Vaccine - 4)", "Intramuscular left deltoid muscle", 104, 0, "0.5ml","4")
        )
    )
    //HPV Vaccine
    val hpvVaccine = "IMHPV-"
    val hpvSeries = SeriesVaccine(
        hpvVaccine,
        "HPV",
        2,
        listOf(
            BasicVaccine(hpvVaccine+"1", "HPV Vaccine 1", "Intramuscular left deltoid muscle", 521, 0, "0.5ml","1"),
            BasicVaccine(hpvVaccine+"2", "HPV Vaccine 2", "Intramuscular left deltoid muscle", 842, 0, "0.5ml","1")
       )
    )

    //Covid

    val covidMain = "IMCOV-"
    val covidMainSeries = SeriesVaccine(
        covidMain,
        "Covid",
        1,
        listOf(
            BasicVaccine(covidMain+"1", "Astrazeneca 1st Dose", "Intramuscular Injection", 939, 0, "0.5ml","1"),
            BasicVaccine(covidMain+"2", "Astrazeneca 2nd Dose", "Intramuscular Injection", 951, 0, "0.5ml","2"),

            BasicVaccine(covidMain+"0", "Johnson & Johnson", "Intramuscular Injection", 939, 0, "0.5ml","1"),

            BasicVaccine(covidMain+"1", "Moderna 1st Dose", "Intramuscular Injection", 939, 0, "0.5ml","1"),
            BasicVaccine(covidMain+"2", "Moderna 2nd Dose", "Intramuscular Injection", 943, 0, "0.5ml","2"),

            BasicVaccine(covidMain+"1", "Pfizer-BioNTech 1st Dose", "Intramuscular Injection", 939, 0, "0.5ml","1"),
            BasicVaccine(covidMain+"2", "Pfizer-BioNTech 2nd Dose", "Intramuscular Injection", 943, 0, "0.5ml","2"),

            BasicVaccine(covidMain+"1", "Sinopharm 1st Dose", "Intramuscular Injection", 939, 0, "0.5ml","1"),
            BasicVaccine(covidMain+"2", "Sinopharm 2nd Dose", "Intramuscular Injection", 943, 0, "0.5ml","2"),
            ),
        related = null,
        false
    )

    return listOf(polioSeries, yellowFeverSeries, bcgSeries, dptSeries, pcvSeries, measlesSeries,covidMainSeries,rotaSeries,vitaminASeries,malariaSeries,hpvSeries)
}

class ImmunizationHandler() {

    val vaccines = createVaccines()
    // Open-closed principle

    fun getVaccineDetailsByBasicVaccineName(vaccineName: String): DbVaccine? {

        return vaccines
            .asSequence()
            .filterIsInstance<SeriesVaccine>()
            .flatMap { it.vaccineList.asSequence() + it } // Flatten SeriesVaccine to include BasicVaccine
            .find { it.vaccineName == vaccineName }
    }

    // Function to get series vaccine details by code
    fun getSeriesVaccineDetailsBySeriesTargetName(targetDisease: String): SeriesVaccine? {
        return vaccines
            .filterIsInstance<SeriesVaccine>()
            .find { it.targetDisease == targetDisease }
    }

    fun eligibleVaccineList(context: Context, patientDetailsViewModel: PatientDetailsViewModel)= runBlocking {
        generateVaccineLists(context, patientDetailsViewModel)
    }

    private suspend fun generateVaccineLists(context: Context, patientDetailsViewModel: PatientDetailsViewModel):
            Pair<List<String>, Map<String, List<String>>> {

        val groupList = mutableListOf<String>()
        val childList = mutableMapOf<String, List<String>>()

        var progressDialog =  ProgressDialog(context)
        progressDialog.setMessage("Loading Vaccines...")
        progressDialog.setTitle("Please wait")
        progressDialog.show()

        val job = Job()
        CoroutineScope(Dispatchers.IO + job).launch {
            val patientDob = FormatterClass().getSharedPref("patientDob", context)
            if (patientDob != null){
                val dobDate = FormatterClass().convertStringToDate(patientDob,"yyyy-MM-dd")
                if (dobDate != null){
                    val dobLocalDate = FormatterClass().convertDateToLocalDate(dobDate)
                    vaccines.forEach { vaccine ->
                        val eligibleVaccines = vaccine.vaccineList.filter { basicVaccine ->
                            checkEligibility(basicVaccine, dobLocalDate)
                        }

                        //Vaccinated List
                        val vaccineList = patientDetailsViewModel.getVaccineList()

                        // Filter out the vaccines that are already in the vaccineList
                        val notVaccinated = eligibleVaccines.filter { eligibleVaccine ->
                            vaccineList.none { it.vaccineName == eligibleVaccine.vaccineName }
                        }

                        if (notVaccinated.isNotEmpty()) {
                            groupList.add(vaccine.targetDisease)
                            childList[vaccine.targetDisease] = notVaccinated.map { it.vaccineName }
                        }

//                    if (eligibleVaccines.isNotEmpty()) {
//                        groupList.add(vaccine.targetDisease)
//                        childList[vaccine.targetDisease] = eligibleVaccines.map { it.vaccineName }
//                    }
                    }
                }

            }
        }.join()

        progressDialog.dismiss()

        return Pair(groupList, childList)
    }



    // Liskov substitution principle

    private fun checkEligibility(basicVaccine: BasicVaccine, dob: LocalDate): Boolean {
        val weeksSinceBirth = ChronoUnit.WEEKS.between(dob, LocalDate.now()).toInt()
        val administrativeWeeksSinceDOB = basicVaccine.administrativeWeeksSinceDOB
        return abs(weeksSinceBirth - administrativeWeeksSinceDOB) <= 2
    }





    fun getNextDoseDetails(vaccineCode: String, dob: LocalDate): Triple<String?, BasicVaccine?, SeriesVaccine?> {
        val seriesVaccine = vaccines
            .filterIsInstance<SeriesVaccine>()
            .find { series -> series.vaccineList.any { it.vaccineCode == vaccineCode } }

        seriesVaccine?.let { series ->
            val currentDoseIndex = series.vaccineList.indexOfFirst { it.vaccineCode == vaccineCode }

            if (currentDoseIndex != -1 && currentDoseIndex < series.vaccineList.size - 1) {
                val nextDoseNumber = currentDoseIndex + 1
                val nextDose = series.vaccineList[nextDoseNumber]

                // Calculate the next date based on the provided dob and administrativeWeeksSinceDOB
                val nextDate = dob.plusWeeks(nextDose.administrativeWeeksSinceDOB.toLong())
                if (nextDate.isAfter(LocalDate.now()) || nextDate.isEqual(LocalDate.now())){
                    return Triple(nextDate.toString(), nextDose, seriesVaccine)
                }

            }
        }

        return Triple(null, null, null)
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
