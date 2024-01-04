package com.intellisoft.chanjoke.vaccine.validations

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
    var vaccineList: List<BasicVaccine>
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


fun createVaccines(): Triple<List<RoutineVaccine>,List<NonRoutineVaccine>,List<PregnancyVaccine>> {

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
            BasicVaccine(polio+"bOPV", "bOPV", "Oral", 2, arrayListOf(), "2 drops","1"),
            BasicVaccine(polio+"OPV-I", "OPV I", "Oral", 6, arrayListOf(), "2 drops","2"),
            BasicVaccine(polio+"OPV-II", "OPV II", "Oral", 10, arrayListOf(10.0), "2 drops","3"),
            BasicVaccine(polio+"OPV-III", "OPV III", "Oral", 14, arrayListOf(14.0), "2 drops","4"),
            BasicVaccine(polio+"IPV I", "IPV I", "Oral", 14, arrayListOf(14.0), "2 drops","5")
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
            BasicVaccine(bcg+"I", "BCG", "Intradermal", 257, arrayListOf(), "0.5ml","1")
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
            BasicVaccine(dpt+"2", "DPT-HepB+Hib 2", "Intramuscular into the upper outer aspect of left thigh", 10, arrayListOf(4.0), "0.5ml","2"),
            BasicVaccine(dpt+"3", "DPT-HepB+Hib 3", "Intramuscular into the upper outer aspect of left thigh", 14, arrayListOf(4.0), "0.5ml","3")
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
            BasicVaccine(pcv+"2", "PCV10 2", "Intramuscular into the upper outer aspect of right thigh", 10, arrayListOf(4.0), "0.5ml","2"),
            BasicVaccine(pcv+"3", "PCV10 3", "Intramuscular into the upper outer aspect of right thigh", 14, arrayListOf(4.0), "0.5ml","3")
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
            BasicVaccine(measles+"2", "Measles-Rubella 2nd Dose", "Subcutaneous into the right upper arm (deltoid muscle)", 79, arrayListOf(78.21), "0.5ml","2")
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
           BasicVaccine(rotaVirus+"2", "Rota Virus 2nd Dose", "Oral", 10, arrayListOf(4.0), "0.5ml","2"),
           BasicVaccine(rotaVirus+"3", "Rota Virus 3rd Dose", "Oral", 14, arrayListOf(4.0), "0.5ml","3"),
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
           BasicVaccine(vitaminA+"2", "Vitamin A 2nd Dose", "Oral", 52, arrayListOf(26.07), "200,000OUI","2"),
           BasicVaccine(vitaminA+"3", "Vitamin A 3rd Dose", "Oral", 78, arrayListOf(26.07), "1Capsule","3"),
           BasicVaccine(vitaminA+"3", "Vitamin A 4th Dose", "Oral", 104, arrayListOf(26.07), "1Capsule","4"),
           BasicVaccine(vitaminA+"3", "Vitamin A 5th Dose", "Oral", 130, arrayListOf(26.07), "1Capsule","5"),
           BasicVaccine(vitaminA+"3", "Vitamin A 6th Dose", "Oral", 156, arrayListOf(26.07), "1Capsule","6"),
           BasicVaccine(vitaminA+"3", "Vitamin A 7th Dose", "Oral", 182, arrayListOf(26.07), "1Capsule","7"),
           BasicVaccine(vitaminA+"3", "Vitamin A 8th Dose", "Oral", 208, arrayListOf(26.07), "1Capsule","8"),
           BasicVaccine(vitaminA+"3", "Vitamin A 9th Dose", "Oral", 234, arrayListOf(26.07), "1Capsule","9"),
           BasicVaccine(vitaminA+"3", "Vitamin A 10th Dose", "Oral", 260, arrayListOf(26.07), "1Capsule","10"),
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
            BasicVaccine(hpvVaccine+"2", "HPV Vaccine 2", "Intramuscular left deltoid muscle", 842, arrayListOf(26.07), "0.5ml","1")
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
                "Covid",
                2,
                listOf(
                    BasicVaccine(covidMain+"ASTR-"+"1", "Astrazeneca 1st Dose", "Intramuscular Injection", 939, arrayListOf(), "0.5ml","1"),
                    BasicVaccine(covidMain+"ASTR-"+"2", "Astrazeneca 2nd Dose", "Intramuscular Injection", 939, arrayListOf(12.0), "0.5ml","2"),
                )
            ),
            RoutineVaccine(
                covidMain+"JnJ",
                "Covid",
                1,
                listOf(
                    BasicVaccine(covidMain+"JnJ-"+"0", "Johnson & Johnson", "Intramuscular Injection", 939, arrayListOf(), "0.5ml","1"),
                )
            ),
            RoutineVaccine(
                covidMain+"MOD-",
                "Covid",
                2,
                listOf(
                    BasicVaccine(covidMain+"MOD-"+"1", "Moderna 1st Dose", "Intramuscular Injection", 939, arrayListOf(), "0.5ml","1"),
                    BasicVaccine(covidMain+"MOD-"+"2", "Moderna 2nd Dose", "Intramuscular Injection", 939, arrayListOf(4.0), "0.5ml","2"),
                )
            ),
            RoutineVaccine(
                covidMain+"SINO-",
                "Covid",
                2,
                listOf(
                    BasicVaccine(covidMain+"SINO-"+"1", "Sinopharm 1st Dose", "Intramuscular Injection", 939, arrayListOf(), "0.5ml","1"),
                    BasicVaccine(covidMain+"SINO-"+"2", "Sinopharm 2nd Dose", "Intramuscular Injection", 939, arrayListOf(4.0), "0.5ml","2"),
                )
            ),
            RoutineVaccine(
                covidMain+"PFIZER-",
                "Covid",
                2,
                listOf(
                    BasicVaccine(covidMain+"PFIZER-"+"1", "Pfizer-BioNTech 1st Dose", "Intramuscular Injection", 939, arrayListOf(), "0.5ml","1"),
                    BasicVaccine(covidMain+"PFIZER-"+"2", "Pfizer-BioNTech 2nd Dose", "Intramuscular Injection", 939, arrayListOf(4.0), "0.5ml","2"),
                )
            ),
        )
    )

    //RABIES
    val rabiesMain = "IMRABIES-"
    val rabiesMainSeries = NonRoutineVaccine(
        rabiesMain,
        "Rabies Post Exposure",
        listOf(
            RoutineVaccine(
                rabiesMain+"RABIES",
                "Rabies Post Exposure",
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

    val routineList = listOf(polioSeries, bcgSeries, dptSeries, pcvSeries, measlesSeries,rotaSeries,vitaminASeries,malariaSeries,hpvSeries)
    val nonRoutineList = listOf(covidMainSeries, rabiesMainSeries, yellowFeverSeries)
    val pregnancyList = listOf(tetanusSeries, influenzaSeries)

    return Triple(routineList, nonRoutineList, pregnancyList)
}

class ImmunizationHandler() {

    val vaccines = createVaccines()
    val vaccineList = createVaccines().toList().flatten()

    // Extension function for DbVaccine to check if the vaccine name matches
    fun DbVaccine.matchesVaccineName(name: String): Boolean {
        return this.vaccineName == name
    }

    // Extension function for List<DbVaccine> to find vaccine details by series target name
    fun getRoutineVaccineDetailsBySeriesTargetName(targetDisease: String): Any? {
        return vaccineList.firstOrNull {
            when (it) {
                is RoutineVaccine -> it.targetDisease == targetDisease
                is NonRoutineVaccine -> it.targetDisease == targetDisease
                is PregnancyVaccine -> it.targetDisease == targetDisease
                else -> false
            }
        }
    }

    // Extension function for List<DbVaccine> to find vaccine details by vaccine name
    fun getVaccineDetailsByBasicVaccineName(vaccineName: String): BasicVaccine? {

        val (routineList, nonRoutineList, pregnancyList) = vaccines

        return listOf(
            routineList.flatMap { it.vaccineList },
            nonRoutineList.flatMap { it.vaccineList.flatMap { it.vaccineList } },
            pregnancyList.flatMap { it.vaccineList }).flatten().filterIsInstance<BasicVaccine>()
            .firstOrNull { it.matchesVaccineName(vaccineName) }

    }



    // Liskov substitution principle
    fun getAllVaccineList(administeredList: ArrayList<BasicVaccine>, ageInWeeks:Int):
            Triple<List<RoutineVaccine>, List<NonRoutineVaccine>, List<PregnancyVaccine>> {

        val (routineList, nonRoutineVaccineList,  pregnancyVaccineList) = vaccines

        /**
         * STEP 1: Get the eligible Vaccines
         */
        // Routine list
        val remainingRoutineList = routineList.map { routineVaccine ->
            routineVaccine.copy(vaccineList = routineVaccine.vaccineList.filter {
                administeredVaccineNotPresent(it, administeredList)
            })
        }.filter { it.vaccineList.isNotEmpty() }.toMutableList()

        //Pregnancy  list
        var remainingPregnancyList = pregnancyVaccineList.map { pregnancyVaccine ->
            pregnancyVaccine.copy(vaccineList = pregnancyVaccine.vaccineList.filter {
                administeredVaccineNotPresent(it, administeredList)
            })
        }.filter { it.vaccineList.isNotEmpty() }

        //Non-routine list
        val remainingNonRoutineList = nonRoutineVaccineList.map { nonRoutineVaccine ->
            nonRoutineVaccine.copy(vaccineList = nonRoutineVaccine.vaccineList.map { routineVaccine ->
                routineVaccine.copy(vaccineList = routineVaccine.vaccineList.filter {
                    administeredVaccineNotPresent(it, administeredList)
                })
            }.filter { it.vaccineList.isNotEmpty() })
        }.filter { it.vaccineList.isNotEmpty() }

        /**
         * Step 2: Perform vaccine specific issues
         */
        //Routine vaccines
        if (ageInWeeks > 2) {
            // Remove bOPV from the list
            remainingRoutineList.forEach { routineVaccine ->
                routineVaccine.vaccineList = routineVaccine.vaccineList.filterNot { it.vaccineCode == "IMPO-bOPV" }
            }
        }

        if (ageInWeeks > 52) {
            // Remove RotaVirus from the list
            remainingRoutineList.forEach { routineVaccine ->
                routineVaccine.vaccineList = routineVaccine.vaccineList.filterNot { it.vaccineCode.startsWith("IMROTA") }
            }
        }

        if (!administeredList.any { it.vaccineCode == "IMBCG-I" } &&
            !remainingRoutineList.any { it.vaccineList.any { it.vaccineCode == "IMBCG-I" } } &&
            ageInWeeks < 257) {
            // BCG has not been administered and is not present and age is below 257 weeks, add it to the list
            val basicVaccine = getVaccineDetailsByBasicVaccineName("BCG")
            val seriesVaccine = basicVaccine?.let { getRoutineSeriesByBasicVaccine(it) }

            remainingRoutineList + seriesVaccine
        }

        //Non Routine Vaccines

        //Pregnancy Vaccines
        /**
         * Remove Pregnancy vaccines from people under 10 years
         */
        if (ageInWeeks < 522){
            remainingPregnancyList = mutableListOf()
        }


        return Triple(remainingRoutineList, remainingNonRoutineList, remainingPregnancyList)

    }


    // Helper function to check if a vaccine is not present in the administered list
    private fun administeredVaccineNotPresent(vaccine: BasicVaccine, administeredList: List<BasicVaccine>): Boolean {
        return administeredList.none { it.vaccineCode == vaccine.vaccineCode }
    }

    // Function to get details of the next dose for a given BasicVaccine
    fun getNextDoseDetails(basicVaccine: BasicVaccine): BasicVaccine? {
        val (routineList, nonRoutineList, pregnancyList) = vaccines

        // Helper function to find the next dose details in a list of vaccines
        fun findNextDoseInList(vaccineList: List<DbVaccine>): BasicVaccine? {
            val indexOfCurrentDose = vaccineList.indexOfFirst { it.vaccineCode == basicVaccine.vaccineCode }

            // If the current dose is found and there is a next dose
            if (indexOfCurrentDose != -1 && indexOfCurrentDose + 1 < vaccineList.size) {
                // Get the details of the next dose
                return vaccineList[indexOfCurrentDose + 1] as? BasicVaccine
            }
            return null
        }

        // Check if the basic vaccine belongs to a routine vaccine
        val routineVaccineContainingDose = routineList.firstOrNull { it.vaccineList.any { it.vaccineCode == basicVaccine.vaccineCode } }

        if (routineVaccineContainingDose != null) {
            // If the routine vaccine is found, find the next dose in its vaccine list
            return findNextDoseInList(routineVaccineContainingDose.vaccineList)
        }

        // Check if the basic vaccine belongs to a non-routine vaccine
        val nonRoutineVaccineContainingDose = nonRoutineList.firstOrNull { it.vaccineList.any { it.vaccineCode == basicVaccine.vaccineCode } }

        if (nonRoutineVaccineContainingDose != null) {
            // If the non-routine vaccine is found, find the next dose in its vaccine list
            return findNextDoseInList(nonRoutineVaccineContainingDose.vaccineList.flatMap { it.vaccineList })
        }

        // Check if the basic vaccine belongs to a pregnancy vaccine
        val pregnancyVaccineContainingDose = pregnancyList.firstOrNull { it.vaccineList.any { it.vaccineCode == basicVaccine.vaccineCode } }

        if (pregnancyVaccineContainingDose != null) {
            // If the pregnancy vaccine is found, find the next dose in its vaccine list
            return findNextDoseInList(pregnancyVaccineContainingDose.vaccineList)
        }

        return null // Return null if the next dose is not found or if the vaccine is not in any category
    }

    fun getRoutineSeriesByBasicVaccine(basicVaccine: BasicVaccine): RoutineVaccine? {
        val (routineList, _, _) = vaccines

        return routineList.find { routineVaccine ->
            routineVaccine.vaccineList.any { it.vaccineCode == basicVaccine.vaccineCode }
        }
    }

    fun getNextBasicVaccineInSeries(series: RoutineVaccine, doseNumber: String): BasicVaccine? {
        return series.vaccineList.firstOrNull { it.doseNumber == doseNumber.toInt().plus(1).toString() }
    }

    fun getMissedRoutineVaccines(
        administeredList: List<BasicVaccine>,
        ageInWeeks: Int
    ): List<BasicVaccine> {
        val immunizationHandler = ImmunizationHandler()
        val (remainingRoutineList, _, remainingPregnancyList) =
            immunizationHandler.getAllVaccineList(ArrayList(administeredList), ageInWeeks)

        // Collect missed vaccines from routine list
        val missedRoutineVaccines = remainingRoutineList.flatMap { routineVaccine ->
            routineVaccine.vaccineList.firstOrNull {
                administeredVaccineNotPresent(it, administeredList)
            }?.let { listOf(it) } ?: emptyList()
        }

        // Collect missed vaccines from pregnancy list
        val missedPregnancyVaccines = remainingPregnancyList.flatMap { it.vaccineList }

        // Combine all missed routine and pregnancy vaccines
        val allMissedVaccines = missedRoutineVaccines + missedPregnancyVaccines

        // Sort missed vaccines based on administrativeWeeksSinceDOB

        return allMissedVaccines.sortedBy { it.administrativeWeeksSinceDOB }
    }







}

