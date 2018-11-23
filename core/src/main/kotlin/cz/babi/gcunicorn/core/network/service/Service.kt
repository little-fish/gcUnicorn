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

package cz.babi.gcunicorn.core.network.service

import cz.babi.gcunicorn.core.exception.network.LoginException
import cz.babi.gcunicorn.core.exception.network.LogoutException
import cz.babi.gcunicorn.core.exception.network.ServiceException
import cz.babi.gcunicorn.core.location.Coordinates
import cz.babi.gcunicorn.core.network.model.Credentials
import cz.babi.gcunicorn.core.network.service.geocachingcom.model.CacheFilter
import cz.babi.gcunicorn.core.network.service.geocachingcom.model.Geocache
import kotlinx.coroutines.Job

/**
 * Service interface providing basic set of methods required for grabbing geocaches.
 *
 * @author Martin Misiarz `<dev.misiarz@gmail.com>`
 * @version 1.0.0
 * @since 1.0.0
 */
interface Service {

    /**
     * Log in with given credentials. If login was successful, it returns nothing. Otherwise it throws an exception.
     * @param credentials Credentials to log in with.
     * @throws [LoginException] If anything goes wrong.
     */
    @Throws(LoginException::class)
    fun login(credentials: Credentials)

    /**
     * Log out currently logged in user. If logout was successful, it returns nothing. Otherwise it throws an exception.
     * @throws [LogoutException] If anything goes wrong.
     */
    @Throws(LogoutException::class)
    fun logout()

    /**
     * Checks whether an user is logged in within given page body.
     * @param pageBody Page body to check.
     * @return True if an user is logged in, otherwise returns false.
     */
    fun isLoggedIn(pageBody: String): Boolean

    /**
     * Looks for caches.
     * @param coordinates Coordinates which represents center of searching.
     * @param cacheFilter Filter used while looking for caches.
     * @param limit Max count of caches to look for.
     * @param geocacheLoadedListener Listener that will be notified every time a geocache is fully loaded.
     * @param parent Parent job which can cancel all subsequent coroutine jobs.
     * @return List of found caches.
     * @throws [ServiceException] If anything goes wrong.
     * @see [CacheFilter]
     */
    @Throws(ServiceException::class)
    suspend fun lookForCaches(coordinates: Coordinates, cacheFilter: CacheFilter, limit: Int, geocacheLoadedListener: GeocacheLoadedListener?, parent: Job?): List<Geocache>

    /**
     * Creates GPX file of given geocaches.
     * Used namespaces:
     * * GPX v1.1
     * * Groundspeak v1.0.1
     * * GSAK v1.6
     *
     * @param geocaches Geocaches to be included in output GPX.
     * @param formatOutput Pass true if the GPX output should be formatted. Otherwise pass false.
     * @return String representation of created GPX file.
     */
    fun createGpx(geocaches: List<Geocache>, formatOutput: Boolean): String
}