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

import cz.babi.gcunicorn.core.network.model.Image

/**
 * Log entry data class.
 *
 * @param id Log entry ID.
 * @param type Log type.
 * @param text Text.
 * @param visited Date (in milliseconds) of log entry creation.
 * @param author Author.
 * @param authorId Author's ID.
 * @param images List of images.
 *
 * @author Martin Misiarz `<dev.misiarz@gmail.com>`
 * @version 1.0.0
 * @since 1.0.0
 */
data class LogEntry(
        var id: Long? = null,
        var type: LogType? = null,
        var text: String? = null,
        var visited: Long? = null,
        var author: String? = null,
        var authorId: Long? = null,
        var images: List<Image>? = null
)