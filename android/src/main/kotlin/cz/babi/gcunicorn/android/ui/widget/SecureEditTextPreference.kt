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

package cz.babi.gcunicorn.android.ui.widget

import android.content.Context
import android.content.SharedPreferences
import android.util.AttributeSet
import androidx.preference.EditTextPreference
import cz.babi.gcunicorn.android.dagger.AndroidApplication
import cz.babi.gcunicorn.android.dagger.qualifier.Named
import cz.babi.gcunicorn.android.dagger.qualifier.Named.Companion.PREFERENCES_PRIVATE
import cz.babi.gcunicorn.android.preference.PreferenceKey
import cz.babi.gcunicorn.android.security.Security
import javax.inject.Inject

/**
 * Custom edit text preference which handles encrypting/decrypting of underlying text.
 *
 * @since 1.0.0
 */
class SecureEditTextPreference : EditTextPreference {
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context) : super(context)

    init {
        AndroidApplication.APPLICATION_COMPONENT.inject(this)
    }

    @Inject
    lateinit var security: Security

    @Inject
    @field:Named(PREFERENCES_PRIVATE)
    lateinit var privateSharedPreferences: SharedPreferences

    override fun getText(): String? {
        val text = super.getText()
        text?.also {
            getSecureKey()?.also { key ->
                return security.decryptWithBase64(key, it)
            }
        }

        return super.getText()
    }

    override fun setText(text: String?) {
        text?.also {
            getSecureKey()?.also { key ->
                super.setText(security.encryptWithBase64(key, it))
                return
            }
        }

        super.setText(text)
    }

    override fun onSetInitialValue(restorePersistedValue: Boolean, defaultValue: Any?) {
        super.setText(if (restorePersistedValue) getPersistedString(null) else defaultValue as String)
    }

    /**
     * Obtains secure key from private preferences.
     */
    private fun getSecureKey() = privateSharedPreferences.getString(PreferenceKey.SECURE_KEY.key, null)
}