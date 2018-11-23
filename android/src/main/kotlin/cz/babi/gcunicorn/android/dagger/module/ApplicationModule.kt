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

package cz.babi.gcunicorn.android.dagger.module

import cz.babi.gcunicorn.android.security.Security
import cz.babi.gcunicorn.android.storage.FileWorker
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Module that provides common dependencies.
 *
 * @author Martin Misiarz `<dev.misiarz@gmail.com>`
 * @version 1.0.0
 * @since 1.0.0
 */
@Module
class ApplicationModule {

    @Provides
    @Singleton
    fun providesSecurity() = Security()

    @Provides
    @Singleton
    fun providesFileWorker() = FileWorker()
}