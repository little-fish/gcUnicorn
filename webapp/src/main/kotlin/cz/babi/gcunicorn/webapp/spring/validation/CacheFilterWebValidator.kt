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

package cz.babi.gcunicorn.webapp.spring.validation

import cz.babi.gcunicorn.core.exception.location.CoordinateParseException
import cz.babi.gcunicorn.core.location.parser.Parser
import cz.babi.gcunicorn.webapp.entity.CacheFilterWeb
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.validation.Errors
import org.springframework.validation.ValidationUtils
import org.springframework.validation.Validator

/**
 * Cache filter validator.
 *
 * @param parser Parser to be used for coordinates validation.
 * @see [CacheFilterWeb]
 *
 * @since 1.0.0
 */
@Component
class CacheFilterWebValidator(val parser: Parser, @Value("\${search.max-count:200}") val maxCount: Int, @Value("\${search.default-count:100}") val defaultCount: Int, @Value("\${search.default-distance-km:10.0}") val defaultDistance: Double) : Validator {

    override fun validate(target: Any, errors: Errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "cacheType", "field.required")
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "coordinates", "field.required")

        val cacheFilterWeb = target as CacheFilterWeb

        try {
            parser.parse(cacheFilterWeb.coordinates ?: "")
        } catch(e: CoordinateParseException) {
            errors.rejectValue("coordinates", "field.illegalArgument")
        }

        if(cacheFilterWeb.distance==null) {
            cacheFilterWeb.distance = defaultDistance
        }

        cacheFilterWeb.count?.let {
            if(it > maxCount) {
                cacheFilterWeb.count = maxCount
            }
        } ?: kotlin.run {
            cacheFilterWeb.count = if (defaultCount > maxCount) { maxCount } else { defaultCount }
        }
    }

    override fun supports(clazz: Class<*>): Boolean = CacheFilterWeb::class.java.isAssignableFrom(clazz)
}