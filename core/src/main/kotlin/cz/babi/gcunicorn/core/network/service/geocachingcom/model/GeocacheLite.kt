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
 * Geocache lite class.
 *
 * @param id ID.
 * @param name Name.
 * @param code Code.
 * @param url Cache's url.
 * @param isPremiumOnly Whether the cache is marked as premium only.
 *
 * @since 1.0.0
 */
open class GeocacheLite(
        var id: Long? = null,
        var name: String? = null,
        var code: String? = null,
        var url: String = "",
        var isPremiumOnly: Boolean? = null
) {
        override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as GeocacheLite

                if (id != other.id) return false
                if (name != other.name) return false
                if (code != other.code) return false
                if (url != other.url) return false
                return isPremiumOnly == other.isPremiumOnly
        }

        override fun hashCode(): Int {
                var result = id?.hashCode() ?: 0
                result = 31 * result + (name?.hashCode() ?: 0)
                result = 31 * result + (code?.hashCode() ?: 0)
                result = 31 * result + url.hashCode()
                result = 31 * result + (isPremiumOnly?.hashCode() ?: 0)
                return result
        }

        override fun toString(): String {
                return "GeocacheLite(id=$id, name=$name, code=$code, url='$url', isPremiumOnly=$isPremiumOnly)"
        }
}