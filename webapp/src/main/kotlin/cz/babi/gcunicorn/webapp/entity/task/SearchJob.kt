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

package cz.babi.gcunicorn.webapp.entity.task

import cz.babi.gcunicorn.`fun`.dateFormat
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job

/**
 * Search job.
 *
 * @param id Search job ID.
 * @param start Start time in milliseconds. Default value is 'now'.
 * @param parent Parent job. Default value is new Job.
 * @param job Deferred job.
 *
 * @author Martin Misiarz `<dev.misiarz@gmail.com>`
 * @version 1.0.0
 * @since 1.0.0
 */
data class SearchJob(val id: Int, val start: Long = System.currentTimeMillis(), val parent: Job = Job(), val job: Deferred<String>) {

    /**
     * Returns status of internal job.
     * @return Status of internal job.
     */
    fun getStatus(): Status {
        return if (job.isCancelled) Status.CANCELED
        else if (job.isActive) Status.ACTIVE
        else if (job.isCompleted) Status.RESOLVED
        else Status.UNKNOWN
    }

    /**
     * Returns start time formatted by given pattern.
     * @param pattern Pattern to be used.
     * @return Formatted start time.
     */
    fun getReadableStart(pattern: String) = start.dateFormat(pattern)
}