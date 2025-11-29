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

package cz.babi.gcunicorn.webapp.spring.web.controller

import cz.babi.gcunicorn.core.network.service.Service
import cz.babi.gcunicorn.core.network.service.geocachingcom.model.CacheFilter
import cz.babi.gcunicorn.core.network.service.geocachingcom.model.CacheType
import cz.babi.gcunicorn.`fun`.Constant
import cz.babi.gcunicorn.`fun`.dateFormat
import cz.babi.gcunicorn.`fun`.logger
import cz.babi.gcunicorn.`fun`.nullableExecute
import cz.babi.gcunicorn.webapp.entity.CacheFilterWeb
import cz.babi.gcunicorn.webapp.entity.JobStatusWeb
import cz.babi.gcunicorn.webapp.entity.task.JobsWrapper
import cz.babi.gcunicorn.webapp.entity.task.SearchJob
import cz.babi.gcunicorn.webapp.entity.task.Status
import cz.babi.gcunicorn.webapp.spring.validation.CacheFilterWebValidator
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpSession
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import java.io.IOException

/**
 * gcUnicorn controller.
 *
 * Handles requests to __/gcUnicorn/&#42;&#42;__ paths.
 *
 * @param cacheFilterWebValidator Validator for web cache filter.
 * @param service Service.
 * @param jobsWrapper Job wrapper. Holds all search jobs.
 * @param simpMessagingTemplate Simple message template.
 *
 * @since 1.0.0
 */
@Controller
@RequestMapping(path = ["/gcUnicorn"])
class GcUnicornController(@param:Autowired private val cacheFilterWebValidator: CacheFilterWebValidator,
                          @param:Autowired private val service: Service,
                          @param:Autowired private val jobsWrapper: JobsWrapper,
                          @param:Autowired private val simpMessagingTemplate: SimpMessagingTemplate) {

    companion object {
        private val LOG: Logger = logger<GcUnicornController>()
    }

    @GetMapping(path = ["/search"])
    suspend fun searchGet(model: Model, httpSession: HttpSession): String {
        fillModelWithDefaultAttributes(model, httpSession)
        model.addAttribute("cacheFilterWeb", CacheFilterWeb())

        return "search"
    }

    @OptIn(DelicateCoroutinesApi::class)
    @PostMapping(path = ["/search"])
    suspend fun searchPost(
        @ModelAttribute(name = "cacheFilterWeb") cacheFilterWeb: CacheFilterWeb,
        @RequestParam(name = "allowDisabled", required = false, defaultValue = "0") allowDisabled: Boolean,
        @RequestParam(name = "includeOwn", required = false, defaultValue = "0") includeOwn: Boolean,
        @RequestParam(name = "includeFound", required = false, defaultValue = "0") includeFound: Boolean,
        @RequestParam(name = "skipPremium", required = false, defaultValue = "1") skipPremium: Boolean,
        bindingResult: BindingResult,
        model: Model,
        httpServletResponse: HttpServletResponse,
        httpSession: HttpSession
    ): String {
        // Process validation of input parameters.
        cacheFilterWebValidator.validate(cacheFilterWeb, bindingResult)
        if(bindingResult.hasErrors()) {
            fillModelWithDefaultAttributes(model, httpSession)
            return "search"
        }

        val parent = Job()
        val jobId = jobsWrapper.getSearchJobsCount(httpSession.id) + 1
        val job = GlobalScope.async {
            service.createGpx(
                    service.lookForCaches(
                            cacheFilterWebValidator.parser.parse(cacheFilterWeb.coordinates!!),
                            CacheFilter(listOf(CacheType.findByCode(cacheFilterWeb.cacheType!!)), cacheFilterWeb.distance!!, allowDisabled, !includeOwn, !includeFound, skipPremium),
                            cacheFilterWeb.count!!,
                            null
                    ),
                    false)

        }.apply {
            invokeOnCompletion {
                // Should be as fast as possible.
                notifyStatusChanged(JobStatusWeb(jobId, jobsWrapper.getSearchJob(httpSession.id, jobId)!!.getStatus()))
                notifyActiveCountChanged(jobsWrapper.getSearchJobsCount(httpSession.id, Status.ACTIVE))
            }
        }

        jobsWrapper.putSearchJob(httpSession.id, SearchJob(parent = parent, job = job, id = jobId))

        httpServletResponse.setHeader("Location", "/queue/$jobId")
        httpServletResponse.status = HttpStatus.ACCEPTED.value()

        return "redirect:queue"
    }

    @GetMapping(path = [ "/queue" ])
    suspend fun queue(model: Model, httpSession: HttpSession): String {
        model.addAttribute("searchJobs", jobsWrapper.getSearchJobs(httpSession.id))
        model.addAttribute("activeJobsCount", jobsWrapper.getSearchJobsCount(httpSession.id, Status.ACTIVE))

        return "queue"
    }

    @GetMapping(path = ["/queue/{jobId}/status"])
    suspend fun queue(@PathVariable(name = "jobId", required = true) jobId: Int, httpServletResponse: HttpServletResponse, httpSession: HttpSession) {
        jobsWrapper.getSearchJob(httpSession.id, jobId).nullableExecute({
            when {
                this.job.isActive -> {
                    httpServletResponse.status = HttpStatus.PROCESSING.value()
                    httpServletResponse.setHeader("Location", "/queue/$jobId")
                }
                this.job.isCancelled -> httpServletResponse.status = HttpStatus.GONE.value()
                this.job.isCompleted -> {
                    httpServletResponse.status = HttpStatus.OK.value()
                    httpServletResponse.setHeader("Location", "/queue/$jobId/gpx")
                }
            }
        }, {
            httpServletResponse.status = HttpStatus.NOT_FOUND.value()
        })
    }

    @ExperimentalCoroutinesApi
    @GetMapping(path = ["/queue/{jobId}/gpx"])
    fun gpx(@PathVariable(name = "jobId", required = true) jobId: Int, httpServletResponse: HttpServletResponse, httpSession: HttpSession) {
        jobsWrapper.getSearchJob(httpSession.id, jobId).nullableExecute({
            if(this.job.isCompleted) {
                httpServletResponse.characterEncoding = Charsets.UTF_8.name()
                httpServletResponse.setHeader("Content-Type", "application/gpx+xml")
                httpServletResponse.setHeader("Content-Disposition", "attachment;filename=gcunicorn_${start.dateFormat(Constant.DATETIME_PATTERN_GPX)}.gpx")

                httpServletResponse.writer.use {
                    try {
                        it.print(job.getCompleted())
                        it.flush()
                    } catch (e: IllegalStateException) {
                        LOG.warn("The job '{}' has not finished yet!", jobId, e)
                    } catch (e: IOException) {
                        LOG.warn("Can not write gpx file to output stream.", e)
                    } finally {
                        try {
                            it.close()
                        } catch (_: IOException) {}
                    }
                }
            } else {
                httpServletResponse.status = HttpStatus.PROCESSING.value()
                httpServletResponse.setHeader("Location", "/queue/$jobId")
            }
        }, {
            httpServletResponse.status = HttpStatus.NOT_FOUND.value()
        })
    }

    /**
     * Some attributes are the same for some requests.
     * @param model Spring model.
     */
    private fun fillModelWithDefaultAttributes(model: Model, httpSession: HttpSession) {
        model.addAttribute("cacheTypes", CacheType.values()
                .filter { it != CacheType.UNKNOWN }
                .toList())
        model.addAttribute("queueSize", jobsWrapper.getSearchJobsCount(httpSession.id))
    }

    /**
     * Sends notification about job status changed.
     * @param newJobStatus New job status.
     */
    private fun notifyStatusChanged(newJobStatus: JobStatusWeb) {
        simpMessagingTemplate.convertAndSend("/topic/queue/statusChanged", newJobStatus)
    }

    /**
     * Sends notification when count of active jobs changed.
     * @param activeCount Count of active jobs.
     */
    private fun notifyActiveCountChanged(activeCount: Int) {
        simpMessagingTemplate.convertAndSend("/topic/queue/activeCountChanged", activeCount)
    }
}
