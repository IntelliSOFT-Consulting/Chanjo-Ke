package com.intellisoft.chanjoke.vaccine.stock_management

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.chanjoke.MainActivity
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.fhir.data.DbVaccineStockDetails
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.fhir.data.NavigationDetails
import java.util.Arrays

class VaccineStockManagement : AppCompatActivity() {
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var recyclerView:RecyclerView
    private var patientId = ""
    private var targetDisease = ""
    private var formatterClass = FormatterClass()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vaccine_stock_management)

        patientId = formatterClass.getSharedPref("patientId", this).toString()
        targetDisease = formatterClass.getSharedPref("targetDisease", this).toString()

        recyclerView = findViewById(R.id.recyclerView)
        layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )
        recyclerView.layoutManager = layoutManager
        recyclerView.setHasFixedSize(true)

        findViewById<Button>(R.id.btnNext).setOnClickListener {

            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("functionToCall", NavigationDetails.ADMINISTER_VACCINE.name)
            intent.putExtra("patientId", patientId)
            startActivity(intent)
        }

        getStockManagement()

    }

    private fun getStockManagement() {

        /**
         * TODO: Get the dosage and the route
         */

        val stockList = ArrayList<String>()
        stockList.addAll(
            listOf(
                "Vaccine batch number",
                "Expiration date",
                "Dose quantity",
                "Body site to administer",
                "Vaccine brand",
                "Vaccine manufacturer",
                "Disease targeted"
            )
        )

        val dbVaccineStockDetailsList= ArrayList<DbVaccineStockDetails>()
        for(i in stockList){
            val dbVaccineStockDetails = DbVaccineStockDetails(i, i)
            dbVaccineStockDetailsList.add(dbVaccineStockDetails)
        }
        val vaccineStockAdapter = VaccineStockAdapter(dbVaccineStockDetailsList, this)
        recyclerView.adapter = vaccineStockAdapter

    }

}