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

package cz.babi.gcunicorn.core.network.service.geocachingcom.model

/**
 * Filter data class.
 *
 * It allows you to specify:
 * * list of [CacheType]s to search for,
 * * max distance (in km),
 * * include disabled caches,
 * * exclude own and found caches,
 * * skip premium caches.
 *
 * @param allowedCacheTypes List of allowed cache types. Default value is empty list.
 * @param maxDistance Max distance in km. Default value is [DISABLED_DISTANCE].
 * @param allowDisabled Whether disabled caches are allowed or not. Default value is true.
 * @param includeOwnAndFound Whether own or already found caches are allowed or not. Default value is true.
 * @param skipPremium Whether premium caches should bi skipper. Default value is tru.
 *
 * @author Martin Misiarz `<dev.misiarz@gmail.com>`
 * @version 1.0.0
 * @since 1.0.0
 */
data class CacheFilter(
        val allowedCacheTypes: List<CacheType> = emptyList(),
        val maxDistance: Double = DISABLED_DISTANCE,
        val allowDisabled: Boolean = true,
        val includeOwnAndFound: Boolean = true,
        val skipPremium: Boolean = true
) {
    companion object {
        const val DISABLED_DISTANCE = -1.0
    }
}