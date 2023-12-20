package com.intellisoft.chanjoke.fhir.data

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.button.MaterialButton
import com.intellisoft.chanjoke.MainActivity
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.detail.PatientDetailActivity
import com.intellisoft.chanjoke.utils.BlurBackgroundDialog
import com.intellisoft.chanjoke.vaccine.validations.ImmunizationHandler
import com.intellisoft.chanjoke.vaccine.validations.NonRoutineVaccine
import com.intellisoft.chanjoke.vaccine.validations.PregnancyVaccine
import com.intellisoft.chanjoke.vaccine.validations.RoutineVaccine

import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Period
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.random.Random

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


    fun saveStockValue(
        administeredProduct: String,
        targetDisease: String,
        context: Context
    ): ArrayList<DbVaccineStockDetails> {
        val stockList = ArrayList<DbVaccineStockDetails>()

        val immunizationHandler = ImmunizationHandler()
        val baseVaccineDetails =
            immunizationHandler.getVaccineDetailsByBasicVaccineName(administeredProduct)
        val vaccineDetails =
            immunizationHandler.getRoutineVaccineDetailsBySeriesTargetName(targetDisease)

        if (vaccineDetails != null && baseVaccineDetails != null) {

            var seriesDoses = ""

            seriesDoses = when(vaccineDetails){
                is RoutineVaccine -> {
                    "${vaccineDetails.seriesDoses}"
                }

                is NonRoutineVaccine -> {
                    val nonRoutineVaccine = vaccineDetails.vaccineList.firstOrNull(){it.targetDisease == targetDisease}
                    "${nonRoutineVaccine?.seriesDoses}"
                }

                is PregnancyVaccine -> {
                    "${vaccineDetails.seriesDoses}"
                }

                else -> {
                    ""
                }
            }

            stockList.addAll(
                listOf(
                    DbVaccineStockDetails("vaccinationTargetDisease", targetDisease),
                    DbVaccineStockDetails("administeredProduct", administeredProduct),

                    DbVaccineStockDetails(
                        "vaccinationSeriesDoses",
                        seriesDoses
                    ),

                    DbVaccineStockDetails(
                        "vaccinationDoseQuantity",
                        baseVaccineDetails.doseQuantity
                    ),
                    DbVaccineStockDetails("vaccinationDoseNumber", baseVaccineDetails.doseNumber),
                    DbVaccineStockDetails("vaccinationBrand", baseVaccineDetails.vaccineName),
                    DbVaccineStockDetails(
                        "vaccinationSite",
                        baseVaccineDetails.administrativeMethod
                    ),

                    DbVaccineStockDetails("vaccinationExpirationDate", ""),
                    DbVaccineStockDetails("vaccinationBatchNumber", ""),
                    DbVaccineStockDetails("vaccinationManufacturer", "")
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



    fun getFormattedAge(
        dob: String?,
        resources: Resources,
    ): String {
        if (dob == null) return ""
        val dobFormat = convertDateFormat(dob)
        if (dobFormat != null) {
            val dobDate = convertStringToDate(dobFormat, "MMM d yyyy")
            if (dobDate != null) {
                val finalDate = convertDateToLocalDate(dobDate)
                return Period.between(finalDate, LocalDate.now()).let {
                    when {
                        it.years > 0 -> resources.getQuantityString(
                            R.plurals.ageYear,
                            it.years,
                            it.years
                        )

                        it.months > 0 -> resources.getQuantityString(
                            R.plurals.ageMonth,
                            it.months,
                            it.months
                        )

                        else -> resources.getQuantityString(R.plurals.ageDay, it.days, it.days)
                    }
                }
            }
        }
        return ""

    }

    fun generateRandomCode(): String {
        // Get current date
        val currentDate = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("ddMMyyyy")
        val formattedDate = dateFormat.format(currentDate)

        /**
         * The code works as follows,
         * The first Code represents the year, month, date represented as per in the alphabetical order
         * The date is added as is
         * The last 4 letters are random number
         */

        // Extract year, month, and day
        val year = formattedDate.substring(4)
        val month = formattedDate.substring(2, 4)
        val day = formattedDate.substring(0, 2)

        // Generate the first three characters
        val firstChar = ('A'.toInt() + year.toInt() % 10).toChar()
        val secondChar = ('A'.toInt() + month.toInt()).toChar()
        val thirdChar = day

        // Generate the next four characters
        val randomChars = generateRandomChars(4)

        // Combine all parts to form the final code
        return "$firstChar$secondChar$thirdChar$randomChars"
    }

    fun generateRandomChars(n: Int): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..n)
            .map { chars[Random.nextInt(chars.length)] }
            .joinToString("")
    }


}