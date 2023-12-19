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

//Routine Vaccine
interface DbVaccine {
    val vaccineCode: String
    val vaccineName: String
    val administrativeMethod: String
    val administrativeWeeksSinceDOB: Int
    val administrativeWeeksSincePrevious: ArrayList<Double>
    val doseQuantity: String
    val doseNumber: String //Dose number within series
}

data class BasicVaccine(
    override val vaccineCode: String,
    override val vaccineName: String,
    override val administrativeMethod: String,
    override val administrativeWeeksSinceDOB: Int,
    override val administrativeWeeksSincePrevious: ArrayList<Double>,
    override val doseQuantity: String,
    override val doseNumber: String, //Dose number within series
) : DbVaccine

//Routine Vaccine
data class RoutineVaccine(
    val diseaseCode: String,
    val targetDisease: String,
    val seriesDoses: Int, // Recommended number of doses for immunity
    val vaccineList: List<BasicVaccine>
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

    override val administrativeWeeksSincePrevious: ArrayList<Double>
        get() = vaccineList.firstOrNull()?.administrativeWeeksSincePrevious ?: arrayListOf()

    override val doseQuantity: String
        get() = vaccineList.firstOrNull()?.doseQuantity ?: "0"
    override val doseNumber: String
        get() = vaccineList.firstOrNull()?.doseNumber ?: "0"
}
//Pregnancy vaccines
data class PregnancyVaccine(
    val diseaseCode: String,
    val targetDisease: String,
    val seriesDoses: Int, // Recommended number of doses for immunity
    val vaccineList: List<BasicVaccine>
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

    override val administrativeWeeksSincePrevious: ArrayList<Double>
        get() = vaccineList.firstOrNull()?.administrativeWeeksSincePrevious ?: arrayListOf()

    override val doseQuantity: String
        get() = vaccineList.firstOrNull()?.doseQuantity ?: "0"
    override val doseNumber: String
        get() = vaccineList.firstOrNull()?.doseNumber ?: "0"
}


//Non-routine vaccines
data class NonRoutineVaccine(
    val diseaseCode: String,
    val targetDisease: String,
    val vaccineList: List<RoutineVaccine>,
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

    override val administrativeWeeksSincePrevious: ArrayList<Double>
        get() = vaccineList.firstOrNull()?.administrativeWeeksSincePrevious ?: arrayListOf()

    override val doseQuantity: String
        get() = vaccineList.firstOrNull()?.doseQuantity ?: "0"
    override val doseNumber: String
        get() = vaccineList.firstOrNull()?.doseNumber ?: "0"
}

//Pregnancy vaccines




fun createVaccines(): List<RoutineVaccine> {

    /**
     * ROUTINE VACCINES
     */

    // Polio
    val polio = "IMPO-"
    val polioSeries = RoutineVaccine(
        polio,
        "Polio",
        5,
        listOf(
            BasicVaccine(polio+"bOPV", "bOPV", "Oral", 0, arrayListOf(), "2 drops","1"),
            BasicVaccine(polio+"OPV-I", "OPV I", "Oral", 6, arrayListOf(), "2 drops","2"),
            BasicVaccine(polio+"OPV-II", "OPV II", "Oral", 10, arrayListOf(), "2 drops","3"),
            BasicVaccine(polio+"OPV-III", "OPV III", "Oral", 14, arrayListOf(), "2 drops","4"),
            BasicVaccine(polio+"IPV I", "IPV I", "Oral", 14, arrayListOf(), "2 drops","5")
        )
    )

    //BCG
    /**
     * TODO: Check about the dosages
     */
    val bcg = "IMBCG-"
    val bcgSeries = RoutineVaccine(
        bcg,
        "BCG",
        1,
        listOf(
            BasicVaccine(bcg+"I", "BCG", "Intradermal", 0, arrayListOf(), "0.5ml","1")
        )
    )

    //DPT-HepB+Hib
    val dpt = "IMDPT-"
    val dptSeries = RoutineVaccine(
        dpt,
        "DPT-HepB+Hib",
        3,
        listOf(
            BasicVaccine(dpt+"1", "DPT-HepB+Hib 1", "Intramuscular into the upper outer aspect of left thigh", 6, arrayListOf(), "0.5ml","1"),
            BasicVaccine(dpt+"2", "DPT-HepB+Hib 2", "Intramuscular into the upper outer aspect of left thigh", 10, arrayListOf(), "0.5ml","2"),
            BasicVaccine(dpt+"3", "DPT-HepB+Hib 3", "Intramuscular into the upper outer aspect of left thigh", 14, arrayListOf(), "0.5ml","3")
        )
    )

    //PCV10
    val pcv = "IMPCV10-"
    val pcvSeries = RoutineVaccine(
        pcv,
        "PCV10",
        3,
        listOf(
            BasicVaccine(pcv+"1", "PCV10 1", "Intramuscular into the upper outer aspect of right thigh", 6, arrayListOf(), "0.5ml","1"),
            BasicVaccine(pcv+"2", "PCV10 2", "Intramuscular into the upper outer aspect of right thigh", 10, arrayListOf(), "0.5ml","2"),
            BasicVaccine(pcv+"3", "PCV10 3", "Intramuscular into the upper outer aspect of right thigh", 14, arrayListOf(), "0.5ml","3")
        )
    )

    //Measles
    val measles = "IMMEAS-"
    val measlesSeries = RoutineVaccine(
        measles,
        "Measles",
        2,
        listOf(
            BasicVaccine(measles+"0", "Measles-Rubella", "Subcutaneous into the right upper arm (deltoid muscle)", 27, arrayListOf(), "0.5ml","0"),
            BasicVaccine(measles+"1", "Measles-Rubella 1st Dose", "Subcutaneous into the right upper arm (deltoid muscle)", 40, arrayListOf(), "0.5ml","1"),
            BasicVaccine(measles+"2", "Measles-Rubella 2nd Dose", "Subcutaneous into the right upper arm (deltoid muscle)", 79, arrayListOf(), "0.5ml","2")
        )
    )

    //Rota Virus
    val rotaVirus = "IMROTA-"
    val rotaSeries = RoutineVaccine(
        rotaVirus,
        "Rota Virus",
        3,
        listOf(
           BasicVaccine(rotaVirus+"1", "Rota Virus 1st Dose", "Oral", 6, arrayListOf(), "0.5ml","1"),
           BasicVaccine(rotaVirus+"2", "Rota Virus 2nd Dose", "Oral", 10, arrayListOf(), "0.5ml","2"),
           BasicVaccine(rotaVirus+"3", "Rota Virus 3rd Dose", "Oral", 14, arrayListOf(), "0.5ml","3"),
        )
    )

    //Vitamin A
    val vitaminA = "IMVIT-"
    val vitaminASeries = RoutineVaccine(
        vitaminA,
        "Vitamin A",
        10,
        listOf(
           BasicVaccine(vitaminA+"1", "Vitamin A 1st Dose", "Oral", 26, arrayListOf(), "100,000OUI","1"),
           BasicVaccine(vitaminA+"2", "Vitamin A 2nd Dose", "Oral", 52, arrayListOf(), "200,000OUI","2"),
           BasicVaccine(vitaminA+"3", "Vitamin A 3rd Dose", "Oral", 78, arrayListOf(), "1Capsule","3"),
           BasicVaccine(vitaminA+"3", "Vitamin A 4th Dose", "Oral", 104, arrayListOf(), "1Capsule","4"),
           BasicVaccine(vitaminA+"3", "Vitamin A 5th Dose", "Oral", 130, arrayListOf(), "1Capsule","5"),
           BasicVaccine(vitaminA+"3", "Vitamin A 6th Dose", "Oral", 156, arrayListOf(), "1Capsule","6"),
           BasicVaccine(vitaminA+"3", "Vitamin A 7th Dose", "Oral", 182, arrayListOf(), "1Capsule","7"),
           BasicVaccine(vitaminA+"3", "Vitamin A 8th Dose", "Oral", 208, arrayListOf(), "1Capsule","8"),
           BasicVaccine(vitaminA+"3", "Vitamin A 9th Dose", "Oral", 234, arrayListOf(), "1Capsule","9"),
           BasicVaccine(vitaminA+"3", "Vitamin A 10th Dose", "Oral", 260, arrayListOf(), "1Capsule","10"),
        )
    )

    //RTS/AS01 (Malaria)
    val malaria = "IMMALA-"
    val malariaSeries = RoutineVaccine(
        malaria,
        "RTS/AS01 (Malaria)",
        4,
        listOf(
            BasicVaccine(malaria+"1", "RTS/AS01 (Malaria Vaccine - 1)", "Intramuscular left deltoid muscle", 26, arrayListOf(), "0.5ml","1"),
            BasicVaccine(malaria+"2", "RTS/AS01 (Malaria Vaccine - 2)", "Intramuscular left deltoid muscle", 30, arrayListOf(), "0.5ml","2"),
            BasicVaccine(malaria+"3", "RTS/AS01 (Malaria Vaccine - 3)", "Intramuscular left deltoid muscle", 39, arrayListOf(), "0.5ml","3"),
            BasicVaccine(malaria+"4", "RTS/AS01 (Malaria Vaccine - 4)", "Intramuscular left deltoid muscle", 104, arrayListOf(), "0.5ml","4")
        )
    )
    //HPV Vaccine
    val hpvVaccine = "IMHPV-"
    val hpvSeries = RoutineVaccine(
        hpvVaccine,
        "HPV",
        2,
        listOf(
            BasicVaccine(hpvVaccine+"1", "HPV Vaccine 1", "Intramuscular left deltoid muscle", 521, arrayListOf(), "0.5ml","1"),
            BasicVaccine(hpvVaccine+"2", "HPV Vaccine 2", "Intramuscular left deltoid muscle", 842, arrayListOf(), "0.5ml","1")
       )
    )

    /**
     * NON-ROUTINE VACCINES
     */


    //Covid

    val covidMain = "IMCOV-"
    val covidMainSeries = NonRoutineVaccine(
        covidMain,
        "Covid",
        listOf(
            RoutineVaccine(
                covidMain+"ASTR",
                "Astrazeneca",
                2,
                listOf(
                    BasicVaccine(covidMain+"ASTR-"+"1", "Astrazeneca 1st Dose", "Intramuscular Injection", 939, arrayListOf(), "0.5ml","1"),
                    BasicVaccine(covidMain+"ASTR-"+"2", "Astrazeneca 2nd Dose", "Intramuscular Injection", 939, arrayListOf(12.0), "0.5ml","2"),
                )
            ),
            RoutineVaccine(
                covidMain+"JnJ",
                "Johnson & Johnson",
                1,
                listOf(
                    BasicVaccine(covidMain+"JnJ-"+"0", "Johnson & Johnson", "Intramuscular Injection", 939, arrayListOf(), "0.5ml","1"),
                )
            ),
            RoutineVaccine(
                covidMain+"MOD-",
                "Moderna",
                2,
                listOf(
                    BasicVaccine(covidMain+"MOD-"+"1", "Moderna 1st Dose", "Intramuscular Injection", 939, arrayListOf(), "0.5ml","1"),
                    BasicVaccine(covidMain+"MOD-"+"2", "Moderna 2nd Dose", "Intramuscular Injection", 939, arrayListOf(4.0), "0.5ml","2"),
                )
            ),
            RoutineVaccine(
                covidMain+"SINO-",
                "Sinopharm",
                2,
                listOf(
                    BasicVaccine(covidMain+"SINO-"+"1", "Sinopharm 1st Dose", "Intramuscular Injection", 939, arrayListOf(), "0.5ml","1"),
                    BasicVaccine(covidMain+"SINO-"+"2", "Sinopharm 2nd Dose", "Intramuscular Injection", 939, arrayListOf(4.0), "0.5ml","2"),
                )
            ),
            RoutineVaccine(
                covidMain+"PFIZER-",
                "Pfizer",
                2,
                listOf(
                    BasicVaccine(covidMain+"PFIZER-"+"1", "Pfizer-BioNTech 1st Dose", "Intramuscular Injection", 939, arrayListOf(), "0.5ml","1"),
                    BasicVaccine(covidMain+"PFIZER-"+"2", "Pfizer-BioNTech 2nd Dose", "Intramuscular Injection", 939, arrayListOf(4.0), "0.5ml","2"),
                )
            ),
        )
    )

    //RABIES
    val rabiesMain = "IMCOV-"
    val rabiesMainSeries = NonRoutineVaccine(
        rabiesMain,
        "Rabies Post Exposure",
        listOf(
            RoutineVaccine(
                rabiesMain+"RABIES",
                "Rabies",
                5,
                listOf(
                    BasicVaccine(rabiesMain+"RABIES-"+"1", "Rabies 1st Dose", "Intramuscular Injection", 0, arrayListOf(), "0.5ml","1"),
                    BasicVaccine(rabiesMain+"RABIES-"+"2", "Rabies 2nd Dose", "Intramuscular Injection", 0, arrayListOf(0.43), "0.5ml","2"),
                    BasicVaccine(rabiesMain+"RABIES-"+"3", "Rabies 3rd Dose", "Intramuscular Injection", 0, arrayListOf(1.0), "0.5ml","3"),
                    BasicVaccine(rabiesMain+"RABIES-"+"4", "Rabies 4th Dose", "Intramuscular Injection", 0, arrayListOf(2.0), "0.5ml","4"),
                    BasicVaccine(rabiesMain+"RABIES-"+"5", "Rabies 5th Dose", "Intramuscular Injection", 0, arrayListOf(4.0), "0.5ml","5"),
                )
            )
        )
    )
    //YELLOW FEVER
    val yellowFever = "IMYF-"
    val yellowFeverSeries = NonRoutineVaccine(
        yellowFever,
        "Yellow Fever",
        listOf(
            RoutineVaccine(
                yellowFever+"YELLOWFEVER",
                "Yellow Fever",
                1,
                listOf(
                    BasicVaccine(yellowFever+"I", "Yellow Fever", "Subcutaneous left upper arm", 40, arrayListOf(), "0.5ml","1")
                )
            )
        )
    )

    /**
     * PREGNANCY VACCINES
     */

    //Tetanus
    val tetanus = "IMTD-"
    val tetanusSeries = PregnancyVaccine(
        tetanus,
        "(TD) Tetanus toxoid vaccination",
        3,
        listOf(
            BasicVaccine(tetanus+"1", "(TD) Tetanus toxoid 1st Dose", "Intramuscular Injection", 0, arrayListOf(17.38, 21.72, 26.07), "0.5ml","1"),
            BasicVaccine(tetanus+"2", "(TD) Tetanus toxoid 2nd Dose", "Intramuscular Injection", 0, arrayListOf(21.72, 26.07, 30.41, 34.76), "0.5ml","2"),
            BasicVaccine(tetanus+"3", "(TD) Tetanus toxoid 3rd Dose", "Intramuscular Injection", 0, arrayListOf(17.38, 21.72, 26.07, 30.41, 34.76), "0.5ml","3"),
            BasicVaccine(tetanus+"4", "(TD) Tetanus toxoid 4th Dose", "Intramuscular Injection", 0, arrayListOf(17.38, 21.72, 26.07, 30.41, 34.76), "0.5ml","4"),
            BasicVaccine(tetanus+"5", "(TD) Tetanus toxoid 5th Dose", "Intramuscular Injection", 0, arrayListOf(17.38, 21.72, 26.07, 30.41, 34.76), "0.5ml","5"),
        )
    )

    //Influenza
    val influenza = "IMINFLU-"
    val influenzaSeries = PregnancyVaccine(
        influenza,
        "Influenza",
        1,
        listOf(
            BasicVaccine(influenza+"1", "Influenza", "Intramuscular Injection", 0, arrayListOf(), "0.5ml","1"),
        )
    )


    return listOf(polioSeries, bcgSeries, dptSeries, pcvSeries, measlesSeries,rotaSeries,vitaminASeries,malariaSeries,hpvSeries)
}

class ImmunizationHandler() {

    val vaccines = createVaccines()
    // Open-closed principle

    fun getVaccineDetailsByBasicVaccineName(vaccineName: String): DbVaccine? {

        return vaccines
            .asSequence()
            .filterIsInstance<RoutineVaccine>()
            .flatMap { it.vaccineList.asSequence() + it } // Flatten RoutineVaccine to include BasicVaccine
            .find { it.vaccineName == vaccineName }
    }

    // Function to get series vaccine details by code
    fun getRoutineVaccineDetailsBySeriesTargetName(targetDisease: String): RoutineVaccine? {
        return vaccines
            .filterIsInstance<RoutineVaccine>()
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





    fun getNextDoseDetails(vaccineCode: String, dob: LocalDate): Triple<String?, BasicVaccine?, RoutineVaccine?> {
        val RoutineVaccine = vaccines
            .filterIsInstance<RoutineVaccine>()
            .find { series -> series.vaccineList.any { it.vaccineCode == vaccineCode } }

        RoutineVaccine?.let { series ->
            val currentDoseIndex = series.vaccineList.indexOfFirst { it.vaccineCode == vaccineCode }

            if (currentDoseIndex != -1 && currentDoseIndex < series.vaccineList.size - 1) {
                val nextDoseNumber = currentDoseIndex + 1
                val nextDose = series.vaccineList[nextDoseNumber]

                // Calculate the next date based on the provided dob and administrativeWeeksSinceDOB
                val nextDate = dob.plusWeeks(nextDose.administrativeWeeksSinceDOB.toLong())
                if (nextDate.isAfter(LocalDate.now()) || nextDate.isEqual(LocalDate.now())){
                    return Triple(nextDate.toString(), nextDose, RoutineVaccine)
                }

            }
        }

        return Triple(null, null, null)
    }





    data class AvailableVaccine(val vaccine: RoutineVaccine, val isEligible: Boolean)


}



//fun main() {
//    val vaccines = listOf(
//        RoutineVaccine(
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
//        RoutineVaccine(
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
