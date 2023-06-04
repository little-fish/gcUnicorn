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
 * Cache types.
 *
 * Codes are taken from: [https://www.geocaching.com/seek/nearest.aspx](https://www.geocaching.com/seek/nearest.aspx).
 *
 * The pattern is used for extracting cache type from the cache's image title.
 *
 * The id is used for exporting into GPX file.
 *
 * @param code Cache type code.
 * @param pattern Cache type pattern.
 * @param id Cache type id.
 * @param wptTypeId Waypoint type id.
 *
 * @since 1.0.0
 */
enum class CacheType(val code: String, val pattern: String, val id: String, val wptTypeId: String) {
    ALL("9a79e6ce-3344-409c-bbe9-496530baf758", "All Geocaches", "", ""),
    TRADITIONAL("32bc9333-5e52-4957-b0f6-5a2c8fc7b257", "Traditional Geocache", "Traditional Cache", "2"),
    MULTI("a5f6d0ad-d2f2-4011-8c14-940a9ebf3c74", "Multi-cache", "Multi-cache", "3"),
    VIRTUAL("294d4360-ac86-4c83-84dd-8113ef678d7e", "Virtual Cache", "Virtual Cache", "4"),
    LETTERBOX("4bdd8fb2-d7bc-453f-a9c5-968563b15d24", "Letterbox Hybrid", "Letterbox hybrid", "5"),
    EVENT("69eb8534-b718-4b35-ae3c-a856a55b0874", "Event Cache", "Event Cache", "6"),
    CITO("57150806-bc1a-42d6-9cf0-538d171a2d22", "Cache In Trash Out Event", "Cache in Trash out Event", "13"),
    EVENT_MEGA("69eb8535-b718-4b35-ae3c-a856a55b0874", "Mega-Event Cache", "Mega-Event Cache", "453"),
    EVENT_LOST_FOUND("3ea6533d-bb52-42fe-b2d2-79a3424d4728", "Lost and Found Event Cache", "Lost and Found Event Cache", "3653"),
    EVENT_GIGA("51420629-5739-4945-8bdd-ccfd434c0ead", "Giga-Event Cache", "Giga-Event Cache", "7005"),
    GROUNDSPEAK_LOST_FOUND("af820035-787a-47af-b52b-becc8b0c0c88", "Groundspeak Lost and Found Celebration", "Groundspeak Lost and Found Celebration", "3774"),
    GROUNDSPEAK_BLOCK_PARTY("bc2f3df2-1aab-4601-b2ff-b5091f6c02e3", "Groundspeak Block Party", "Groundspeak Block Party", "4738"),
    GROUNDSPEAK_HQ("416f2494-dc17-4b6a-9bab-1a29dd292d8c", "Groundspeak HQ", "Groundspeak HQ", "3773"),
    UNKNOWN("", "Unknown Cache", "", ""),
    MYSTERY("40861821-1835-4e11-b666-8d41064d03fe", "Mystery Cache", "Unknown Cache", "8"),
    PROJECT_APE("2555690d-b2bc-4b55-b5ac-0cb704c0b768", "Project APE Cache", "Project Ape Cache", "9"),
    WEBCAM("31d2ae3c-c358-4b5f-8dcd-2185bf472d3d", "Webcam Cache", "Webcam Cache", "11"),
    EARTH("c66f5cf3-9523-4549-b8dd-759cd2f18db8", "EarthCache", "Earthcache", "137"),
    GPS_ADVENTURE("72e69af2-7986-4990-afd9-bc16cbbb4ce3", "GPS Adventures Exhibit", "GPS Adventures Exhibit", "1304"),
    WHERIGO("0544fa55-772d-4e5c-96a9-36a51ebcf5c9", "Wherigo Cache", "Wherigo Cache", "1858");

    companion object {

        /**
         * Finds cache type by given pattern.
         * @param pattern Cache type pattern.
         * @return Cache type if found. Or [UNKNOWN] type if there is no match.
         */
        fun findByPattern(pattern: String) = values().find { cacheType -> cacheType.pattern == pattern } ?: UNKNOWN

        /**
         * Finds cache type by given code.
         * @param code Cache type code.
         * @return Cache type if found. Or [UNKNOWN] type if there is no match.
         */
        fun findByCode(code: String) = values().find { cacheType -> cacheType.code == code } ?: UNKNOWN

        /**
         * Finds cache type by given wptTypeId.
         * @param wptTypeId Cache wptTypeId.
         * @return Cache type if found. Or [UNKNOWN] type if there is no match.
         */
        fun findByWptTypeId(wptTypeId: String) = values().find { cacheType -> cacheType.wptTypeId == wptTypeId } ?: UNKNOWN
    }
}