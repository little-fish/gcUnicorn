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

import java.util.*

/**
 * Log types.
 *
 * @param iconId Icon ID.
 * @param isFoundLog Flag determines whether the log type can be taken as 'found log'.
 * @param type Log type.
 *
 * @author Martin Misiarz `<dev.misiarz@gmail.com>`
 * @version 1.0.0
 * @since 1.0.0
 */
enum class LogType(val iconId: String, val isFoundLog: Boolean, val type: String) {
    FOUND_IT("2", true, "Found it"),
    DIDNT_FIND_IT("3", false, "Didn't find it"),
    WRITE_NOTE("4", false, "Write note"),
    PUBLISH_LISTING("24", false, "Enable Listing"),
    ENABLE_LISTING("23", false, "Enable Listing"),
    ARCHIVE("5", false, "Archive"),
    UNARCHIVE("12", false, "Unarchive"),
    TEMPORARILY_DISABLE_LISTING("22", false, "Temporarily Disable Listing"),
    NEEDS_ARCHIVE("7", false, "Needs Archived"),
    WILL_ATTEND("9", false, "Will Attend"),
    ATTENDED("10", true, "Attended"),
    RETRIEVED_IT("13", false, "retrieved it"),
    PLACED_IT("14", false, "placed it"),
    GRABBED_IT("19", false, "grabbed it"),
    NEEDS_MAINTENANCE("45", false, "Needs Maintenance"),
    OWNER_MAINTENANCE("46", false, "Owner Maintenance"),
    UPDATE_COORDINATES("47", false, "Update Coordinates"),
    DISCOVERED_IT("48", false, "Discovered It"),
    POST_REVIEWER_NOTE("18", false, "Post Reviewer Note"),
    SUBMIT_FOR_REVIEW("76", false, "submit for review"),
    VISIT("75", false, "visit"),
    WEBCAM_PHOTO_TAKEN("11", true, "Webcam Photo Taken"),
    ANNOUNCEMENT("74", false, "Announcement"),
    MOVE_COLLECTION("69", false, "unused_collection"),
    MOVE_INVENTORY("70", false, "unused_inventory"),
    RETRACT("25", false, "Retract Listing"),
    MARKED_MISSING("16", false, "marked missing"),
    OC_TEAM_COMMENT("-", false, "X1"),
    UNKNOWN("0", false, "");

    companion object {

        /**
         * Finds log type based on given icon id.
         *
         * There is a special case for post reviewer note, which appears sometimes as 18.png (in individual entries) or as 68.png (in logs counts).
         * @param iconId Icon ID.
         * @return Found log type or [UNKNOWN] if there was no match.
         */
        fun findByIconId(iconId: String) = values().find { logType -> logType.iconId==iconId } ?: if(iconId=="68") POST_REVIEWER_NOTE else UNKNOWN

        /**
         * Finds log type based on given type.
         * @param type Type.
         * @return Found log type or [UNKNOWN] if there was no match.
         */
        fun findByType(type: String) = values().find { logType -> logType.type.lowercase(Locale.getDefault()) == type.lowercase(Locale.getDefault()) } ?: UNKNOWN
    }
}