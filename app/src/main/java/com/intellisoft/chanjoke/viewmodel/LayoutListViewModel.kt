package com.intellisoft.chanjoke.viewmodel


import android.app.Application
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import com.intellisoft.chanjoke.R

class LayoutListViewModel(application: Application, private val state: SavedStateHandle) :
    AndroidViewModel(application) {

    fun getLayoutList(): List<Layout> {
        return Layout.values().toList()
    }

    enum class Layout(
        @DrawableRes val iconId: Int,
        val textId: String,
    ) {
        SEARCH_CLIENT(R.drawable.search, "Search Client"),
        REGISTER_CLIENT(R.drawable.register, "Register Client"),
        UPDATE_CLIENT_HISTORY(R.drawable.update_client, "Update Vaccine History"),
        ADMINISTER_VACCINE(R.drawable.administer, "Administer vaccine"),
        REFERRALS(R.drawable.referral, "Referrals"),
        REPORTS(R.drawable.ic_action_reports, "Reports"),
        APPOINMENTS(R.drawable.appoinments, "Appointments"),
    }
}
