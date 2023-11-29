package com.intellisoft.chanjoke.fhir.data

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.util.Log
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.vaccine.validations.ImmunizationHandler
import com.intellisoft.chanjoke.vaccine.validations.VaccinationManager
import com.intellisoft.chanjoke.vaccine.validations.VaccineDetails
import com.intellisoft.chanjoke.viewmodel.PatientDetailsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale

class FormatterClass {
    fun saveSharedPref(key: String, value: String, context: Context) {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences(context.getString(R.string.app_name), MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(key, value);
        editor.apply();
    }
    fun getSharedPref(key: String, context: Context): String? {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences(context.getString(R.string.app_name), MODE_PRIVATE)
        return sharedPreferences.getString(key, null)

    }
    fun deleteSharedPref(key: String, context: Context) {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences(context.getString(R.string.app_name), MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove(key);
        editor.apply();

    }
    fun convertStringToDate(dateString: String, format: String): Date? {
        return try {
            val dateFormat = SimpleDateFormat(format, Locale.getDefault())
            dateFormat.parse(dateString) ?: Date()
        }catch (e:Exception){
            null
        }

    }
    fun convertDateToLocalDate(date: Date): LocalDate {
        val instant = date.toInstant()
        return instant.atZone(java.time.ZoneId.systemDefault()).toLocalDate()
    }
    fun removeNonNumeric(input: String): String {
        // Regex pattern to match numeric values (with optional decimal part)
        val numericPattern = Regex("[0-9]+(\\.[0-9]+)?")

        // Find the numeric part in the input string
        val matchResult = numericPattern.find(input)

        // Extract the numeric value or return an empty string if not found
        return matchResult?.value ?: ""
    }
    fun convertDateFormat(inputDate: String): String? {
        // Define the input date formats to check
        val inputDateFormats = arrayOf(
            "yyyy-MM-dd",
            "MM/dd/yyyy",
            "yyyyMMdd",
            "dd-MM-yyyy",
            "yyyy/MM/dd",
            "MM-dd-yyyy",
            "dd/MM/yyyy",
            "yyyyMMddHHmmss",
            "yyyy-MM-dd HH:mm:ss",
            "EEE, dd MMM yyyy HH:mm:ss Z",  // Example: "Mon, 25 Dec 2023 12:30:45 +0000"
            "yyyy-MM-dd'T'HH:mm:ssXXX",     // ISO 8601 with time zone offset (e.g., "2023-11-29T15:44:00+03:00")
            "EEE MMM dd HH:mm:ss zzz yyyy", // Example: "Sun Jan 01 00:00:00 GMT+03:00 2023"

            // Add more formats as needed
        )



        // Try parsing the input date with each format
        for (format in inputDateFormats) {
            try {
                val parsedDate = SimpleDateFormat(format, Locale.getDefault()).parse(inputDate)

                // If parsing succeeds, format and return the date in the desired format
                parsedDate?.let {
                    return SimpleDateFormat("MMM d yyyy", Locale.getDefault()).format(it)
                }
            } catch (e: ParseException) {
                // Continue to the next format if parsing fails
            }
        }

        // If none of the formats match, return an error message or handle it as needed
        return null
    }

    fun calculateDateAfterWeeksAsString(dob: LocalDate, weeksAfterDob: Long): String {
        val calculatedDate = dob.plusWeeks(weeksAfterDob)
        return calculatedDate.toString()
    }

    fun getEligibleVaccines(
        context: Context,
        patientDetailsViewModel: PatientDetailsViewModel):List<String>{

        val dob = getSharedPref("patientDob", context)
        val patientId = getSharedPref("patientId", context)

        if (patientId != null && dob != null){
            //Convert dob to LocalDate
            val birthDate = LocalDate.parse(dob)

            /**
             * Get the vaccines from BaseVaccine
             */
            val vaccinationManager = VaccinationManager()
            val vaccineList = vaccinationManager.getEligibleVaccines(birthDate)

            /**
             * Get the Vaccines for this person
             */
            val vaccinationList =  patientDetailsViewModel.getEncounterList()

            val missingVaccineList = vaccineList.filter { vaccine ->
                vaccinationList.none { adverseEvent -> adverseEvent.vaccineName == vaccine.name }
            }.map { it.name }
            /**
             * Get recommendations and check if the vaccine has already been created as a recommendation
             */
            val recommendationList = patientDetailsViewModel.recommendationList()

            val listToRecommend = missingVaccineList.filter { vaccine ->
                recommendationList.none() {recommend ->
                    val targetDisease = recommend.targetDisease.replace(" ", "").uppercase()
                    val missingVaccine = vaccine.replace(" ", "").uppercase()
                    targetDisease == missingVaccine
                }
            }

            return listToRecommend
        }
        return emptyList()
    }

    fun generateStockValue(vaccineDetails: VaccineDetails,context: Context):ArrayList<DbVaccineStockDetails>{

        val targetDisease = getSharedPref("targetDisease", context).toString()
        val stockList = ArrayList<DbVaccineStockDetails>()
        stockList.addAll(
            listOf(
                DbVaccineStockDetails("vaccinationTargetDisease",targetDisease.lowercase().capitalize(Locale.ROOT)),
                DbVaccineStockDetails("vaccinationDosage",vaccineDetails.dosage),
                DbVaccineStockDetails("vaccinationAdministrationMethod",vaccineDetails.administrationMethod),
                DbVaccineStockDetails("vaccinationDoseNumber",vaccineDetails.doseNumber),
                DbVaccineStockDetails("vaccinationSeriesDoses",vaccineDetails.seriesDoses),
                DbVaccineStockDetails("vaccinationBatchNumber",""),
                DbVaccineStockDetails("vaccinationExpirationDate",""),
                DbVaccineStockDetails("vaccinationBrand",""),
                DbVaccineStockDetails("vaccinationManufacturer","")
            )
        )

        //Save to shared pref
        stockList.forEach{
            saveSharedPref(it.name,it.value,context)
        }
        return stockList
    }

    fun saveStockValue(administeredProduct:String, targetDisease:String, context: Context):ArrayList<DbVaccineStockDetails>{
        val stockList = ArrayList<DbVaccineStockDetails>()

        val immunizationHandler = ImmunizationHandler()
        val baseVaccineDetails = immunizationHandler.getVaccineDetailsByBasicVaccineName(administeredProduct)
        val seriesVaccineDetails = immunizationHandler.getSeriesVaccineDetailsBySeriesTargetName(targetDisease)

        if (seriesVaccineDetails != null && baseVaccineDetails != null){

            stockList.addAll(
                listOf(
                    DbVaccineStockDetails("vaccinationTargetDisease",targetDisease),
                    DbVaccineStockDetails("administeredProduct",administeredProduct),

                    DbVaccineStockDetails("vaccinationSeriesDoses",seriesVaccineDetails.seriesDoses.toString()),

                    DbVaccineStockDetails("vaccinationDoseQuantity",baseVaccineDetails.doseQuantity),
                    DbVaccineStockDetails("vaccinationDoseNumber",baseVaccineDetails.doseNumber),
                    DbVaccineStockDetails("vaccinationBrand",baseVaccineDetails.vaccineName),
                    DbVaccineStockDetails("vaccinationSite",baseVaccineDetails.administrativeMethod),

                    DbVaccineStockDetails("vaccinationExpirationDate",""),
                    DbVaccineStockDetails("vaccinationBatchNumber",""),
                    DbVaccineStockDetails("vaccinationManufacturer","")
                )
            )

            //Save to shared pref
            stockList.forEach{
                saveSharedPref(it.name,it.value,context)
            }

        }
        return stockList


    }

    fun formatString(input: String): String {
        val words = input.split("(?=[A-Z])".toRegex())
        val result = words.joinToString(" ") { it.capitalize() }
        return result
    }


}