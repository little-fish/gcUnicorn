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

package cz.babi.gcunicorn.core.network.service

import cz.babi.gcunicorn.core.network.service.geocachingcom.model.Geocache

/**
 * Listener that will be notified once a geocache is loaded.
 *
 * @since 1.0.0
 */
interface GeocacheLoadedListener {

    /**
     * Geocache has been loaded.
     * @param geocache Loaded geocache.
     */
    fun geocacheLoaded(geocache: Geocache)
}