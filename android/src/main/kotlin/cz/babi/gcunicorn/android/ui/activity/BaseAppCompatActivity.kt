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

package cz.babi.gcunicorn.android.ui.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import cz.babi.gcunicorn.android.`fun`.androidApplication
import cz.babi.gcunicorn.android.dagger.qualifier.Named
import cz.babi.gcunicorn.android.dagger.qualifier.Named.Companion.PREFERENCES_NORMAL
import cz.babi.gcunicorn.android.dagger.qualifier.Named.Companion.PREFERENCES_PRIVATE
import cz.babi.gcunicorn.android.security.Security
import javax.inject.Inject

/**
 * Base activity.
 *
 * It stores properties injected by Dagger.
 *
 * @author Martin Misiarz `<dev.misiarz@gmail.com>`
 * @version 1.0.0
 * @since 1.0.0
 */
abstract class BaseAppCompatActivity : AppCompatActivity() {

    @Inject
    protected lateinit var security: Security

    @Inject
    @field:Named(PREFERENCES_NORMAL)
    protected lateinit var sharedPreferences: android.content.SharedPreferences

    @Inject
    @field:Named(PREFERENCES_PRIVATE)
    protected lateinit var privateSharedPreferences: android.content.SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        androidApplication.applicationComponent().inject(this)

        onAfterCreate(savedInstanceState)
    }

    /**
     * Don't call super.onCreate(savedInstanceState). This method has been already called.
     * @param savedInstanceState Saved instance.
     */
    abstract fun onAfterCreate(savedInstanceState: Bundle?)
}
