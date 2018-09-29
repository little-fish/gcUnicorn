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
 * Cache sizes.
 *
 * The pattern is used for extracting cache size from the cache's size image file.
 *
 * @param pattern Cache size type pattern.
 * @param id Cache size type id.
 *
 * @author Martin Misiarz `<dev.misiarz@gmail.com>`
 * @version 1.0.0
 * @since 1.0.0
 */
enum class CacheSizeType(val pattern: String, val id: String) {
    MICRO("micro", "Micro"),
    SMALL("small", "Small"),
    REGULAR("regular", "Regular"),
    LARGE("large", "Large"),
    OTHER("other", "Other"),
    VIRTUAL("virtual", "Virtual"),
    NOT_CHOSEN("not_chosen", "Not chosen"),
    UNKNOWN("unknown", "Unknown");

    companion object {

        /**
         * Finds cache size by given pattern.
         * @param pattern Cache size pattern.
         * @return Cache size if found. Or [UNKNOWN] type if there is no match.
         */
        fun findByPattern(pattern: String) = CacheSizeType.values().find { cacheSize -> cacheSize.pattern==pattern } ?: CacheSizeType.UNKNOWN
    }
}