/*
 * gcUnicorn
 * Copyright (C) 2018  Martin Misiarz
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
import android.support.v7.preference.Preference
import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat
import cz.babi.gcunicorn.android.R
import cz.babi.gcunicorn.android.preference.PreferenceKey

/**
 * Settings fragment.
 *
 * @author Martin Misiarz
 * @author dev.misiarz@gmail.com
 */
class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferencesFix(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        findPreference(PreferenceKey.GC_USERNAME.key).apply {
            summary = sharedPreferences.getString(PreferenceKey.GC_USERNAME.key, null)
            onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                summary = newValue?.toString()
                true
            }
        }

        findPreference(PreferenceKey.GC_PASSWORD.key).apply {
            summary = generateSecretSummary(sharedPreferences.getString(PreferenceKey.GC_PASSWORD.key, null))
            onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                summary = generateSecretSummary(newValue?.toString())
                true
            }
        }
    }

    /**
     * Generates summary for password field.
     */
    private fun generateSecretSummary(value: String?) = when {
        value==null || value.isEmpty() -> null
        else -> "<${resources.getString(R.string.text_encrypted)}>"
    }
}