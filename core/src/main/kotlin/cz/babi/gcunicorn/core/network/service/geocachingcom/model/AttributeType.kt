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
 * Attribute types.
 *
 * @param id Attribute ID.
 * @param pattern Attribute pattern.
 *
 * @author Martin Misiarz `<dev.misiarz@gmail.com>`
 * @version 1.0.0
 * @since 1.0.0
 */
enum class AttributeType(val id: Int, val pattern: String) {
    DOGS(1, "dogs"),
    FEE(2, "fee"),
    RAPPELLING(3, "rappelling"),
    BOAT(4, "boat"),
    SCUBA(5, "scuba"),
    KIDS(6, "kids"),
    ONE_HOUR(7, "onehour"),
    SCENIC(8, "scenic"),
    HIKING(9, "hiking"),
    CLIMBING(10, "climbing"),
    WADING(11, "wading"),
    SWIMMING(12, "swimming"),
    AVAILABLE(13, "available"),
    NIGHT(14, "night"),
    WINTER(15, "winter"),
    POISONOAK(17, "poisonoak"),
    DANGEROUS_ANIMALS(18, "dangerousanimals"),
    TICKS(19, "ticks"),
    MINE(20, "mine"),
    CLIFF(21, "cliff"),
    HUNTING(22, "hunting"),
    DANGER(23, "danger"),
    WHEELCHAIR(24, "wheelchair"),
    PARKING(25, "parking"),
    PUBLIC(26, "public"),
    WATER(27, "water"),
    REST_ROOMS(28, "restrooms"),
    PHONE(29, "phone"),
    PICNIC(30, "picnic"),
    CAMPING(31, "camping"),
    BICYCLES(32, "bicycles"),
    MOTORCYCLES(33, "motorcycles"),
    QUADS(34, "quads"),
    JEEPS(35, "jeeps"),
    SNOWMOBILES(36, "snowmobiles"),
    HORSES(37, "horses"),
    CAMPFIRES(38, "campfires"),
    THORN(39, "thorn"),
    STEALTH(40, "stealth"),
    STROLLER(41, "stroller"),
    FIRST_AID(42, "firstaid"),
    COW(43, "cow"),
    FLASHLIGHT(44, "flashlight"),
    LANDF(45, "landf"),
    RV(46, "rv"),
    FIELD_PUZZLE(47, "field_puzzle"),
    UV(48, "uv"),
    SNOWSHOES(49, "snowshoes"),
    SKIIS(50, "skiis"),
    S_TOOL(51, "s_tool"),
    NIGHT_CACHE(52, "nightcache"),
    PARKNGRAB(53, "parkngrab"),
    ABANDONE_BUILDING(54, "abandonebuilding"),
    HIKE_SHORT(55, "hike_short"),
    HIKE_MED(56, "hike_med"),
    HIKE_LONG(57, "hike_long"),
    FUEL(58, "fuel"),
    FOOD(59, "food"),
    WIRELESS_BEACON(60, "wirelessbeacon"),
    PARTNERSHIP(61, "partnership"),
    SEASONAL(62, "seasonal"),
    TOURIST_OK(63, "touristok"),
    TREECLIMBING(64, "treeclimbing"),
    FRONT_YARD(65, "frontyard"),
    TEAMWORK(66, "teamwork"),
    GEOTOUR(67, "geotour");

    companion object {

        /**
         * Finds cache attribute by given pattern.
         * @param pattern Attribute type pattern.
         * @return Cache attribute if found. Or null if there is no match.
         */
        fun findByPattern(pattern: String) = values().find { attribute -> attribute.pattern==pattern }
    }
}