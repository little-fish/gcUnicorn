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

package cz.babi.gcunicorn.android.dagger.module

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import cz.babi.gcunicorn.android.dagger.qualifier.Named
import cz.babi.gcunicorn.android.preference.PreferenceKey
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Module that provides Android related dependencies.
 *
 * @since 1.0.0
 */
@Module
class AndroidModule {

    @Provides
    @Singleton
    @Named(Named.PREFERENCES_NORMAL)
    fun providesSharedPreferences(application: Application) = PreferenceManager.getDefaultSharedPreferences(application)!!

    @Provides
    @Singleton
    @Named(Named.PREFERENCES_PRIVATE)
    fun providesPrivateSharedPreferences(application: Application): SharedPreferences = application.getSharedPreferences(PreferenceKey.PRIVATE_PREFS, Context.MODE_PRIVATE)!!
}