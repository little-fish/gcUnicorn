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

/**
 * Waypoint types.
 *
 * @param id ID.
 * @param type Type.
 *
 * @since 1.0.0
 */
enum class WaypointType(val id: String, val type: String) {
    FINAL("flag", "Final Location"),
    OWN("own", "Own"),
    PARKING("pkg", "Parking Area"),
    PUZZLE("puzzle", "Virtual Stage"),
    STAGE("stage", "Physical Stage"),
    TRAILHEAD("trailhead", "Trailhead"),
    WAYPOINT("waypoint", "Reference Point"),
    ORIGINAL("original", "Original Coordinates");

    companion object {

        /**
         * Finds waypoint type be id.
         * @param id ID to search for.
         * @return Found waypoint type, or [WAYPOINT] if there was no match.
         */
        fun findById(id: String) = values().find { it.id==id } ?: WAYPOINT
    }
}