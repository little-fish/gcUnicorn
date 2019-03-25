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

package cz.babi.gcunicorn.core.network.service.geocachingcom

import cz.babi.gcunicorn.`fun`.containsHtml
import cz.babi.gcunicorn.`fun`.format
import cz.babi.gcunicorn.`fun`.logger
import cz.babi.gcunicorn.`fun`.nullableExecute
import cz.babi.gcunicorn.`fun`.rot13
import cz.babi.gcunicorn.core.exception.location.CoordinateParseException
import cz.babi.gcunicorn.core.exception.network.LoginException
import cz.babi.gcunicorn.core.exception.network.LogoutException
import cz.babi.gcunicorn.core.exception.network.NetworkException
import cz.babi.gcunicorn.core.exception.network.ServiceException
import cz.babi.gcunicorn.core.location.Coordinates
import cz.babi.gcunicorn.core.location.parser.Parser
import cz.babi.gcunicorn.core.network.Network
import cz.babi.gcunicorn.core.network.model.Credentials
import cz.babi.gcunicorn.core.network.model.DeferredResult
import cz.babi.gcunicorn.core.network.model.HttpParameters
import cz.babi.gcunicorn.core.network.model.Image
import cz.babi.gcunicorn.core.network.service.GeocacheLoadedListener
import cz.babi.gcunicorn.core.network.service.Service
import cz.babi.gcunicorn.core.network.service.geocachingcom.model.Attribute
import cz.babi.gcunicorn.core.network.service.geocachingcom.model.AttributeType
import cz.babi.gcunicorn.core.network.service.geocachingcom.model.CacheFilter
import cz.babi.gcunicorn.core.network.service.geocachingcom.model.CacheSizeType
import cz.babi.gcunicorn.core.network.service.geocachingcom.model.CacheType
import cz.babi.gcunicorn.core.network.service.geocachingcom.model.Geocache
import cz.babi.gcunicorn.core.network.service.geocachingcom.model.GeocacheLite
import cz.babi.gcunicorn.core.network.service.geocachingcom.model.LogEntry
import cz.babi.gcunicorn.core.network.service.geocachingcom.model.LogType
import cz.babi.gcunicorn.core.network.service.geocachingcom.model.Trackable
import cz.babi.gcunicorn.core.network.service.geocachingcom.model.TrackableBrand
import cz.babi.gcunicorn.core.network.service.geocachingcom.model.Waypoint
import cz.babi.gcunicorn.core.network.service.geocachingcom.model.WaypointType
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonTreeParser
import kotlinx.serialization.json.content
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.longOrNull
import org.redundent.kotlin.xml.xml
import org.slf4j.Logger
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.CancellationException
import java.util.concurrent.atomic.AtomicInteger
import java.util.regex.Pattern
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Service implementation for Groundspeak's geocaching.com web page.
 *
 * @param network Network. It is used for communication with external sites.
 * @param parser Coordination parser. Parse used for parsing geocaches' coordinates.
 *
 * @author Martin Misiarz `<dev.misiarz@gmail.com>`
 * @version 1.0.0
 * @since 1.0.0
 */
class GeoCachingCom(private val network: Network, private val parser: Parser) : Service {

    companion object {
        private val LOG: Logger = logger<GeoCachingCom>()
    }

    /**
     * After the user has been logged in successfully, it switches language of Geocaching page to English.
     * @param credentials Credentials to log in with.
     * @throws [LoginException] If login failed because of network problem.
     * @see Service.login
     */
    override fun login(credentials: Credentials) {
        LOG.debug("Starting log-in process..")

        try {
            val loginPageBody = network.getResponseStringBody(network.getRequest(Constant.URI_LOGIN))

            if(isLoggedIn(loginPageBody)) {
                LOG.debug("User is already logged in.")

                if(!isLanguageEnglish(loginPageBody)) {
                    switchToEnglish()
                }

                return
            }

            val requestVerificationToken = extractRequestVerificationToken(loginPageBody) ?: throw NetworkException("Can not extract '${Parameter.REQUEST_VERIFICATION_TOKEN.parameterName}' token.")

            val parameters = HttpParameters(
                    Parameter.USERNAME.parameterName, credentials.username,
                    Parameter.PASSWORD.parameterName, credentials.password,
                    Parameter.REQUEST_VERIFICATION_TOKEN.parameterName, requestVerificationToken
            )

            val loginResponseBody = network.getResponseStringBody(network.postRequest(Constant.URI_LOGIN, parameters, null))

            if(!isLoggedIn(loginResponseBody)) throw NetworkException("An error occurred during login process.")

            LOG.debug("User has been logged in successfully.")

            if(!isLanguageEnglish(loginPageBody)) switchToEnglish()
        } catch(e: NetworkException) {
            LOG.error("Can not finish log in process.", e)
            throw LoginException("Can not finish log in process.", e)
        }
    }

    override fun logout() {
        LOG.debug("Starting log-out process..")

        try {
            network.getResponseStringBody(network.postRequest(Constant.URI_LOGOUT))
            network.clearCookies()

            LOG.debug("User has been logged out successfully.")
        } catch(e: NetworkException) {
            LOG.error("Can not log out.", e)
            throw LogoutException("Can not log out.", e)
        }
    }

    override fun isLoggedIn(pageBody: String) = Constant.REGEX_IS_LOGGED_IN.containsMatchIn(pageBody)

    override suspend fun lookForCaches(coordinates: Coordinates, cacheFilter: CacheFilter, limit: Int, geocacheLoadedListener: GeocacheLoadedListener?, parent: Job?): List<Geocache> {
        LOG.debug("Starting looking for caches..")

        val geoCaches = mutableListOf<Geocache>()

        // Prepare basic set of parameters.
        val httpParameters = HttpParameters(
                Parameter.LATITUDE.parameterName, coordinates.latitude.toString(),
                Parameter.LONGITUDE.parameterName, coordinates.longitude.toString(),
                Parameter.CACHE_FILTER.parameterName, if(cacheFilter.allowedCacheTypes.isEmpty() || cacheFilter.allowedCacheTypes.size>1) CacheType.ALL.code else cacheFilter.allowedCacheTypes[0].code
        )

        // Exclude own or found caches if required.
        if(!cacheFilter.includeOwnAndFound) {
            httpParameters.put(Parameter.EXCLUDE_OWN.parameterName, "1")
        }

        try {
            // Get page with caches.
            val response = network.getRequest(Constant.URI_SEARCH, httpParameters, null)

            var responseBody = network.getResponseStringBody(response)

            // Check whether user is logged in.
            if (!isLoggedIn(responseBody)) throw ServiceException("User is not logged in.")

            // Save request URL for future search by "next page".
            val requestUrl = Constant.URI_SEARCH + "?" + response.request().url().encodedQuery()

            var deferredResult: DeferredResult
            val counter = AtomicInteger(0)

            loading@ while (geoCaches.size < limit) {
                // Fill given list with loaded caches.
                deferredResult = getGeocacheLoaders(responseBody, limit, counter, cacheFilter, requestUrl, parent)

                // Start all deferred jobs.
                deferredResult.defferedJobs.forEach { it.start() }

                LOG.debug("Started {} coroutine jobs.", deferredResult.defferedJobs.size)

                runBlocking {
                    deferredResult.defferedJobs.forEach { job ->
                        try {
                            val geoCache = job.await()

                            if (geoCache.canLoadInfo) {
                                geoCaches.add(geoCache)

                                // Notify the listener.
                                geocacheLoadedListener?.let {
                                    GlobalScope.launch {
                                        it.geocacheLoaded(geoCache)
                                    }
                                }

                                LOG.debug("#{} cache successfully loaded: '{}'", geoCaches.size, geoCache.url)
                            } else {
                                LOG.debug("Could not load details for '{}'.", geoCache.url)
                            }
                        } catch (e: CancellationException) {
                            LOG.warn("Coroutine job has been cancelled.", e)
                        } catch (e: NetworkException) {
                            LOG.warn("Can not load geocache.", e)
                        }
                    }
                }

                // No jobs created, get out of here.
                if (deferredResult.defferedJobs.size == 0) {
                    break@loading
                }

                if (geoCaches.size < limit) {
                    // Set counter to actual count.
                    counter.set(geoCaches.size)

                    // Load page with next geocaches.
                    try {
                        responseBody = network.getResponseStringBody(network.postRequest(requestUrl, deferredResult.nextPageHeaders, null))
                    } catch (e: NetworkException) {
                        LOG.warn("Could not load page with next geocaches.", e)
                        break@loading
                    }
                }
            }

            LOG.debug("{} caches has been found.", geoCaches.size)

            return geoCaches
        } catch (networkException: NetworkException) {
            LOG.error("Can not download caches.", networkException)
            throw ServiceException("Can not download caches.", networkException)
        }
    }

    override fun createGpx(geocaches: List<Geocache>, formatOutput: Boolean): String {
        val nsGroundspeak = "groundspeak" to "http://www.groundspeak.com/cache/1/0/1"
        val nsGsak = "gsak" to "http://www.gsak.net/xmlv1/6"

        val gpx = xml("gpx") {
            attributes("version" to "1.1", "creator" to "gcUnicorn")
            xmlns = "http://www.topografix.com/GPX/1/1"
            namespace(nsGroundspeak.first, nsGroundspeak.second)
            namespace(nsGsak.first, nsGsak.second)

            element("metadata") {
                element("author") { -"gcUnicorn"}
                element("time") { -Date().format(Constant.PATTERN_DATE_GPX, Locale.US) }
            }

            geocaches.forEach { geocache->
                if(geocache.coordinates==null) return@forEach

                element("wpt") {
                    attributes(
                            "lat" to geocache.coordinates!!.latitude.toString(),
                            "lon" to geocache.coordinates!!.longitude.toString())
                    geocache.hiddenDate?.let {
                        element("time") { -Date(it).format(Constant.PATTERN_DATE_GPX, Locale.US) }
                    }
                    geocache.code?.let {
                        element("name") { -it }
                    }
                    geocache.name?.let {
                        element("desc") { -it }
                    }
                    // Link is created from geocache's code.
                    geocache.code?.let {
                        element("link") {
                            element("href") { -Constant.URI_CACHE_SHORT.plus(it) }
                            geocache.name?.let {
                                element("text") { -it }
                            }
                        }
                    }
                    geocache.found?.let {
                        element("sym") { if(it) -"Geocache Found" else -"Geocache" }
                    }
                    geocache.type?.let {
                        element("type") { -"Geocache|${it.id}" }
                    }
                    element("${nsGroundspeak.first}:cache") {
                        geocache.id?.let { attribute("id", it) }
                        geocache.isDisabled?.let { attribute("available", !it) }
                        geocache.isArchived?.let { attribute("archived", it) }

                        geocache.name?.let {
                            element("${nsGroundspeak.first}:name") { -it }
                        }
                        geocache.ownerName?.let {
                            element("${nsGroundspeak.first}:placed_by") { -it }
                        }
                        geocache.ownerId?.let {
                            element("${nsGroundspeak.first}:owner") { -it }
                        }
                        geocache.type?.let {
                            element("${nsGroundspeak.first}:type") { -it.id }
                        }
                        geocache.sizeType?.let {
                            element("${nsGroundspeak.first}:container") { -it.id }
                        }
                        geocache.attributes?.let {
                            element("${nsGroundspeak.first}:attributes") {
                                it.forEach { attribute ->
                                    element("${nsGroundspeak.first}:attribute") {
                                        attributes("id" to attribute.type.id, "inc" to if(attribute.enabled) "1" else "0")
                                    }
                                }
                            }
                        }
                        geocache.difficulty?.let {
                            element("${nsGroundspeak.first}:difficulty") { -it.toString() }
                        }
                        geocache.terrain?.let {
                            element("${nsGroundspeak.first}:terrain") { -it.toString() }
                        }
                        geocache.getCountry()?.let {
                            element("${nsGroundspeak.first}:country") { -it }
                        }
                        geocache.getState()?.let {
                            element("${nsGroundspeak.first}:state") { -it }
                        }
                        geocache.shortDescription?.let {
                            element("${nsGroundspeak.first}:short_description") {
                                attribute("html", if(it.containsHtml()) "true" else "false")
                                -it
                            }
                        }
                        geocache.longDescription?.let {
                            element("${nsGroundspeak.first}:long_description") {
                                attribute("html", if(it.containsHtml()) "true" else "false")
                                -it
                            }
                        }
                        geocache.hint?.let {
                            element("${nsGroundspeak.first}:encoded_hints") { -it.rot13() }
                        }
                        geocache.logEntries?.let {
                            element("${nsGroundspeak.first}:logs") {
                                it.forEach { logEntry ->
                                    element("${nsGroundspeak.first}:log") {
                                        logEntry.id?.let {
                                            attribute("id", it.toString())
                                        }
                                        logEntry.visited?.let {
                                            element("${nsGroundspeak.first}:date") {
                                                -Date(it).format(Constant.PATTERN_DATE_GPX, Locale.US)
                                            }
                                        }
                                        logEntry.type?.let {
                                            element("${nsGroundspeak.first}:type") { -it.type }
                                        }
                                        logEntry.author?.let {
                                            element("${nsGroundspeak.first}:finder") {
                                                logEntry.authorId?.let {
                                                    attribute("id", it.toString())
                                                }
                                                -it
                                            }
                                        }
                                        logEntry.text?.let {
                                            element("${nsGroundspeak.first}:text") {
                                                attribute("encoded", "False")
                                                -it
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        geocache.inventory?.let {
                            element("${nsGroundspeak.first}:travelbugs") {
                                it.forEach { trackable ->
                                    element("${nsGroundspeak.first}:travelbug") {
                                        trackable.id?.let {
                                            attribute("id", it)
                                        }
                                        trackable.code?.let {
                                            attribute("ref", it)
                                        }
                                        trackable.name?.let {
                                            element("${nsGroundspeak.first}:name") { -it }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    element("${nsGsak.first}:wptExtension") {
                        geocache.onWatchList?.let {
                            element("${nsGsak.first}:Watch") { -it.toString() }
                        }
                        geocache.found?.let {
                            element("${nsGsak.first}:Found") { -it.toString() }
                        }
                        geocache.isPremiumOnly?.let {
                            element("${nsGsak.first}:IsPremium") { -it.toString() }
                        }
                        geocache.favoriteCount?.let {
                            element("${nsGsak.first}:FavPoints") { -it.toString() }
                        }
                        geocache.personalNote?.let {
                            element("${nsGsak.first}:GcNote") { -it }
                        }
                        geocache.guid?.let {
                            element("${nsGsak.first}:Guid") { -it }
                        }
                        geocache.spoilers?.let {
                            element("${nsGsak.first}:CacheImages") {
                                it.forEach { image ->
                                    element("${nsGsak.first}:CacheImage") {
                                        image.title?.let {
                                            element("${nsGsak.first}:iname") { -it }
                                        }
                                        image.description?.let {
                                            element("${nsGsak.first}:idescription") { -it }
                                        }
                                        image.guid?.let {
                                            element("${nsGsak.first}:iguid") { -it }
                                        }
                                        element("${nsGsak.first}:iimage") { -image.uri }
                                    }
                                }
                            }
                        }
                        geocache.logEntries?.let {
                            if(it.filter { logEntry -> logEntry.images!=null }.count()>0) {
                                element("${nsGsak.first}:LogImages") {
                                    it.forEach logEntry@ { logEntry ->
                                        if (logEntry.id == null) return@logEntry
                                        logEntry.images?.let {
                                            it.forEach { logImage ->
                                                element("${nsGsak.first}:LogImage") {
                                                    element("${nsGsak.first}:ilogid") {
                                                        -logEntry.id.toString()
                                                    }
                                                    logImage.title?.let {
                                                        element("${nsGsak.first}:iname") { -it }
                                                    }
                                                    logImage.description?.let {
                                                        element("${nsGsak.first}:idescription") { -it }
                                                    }
                                                    logImage.guid?.let {
                                                        element("${nsGsak.first}:iguid") { -it }
                                                    }
                                                    element("${nsGsak.first}:iimage") {
                                                        -logImage.uri
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                geocache.waypoints?.let {
                    it.forEach waypoint@ { waypoint ->
                        if(geocache.code==null) return@waypoint

                        element("wpt") {
                            waypoint.coordinates?.let {
                                attributes("lat" to it.latitude, "lon" to it.longitude)
                            }
                            waypoint.prefix?.let {
                                element("name") { -(it + geocache.code?.substring(2)) }
                            }
                            waypoint.note?.let {
                                element("cmt") { -it }
                            }
                            waypoint.name?.let {
                                element("desc") { -it }
                            }
                            waypoint.type?.let {
                                element("sym") { -it.type }
                                element("type") { -"Waypoint|${it.type}" }
                            }
                            element("${nsGsak.first}:wptExtension") {
                                element("${nsGsak.first}:Parent") {
                                    -geocache.code!!
                                }
                            }
                        }
                    }
                }
            }
        }

        return String(gpx.toString(formatOutput).toByteArray())
    }

    /**
     * Fills given list with caches.
     *
     * If given page doesn't contain any caches, the method returns.
     *
     * Once the list reaches given limit, the method returns.
     * @param pageBody Page body to parse.
     * @param limit Geocache list limit.
     * @param counter Actual counter of already created loaders.
     * @param cacheFilter Cache filter.
     * @param requestUrl Request URL. It is used as a POST url for loading next caches.
     * @param parent Parent job.
     * @return Deferred result containing deferred jobs for loading caches and next page headers.
     * @throws [NetworkException] If anything goes wrong while loading cache details.
     */
    @Throws(NetworkException::class)
    private suspend fun getGeocacheLoaders(pageBody: String, limit: Int, counter: AtomicInteger, cacheFilter: CacheFilter, requestUrl: String, parent: Job?): DeferredResult {
        val deferredResult = DeferredResult(nextPageHeaders = getParamsForSearchByNextPage(pageBody))

        // Get inner table containing caches' details.
        val resultTable = Constant.REGEX_SEARCH_RESULTS_TABLE.find(pageBody)?.value?.trim() ?: return deferredResult
        var foundAnyResult = false

        // POSTPONED: Could it be rewritten so the very first match returns correct data?
        // Get all rows containing caches' details.
        resultTable@ for(cacheMatch in Constant.REGEX_SEARCH_RESULTS_CACHE.findAll(resultTable)) {
            if(counter.get()<limit) {
                // If there is no URL, we can consider there will be no other fields as well, so skip this match.
                val cacheUrl = Constant.REGEX_SEARCH_RESULTS_CACHE_URL.find(cacheMatch.value)?.groupValues?.get(1)
                        ?: continue

                if (!foundAnyResult) foundAnyResult = true

                // Once the cache has greater distance than allowed, stop looking for next caches.
                val cacheDistance = Constant.REGEX_SEARCH_RESULTS_CACHE_DISTANCE_KM.find(cacheMatch.value)?.groupValues?.get(1)?.toDouble()
                        ?: Constant.REGEX_SEARCH_RESULTS_CACHE_DISTANCE_MI.find(cacheMatch.value)?.groupValues?.get(1)?.toDouble()?.times(1.609344)
                        ?: CacheFilter.DISABLED_DISTANCE
                if (cacheFilter.maxDistance != CacheFilter.DISABLED_DISTANCE && cacheFilter.maxDistance < cacheDistance) return deferredResult

                // Exclude types not in the filter.
                val cacheType = CacheType.findByPattern(Constant.REGEX_SEARCH_RESULTS_CACHE_TYPE.find(cacheMatch.value)?.groupValues?.get(1)
                        ?: "")
                if (!cacheFilter.allowedCacheTypes.contains(CacheType.ALL) && !cacheFilter.allowedCacheTypes.contains(cacheType)) continue

                val isCacheDisabled = Constant.REGEX_SEARCH_RESULTS_CACHE_IS_DISABLED.containsMatchIn(cacheMatch.value)

                // Exclude disabled.
                if (!cacheFilter.allowDisabled && isCacheDisabled) continue

                val isPremium = Constant.REGEX_SEARCH_RESULTS_CACHE_PREMIUM.containsMatchIn(cacheMatch.value)
                if (cacheFilter.skipPremium && isPremium) continue

                val geocacheLite = GeocacheLite()
                geocacheLite.url = cacheUrl
                geocacheLite.distance = cacheDistance
                geocacheLite.type = cacheType
                geocacheLite.isDisabled = isCacheDisabled
                geocacheLite.isPremiumOnly = isPremium

                // Load geocache's details from geocache's url.
                deferredResult.defferedJobs.add(
                    GlobalScope.async(context = Dispatchers.Default + (parent ?: EmptyCoroutineContext), start = CoroutineStart.LAZY) {
                        loadGeocacheDetails(geocacheLite)
                    }
                )

                counter.incrementAndGet()
            } else break@resultTable
        }

        // Check whether we need to load more caches.
        if(foundAnyResult && counter.get()<limit) {
            val nextPage = network.getResponseStringBody(network.postRequest(requestUrl, deferredResult.nextPageHeaders, null))

            // Load next deferred jobs.
            val nextDeferredResult = getGeocacheLoaders(nextPage, limit, counter, cacheFilter, requestUrl, parent)
            deferredResult.defferedJobs.addAll(nextDeferredResult.defferedJobs)
            deferredResult.nextPageHeaders = nextDeferredResult.nextPageHeaders
        }

        return deferredResult
    }

    /**
     * Loads details for given geocache. It loads geocache's url and parses details.
     *
     * Skipped content:
     * * Background image
     *
     * @param geocacheLite Geocache to load details for.
     * @return Loaded geocache.
     * @throws [NetworkException] If a page with geocache details can't be loaded.
     */
    @Throws(NetworkException::class)
    private fun loadGeocacheDetails(geocacheLite: GeocacheLite): Geocache {
        val geocache = Geocache(geocacheLite)

        LOG.debug("Start loading details for '{}'..", geocache.url)

        val pageBody = network.getResponseStringBody(network.getRequest(geocache.url))

        // WAITING: I have no premium membership active, so I am not able to see Premium cache's source page. If anybody provides it to me, I will be able to change current implementation.
        // Check whether cache is premium only and logged in user is not.
        if(Constant.REGEX_CACHE_PREMIUM_ONLY.containsMatchIn(pageBody)) {
            LOG.debug("Ups, given cache is Premium only, but logged in user is a Basic member. The cache will be skipped.")

            // Don't care about the cache if logged in user is not a Premium member and we can not load additional info.
            geocache.canLoadInfo = false
            return geocache
        } else {
            geocache.canLoadInfo = true
        }

        // Check whether cache is archived.
        geocache.isArchived = Constant.REGEX_CACHE_IS_ARCHIVED.containsMatchIn(pageBody) || Constant.REGEX_CACHE_IS_LOCKED.containsMatchIn(pageBody)

        // Check whether cache is favorite.
        geocache.isFavorite = Constant.REGEX_CACHE_IS_FAVORITE.containsMatchIn(pageBody)

        // Load cache's name.
        Constant.REGEX_CACHE_NAME.find(pageBody)?.groupValues?.get(1)?.trim().nullableExecute({
            geocache.name = this
        }, {
            LOG.warn("Can not parse cache's name from cache's page '{}'.", geocache.url)
        })

        // Load cache's code.
        Constant.REGEX_CACHE_CODE.find(pageBody)?.groupValues?.get(1).nullableExecute({
            geocache.code = this
        }, {
            LOG.warn("Can not parse cache's code from cache's page '{}'.", geocache.url)
        })

        // Load cache's id.
        Constant.REGEX_CACHE_ID.find(pageBody)?.groupValues?.get(1).nullableExecute({
            try {
                geocache.id = toLong()
            } catch(e: NumberFormatException) {
                LOG.warn("Can not parse cache'id '{}' to number.", this, e)
            }
        }, {
            LOG.warn("Can not parse cache's id from cache's page '{}'.", geocache.url)
        })

        // Load cache's GUID.
        Constant.REGEX_CACHE_GUID.find(pageBody)?.groupValues?.get(1)?.trim().nullableExecute({
            geocache.guid = this
        }, {
            LOG.warn("Can not parse cache's GUID from cache's page '{}'.", geocache.url)
        })

        // Load cache's watchlist count.
        Constant.REGEX_CACHE_WATCHLIST_COUNT.find(pageBody)?.groupValues?.get(1)?.trim().nullableExecute({
            try {
                geocache.watchlistCount = toInt()
            } catch (e: NumberFormatException) {
                LOG.warn("Can not convert cache's watchlist count into the number.", e)
            }
        }, {
            LOG.warn("Can not parse cache's watchlist count from cache's page '{}'.", geocache.url)
        })

        // Load cache's terrain.
        Constant.REGEX_CACHE_TERRAIN.find(pageBody)?.groupValues?.get(1).nullableExecute({
            try {
                geocache.terrain = toDouble()
            } catch(e: NumberFormatException) {
                LOG.warn("Can not convert cache's terrain into the number.", e)
            }
        }, {
            LOG.warn("Can not parse cache's terrain from cache's page '{}'.", geocache.url)
        })

        // Load cache's difficulty.
        Constant.REGEX_CACHE_DIFFICULTY.find(pageBody)?.groupValues?.get(1).nullableExecute({
            try {
                geocache.difficulty = toDouble()
            } catch(e: NumberFormatException) {
                LOG.warn("Can not convert cache's difficulty into the number.", e)
            }
        }, {
            LOG.warn("Can not parse cache's difficulty from cache's page '{}'.", geocache.url)
        })

        // Load cache's size.
        Constant.REGEX_CACHE_SIZE.find(pageBody)?.groupValues?.get(1).nullableExecute({
            geocache.sizeType = CacheSizeType.findByPattern(this)
        }, {
            LOG.warn("Can not parse cache's size from cache's page '{}'.", geocache.url)
        })

        // Load cache's favorite count.
        Constant.REGEX_CACHE_FAVORITE_COUNT.find(pageBody)?.groupValues?.get(1)?.trim().nullableExecute({
            try {
                geocache.favoriteCount = toInt()
            } catch(e: NumberFormatException) {
                LOG.warn("Can not convert cache's favorite count into the number.", e)
            }
        }, {
            LOG.warn("Can not parse cache's favorite count from cache's page '{}'.", geocache.url)
        })

        // Load cache's owner name.
        Constant.REGEX_CACHE_OWNER_NAME.find(pageBody)?.groupValues?.get(1).nullableExecute({
            geocache.ownerName = this
        },{
            LOG.warn("Can not parse cache's owner name from cache's page '{}'.", geocache.url)
        })

        // Load cache's owner ID.
        Constant.REGEX_CACHE_OWNER_ID.find(pageBody)?.groupValues?.get(1).nullableExecute({
            geocache.ownerId = network.decode(this)
        }, {
            LOG.warn("Can not parse cache's owner ID from cache's page '{}'.", geocache.url)
        })

        // Load cache's hidden date.
        Constant.REGEX_CACHE_HIDDEN.find(pageBody)?.groupValues?.get(1)?.trim().nullableExecute({
            try {
                geocache.hiddenDate = SimpleDateFormat(Constant.PATTERN_DATE_PAGE, Locale.ENGLISH).parse(this).time
            } catch(e: ParseException) {
                LOG.warn("Can not parse cache's hidden date.", e)
            }
        }, {
            LOG.warn("Can not parse cache's hidden date from cache's page '{}'.", geocache.url)
        })

        // Load cache's coordinates.
        Constant.REGEX_CACHE_COORDINATES.find(pageBody)?.groupValues?.get(1).nullableExecute({
            try {
                geocache.coordinates = parser.parse(this)
            } catch(e: CoordinateParseException) {
                LOG.warn("Can not parse cache's coordinates.", e)
            }
        }, {
            LOG.warn("Can not parse cache's latitude and longitude from cache's page '{}'.", geocache.url)
        })

        // Load cache's location.
        Constant.REGEX_CACHE_LOCATION.find(pageBody)?.groupValues?.get(1).nullableExecute({
            geocache.location = this
        }, {
            LOG.warn("Can not parse cache's location from cache's page '{}'.", geocache.url)
        })

        // Load cache's hint.
        Constant.REGEX_CACHE_HINT.find(pageBody)?.groupValues?.get(1)?.trim().nullableExecute({
            geocache.hint = this
        }, {
            LOG.warn("Can not parse cache's hint from cache's page '{}'.", geocache.url)
        })

        // Load cache's personal note. This is OPTIONAL so no LOG message if there is no match.
        Constant.REGEX_CACHE_PERSONAL_NOTE.find(pageBody)?.groupValues?.get(1)?.trim()?.let {
            geocache.personalNote = it
        }

        // Load cache's short description.
        Constant.REGEX_CACHE_DESCRIPTION_SHORT.find(pageBody)?.groupValues?.get(1)?.trim().nullableExecute({
            geocache.shortDescription = this
        }, {
            LOG.warn("Can not parse cache's short description from cache's page '{}'.", geocache.url)
        })

        // Load cache's longDescription.
        val cacheDescription = Constant.REGEX_CACHE_DESCRIPTION.find(pageBody)?.groupValues?.get(1)?.trim()
        if(cacheDescription==null) {
            LOG.warn("Can not parse cache's longDescription from cache's page '{}'.", geocache.url)
        }

        // Load cache's related page. This is OPTIONAL so no LOG message if there is no match.
        val cacheDescriptionRelatedPage = Constant.REGEX_CACHE_DESCRIPTION_RELATED_PAGE.find(pageBody)?.groupValues?.get(1)?.trim()

        if(cacheDescriptionRelatedPage!=null && cacheDescriptionRelatedPage.isNotEmpty()) {
            geocache.longDescription = cacheDescription ?: "" + String.format("<br/><br/><a href=\"%s\"><b>%s</b></a>", cacheDescriptionRelatedPage, cacheDescriptionRelatedPage)
        } else if(cacheDescription!=null) {
            geocache.longDescription = cacheDescription
        }

        // Load cache's attributes.
        Constant.REGEX_CACHE_ALL_ATTRIBUTES.find(pageBody)?.groupValues?.get(1).nullableExecute({
            val cacheAttributes = mutableListOf<Attribute>()

            Constant.REGEX_CACHE_ATTRIBUTE.findAll(this).forEach { cacheAttributeMatch ->
                val attributeTitle = if(cacheAttributeMatch.groupValues.size==3) cacheAttributeMatch.groupValues[2] else null
                // Skip blank attributes.
                if(attributeTitle!= null && attributeTitle!="blank") {
                    val attributeImage = cacheAttributeMatch.groupValues[1]
                    val startIndex = attributeImage.lastIndexOf("/")
                    val endIndex = attributeImage.lastIndexOf(".")

                    if(startIndex>-1 && endIndex>startIndex) {
                        val attributeString = attributeImage.substring(startIndex+1, endIndex).toLowerCase().replace("-", "_")
                        AttributeType.findByPattern(attributeString.substringBeforeLast("_"))?.let {
                            cacheAttributes.add(Attribute(it, attributeString.substringAfterLast("_")=="yes"))
                        }
                    } else {
                        LOG.warn("Can not parse cache's attribute from '{}'.", attributeImage)
                    }
                }
            }

            if(cacheAttributes.isNotEmpty()) {
                geocache.attributes = cacheAttributes
            }
        }, {
            LOG.warn("Can not parse cache's attributes from cache's page '{}'.", geocache.url)
        })

        // Check whether cache has been found by a user.
        geocache.found = Constant.REGEX_CACHE_FOUND.containsMatchIn(pageBody)

        // Check whether cache is on user's watchlist.
        geocache.onWatchList = Constant.REGEX_CACHE_ON_WATCHLIST.containsMatchIn(pageBody)

        // Load cache's spoilers.
        val cacheSpoilerImages = mutableListOf<Image>()
        Constant.REGEX_CACHE_SPOILER_IMAGES.findAll(pageBody).forEach { matchResult ->
            val spoilerUri = matchResult.groupValues[1]
            if(spoilerUri.isNotEmpty()) {
                val spoilerGuid = Constant.REGEX_CACHE_SPOILED_IMAGE_GUID.find(spoilerUri)?.groupValues?.get(1)
                val spoilerTitle = matchResult.groupValues[2]
                var spoilerDescription: String? = matchResult.groupValues[3]
                if(spoilerDescription!=null && spoilerDescription.isEmpty()) spoilerDescription = null

                cacheSpoilerImages.add(Image(spoilerUri, spoilerGuid, spoilerTitle, spoilerDescription))
            }
        }

        if(cacheSpoilerImages.isNotEmpty()) {
            geocache.spoilers = cacheSpoilerImages
        }

        // Load cache's inventory. This is OPTIONAL so no LOG message if there is no match.
        Constant.REGEX_CACHE_INVENTORY.find(pageBody)?.groupValues?.get(1)?.let {
            val cacheInventory = mutableListOf<Trackable>()

            Constant.REGEX_CACHE_INVENTORY_ITEMS.findAll(it).forEach { matchResult ->
                val trackableGuid = matchResult.groupValues[1]
                val trackableName = matchResult.groupValues[2]

                val trackable = Trackable(trackableGuid, trackableName, TrackableBrand.TRAVELBUG)
                loadTrackableDetails(trackable)

                cacheInventory.add(trackable)
            }

            if(cacheInventory.isNotEmpty()) {
                geocache.inventory = cacheInventory
            }
        }

        // Load cache's logs' counts.
        Constant.REGEX_CACHE_LOGCOUNTS.find(pageBody)?.groupValues?.get(1).nullableExecute({
            val cacheLogsCounts = mutableMapOf<LogType, Int>()

            Constant.REGEX_CACHE_LOGCOUNTS_ITEM.findAll(this).forEach { matchResult ->
                val logIconId = matchResult.groupValues[1]
                val logCount = matchResult.groupValues[2].replace(",", "")
                val logType = LogType.findByIconId(logIconId)

                if(logIconId.isNotEmpty() && logCount.isNotEmpty() && logType!=LogType.UNKNOWN) {
                    try {
                        cacheLogsCounts[logType] = logCount.toInt()
                    } catch(e: NumberFormatException) {
                        LOG.warn("Can not parse {}'s log count defined as '{}'.", logType, logCount, e)
                    }
                }
            }

            if(cacheLogsCounts.isNotEmpty()) {
                geocache.logCounts = cacheLogsCounts
            }
        }, {
            LOG.warn("Can not parse cache's logs' counts from cache's page '{}'.", geocache.url)
        })

        // Load cache way points. This is OPTIONAL so no LOG message if there is no match.
        Constant.REGEX_CACHE_WAYPOINTS.find(pageBody)?.groupValues?.get(1)?.let {
            val cacheWayPoints = mutableListOf<Waypoint>()

            Constant.REGEX_CACHE_WAYPOINTS_ITEM.findAll(it).forEach { matchResult ->
                val waypointItemWholeMatch = matchResult.groupValues[0]
                val waypointItemFirstLine = matchResult.groupValues[1]

                // Load way point's note. This is OPTIONAL.
                val waypointNote = Constant.REGEX_CACHE_WAYPOINTS_ITEM_NOTE.find(waypointItemWholeMatch)?.groupValues?.get(1)?.trim()

                // Split the first table line into columns.
                val columns = Constant.REGEX_CACHE_WAYPOINTS_ITEM_COLUMN.split(waypointItemFirstLine)

                val waypointName = Constant.REGEX_CACHE_WAYPOINTS_ITEM_NAME.find(columns[5])?.groupValues?.get(1)?.trim()
                if(waypointName==null) {
                    LOG.warn("Can not parse way point's name from cache's page '{}'.", geocache.url)
                }

                var waypointType: WaypointType? = null
                val waypointTypeBody = Constant.REGEX_CACHE_WAYPOINTS_ITEM_TYPE.find(columns[2])?.groupValues?.get(1)
                if(waypointTypeBody!=null) {
                    waypointType = WaypointType.findById(waypointTypeBody)
                } else {
                    LOG.warn("Can not parse way point's type of '{}' from cache's page '{}'.", waypointName, geocache.url)
                }

                val waypointPrefix = Constant.REGEX_CACHE_WAYPOINTS_ITEM_PREFIX.find(columns[3])?.groupValues?.get(1)?.trim()
                if(waypointPrefix==null) {
                    LOG.warn("Can not parse way point's prefix of '{}' from cache's page '{}'.", waypointName, geocache.url)
                }

                val waypointLookup = Constant.REGEX_CACHE_WAYPOINTS_ITEM_LOOKUP.find(columns[4])?.groupValues?.get(1)?.trim()
                if(waypointLookup==null) {
                    LOG.warn("Can not parse way point's lookup of '{}' from cache's page '{}'.", waypointName, geocache.url)
                }

                var waypointCoordinates: Coordinates? = null
                val waypointCoordinatesBody = Constant.REGEX_CACHE_WAYPOINTS_ITEM_COORDINATIONS.find(columns[6])?.groupValues?.get(1)?.trim()
                if(waypointCoordinatesBody!=null && waypointCoordinatesBody.isNotEmpty() && waypointCoordinatesBody!="???") {
                    try {
                        waypointCoordinates = parser.parse(waypointCoordinatesBody)
                    } catch(e: CoordinateParseException) {
                        LOG.warn("Can not parse way point's coordinates of '{}' from cache's page '{}'.", waypointName, geocache.url, e)
                    }
                }

                cacheWayPoints.add(Waypoint(waypointName, waypointType, waypointPrefix, waypointLookup, waypointNote, waypointCoordinates))
            }

            if(cacheWayPoints.isNotEmpty()) {
                geocache.waypoints = cacheWayPoints
            }
        }

        // Load cache's log entries.
        Constant.REGEX_USER_TOKEN.find(pageBody)?.groupValues?.get(1).nullableExecute({
            var cacheLogEntries: MutableList<LogEntry>? = null

            try {
                // Load all but own and friends' log entries.
                val logEntries = loadLogEntries(this, geocache.url)
                if(logEntries!=null) {
                    cacheLogEntries = mutableListOf()
                    cacheLogEntries.addAll(logEntries)
                }
            } catch(e: NetworkException) {
                LOG.warn("Can not load log entries for cache '{}'.", geocache.url, e)
            }

            if(cacheLogEntries!=null && cacheLogEntries.isNotEmpty()) {
                geocache.logEntries = cacheLogEntries
            }
        }, {
            LOG.warn("Can not parse userToken from cache's page '{}'.", geocache.url)
        })

        return geocache
    }

    /**
     * Loads details of given trackable.
     * @param trackable Trackable to load details for.
     */
    private fun loadTrackableDetails(trackable: Trackable) {
        trackable.guid?.let { guid ->
            LOG.debug("Start loading details for trackable: {}.", guid)

            val parameters = HttpParameters(
                    Parameter.TRACKABLE_GUID.parameterName, guid
            )

            try {
                val trackablePage = network.getResponseStringBody(network.getRequest(Constant.URI_TRACKABLE, parameters, null))

                Constant.REGEX_TRACKABLE_CODE.find(trackablePage)?.groupValues?.get(1)?.let {
                    trackable.code = it
                }

                Constant.REGEX_TRACKABLE_ID.find(trackablePage)?.groupValues?.get(1)?.let {
                    try {
                        trackable.id = it.toLong()
                    } catch(e: NumberFormatException) {
                        LOG.warn("Can not parse trackable id for: '{}'.", guid, e)
                    }
                }
            } catch(e: NetworkException) {
                LOG.warn("Can not load trackable's details for: '{}'.", guid, e)
            }
        }
    }

    /**
     * Loads log entries.
     * @param userToken User token used for loading log entries for specific geocache.
     * @param geocacheUrl Geocache's url.
     * @return List of log entries. Or empty list of no log entries have been found. Or null if obtained response can not be recognized.
     * @throws [NetworkException] If anything goes wrong.
     */
    @Throws(NetworkException::class)
    private fun loadLogEntries(userToken: String, geocacheUrl: String): List<LogEntry>? {
        LOG.debug("Start loading log entries for '{}'.", geocacheUrl)

        val parameters = HttpParameters(
                Parameter.LOG_USER_TOKEN.parameterName, userToken,
                Parameter.LOG_IDX.parameterName, "1",
                Parameter.LOG_COUNT.parameterName, Constant.DEFAULT_LOGS_COUNT.toString(),
                Parameter.LOG_DECRYPT.parameterName, "false"
        )

        val cacheLogs = network.getResponseStringBody(network.getRequest(Constant.URI_CACHE_LOGBOOK, parameters, null))

        try {
            val logEntries = JsonTreeParser.parse(cacheLogs)

            if(logEntries.getOrNull(Constant.REQUEST_STATUS)?.content=="success") {
                val cacheLogEntries = mutableListOf<LogEntry>()

                logEntries.getArrayOrNull(Constant.REQUEST_DATA)
                        ?.forEach { jsonLogEntryElement ->
                            val logEntry = LogEntry()
                            val jsonLogEntry = jsonLogEntryElement.jsonObject

                            jsonLogEntry.getOrNull(Constant.LOG_ID)?.longOrNull.nullableExecute({
                                logEntry.id = this
                            }, {
                                LOG.warn("Can not obtain log entry's id.")
                            })

                            jsonLogEntry.getOrNull(Constant.LOG_TYPE)?.contentOrNull.nullableExecute({
                                logEntry.type = LogType.findByType(this)
                            }, {
                                LOG.warn("Can not obtain log entry's id.")
                            })

                            jsonLogEntry.getOrNull(Constant.LOG_TEXT)?.contentOrNull?.trim()?.replace("<p>", "")?.replace("</p>", "").nullableExecute({
                                logEntry.text = this
                            }, {
                                LOG.warn("Can not obtain log entry's text.")
                            })

                            jsonLogEntry.getOrNull(Constant.LOG_VISITED)?.contentOrNull.nullableExecute({
                                try {
                                    logEntry.visited = SimpleDateFormat(Constant.PATTERN_DATE_PAGE, Locale.ENGLISH).parse(this).time
                                } catch (e: ParseException) {
                                    LOG.warn("Can not parse log visited date.", e)
                                }
                            }, {
                                LOG.warn("Can not obtain log visited.")
                            })

                            jsonLogEntry.getOrNull(Constant.LOG_AUTHOR)?.contentOrNull.nullableExecute({
                                logEntry.author = this
                            }, {
                                LOG.warn("Can not obtain log author.")
                            })

                            jsonLogEntry.getOrNull(Constant.LOG_AUTHOR_ID)?.longOrNull.nullableExecute({
                                logEntry.authorId = this
                            }, {
                                LOG.warn("Can not obtain log author's id.")
                            })

                            val logImages = mutableListOf<Image>()
                            jsonLogEntry.getOrNull(Constant.LOG_IMAGES)?.jsonArray?.forEach logImageTree@ { logImageTreeElement ->
                                val logImageTree = logImageTreeElement.jsonObject
                                val imageFileName = logImageTree.getOrNull(Constant.LOG_IMAGE_FILENAME)?.contentOrNull ?: return@logImageTree

                                val logImage = Image(Constant.URI_IMAGE_LARGE + imageFileName)
                                logImage.guid = imageFileName.substringBefore(".")

                                logImageTree.getOrNull(Constant.LOG_IMAGE_NAME)?.contentOrNull.nullableExecute({
                                    if (isNotEmpty()) {
                                        logImage.title = this
                                    }
                                }, {
                                    LOG.warn("Can not obtain log image's name.")
                                })

                                logImageTree.getOrNull(Constant.LOG_IMAGE_DESCRIPTION)?.contentOrNull.nullableExecute({
                                    if (isNotEmpty()) logImage.description = this
                                }, {
                                    LOG.warn("Can not obtain log image's description.")
                                })

                                logImages.add(logImage)
                            }

                            if (logImages.isNotEmpty()) {
                                logEntry.images = logImages
                            }

                            cacheLogEntries.add(logEntry)
                        }

                return cacheLogEntries
            }

            return null
        } catch (e: Exception) {
            LOG.warn("Can not parse log entries for cache '{}'.", geocacheUrl)
            return null
        }
    }

    /**
     * Returns parameters necessary for loading next bunch of caches.
     * @param pageBody Page body to extract necessary parameters from.
     * @return Parameters necessary for loading next bunch of caches.
     */
    private fun getParamsForSearchByNextPage(pageBody: String): HttpParameters {
        val nextPageParameters = HttpParameters()

        nextPageParameters.put(Parameter.NEXT_PAGE_EVENT_TARGET.parameterName, Constant.NEXT_PAGE_EVENT_TARGET)
        nextPageParameters.put(Parameter.NEXT_PAGE_EVENT_ARGUMENT.parameterName, "")
        nextPageParameters.put(Parameter.NEXT_PAGE_VIEWSTATE_COUNT.parameterName, Constant.REGEX_VIEWSTATE_COUNT.find(pageBody)?.groupValues?.get(1) ?: "")

        // There could be more view states parameters.
        Constant.REGEX_VIEWSTATES.findAll(pageBody).forEach { viewState ->
            val index = viewState.groupValues[1]
            val value = viewState.groupValues[2]

            nextPageParameters.put(Parameter.NEXT_PAGE_VIEWSTATE.parameterName + index, value)
        }

        return nextPageParameters
    }

    /**
     * Switch to English version of Geocaching page.
     * @throws [NetworkException] If anything goes wrong.
     */
    @Throws(NetworkException::class)
    private fun switchToEnglish() {
        LOG.debug("Switching to English version of Geocaching page..")

        network.getResponseStringBody(network.getRequest(Constant.URI_SWITCH_TO_ENGLISH))
    }

    /**
     * Check whether language on given page is English or not.
     * @param pageBody HTML page.
     * @return True if language is English, otherwise returns false.
     */
    private fun isLanguageEnglish(pageBody: String): Boolean {
        return Constant.REGEX_LANGUAGE_SELECTED.find(pageBody)?.groupValues?.get(1) == "English"
    }

    /**
     * Extract value of '__RequestVerificationToken' from given HTML page.
     * @param pageBody HTML page.
     * @return Extracted value from given HTML page. Or null if given HTML page doesn't contain seeking value.
     */
    private fun extractRequestVerificationToken(pageBody: String): String? {
        return Constant.REGEX_REQUEST_VERIFICATION_TOKEN.find(pageBody)?.groupValues?.get(1)
    }
}

/**
 * Parameters' names.
 *
 * @param parameterName Parameter name.
 *
 * @author Martin Misiarz `<dev.misiarz@gmail.com>`
 * @version 1.0.0
 * @since 1.0.0
 */
enum class Parameter(val parameterName: String) {
    USERNAME("UsernameOrEmail"),
    PASSWORD("Password"),
    REQUEST_VERIFICATION_TOKEN("__RequestVerificationToken"),
    LATITUDE("lat"),
    LONGITUDE("lng"),
    CACHE_FILTER("cFilter"),
    /** Possible values are: [1, 0]. */
    EXCLUDE_OWN("ex"),
    NEXT_PAGE_VIEWSTATE("__VIEWSTATE"),
    NEXT_PAGE_VIEWSTATE_COUNT("__VIEWSTATEFIELDCOUNT"),
    NEXT_PAGE_EVENT_TARGET("__EVENTTARGET"),
    NEXT_PAGE_EVENT_ARGUMENT("__EVENTARGUMENT"),
    LOG_USER_TOKEN("tkn"),
    LOG_IDX("idx"),
    LOG_COUNT("num"),
    LOG_DECRYPT("decrypt"),
    TRACKABLE_GUID("guid")
}

/**
 * Set of used constants.
 *
 * @author Martin Misiarz `<dev.misiarz@gmail.com>`
 * @version 1.0.0
 * @since 1.0.0
 */
object Constant {
    const val URI_LOGIN = "https://www.geocaching.com/account/signin"
    const val URI_LOGOUT = "https://www.geocaching.com/account/logout"
    const val URI_SEARCH = "https://www.geocaching.com/seek/nearest.aspx"
    const val URI_SWITCH_TO_ENGLISH = "https://www.geocaching.com/play/culture/set?model.SelectedCultureCode=en-US"
    const val URI_CACHE_LOGBOOK = "https://www.geocaching.com/seek/geocache.logbook"
    const val URI_CACHE_SHORT = "https://coord.info/"
    const val URI_TRACKABLE = "https://www.geocaching.com/track/details.aspx"
    const val URI_IMAGE_LARGE = "https://img.geocaching.com/cache/log/"
    const val NEXT_PAGE_EVENT_TARGET = "ctl00\$ContentBody\$pgrTop\$ctl08"
    const val PATTERN_DATE_PAGE = "MM/dd/yyyy"
    const val PATTERN_DATE_GPX = "yyyy-MM-dd'T'HH:mm:ss'Z'"
    const val DEFAULT_LOGS_COUNT = 35
    const val REQUEST_DATA = "data"
    const val LOG_IMAGES = "Images"
    const val LOG_IMAGE_NAME = "Name"
    const val LOG_IMAGE_DESCRIPTION = "Descr"
    const val LOG_IMAGE_FILENAME = "FileName"
    const val REQUEST_STATUS = "status"
    const val LOG_ID = "LogID"
    const val LOG_TYPE = "LogType"
    const val LOG_VISITED = "Visited"
    const val LOG_TEXT = "LogText"
    const val LOG_AUTHOR = "UserName"
    const val LOG_AUTHOR_ID = "AccountID"

    @JvmField val REGEX_LANGUAGE_SELECTED = "<div class=\"language-dropdown[\\s\\S]*?<li class=\"selected\">.*>(.*)</a></li>".toRegex()
    @JvmField val REGEX_REQUEST_VERIFICATION_TOKEN = "<input name=\"__RequestVerificationToken\" type=\"hidden\" value=\"([^\"]*)\"[^/>]*".toRegex()
    @JvmField val REGEX_IS_LOGGED_IN = "<span class=\"user-name\">(.*)</span>".toRegex()
    @JvmField val REGEX_USER_TOKEN = "userToken\\s*=\\s*'([^']+)'".toRegex()
    @JvmField val REGEX_SEARCH_RESULTS_TABLE = "<table.+class=\".*SearchResultsTable.*\">([\\s\\S]*?)</table>".toRegex()
    @JvmField val REGEX_SEARCH_RESULTS_CACHE = "<tr.+class=\".*\">([\\s\\S]*?)</tr>".toRegex()
    @JvmField val REGEX_SEARCH_RESULTS_CACHE_URL = "<td class=\"Merge\">[\\s\\S]*?<a href=\"(.*)\" class=\"lnk\">".toRegex()
    @JvmField val REGEX_SEARCH_RESULTS_CACHE_TYPE = "<a href=\".*\".*><img.*title=\"(.*)\".*class=\"SearchResultsWptType\".*?</a>".toRegex()
    @JvmField val REGEX_SEARCH_RESULTS_CACHE_IS_DISABLED = "<a href=\".*\".*class=\"lnk.*Strike\".*>.*</a>".toRegex()
    @JvmField val REGEX_SEARCH_RESULTS_CACHE_DISTANCE_MI = "<span.*<img src=\"/images/icons/compass/.*\".*>(.*)mi</span>".toRegex()
    @JvmField val REGEX_SEARCH_RESULTS_CACHE_DISTANCE_KM = "<span.*<img src=\"/images/icons/compass/.*\".*>(.*)km</span>".toRegex()
    @JvmField val REGEX_SEARCH_RESULTS_CACHE_PREMIUM = "<img src=\".+premium_only\\.png\".+>".toRegex()
    @JvmField val REGEX_VIEWSTATE_COUNT = "id=\"__VIEWSTATEFIELDCOUNT\".*value=\"(\\d+)\"".toRegex()
    @JvmField val REGEX_VIEWSTATES = "id=\"__VIEWSTATE(\\d+)?\".*value=\"(.*)\"".toRegex()
    @JvmField val REGEX_CACHE_NAME = "<span id=\"ctl00_ContentBody_CacheName\">(.*)</span>".toRegex()
    @JvmField val REGEX_CACHE_CODE = "<span id=\"ctl00_ContentBody_CoordInfoLinkControl1_uxCoordInfoCode\".*>(.*)</span>".toRegex()
    @JvmField val REGEX_CACHE_ID = "/my/watchlist\\.aspx\\?w=([^\"]+)\"".toRegex()
    @JvmField val REGEX_CACHE_TERRAIN = "<span id=\"ctl00_ContentBody_Localize12\".*alt=\"(.*) out of 5\"".toRegex()
    @JvmField val REGEX_CACHE_DIFFICULTY = "<span id=\"ctl00_ContentBody_uxLegendScale\".*alt=\"(.*) out of 5\"".toRegex()
    @JvmField val REGEX_CACHE_FAVORITE_COUNT = "<span class=\"favorite-value\">([\\s\\S]*?)</span>".toRegex()
    @JvmField val REGEX_CACHE_OWNER_NAME = "<div id=\"ctl00_ContentBody_mcd1\">[^<]+<a href=\"[^\"]+\">([^<]+)</a>".toRegex()
    @JvmField val REGEX_CACHE_OWNER_ID = "<a href=\"/seek/nearest\\.aspx\\?u=(.*?)\">.{1,50}?seek/nearest\\.aspx\\?ul=\\1\">".toRegex()
    @JvmField val REGEX_CACHE_HIDDEN = "ctl00_ContentBody_mcd2[^:]+:\\s*([^<]+?)<".toRegex()
    @JvmField val REGEX_CACHE_PREMIUM_ONLY = "<section class=\"premium-upgrade-widget\">".toRegex()
    @JvmField val REGEX_CACHE_COORDINATES = "<span id=\"uxLatLon\"[^>]*>(.*?)</span>".toRegex()
    @JvmField val REGEX_CACHE_LOCATION = "<span id=\"ctl00_ContentBody_Location\">In (?:<a href=[^>]*>)?(.*?)<".toRegex()
    @JvmField val REGEX_CACHE_HINT = "<div id=\"div_hint\"[^>]*>([\\s\\S]*?)</div>".toRegex()
    @JvmField val REGEX_CACHE_PERSONAL_NOTE = "<span id=\"cache_note\"[^>]*>([\\s\\S]*?)</span>".toRegex()
    @JvmField val REGEX_CACHE_DESCRIPTION_SHORT = "<span id=\"ctl00_ContentBody_ShortDescription\">([\\s\\S]*?)</span>\\s*</div>".toRegex()
    @JvmField val REGEX_CACHE_DESCRIPTION = "<span id=\"ctl00_ContentBody_LongDescription\">([\\s\\S]*?)</span>\\s*</div>\\s*<(p|div) id=\"ctl00_ContentBody".toRegex()
    @JvmField val REGEX_CACHE_DESCRIPTION_RELATED_PAGE = "ctl00_ContentBody_uxCacheUrl.*? href=\"(.*?)\">".toRegex()
    @JvmField val REGEX_CACHE_ALL_ATTRIBUTES = "(<img src=\"/images/attributes.*?)(?:<p).*?".toRegex()
    @JvmField val REGEX_CACHE_ATTRIBUTE = "<img src=\"([^\"]+)\" alt=\"([^\"]+?)\"".toRegex()
    @JvmField val REGEX_CACHE_IS_LOCKED = "<div id=\"ctl00_ContentBody_archivedMessage\"".toRegex()
    @JvmField val REGEX_CACHE_IS_ARCHIVED = "<div id=\"ctl00_ContentBody_lockedMessage\"".toRegex()
    @JvmField val REGEX_CACHE_IS_FAVORITE = "<div id=\"pnlFavoriteCache\">".toRegex()
    @JvmField val REGEX_CACHE_GUID = Pattern.compile(Pattern.quote("&wid=") + "([0-9a-z\\-]+)" + Pattern.quote("&")).toRegex()
    @JvmField val REGEX_CACHE_WATCHLIST_COUNT = "watchlist\\.aspx[^(]*\\((\\d+)\\)".toRegex()
    @JvmField val REGEX_CACHE_SIZE = "/icons/container/([a-z]+)\\.".toRegex()
    @JvmField val REGEX_CACHE_FOUND = ("logtypes/48/(" + LogType.values().filter { it.isFoundLog }.joinToString("|", "", "") { it.iconId } + ").png\" id=\"ctl00_ContentBody_GeoNav_logTypeImage\"").toRegex()
    @JvmField val REGEX_CACHE_ON_WATCHLIST = Pattern.compile(Pattern.quote("watchlist.aspx") + ".{1,50}" + Pattern.quote("action=rem")).toRegex()
    @JvmField val REGEX_CACHE_SPOILER_IMAGES = "<a href=\"(https?://img(?:cdn)?\\.geocaching\\.com[^.]+\\.(?:jpg|jpeg|png|gif|bmp))\"[^>]+>([^<]*)</a>.*?(?:description\"[^>]*>([^<]+)</span>)?</li>".toRegex()
    @JvmField val REGEX_CACHE_SPOILED_IMAGE_GUID = "large/([a-z0-9\\-]+)\\.".toRegex()
    @JvmField val REGEX_CACHE_INVENTORY = "ctl00_ContentBody_uxTravelBugList_uxInventoryLabel\">[\\s\\S]*?WidgetBody([\\s\\S]*?)<div".toRegex()
    @JvmField val REGEX_CACHE_INVENTORY_ITEMS = "[^<]*<li>[^<]*<a href=\"[a-z0-9\\-_.?/:@]*/track/details\\.aspx\\?guid=([0-9a-z\\-]+)[^\"]*\"[^>]*>[^<]*<img src=\"[^\"]+\"[^>]*>[^<]*<span>([^<]+)</span>[^<]*</a>[^<]*</li>".toRegex()
    @JvmField val REGEX_CACHE_LOGCOUNTS = "<span id=\"ctl00_ContentBody_lblFindCounts\"><p(.+?)</p></span>".toRegex()
    @JvmField val REGEX_CACHE_LOGCOUNTS_ITEM = "logtypes/([0-9]+)\\.[^>]+>\\s*([0-9,.]+)".toRegex()
    @JvmField val REGEX_CACHE_WAYPOINTS = "id=\"ctl00_ContentBody_Waypoints\"[\\s\\S]*?<tbody>([\\s\\S]*?)</tbody>".toRegex()
    @JvmField val REGEX_CACHE_WAYPOINTS_ITEM = "<tr.+>([\\s\\S]*?)</tr>[\\s\\S]*?(?:</tr>)".toRegex()
    @JvmField val REGEX_CACHE_WAYPOINTS_ITEM_COLUMN = "(?=<td.*>)".toRegex()
    @JvmField val REGEX_CACHE_WAYPOINTS_ITEM_NOTE = "colspan=\"6\">([\\s\\S]*?)</td>".toRegex()
    @JvmField val REGEX_CACHE_WAYPOINTS_ITEM_NAME = ">[^<]*<a[^>]+>([^<]*)</a>".toRegex()
    @JvmField val REGEX_CACHE_WAYPOINTS_ITEM_TYPE = "/WptTypes/sm/(.+).jpg".toRegex()
    @JvmField val REGEX_CACHE_WAYPOINTS_ITEM_PREFIX = "id=\"awpt.*\">([\\s\\S]*?)</span>".toRegex()
    @JvmField val REGEX_CACHE_WAYPOINTS_ITEM_LOOKUP = ">([\\s\\S]*?)</td>".toRegex()
    @JvmField val REGEX_CACHE_WAYPOINTS_ITEM_COORDINATIONS = ">([\\s\\S]*?)&nbsp;[\\s\\S]*?</td>".toRegex()
    @JvmField val REGEX_TRACKABLE_CODE = "CoordInfoCode\">(TB[0-9A-Z]+)<".toRegex()
    @JvmField val REGEX_TRACKABLE_ID = "/my/watchlist\\.aspx\\?b=(\\d+)\"".toRegex()
}
