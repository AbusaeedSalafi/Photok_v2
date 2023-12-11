/*
 *   Copyright 2020-2023 Leon Latsch
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.lockmedia.applock.settings.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import dagger.hilt.android.AndroidEntryPoint
import com.lockmedia.applock.R
import com.lockmedia.applock.backupData.ui.BackupBottomSheetDialogFragment
import com.lockmedia.applock.databinding.BindingConverters
import com.lockmedia.applock.other.extensions.show
import com.lockmedia.applock.other.extensions.startActivityForResultAndIgnoreTimer
import com.lockmedia.applock.other.openUrl
import com.lockmedia.applock.other.setAppDesign
import com.lockmedia.applock.settings.data.Config
import com.lockmedia.applock.settings.ui.changepassword.ChangePasswordDialog
import com.lockmedia.applock.uiunits.Dialogs
import javax.inject.Inject

/**
 * Preference Fragment. Loads preferences from xml resource.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat() {

    private val viewModel: SettingsViewModel by viewModels()
    private var toolbar: Toolbar? = null

    @Inject
    lateinit var config: Config

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        toolbar = view.findViewById(R.id.custom_menu_lock)

        toolbar?.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }



    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)

        setupAppCategory()
        setupSecurityCategory()
        setupAdvancedCategory()
        setupOtherCategory()
    }


    private fun setupAppCategory() {
        addCallbackTo<ListPreference>(Config.SYSTEM_DESIGN) {
            setAppDesign(it as String)
        }
    }

    private fun setupSecurityCategory() {
        addActionTo(KEY_ACTION_CHANGE_PASSWORD) {
            ChangePasswordDialog().show(childFragmentManager)
        }




    }

    private fun setupAdvancedCategory() {
        addActionTo(KEY_ACTION_RESET) {
            Dialogs.showConfirmDialog(
                requireContext(),
                getString(R.string.settings_advanced_reset_confirmation)
            ) { _, _ ->
                viewModel.resetComponents()
            }
        }

        addActionTo(KEY_ACTION_BACKUP) {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
            intent.type = "application/zip"
            intent.putExtra(
                Intent.EXTRA_TITLE,
                "photok_backup_${BindingConverters.millisToFormattedDateConverter(System.currentTimeMillis())}.zip"
            )
            startActivityForResultAndIgnoreTimer(
                Intent.createChooser(intent, "Select Backup File"),
                REQ_BACKUP
            )
        }
    }

    private fun setupOtherCategory() {
        addActionTo(KEY_ACTION_FEEDBACK) {
            val emailIntent = Intent(
                Intent.ACTION_SENDTO,
                Uri.fromParts(
                    SCHEMA_MAILTO,
                    getString(R.string.settings_other_feedback_mail_emailaddress),
                    null
                )
            )
            emailIntent.putExtra(
                Intent.EXTRA_SUBJECT,
                getString(R.string.settings_other_feedback_mail_subject)
            )
            emailIntent.putExtra(
                Intent.EXTRA_TEXT,
                getString(R.string.settings_other_feedback_mail_body)
            )
            startActivity(
                Intent.createChooser(
                    emailIntent,
                    getString(R.string.settings_other_feedback_title)
                )
            )
        }





        addActionTo(KEY_ACTION_ABOUT) {

            val url = getString(R.string.aboutus)
            openUrl(requireContext(), url)

        }
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_BACKUP && resultCode == Activity.RESULT_OK) {
            val uri = data?.data
            uri ?: return
            BackupBottomSheetDialogFragment(uri).show(requireActivity().supportFragmentManager)
        }
    }

    private fun addActionTo(preferenceId: String, action: () -> Unit) {
        preferenceManager
            .findPreference<Preference>(preferenceId)
            ?.setOnPreferenceClickListener {
                action()
                true
            }
    }

    private fun <T : Preference> addCallbackTo(preferenceId: String, action: (value: Any) -> Unit) {
        preferenceManager.findPreference<T>(preferenceId)
            ?.setOnPreferenceChangeListener { _, newValue ->
                action(newValue)
                true
            }
    }

    companion object {
        const val REQ_BACKUP = 42

        const val SCHEMA_MAILTO = "mailto"

        const val KEY_ACTION_RESET = "action_reset_safe"
        const val KEY_ACTION_CHANGE_PASSWORD = "action_change_password"
        const val KEY_ACTION_BACKUP = "action_backup_safe"
        const val KEY_ACTION_FEEDBACK = "action_feedback"
        const val KEY_ACTION_ABOUT = "action_about"

    }
    override fun onResume() {
        super.onResume()
      //  showBottomNavigationBar(true)
    }

    override fun onPause() {
        super.onPause()
       // showBottomNavigationBar(false)
    }


}