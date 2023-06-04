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

package cz.babi.gcunicorn.core.network.service.geocachingcom.model

import cz.babi.gcunicorn.core.location.Coordinates
import cz.babi.gcunicorn.core.network.model.Image

/**
 * Geocache data class.
 *
 * @param guid GUID.
 * @param type Cache's type.
 * @param isDisabled Whether the cache is disabled or not.
 * @param ownerName Owner name.
 * @param ownerId Owner ID.
 * @param isArchived Is geocache archived.
 * @param isFavorite Is geocache marked as favorite for logged-in user.
 * @param watchlistCount Watchlist count.
 * @param terrain Terrain.
 * @param difficulty Difficulty.
 * @param sizeType Size type.
 * @param favoriteCount Favorite count.
 * @param hiddenDate Date of hide.
 * @param coordinates Coordinates.
 * @param location Location.
 * @param hint Hint.
 * @param personalNote Personal note.
 * @param longDescription Long description.
 * @param shortDescription Short description.
 * @param attributes List of attributes.
 * @param found Is cache already found by logged-in user.
 * @param onWatchList Is cache on logged in user's watchlist.
 * @param spoilers List of spoilers.
 * @param inventory Cache inventory.
 * @param logCounts Counts of log types.
 * @param waypoints Cache's waypoints.
 * @param logEntries Log entries.
 *
 * @since 1.0.0
 */
data class Geocache(
        var guid: String? = null,
        var type: CacheType? = null,
        var isDisabled: Boolean? = null,
        var ownerName: String? = null,
        var ownerId: String? = null,
        var isArchived: Boolean? = null,
        var isFavorite: Boolean? = null,
        var watchlistCount: Int? = null,
        var terrain: Double? = null,
        var difficulty: Double? = null,
        var sizeType: CacheSizeType? = null,
        var favoriteCount: Int? = null,
        var hiddenDate: Long? = null,
        var coordinates: Coordinates? = null,
        var location: String? = null,
        var hint: String? = null,
        var personalNote: String? = null,
        var longDescription: String? = null,
        var shortDescription: String? = null,
        var attributes: List<Attribute>? = null,
        var found: Boolean? = null,
        var onWatchList: Boolean? = null,
        var spoilers: List<Image>? = null,
        var inventory: List<Trackable>? = null,
        var logCounts: Map<LogType, Int>? = null,
        var waypoints: List<Waypoint>? = null,
        var logEntries: List<LogEntry>? = null
) : GeocacheLite() {
        constructor(geocacheLite: GeocacheLite) : this() {
                url = geocacheLite.url
                isPremiumOnly = geocacheLite.isPremiumOnly
        }

        fun getState() = location?.split(",")?.getOrNull(0)?.trim()
        fun getCountry() = location?.split(",")?.getOrNull(1)?.trim()
}