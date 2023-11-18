/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intellisoft.chanjoke.vaccine

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import com.intellisoft.chanjoke.MainActivity
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.google.android.fhir.datacapture.QuestionnaireFragment
import com.intellisoft.chanjoke.fhir.data.NavigationDetails

/** A fragment class to show patient registration screen. */
class AdministerVaccineFragment : Fragment(R.layout.administer_vaccine) {

  private val viewModel: AdministerVaccineViewModel by viewModels()
  private val formatterClass = FormatterClass()
  private var patientId : String? = null

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setUpActionBar()
    setHasOptionsMenu(true)
    updateArguments()
    if (savedInstanceState == null) {
      addQuestionnaireFragment()
    }

    patientId = formatterClass.getSharedPref("patientId", requireContext())
    observeResourcesSaveAction()

    childFragmentManager.setFragmentResultListener(
      QuestionnaireFragment.SUBMIT_REQUEST_KEY,
      viewLifecycleOwner,
    ) { _, _ ->
      onSubmitAction()
    }
  }

  private fun observeResourcesSaveAction() {
    viewModel.isResourcesSaved.observe(viewLifecycleOwner) {
      if (!it) {
        Toast.makeText(requireContext(), getString(R.string.inputs_missing), Toast.LENGTH_SHORT)
          .show()
        return@observe
      }
      Toast.makeText(requireContext(), getString(R.string.resources_saved), Toast.LENGTH_SHORT)
        .show()

      val questionnaireJson = formatterClass.getSharedPref("questionnaireJson", requireContext())
      if (questionnaireJson == "update_history.json"){
        createDialog()
      }else{
        val intent = Intent(requireContext(), MainActivity::class.java)
        startActivity(intent)

        NavHostFragment.findNavController(this).navigateUp()
      }


    }
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      android.R.id.home -> {
        NavHostFragment.findNavController(this).navigateUp()
        true
      }
      else -> super.onOptionsItemSelected(item)
    }
  }

  private fun setUpActionBar() {
    (requireActivity() as AppCompatActivity).supportActionBar?.apply {
      title = "Administer Vaccine"
      setDisplayHomeAsUpEnabled(true)
    }
  }

  private fun updateArguments() {

    val questionnaireJson = formatterClass.getSharedPref("questionnaireJson", requireContext())

    requireArguments()
      .putString(QUESTIONNAIRE_FILE_PATH_KEY, questionnaireJson)
  }

  private fun addQuestionnaireFragment() {
    childFragmentManager.commit {
      replace(
        R.id.administerVaccine,
        QuestionnaireFragment.builder().setQuestionnaire(viewModel.questionnaire).build(),
        QUESTIONNAIRE_FRAGMENT_TAG,
      )
    }
  }

  private fun onSubmitAction() {

    val questionnaireFragment =
      childFragmentManager.findFragmentByTag(QUESTIONNAIRE_FRAGMENT_TAG) as QuestionnaireFragment
    viewModel.saveScreenerEncounter(
      questionnaireFragment.getQuestionnaireResponse(),
      patientId.toString(),
    )

  }



  private fun createDialog() {

    val builder = AlertDialog.Builder(requireContext())
    builder.setTitle("Record updated successfully")
    builder.setMessage("Do you want to update Vaccination Details")
    builder.setPositiveButton("Update") { _: DialogInterface, i: Int ->

      formatterClass.saveSharedPref("questionnaireJson","update_history_specifics.json", requireContext())

      val intent = Intent(context, MainActivity::class.java)
      intent.putExtra("functionToCall", NavigationDetails.ADMINISTER_VACCINE.name)
      intent.putExtra("patientId", patientId)
      startActivity(intent)

    }
    builder.setNegativeButton("Close") { dialogInterface: DialogInterface, i: Int ->
      dialogInterface.dismiss()
    }

    val dialog: AlertDialog = builder.create()
    dialog.show()

  }


  companion object {
    const val QUESTIONNAIRE_FILE_PATH_KEY = "questionnaire-file-path-key"
    const val QUESTIONNAIRE_FRAGMENT_TAG = "questionnaire-fragment-tag"
  }
}
