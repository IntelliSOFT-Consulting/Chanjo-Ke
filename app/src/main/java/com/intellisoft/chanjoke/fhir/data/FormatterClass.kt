package com.intellisoft.chanjoke.fhir.data

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.intellisoft.chanjoke.R
import java.text.ParseException
import java.text.SimpleDateFormat
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



}