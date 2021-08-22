package com.title.joke.service

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.httpPut
import com.github.kittinunf.fuel.jackson.responseObject
import com.github.kittinunf.result.Result
import com.title.joke.dto.ActivityDto
import com.title.joke.dto.EventDataDto
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.unbescape.json.JsonEscape
import kotlin.collections.LinkedHashMap

@Service
class ActivityService(
    @Value("\${strava.base-activity-url}")
    val baseActivityUrl: String,
    private val tokenService: OAuthTokenService,
    private val titleService: TitleService
) {
    private val logger = LoggerFactory.getLogger(ActivityService::class.java)
    private val maxEntries = 30
    private val eventsCache = object : LinkedHashMap<String, EventDataDto>(maxEntries) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, EventDataDto>?): Boolean {
            return size > maxEntries
        }
    }

    suspend fun updateActivity(eventData: EventDataDto) {
        // TODO refactor to use chain of responsibilities
        if (eventData.object_type == "activity") {
            if (eventData.aspect_type == "create") {
                if (isDuplicateCreateEvent(eventData)) return
                logger.info("Setting title for activity: ${eventData.object_id}")
                updateTitle(eventData)
            } else if (eventData.aspect_type == "update") {
                if (eventData.updates.containsKey("title") &&
                    eventData.updates["title"]?.trim().equals("next title", ignoreCase = true)
                ) {
                    logger.info("User requested different title for activity: ${eventData.object_id}")
                    updateTitle(eventData)
                }
            }
        }
        if (eventData.object_type == "athlete" && eventData.aspect_type == "update" && eventData.updates["authorized"] == "false") {
            tokenService.deactivateAthleteToken(eventData.owner_id)
        }
    }

    private fun isDuplicateCreateEvent(eventData: EventDataDto): Boolean {
        if (eventsCache.containsKey(eventData.object_id)) {
            logger.info("Received duplicate event $eventData")
            return true
        } else {
            eventsCache[eventData.object_id] = eventData
        }
        return false
    }

    private fun updateTitle(eventData: EventDataDto) {
        val bearerToken = tokenService.getTokenForAthlete(eventData.owner_id)
        val activityTitle = titleService.generateTitle()
        updateTitleOnStrava(bearerToken, activityTitle, eventData)
    }

    private fun updateTitleOnStrava(bearerToken: String, activityTitle: String, eventData: EventDataDto) {
        logger.info("Updating activity title for $baseActivityUrl${eventData.object_id}")

        val mapper = ObjectMapper().registerKotlinModule()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        val (_, response, result) = "$baseActivityUrl${eventData.object_id}"
            .httpPut()
            .header("Authorization", bearerToken)
            .jsonBody(
                """
                         {
                            "name": "${JsonEscape.escapeJson(activityTitle)}",
                            "description": "Get fun dad jokes for your activity at https://www.titleworkout.pro"
                         }
                     """.trimIndent()
            )
            .responseObject<ActivityDto>(mapper)

        when (result) {
            is Result.Failure -> {
                val e = result.getException()
                logger.error("Strava API returned ${response.statusCode} with body ${response.body()}")
                logger.error("Error when updating activity with id: ${eventData.object_id}", e)
                throw e
            }
            is Result.Success -> {
                val dto: ActivityDto = result.get()
                logger.info("Successfully updated activity with id:${dto.id} to title: ${dto.name}")
            }
        }
    }
}