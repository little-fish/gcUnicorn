/*
 * gcUnicorn
 * Copyright (C) 2023  Martin Misiarz
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package cz.babi.gcunicorn.android.ui.fragment

import android.os.Bundle
import android.text.InputType
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import cz.babi.gcunicorn.android.R
import cz.babi.gcunicorn.android.preference.PreferenceKey

/**
 * Settings fragment.
 *
 * @author Martin Misiarz
 * @author dev.misiarz@gmail.com
 */
class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        findPreference<Preference>(PreferenceKey.GC_USERNAME.key)?.apply {
            summary = sharedPreferences?.getString(PreferenceKey.GC_USERNAME.key, null)
            onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                summary = newValue?.toString()
                true
            }
        }

        findPreference<Preference>(PreferenceKey.GC_PASSWORD.key)?.apply {
            summary = generateSecretSummary(sharedPreferences?.getString(PreferenceKey.GC_PASSWORD.key, null))
            onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                summary = generateSecretSummary(newValue?.toString())
                true
            }

            if (this is EditTextPreference) {
                setOnBindEditTextListener { editText ->
                    editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                }
            }
        }
    }

    /**
     * Generates summary for password field.
     */
    private fun generateSecretSummary(value: String?) = when {
        value.isNullOrEmpty() -> null
        else -> "<${resources.getString(R.string.text_encrypted)}>"
    }
}