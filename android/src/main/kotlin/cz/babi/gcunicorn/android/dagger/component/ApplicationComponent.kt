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

package cz.babi.gcunicorn.android.dagger.component

import android.app.Application
import cz.babi.gcunicorn.android.dagger.module.AndroidModule
import cz.babi.gcunicorn.android.dagger.module.ApplicationModule
import cz.babi.gcunicorn.android.dagger.module.ServiceModule
import cz.babi.gcunicorn.android.service.UnicornService
import cz.babi.gcunicorn.android.ui.activity.BaseAppCompatActivity
import cz.babi.gcunicorn.android.ui.widget.SecureEditTextPreference
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

/**
 * Single application component.
 *
 * @since 1.0.0
 */
@Singleton
@Component(modules = [ AndroidModule::class, ApplicationModule::class, ServiceModule::class ])
interface ApplicationComponent {

    fun inject(baseAppCompatActivity: BaseAppCompatActivity)
    fun inject(secureEditTextPreference: SecureEditTextPreference)
    fun inject(unicornService: UnicornService)

    @Component.Builder
    interface Builder {
        fun build(): ApplicationComponent

        @BindsInstance
        fun application(application: Application): ApplicationComponent.Builder
    }
}