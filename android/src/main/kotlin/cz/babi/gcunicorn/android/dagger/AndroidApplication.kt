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

package cz.babi.gcunicorn.android.dagger

import androidx.multidex.MultiDexApplication
import cz.babi.gcunicorn.android.dagger.component.ApplicationComponent
import cz.babi.gcunicorn.android.dagger.component.DaggerApplicationComponent

/**
 * Custom application class.
 * It takes care about instantiation of Dagger component.
 *
 * @author Martin Misiarz `<dev.misiarz@gmail.com>`
 * @version 1.0.0
 * @since 1.0.0
 */
class AndroidApplication : MultiDexApplication() {

    companion object {
        // Let's make the component to be a companion to be able to obtain it from outside the Android world.
        lateinit var APPLICATION_COMPONENT: ApplicationComponent
    }

    init {
        APPLICATION_COMPONENT = DaggerApplicationComponent.builder()
                .application(this)
                .build()
    }

    fun applicationComponent() = APPLICATION_COMPONENT
}