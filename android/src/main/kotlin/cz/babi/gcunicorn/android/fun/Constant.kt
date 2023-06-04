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

package cz.babi.gcunicorn.android.`fun`

/**
 * Application wide constants.
 *
 * @since 1.0.0
 */
const val PERMISSION_LOCATION_FINE = 1
const val PERMISSION_LOCATION_COARSE = 2
const val PERMISSION_WRITE_EXTERNAL_STORAGE = 3
const val PERMISSION_WAKE_LOCK = 4
const val PERMISSION_READ_EXTERNAL_STORAGE = 5
const val FILTER_MAX_CACHE_COUNT = 200
const val FILTER_DEFAULT_CACHE_COUNT = 100
const val FILTER_MAX_DISTANCE = 10.0
const val NOTIFICATION_CHANNEL_ID = "cz.babi.gcunicorn.android.NOTIFICATION"
const val NOTIFICATION_ID_DOWNLOADING = 1
const val SERVICE_JOB_ID = 1
const val DATETIME_PATTERN_GPX = "yyyy-MM-dd_HH-mm-ss"
const val DATETIME_PATTERN_UI = "yyyy-MM-dd HH:mm:ss"
const val ACTION_CANCEL_DOWNLOADING = "cz.babi.gcunicorn.android.ACTION.cancel"
const val ACTION_NOTIFY = "cz.babi.gcunicorn.android.ACTION.notify"
const val ACTION_EXPORT_GPX = "cz.babi.gcunicorn.android.ACTION.export.gpx"
const val ACTIVITY_RESULT_PLACE_PICKER = 1
const val ACTIVITY_RESULT_OPEN_DOCUMENT_TREE = 2
