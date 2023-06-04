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

package cz.babi.gcunicorn.webapp.entity.task

import java.util.concurrent.ConcurrentHashMap

/**
 * Jobs wrapper.
 *
 * Holds [SearchJob]s for specific users in internal map where key is user's session id.
 *
 * @property sessionJobs Map where key is session ID and value is mutable list of search jobs.
 *
 * @since 1.0.0
 */
class JobsWrapper {
    private val sessionJobs: ConcurrentHashMap<String, MutableList<SearchJob>> = ConcurrentHashMap()

    /**
     * Returns search jobs for given session ID.
     * @param sessionId Session ID.
     * @return Search jobs.
     */
    private fun getSessionJobs(sessionId: String) = sessionJobs.getOrPut(sessionId) { mutableListOf() }!!

    /**
     * Returns search jobs for given session ID.
     * @param sessionId Session ID.
     * @return Search jobs.
     */
    fun getSearchJobs(sessionId: String) = getSessionJobs(sessionId)

    /**
     * Returns search job of given ID for given session ID.
     * @param sessionId Session ID.
     * @param jobId Job ID.
     * @return Search job.
     */
    fun getSearchJob(sessionId: String, jobId: Int) = getSessionJobs(sessionId).find { it.id==jobId }

    /**
     * Returns search jobs count for given session ID.
     * @param sessionId Session ID.
     * @return Search jobs count.
     */
    fun getSearchJobsCount(sessionId: String) = getSessionJobs(sessionId).size

    /**
     * Returns search jobs count of given status for given session ID.
     * @param sessionId Session ID.
     * @param status Status.
     * @return Search jobs count.
     */
    fun getSearchJobsCount(sessionId: String, status: Status) = getSessionJobs(sessionId).count { it.getStatus()==status }

    /**
     * Stores given search job of given job ID and given session ID.
     * @param sessionId Session ID.
     * @param searchJob Search job to store.
     */
    fun putSearchJob(sessionId: String, searchJob: SearchJob) {
        getSessionJobs(sessionId).add(searchJob)
    }
}