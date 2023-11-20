package com.intellisoft.chanjoke.vaccine.stock_management

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.fhir.data.DbVaccineStockDetails

class VaccineStockManagement : AppCompatActivity() {
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var recyclerView:RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vaccine_stock_management)

        recyclerView = findViewById(R.id.recyclerView)
        layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )
        recyclerView.layoutManager = layoutManager
        recyclerView.setHasFixedSize(true)

        getStockManagement()

    }

    private fun getStockManagement() {

        val dbVaccineStockDetailsList= ArrayList<DbVaccineStockDetails>()
        for(i in 1..10){
            val dbVaccineStockDetails = DbVaccineStockDetails(i.toString(), "xx $i")
            dbVaccineStockDetailsList.add(dbVaccineStockDetails)
        }
        val vaccineStockAdapter = VaccineStockAdapter(dbVaccineStockDetailsList, this)
        recyclerView.adapter = vaccineStockAdapter

    }
}