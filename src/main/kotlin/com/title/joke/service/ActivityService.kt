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

@Service
class ActivityService(
    @Value("\${strava.base-activity-url}")
    val baseActivityUrl: String,
    private val tokenService: OAuthTokenService,
    private val titleService: TitleService
) {
    private val logger = LoggerFactory.getLogger(ActivityService::class.java)

    fun updateActivity(eventData: EventDataDto) {
        // TODO refactor to use chain of responsibilities
        if (eventData.object_type == "activity") {
            if (eventData.aspect_type == "create") {
                logger.debug("Fetching athlete token")
                val bearerToken = tokenService.getTokenForAthlete(eventData.owner_id)
                val activityTitle = titleService.generateTitle()
                updateTitleOnStrava(bearerToken, activityTitle, eventData)
            }
        }
    }

    private fun updateTitleOnStrava(bearerToken: String, activityTitle: String, eventData: EventDataDto) {
        logger.info("Updating activity title for $baseActivityUrl${eventData.object_id}")

        val mapper = ObjectMapper().registerKotlinModule()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        // TODO change to Async
        val (_, _, result) = "$baseActivityUrl${eventData.object_id}"
            .httpPut()
            .header("Authorization", bearerToken)
            .jsonBody(
                """
                         {
                            "name": "${JsonEscape.escapeJson(activityTitle)}",
                            "description": "Joke is automatically generated from: https://icanhazdadjoke.com. If you find it hurtful, please contact me and I will take it down"
                         }
                     """.trimIndent()
            )
            .responseObject<ActivityDto>(mapper)

        when (result) {
            is Result.Failure -> {
                val e = result.getException()
                logger.error("Error when updating activity with id: ${eventData.object_id}", e)
                throw e
            }
            is Result.Success -> {
                val dto: ActivityDto = result.get()
                logger.info("Successfully updated activity with id:${dto.id} to name: ${dto.name}")
            }
        }
    }
}